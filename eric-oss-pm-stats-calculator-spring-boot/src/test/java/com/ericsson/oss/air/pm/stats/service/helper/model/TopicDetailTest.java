/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.helper.model;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.service.helper.model.TopicDetail.KafkaPartition;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TopicDetailTest {

    @ParameterizedTest
    @MethodSource("provideInitiateData")
    void shouldInitiate(final List<LatestProcessedOffset> latestProcessedOffsets, final Map<KafkaPartition, Long> expected) {
        final TopicDetail actual = TopicDetail.of(latestProcessedOffsets);

        assertThat(actual.getPartitionOffsets()).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @MethodSource("provideGetPartitionData")
    @ParameterizedTest(name = "[{index}] For partition ''{0}'' offset: ''{1}''")
    void shouldGiveBackOffset(final int partition, final long expected) {
        final TopicDetail target = TopicDetail.of(List.of(latestProcessedOffset(0, 10, "exe1")));

        final long actual = target.getOffset(partition);

        assertThat(actual).isEqualTo(expected);
    }

    static LatestProcessedOffset latestProcessedOffset(final int partition, final long offset, final String group) {
        ExecutionGroup executionGroup = ExecutionGroup.builder().withName(group).build();
        return LatestProcessedOffset.builder()
                .withTopicPartition(partition)
                .withTopicPartitionOffset(offset)
                .withExecutionGroup(executionGroup)
                .build();
    }

    static Stream<Arguments> provideInitiateData() {
        LatestProcessedOffset partition0Exe1 = latestProcessedOffset(0, 10, "exe1");
        LatestProcessedOffset partition1Exe1 = latestProcessedOffset(1, 5, "exe1");
        LatestProcessedOffset partition0Exe2 = latestProcessedOffset(0, 5, "exe2");
        return Stream.of(
                Arguments.of(List.of(partition0Exe1, partition1Exe1), Map.of(KafkaPartition.of(0), 10L, KafkaPartition.of(1), 5L)),
                Arguments.of(List.of(partition0Exe1, partition0Exe2), Map.of(KafkaPartition.of(0), 5L))
        );
    }

    static Stream<Arguments> provideGetPartitionData() {
        return Stream.of(
                Arguments.of(0, 10),    //present partition
                Arguments.of(1, 0)      //missing partition
        );
    }
}