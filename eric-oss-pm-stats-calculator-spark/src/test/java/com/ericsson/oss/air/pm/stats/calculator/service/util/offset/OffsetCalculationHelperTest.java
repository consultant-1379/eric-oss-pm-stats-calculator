/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer.TopicPartitionOffsetDeserializer;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class OffsetCalculationHelperTest {
    @Mock CalculationProperties calculationPropertiesMock;
    @Mock MessageCounter messageCounterMock;
    @Mock TopicPartitionOffsetDeserializer topicPartitionOffsetDeserializerMock;

    @InjectMocks OffsetCalculationHelper objectUnderTest;

    @ParameterizedTest
    @MethodSource("provideIsLastReadData")
    void shouldVerifyIsLastRead(final Map<TopicPartition, EndOffset> total, final Map<TopicPartition, EndOffset> processed, final boolean expected) {
        final Topic topic = Topic.of("kafka-topic");
        final String endingOffset = "JSON-end-offsets";

        when(messageCounterMock.countMessagesByPartition(topic)).thenReturn(total);
        when(topicPartitionOffsetDeserializerMock.deserialize(endingOffset)).thenReturn(processed);

        final boolean actual = objectUnderTest.isLastRead(topic, endingOffset);

        verify(messageCounterMock).countMessagesByPartition(topic);
        verify(topicPartitionOffsetDeserializerMock).deserialize(endingOffset);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideIsNoTooMuchData")
    void shouldVerifyIsNoTooMuchData(final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistances, final boolean expected) {
        when(calculationPropertiesMock.getKafkaBucketSize()).thenReturn(25);

        final boolean actual = objectUnderTest.isNoTooMuchData(topicPartitionOffsetDistances);

        verify(calculationPropertiesMock).getKafkaBucketSize();

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideIsLastReadData() {
        return Stream.of(
                Arguments.of(
                        total(ImmutableMap.of(
                                topicPartition(0), endOffset(10),
                                topicPartition(1), endOffset(10)
                        )),
                        processed(ImmutableMap.of(
                                topicPartition(0), endOffset(5),    /* Can read messages from here */
                                topicPartition(1), endOffset(10)
                        )),
                        false
                ),
                Arguments.of(
                        total(ImmutableMap.of(
                                topicPartition(0), endOffset(10),
                                topicPartition(1), endOffset(10)
                        )),
                        processed(ImmutableMap.of(
                                topicPartition(0), endOffset(10),
                                topicPartition(1), endOffset(10)
                        )),
                        true
                )
        );
    }


    static Stream<Arguments> provideIsNoTooMuchData() {
        return Stream.of(
                Arguments.of(
                        Arrays.asList(
                                TopicPartitionOffsetDistance.of(topicPartition(0), startOffset(0), endOffset(4)),
                                TopicPartitionOffsetDistance.of(topicPartition(1), startOffset(0), endOffset(20))
                        ),
                        true
                ),
                Arguments.of(
                        Arrays.asList(
                                TopicPartitionOffsetDistance.of(topicPartition(0), startOffset(0), endOffset(5)),
                                TopicPartitionOffsetDistance.of(topicPartition(1), startOffset(0), endOffset(20))
                        ),
                        true
                ),
                Arguments.of(
                        Arrays.asList(
                                TopicPartitionOffsetDistance.of(topicPartition(0), startOffset(0), endOffset(6)),
                                TopicPartitionOffsetDistance.of(topicPartition(1), startOffset(0), endOffset(20))
                        ),
                        false
                )
        );
    }

    static Map<TopicPartition, EndOffset> total(final Map<TopicPartition, EndOffset> total) { return total; }

    static Map<TopicPartition, EndOffset> processed(final Map<TopicPartition, EndOffset> processed) { return processed; }

    static StartOffset startOffset(final int offset) {
        return StartOffset.of(offset);
    }

    static EndOffset endOffset(final int offset) {
        return EndOffset.of(offset);
    }

    static TopicPartition topicPartition(final int partition) {
        return new TopicPartition("kafka-topic", partition);
    }
}