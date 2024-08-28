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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OffsetDistanceCalculator {

    /**
     * Calculates the distance between the last offset for a given partition and the last offset for that partition from the database.
     * <br>
     * The distance in this case is the number of un-read messages on Kafka.
     * <br>
     * Distance calculation examples:
     * <pre>
     *     distance_between(<strong>Kafka(50)</strong>, <strong>database(30)</strong>) = 20
     *     distance_between(<strong>Kafka(50)</strong>, <strong>null</strong>)         = 50
     * </pre>
     *
     * @param latestProcessedOffsets
     *         latest offsets known in the database
     * @param endOffsets
     *         latest offsets on Kafka
     * @return {@link List} of {@link TopicPartitionOffsetDistance} containing distances between offsets on Kafka and in the database
     * @implNote The implementation expects unique elements in the {@link List} of {@link LatestProcessedOffset}.
     */
    public List<TopicPartitionOffsetDistance> calculateOffsetDistances(@NonNull final List<? extends LatestProcessedOffset> latestProcessedOffsets,
                                                                       final Map<TopicPartition, EndOffset> endOffsets) {
        return calculateOffsetDistances(determineStartOffsets(latestProcessedOffsets), endOffsets);
    }

    private static List<TopicPartitionOffsetDistance> calculateOffsetDistances(final Map<TopicPartition, StartOffset> startOffsets,
                                                                               final @NonNull Map<TopicPartition, EndOffset> endOffsets) {
        final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistances = new ArrayList<>(endOffsets.size());

        endOffsets.forEach((topicPartition, endOffset) -> {
            final StartOffset startOffset = startOffsets.get(topicPartition);
            log.info("For topic partition '{}' start offset '{}' end offset '{}'", topicPartition, startOffset.number(), endOffset.number());
            topicPartitionOffsetDistances.add(TopicPartitionOffsetDistance.of(topicPartition, startOffset, endOffset));
        });

        return topicPartitionOffsetDistances;
    }

    private static Map<TopicPartition, StartOffset> determineStartOffsets(@NonNull final List<? extends LatestProcessedOffset> latestProcessedOffsets) {
        return latestProcessedOffsets.stream().collect(Collectors.toMap(
                LatestProcessedOffset::asTopicPartition,
                StartOffset::from
        ));
    }

}
