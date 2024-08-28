/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.max;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset.LatestProcessedOffsetBuilder;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.OffsetBucketCalculator;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.facade.OffsetCacheFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.model.TopicAggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.distance.OffsetDistanceCalculator;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer.TopicPartitionOffsetDeserializer;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer.TopicPartitionOffsetSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetCalculatorFacadeImplTest {

    MessageCounter messageCounterMock = mock(MessageCounter.class);
    OffsetBucketCalculator offsetBucketCalculatorMock = mock(OffsetBucketCalculator.class);
    OffsetCacheFacade offsetCacheFacadeMock = mock(OffsetCacheFacade.class);
    OffsetCalculationHelper offsetCalculationHelperMock = mock(OffsetCalculationHelper.class);
    OffsetDistanceCalculator offsetDistanceCalculatorMock = mock(OffsetDistanceCalculator.class);
    TopicPartitionOffsetSerializer topicPartitionOffsetSerializerMock = mock(TopicPartitionOffsetSerializer.class);

    OffsetCalculatorFacadeImpl objectUnderTest = new OffsetCalculatorFacadeImpl(
            messageCounterMock, offsetBucketCalculatorMock,
            offsetCacheFacadeMock, offsetCalculationHelperMock,
            offsetDistanceCalculatorMock, topicPartitionOffsetSerializerMock,
            new TopicPartitionOffsetDeserializer(new ObjectMapper())
    );

    @Nested
    @DisplayName("Should generate offset list")
    class ShouldGenerateOffsetList {
        private static final String TOPIC = "topic";

        @Captor ArgumentCaptor<List<LatestProcessedOffset>> latestProcessedOffsetCaptor;

        @Mock Dataset<Row> datasetMock;
        @Mock RelationalGroupedDataset relationalGroupedDatasetMock;
        @Mock Dataset<Row> resultDatasetMock;
        @Mock Column maxColumnMock;
        @Mock Column resultMaxColumnMock;
        @Mock Column partitionColumnMock;
        @Mock Column resultPartitionColumnMock;
        @Mock Row rowMock;
        @Mock Row rowMock2;

        @Test
        void shouldGenerateOffsetsFromDataset() {
            final String offset = "offset";
            final String partition = "partition";

            try (final MockedStatic<functions> functionsMockedStatic = mockStatic(functions.class)) {

                final Verification maxColumnVerification = () -> max(offset);
                final Verification partitionColumnVerification = () -> col(partition);

                functionsMockedStatic.when(maxColumnVerification).thenReturn(maxColumnMock);
                functionsMockedStatic.when(partitionColumnVerification).thenReturn(partitionColumnMock);

                when(partitionColumnMock.as(partition)).thenReturn(resultPartitionColumnMock);
                when(datasetMock.groupBy(resultPartitionColumnMock)).thenReturn(relationalGroupedDatasetMock);
                when(maxColumnMock.as(offset)).thenReturn(resultMaxColumnMock);
                when(relationalGroupedDatasetMock.agg(resultMaxColumnMock)).thenReturn(resultDatasetMock);
                when(resultDatasetMock.collectAsList()).thenReturn(Arrays.asList(rowMock, rowMock2));
                when(rowMock.getAs(partition)).thenReturn(0);
                when(rowMock2.getAs(partition)).thenReturn(1);
                when(rowMock.getAs(offset)).thenReturn(15L);
                when(rowMock2.getAs(offset)).thenReturn(17L);

                objectUnderTest.generateOffsetsListAndStore(datasetMock, TOPIC, 60);

                verify(partitionColumnMock).as(partition);
                verify(datasetMock).groupBy(resultPartitionColumnMock);
                verify(maxColumnMock).as(offset);
                verify(relationalGroupedDatasetMock).agg(resultMaxColumnMock);
                verify(resultDatasetMock).collectAsList();
                verify(rowMock).getAs(partition);
                verify(rowMock).getAs(offset);
                verify(rowMock2).getAs(partition);
                verify(rowMock2).getAs(offset);

                verify(offsetCacheFacadeMock).cache(eq(TopicAggregationPeriod.of(TOPIC, 60)), latestProcessedOffsetCaptor.capture());

                assertThat(latestProcessedOffsetCaptor.getValue()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                        latestProcessedOffset(TOPIC, 0, 15),
                        latestProcessedOffset(TOPIC, 1, 17)
                );
            }
        }

        @Test
        void shouldGenerateOffsets() {
            final String topic = "topic";
            final int aggregationPeriod = 60;
            final String endOffsets = String.format("{ \"%s\": { \"0\":10, \"1\":15, \"2\":20, \"3\":25, \"4\":0 } }", topic);

            objectUnderTest.generateOffsetsListAndStore(topic, aggregationPeriod, endOffsets);

            verify(offsetCacheFacadeMock).cache(eq(TopicAggregationPeriod.of(topic, aggregationPeriod)), latestProcessedOffsetCaptor.capture());

            assertThat(latestProcessedOffsetCaptor.getValue()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                    latestProcessedOffset(topic, 0, 9),
                    latestProcessedOffset(topic, 1, 14),
                    latestProcessedOffset(topic, 2, 19),
                    latestProcessedOffset(topic, 3, 24),
                    latestProcessedOffset(topic, 4, -1)
            );
        }
    }

    @Nested
    @DisplayName("Should Calculate Starting Offsets With Given Data")
    class ShouldCalculateStartingOffsetWithGivenData {

        @Test
        void shouldGiveBackEarliestForEmptyQuery() {
            final Topic topic = Topic.of("kafka-topic");
            final int aggregationPeriod = 60;

            when(offsetCacheFacadeMock.determineStartingOffset(topic, aggregationPeriod)).thenReturn(Collections.emptyList());

            final String actual = objectUnderTest.defineStartingOffset(topic, aggregationPeriod);

            verify(offsetCacheFacadeMock).determineStartingOffset(topic, aggregationPeriod);

            assertThat(actual).isEqualTo("earliest");
        }

        @Test
        void shouldDefineStartingOffset(@Mock final List<LatestProcessedOffset> latestProcessedOffsetsMock) {
            final Topic topic = Topic.of("kafka-topic");
            final int aggregationPeriod = 60;

            when(offsetCacheFacadeMock.determineStartingOffset(topic, aggregationPeriod)).thenReturn(latestProcessedOffsetsMock);
            when(topicPartitionOffsetSerializerMock.serializeLatestProcessedOffset(topic, latestProcessedOffsetsMock)).thenReturn("serialized");

            final String actual = objectUnderTest.defineStartingOffset(topic, aggregationPeriod);

            verify(offsetCacheFacadeMock).determineStartingOffset(topic, aggregationPeriod);
            verify(topicPartitionOffsetSerializerMock).serializeLatestProcessedOffset(topic, latestProcessedOffsetsMock);

            assertThat(actual).isEqualTo("serialized");
        }
    }

    @Nested
    class DefineEndOffset {
        @Test
        void shouldReturnOffsetsImmediately_whenThereIsNoToMuchData(
                @Mock final Map<TopicPartition, EndOffset> topicPartitionEndOffsetMock,
                @Mock final List<LatestProcessedOffset> latestProcessedOffsetsMock,
                @Mock final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistancesMock) {
            final Topic topic = Topic.of("kafka-topic");
            final int aggregationPeriod = 60;

            when(messageCounterMock.countMessagesByPartition(topic)).thenReturn(topicPartitionEndOffsetMock);
            when(offsetCacheFacadeMock.determineStartingOffset(topic, aggregationPeriod)).thenReturn(latestProcessedOffsetsMock);
            when(offsetDistanceCalculatorMock.calculateOffsetDistances(latestProcessedOffsetsMock,topicPartitionEndOffsetMock)).thenReturn(topicPartitionOffsetDistancesMock);
            when(offsetCalculationHelperMock.isNoTooMuchData(topicPartitionOffsetDistancesMock)).thenReturn(true);
            when(topicPartitionOffsetSerializerMock.serializeTopicPartitionEndOffset(topic, topicPartitionEndOffsetMock)).thenReturn("serialized");

            final String actual = objectUnderTest.defineEndOffset(topic, aggregationPeriod);

            verify(messageCounterMock).countMessagesByPartition(topic);
            verify(offsetCacheFacadeMock).determineStartingOffset(topic, aggregationPeriod);
            verify(offsetDistanceCalculatorMock).calculateOffsetDistances(latestProcessedOffsetsMock, topicPartitionEndOffsetMock);
            verify(offsetCalculationHelperMock).isNoTooMuchData(topicPartitionOffsetDistancesMock);
            verify(topicPartitionOffsetSerializerMock).serializeTopicPartitionEndOffset(topic, topicPartitionEndOffsetMock);

            verify(offsetBucketCalculatorMock, never()).calculateNextBucket(anyList());
            verify(topicPartitionOffsetSerializerMock, never()).serializePartitionOffsetDistance(eq(topic), anyMap());

            assertThat(actual).isEqualTo("serialized");
        }
    }

    @Test
    void shouldCalculateNextBucketWhenTooMuchData(
            @Mock final Map<TopicPartition, EndOffset> topicPartitionEndOffsetMock,
            @Mock final List<LatestProcessedOffset> latestProcessedOffsetsMock,
            @Mock final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistancesMock,
            @Mock final Map<Partition, OffsetDistance> nextBucketMock) {
        final Topic topic = Topic.of("kafka-topic");
        final int aggregationPeriod = 60;

        when(messageCounterMock.countMessagesByPartition(topic)).thenReturn(topicPartitionEndOffsetMock);
        when(offsetCacheFacadeMock.determineStartingOffset(topic, aggregationPeriod)).thenReturn(latestProcessedOffsetsMock);
        when(offsetDistanceCalculatorMock.calculateOffsetDistances(latestProcessedOffsetsMock,topicPartitionEndOffsetMock)).thenReturn(topicPartitionOffsetDistancesMock);
        when(offsetCalculationHelperMock.isNoTooMuchData(topicPartitionOffsetDistancesMock)).thenReturn(false);
        when(offsetBucketCalculatorMock.calculateNextBucket(topicPartitionOffsetDistancesMock)).thenReturn(nextBucketMock);
        when(topicPartitionOffsetSerializerMock.serializePartitionOffsetDistance(topic, nextBucketMock)).thenReturn("serialized");

        final String actual = objectUnderTest.defineEndOffset(topic, aggregationPeriod);

        verify(messageCounterMock).countMessagesByPartition(topic);
        verify(offsetCacheFacadeMock).determineStartingOffset(topic, aggregationPeriod);
        verify(offsetDistanceCalculatorMock).calculateOffsetDistances(latestProcessedOffsetsMock, topicPartitionEndOffsetMock);
        verify(offsetCalculationHelperMock).isNoTooMuchData(topicPartitionOffsetDistancesMock);
        verify(offsetBucketCalculatorMock).calculateNextBucket(topicPartitionOffsetDistancesMock);
        verify(topicPartitionOffsetSerializerMock).serializePartitionOffsetDistance(topic, nextBucketMock);

        assertThat(actual).isEqualTo("serialized");
    }

    static LatestProcessedOffset latestProcessedOffset(final String topic, final int partition, final long topicPartitionOffset) {
        final LatestProcessedOffsetBuilder builder = LatestProcessedOffset.builder();
        builder.withTopicName(topic);
        builder.withTopicPartition(partition);
        builder.withTopicPartitionOffset(topicPartitionOffset);
        builder.withFromKafka(false);
        return builder.build();
    }
}