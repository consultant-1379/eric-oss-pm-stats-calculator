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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.model.TopicAggregationPeriod;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrentOffsetCache {
    private final Map<TopicAggregationPeriod, List<LatestProcessedOffset>> currentOffsets = new ConcurrentHashMap<>();

    public List<LatestProcessedOffset> readCache(final TopicAggregationPeriod topicAggregationPeriod) {
        return currentOffsets.get(topicAggregationPeriod);
    }

    public void cache(final TopicAggregationPeriod topicAggregationPeriod, final List<LatestProcessedOffset> latestProcessedOffsets) {
        currentOffsets.merge(topicAggregationPeriod, latestProcessedOffsets, (oldOffsets, newOffsets) -> {
            for (final LatestProcessedOffset newOffset : newOffsets) {
                findOldOffset(oldOffsets, newOffset).ifPresent(oldOffset -> {
                    final TopicPartition topicPartition = oldOffset.asTopicPartition();
                    final Long oldOffsetValue = oldOffset.getTopicPartitionOffset();
                    final Long newOffsetValue = newOffset.getTopicPartitionOffset();

                    log.info("Current offset for '{}' updated from '{}' to '{}'", topicPartition, oldOffsetValue, newOffsetValue);
                    oldOffset.setTopicPartitionOffset(newOffsetValue);
                    oldOffset.setFromKafka(newOffset.getFromKafka());
                });
            }

            return oldOffsets;
        });
    }

    public List<LatestProcessedOffset> getCurrentOffsets() {
        return currentOffsets.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private static Optional<LatestProcessedOffset> findOldOffset(final List<LatestProcessedOffset> offsets, final LatestProcessedOffset target) {
        for (final LatestProcessedOffset offset : offsets) {
            if (areOffsetsEqual(offset, target)) {
                return Optional.of(offset);
            }
        }

        return Optional.empty();
    }

    private static boolean areOffsetsEqual(final LatestProcessedOffset left, final LatestProcessedOffset right) {
        /* Execution groups are not set at this point of the comparison */
        return Objects.equals(left.getTopicName(), right.getTopicName()) && Objects.equals(left.getTopicPartition(), right.getTopicPartition());
    }

}
