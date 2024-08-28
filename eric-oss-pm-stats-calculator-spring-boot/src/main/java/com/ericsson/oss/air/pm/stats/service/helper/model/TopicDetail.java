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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;

import lombok.Data;

@Data
public class TopicDetail {

    private Map<KafkaPartition, Long> partitionOffsets;

    private TopicDetail(final Map<KafkaPartition, Long> partitionOffsets) {
        this.partitionOffsets = new HashMap<>(partitionOffsets);
    }

    public static TopicDetail of(final List<LatestProcessedOffset> latestProcessedOffsets) {
        return new TopicDetail(createMap(latestProcessedOffsets));
    }

    private static Map<KafkaPartition, Long> createMap(final List<LatestProcessedOffset> latestProcessedOffsets) {
        final Map<KafkaPartition, Long> partitionOffsets = new HashMap<>();
        latestProcessedOffsets.forEach(latestProcessedOffset -> {
            final KafkaPartition kafkaPartition = new KafkaPartition(latestProcessedOffset.getTopicPartition());
            partitionOffsets.put(kafkaPartition, getSmaller(partitionOffsets.get(kafkaPartition), latestProcessedOffset.getTopicPartitionOffset()));
        });
        return partitionOffsets;
    }

    private static Long getSmaller(final Long mapValue, final Long current) {
        return mapValue == null ? current : Math.min(mapValue, current);
    }

    public Long getOffset(final int partition) {
        final Long partitionOffset = partitionOffsets.get(KafkaPartition.of(partition));
        return partitionOffset == null ? 0 : partitionOffset;
    }

    @Data(staticConstructor = "of")
    public static class KafkaPartition {
        private final Integer partition;
    }
}
