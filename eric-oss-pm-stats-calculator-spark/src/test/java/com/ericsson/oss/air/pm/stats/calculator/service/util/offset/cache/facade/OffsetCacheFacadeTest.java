/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.facade;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset.LatestProcessedOffsetBuilder;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.CurrentOffsetCache;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.PersistedOffsetCache;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.model.TopicAggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetCacheFacadeTest {

    @Mock MessageCounter messageCounterMock;
    @Mock CurrentOffsetCache currentOffsetCacheMock;
    @Mock PersistedOffsetCache persistedOffsetCacheMock;

    @InjectMocks OffsetCacheFacade objectUnderTest;

    @Test
    void shouldReadStartingOffsetFromMemory_andIncrementOffsets() {
        final int aggregationPeriod = 60;
        final Topic targetTopic = Topic.of("kafka-topic");
        final LatestProcessedOffset latestProcessedOffset1 = latestProcessedOffset(targetTopic.name(), 0, 10, true);
        final LatestProcessedOffset latestProcessedOffset2 = latestProcessedOffset(targetTopic.name(), 1, 10, false);

        when(currentOffsetCacheMock.readCache(topicAggregationPeriod(targetTopic.name(), aggregationPeriod))).thenReturn(List.of(
                latestProcessedOffset1,
                latestProcessedOffset2
        ));
        when(messageCounterMock.countMessagesByPartition(targetTopic)).thenReturn(Map.of(
                new TopicPartition(targetTopic.name(),0), EndOffset.of(15),
                new TopicPartition(targetTopic.name(),1), EndOffset.of(15)
        ));

        final List<LatestProcessedOffset> actual = objectUnderTest.determineStartingOffset(targetTopic, aggregationPeriod);

        verify(currentOffsetCacheMock).readCache(topicAggregationPeriod(targetTopic.name(), aggregationPeriod));
        verify(messageCounterMock).countMessagesByPartition(targetTopic);

        assertThat(actual)
                .satisfies(latestProcessedOffset -> {
                    final long offset = latestProcessedOffset1.getTopicPartitionOffset();
                    assertLatestProcessOffset(latestProcessedOffset, latestProcessedOffset1, offset);
                }, atIndex(0))
                .satisfies(latestProcessedOffset -> {
                    final long offset = latestProcessedOffset2.getTopicPartitionOffset() + 1;
                    assertLatestProcessOffset(latestProcessedOffset, latestProcessedOffset2, offset);
                }, atIndex(1));
    }

    @Test
    void shouldReadStartingOffsetFromDatabase_andCacheToMemory_andIncrementOffsets() {
        final int aggregationPeriod = 60;
        final Topic targetTopic = Topic.of("kafka-topic");
        final LatestProcessedOffset latestProcessedOffset1 = latestProcessedOffset(targetTopic.name(), 0, 10, false);
        final LatestProcessedOffset latestProcessedOffset2 = latestProcessedOffset(targetTopic.name(), 1, 10, true);
        final LatestProcessedOffset latestProcessedOffset3 = latestProcessedOffset(targetTopic.name(), 2, 10, false);

        when(currentOffsetCacheMock.readCache(topicAggregationPeriod(targetTopic.name(), aggregationPeriod))).thenReturn(null);
        when(persistedOffsetCacheMock.loadPersistedOffsets()).thenReturn(Map.of(
                targetTopic, List.of(latestProcessedOffset1, latestProcessedOffset2, latestProcessedOffset3)
        ));
        when(messageCounterMock.countMessagesByPartition(targetTopic)).thenReturn(Map.of(
                new TopicPartition(targetTopic.name(),0), EndOffset.of(15),
                new TopicPartition(targetTopic.name(),1), EndOffset.of(15),
                new TopicPartition(targetTopic.name(),2), EndOffset.of(10)
        ));

        final List<LatestProcessedOffset> actual = objectUnderTest.determineStartingOffset(targetTopic, aggregationPeriod);

        verify(currentOffsetCacheMock).readCache(topicAggregationPeriod(targetTopic.name(), aggregationPeriod));
        verify(persistedOffsetCacheMock).loadPersistedOffsets();
        verify(messageCounterMock, times(2)).countMessagesByPartition(targetTopic);
        /* The cached value is modified, can only verify it was called */
        verify(currentOffsetCacheMock).cache(eq(topicAggregationPeriod(targetTopic.name(), aggregationPeriod)), anyList());

        assertThat(actual)
                .satisfies(latestProcessedOffset -> {
                    final long offset = latestProcessedOffset1.getTopicPartitionOffset() + 1;
                    assertLatestProcessOffset(latestProcessedOffset, latestProcessedOffset1, offset);
                }, atIndex(0))
                .satisfies(latestProcessedOffset -> {
                    final long offset = latestProcessedOffset2.getTopicPartitionOffset();
                    assertLatestProcessOffset(latestProcessedOffset, latestProcessedOffset2, offset);
                }, atIndex(1))
                .satisfies(latestProcessedOffset -> {
                    final long offset = latestProcessedOffset3.getTopicPartitionOffset();
                    assertLatestProcessOffset(latestProcessedOffset, latestProcessedOffset3, offset);
                }, atIndex(2));
    }

    @Test
    void shouldReadStartingOffsetFromKafka_andCacheToMemory_andNotIncrementOffsets() {
        final int aggregationPeriod = 60;
        final Topic targetTopic = Topic.of("kafka-topic");

        /* Preserving insertion order as AssertJ does not offer recursive comparison for `containsExactlyInAnyOrder` list assert implementation */
        final Map<TopicPartition, StartOffset> startingOffsets = new LinkedHashMap<>(2);
        startingOffsets.put(new TopicPartition(targetTopic.name(), 0), StartOffset.of(10));
        startingOffsets.put(new TopicPartition(targetTopic.name(), 1), StartOffset.of(0));

        when(currentOffsetCacheMock.readCache(topicAggregationPeriod(targetTopic.name(), aggregationPeriod))).thenReturn(emptyList());
        when(persistedOffsetCacheMock.loadPersistedOffsets()).thenReturn(emptyMap());
        when(messageCounterMock.getStartOffsetForTopic(targetTopic)).thenReturn(startingOffsets);

        final List<LatestProcessedOffset> actual = objectUnderTest.determineStartingOffset(targetTopic, aggregationPeriod);

        verify(currentOffsetCacheMock).readCache(topicAggregationPeriod(targetTopic.name(), aggregationPeriod));
        verify(persistedOffsetCacheMock).loadPersistedOffsets();
        verify(messageCounterMock).getStartOffsetForTopic(targetTopic);
        /* LatestProcessedOffset has equals on ID that is not set thus using the anyList */
        verify(currentOffsetCacheMock).cache(eq(topicAggregationPeriod(targetTopic.name(), aggregationPeriod)), anyList());

        assertThat(actual).usingRecursiveComparison().isEqualTo(List.of(
                latestProcessedOffset(targetTopic.name(), 0,10, true),
                latestProcessedOffset(targetTopic.name(), 1,0, true)
        ));
    }

    static LatestProcessedOffset latestProcessedOffset(final String topicName, final int partition, final long offset, final boolean kafkaRead) {
        final LatestProcessedOffsetBuilder builder = LatestProcessedOffset.builder();
        builder.withTopicName(topicName);
        builder.withTopicPartition(partition);
        builder.withTopicPartitionOffset(offset);
        builder.withFromKafka(kafkaRead);
        return builder.build();
    }

    static TopicAggregationPeriod topicAggregationPeriod(final String name, final int aggregationPeriod) {
        return TopicAggregationPeriod.of(Topic.of(name), aggregationPeriod);
    }

    static void assertLatestProcessOffset(final LatestProcessedOffset actual, final LatestProcessedOffset original, final long offset) {
        assertThat(actual).usingRecursiveComparison().isNotSameAs(original).isEqualTo(
                latestProcessedOffset(original.getTopicName(), original.getTopicPartition(), offset, original.getFromKafka())
        );
    }
}