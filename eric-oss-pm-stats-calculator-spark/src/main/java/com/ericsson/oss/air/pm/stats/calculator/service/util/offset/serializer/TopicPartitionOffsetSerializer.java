/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.util.CollectionHelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicPartitionOffsetSerializer {
    private final ObjectMapper objectMapper;

    public String serializeLatestProcessedOffset(@NonNull final Topic topic, @NonNull final List<? extends LatestProcessedOffset> latestProcessedOffsets) {
        return writeValueAsString(Collections.singletonMap(
                topic.name(),
                latestProcessedOffsets.stream().collect(Collectors.toMap(
                        LatestProcessedOffset::getTopicPartition,
                        LatestProcessedOffset::getTopicPartitionOffset
                ))
        ));
    }

    public String serializeTopicPartitionEndOffset(@NonNull final Topic topic, final Map<TopicPartition, EndOffset> topicPartitionEndOffset) {
        return serializePartitionEndOffset(topic, CollectionHelpers.transformKey(topicPartitionEndOffset, Partition::from));
    }

    public String serializePartitionOffsetDistance(@NonNull final Topic topic, final Map<Partition, OffsetDistance> partitionOffsetDistance) {
        return serializePartitionEndOffset(topic, CollectionHelpers.transformValue(partitionOffsetDistance, OffsetDistance::endOffset));
    }

    public String serializePartitionEndOffset(@NonNull final Topic topic, final Map<Partition, EndOffset> partitionEndOffset) {
        return writeValueAsString(Collections.singletonMap(
                topic.name(),
                mapPartitionOffset(partitionEndOffset)
        ));
    }

    @SneakyThrows /* Not expecting any serialization issue */
    private String writeValueAsString(final Map<String, Map<Integer, Long>> value) {
        return objectMapper.writeValueAsString(value);
    }

    private static Map<Integer, Long> mapPartitionOffset(@NonNull final Map<Partition, EndOffset> partitionEndOffset) {
        return partitionEndOffset.entrySet().stream().collect(Collectors.toMap(
                entry -> { final Partition partition = entry.getKey(); return partition.number(); },
                entry -> { final EndOffset endOffset = entry.getValue(); return endOffset.number(); }
        ));
    }
}
