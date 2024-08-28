/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset.LatestProcessedOffsetBuilder;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.model.TopicAggregationPeriod;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class CurrentOffsetCacheTest {
    CurrentOffsetCache objectUnderTest = new CurrentOffsetCache();

    @Test
    void shouldGetAll() {
        final List<LatestProcessedOffset> forTopic1 = Arrays.asList(
                latestProcessedOffset("topic1", 0, 10L, true),
                latestProcessedOffset("topic1", 1, 10L, true),
                latestProcessedOffset("topic1", 2, 10L, true)
        );

        final List<LatestProcessedOffset> forTopic2 = Arrays.asList(
                latestProcessedOffset("topic2", 0, 10L, true),
                latestProcessedOffset("topic2", 1, 10L, true),
                latestProcessedOffset("topic2", 2, 10L, true)
        );

        objectUnderTest.cache(TopicAggregationPeriod.of("topic1", 60), forTopic1);
        objectUnderTest.cache(TopicAggregationPeriod.of("topic2", 60), forTopic2);

        final List<LatestProcessedOffset> actual = objectUnderTest.getCurrentOffsets();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(CollectionUtils.union(forTopic1, forTopic2));
    }

    @Test
    void shouldMergeOffsets() {
        final List<LatestProcessedOffset> firstIteration = List.of(
                latestProcessedOffset("topic1", 0, 0L, true),
                latestProcessedOffset("topic1", 1, 0L, true)
        );

        final List<LatestProcessedOffset> secondIteration = List.of(
                latestProcessedOffset("topic1", 0, 10L, false)
        );

        objectUnderTest.cache(TopicAggregationPeriod.of("topic1", 60), firstIteration);
        objectUnderTest.cache(TopicAggregationPeriod.of("topic1", 60), secondIteration);

        final List<LatestProcessedOffset> actual = objectUnderTest.getCurrentOffsets();

        assertThat(actual).satisfiesExactlyInAnyOrder(
                offset -> assertThat(offset).usingRecursiveComparison().isEqualTo(latestProcessedOffset("topic1", 0, 10L, false)),
                offset -> assertThat(offset).usingRecursiveComparison().isEqualTo(latestProcessedOffset("topic1", 1, 0L, true))
        );
    }

    static LatestProcessedOffset latestProcessedOffset(final String topic, final int topicPartition, final long topicPartitionOffset, final boolean fromKafka) {
        final LatestProcessedOffsetBuilder builder = LatestProcessedOffset.builder();
        builder.withTopicName(topic);
        builder.withTopicPartition(topicPartition);
        builder.withTopicPartitionOffset(topicPartitionOffset);
        builder.withFromKafka(fromKafka);
        return builder.build();
    }

}