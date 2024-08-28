/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.distance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OffsetDistanceCalculatorTest {
    OffsetDistanceCalculator objectUnderTest = new OffsetDistanceCalculator();

    @Test
    void shouldRaiseExceptionWhenMappingMultipleLatestProcessedOffsetsForStartOffsets() {
        final List<LatestProcessedOffset> latestProcessedOffsets = Arrays.asList(
                latestProcessedOffset(0, 1),
                latestProcessedOffset(0, 2)
        );

        Assertions.assertThatThrownBy(() -> objectUnderTest.calculateOffsetDistances(latestProcessedOffsets, Collections.emptyMap()))
                  .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("provideCalculateOffsetDistancesData")
    void shouldVerifyCalculateOffsetDistances(final Map<TopicPartition, EndOffset> endOffsets,
                                              final List<LatestProcessedOffset> latestProcessedOffsets,
                                              final List<TopicPartitionOffsetDistance> expected) {
        final List<TopicPartitionOffsetDistance> actual = objectUnderTest.calculateOffsetDistances(latestProcessedOffsets, endOffsets);

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> provideCalculateOffsetDistancesData() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                topicPartition(0), endOffset(5),
                                topicPartition(1), endOffset(10),
                                topicPartition(2), endOffset(15),
                                topicPartition(3), endOffset(20)
                        ),
                        List.of(
                                latestProcessedOffset(0, 1),
                                latestProcessedOffset(1, 8),
                                latestProcessedOffset(2, 10),
                                latestProcessedOffset(3, 20)
                        ),
                        List.of(
                                TopicPartitionOffsetDistance.of(topicPartition(0), startOffset(1), endOffset(5)),
                                TopicPartitionOffsetDistance.of(topicPartition(1), startOffset(8), endOffset(10)),
                                TopicPartitionOffsetDistance.of(topicPartition(2), startOffset(10), endOffset(15)),
                                TopicPartitionOffsetDistance.of(topicPartition(3), startOffset(20), endOffset(20))
                        )
                )
        );
    }

    static LatestProcessedOffset latestProcessedOffset(final int partition, final long offset) {
        return LatestProcessedOffset.builder().withTopicName("kafka-topic").withTopicPartition(partition).withTopicPartitionOffset(offset).build();
    }

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