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

import static com.ericsson.oss.air.pm.stats.calculator.util.CollectionHelpers.deepCopy;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetCacheFacade {
    private final CurrentOffsetCache currentOffsetCache;
    private final PersistedOffsetCache persistedOffsetCache;
    private final MessageCounter messageCounter;

    public List<LatestProcessedOffset> determineStartingOffset(final Topic topic, final Integer aggregationPeriod) {
        return readLatestProcessedOffsets(TopicAggregationPeriod.of(topic, aggregationPeriod));
    }

    public List<LatestProcessedOffset> cache(final TopicAggregationPeriod topicAggregationPeriod, final List<LatestProcessedOffset> latestProcessedOffsets) {
        currentOffsetCache.cache(topicAggregationPeriod, latestProcessedOffsets);
        return latestProcessedOffsets;
    }


    public List<LatestProcessedOffset> getCurrentOffsets() {
        return currentOffsetCache.getCurrentOffsets();
    }

    private List<LatestProcessedOffset> readLatestProcessedOffsets(final TopicAggregationPeriod topicAggregationPeriod) {
        final List<LatestProcessedOffset> memory = readFromMemory(topicAggregationPeriod);
        if (isNotEmpty(memory)) {
            // To hide side effect of incrementing start offsets
            final List<LatestProcessedOffset> copy = deepCopy(memory);

            return incrementStartingOffsets(copy);
        }

        final List<LatestProcessedOffset> database = readFromDatabase(topicAggregationPeriod);
        if (isNotEmpty(database)) {
            // To hide side effect of incrementing start offsets, and setting value in a cached reference we deep copy the entities
            final List<LatestProcessedOffset> cache = cache(topicAggregationPeriod, deepCopy(database));

            return incrementStartingOffsets(cache);
        }

        // Not need to verify for size as if there are no messages to read then the Spark executable stops at the beginning
        // For Kafka there is no need to increment the start offset as it is the origin starting offset
        return cache(topicAggregationPeriod, readFromKafka(topicAggregationPeriod));
    }

    private List<LatestProcessedOffset> incrementStartingOffsets(@NonNull final List<LatestProcessedOffset> readLatestProcessedOffsets) {
        for (final LatestProcessedOffset latestProcessedOffset : readLatestProcessedOffsets) {
            if (latestProcessedOffset.doesNeedIncrement()) {
                final Topic topic = Topic.of(latestProcessedOffset.getTopicName());

                final Map<TopicPartition, EndOffset> topicPartitionEndOffsets = messageCounter.countMessagesByPartition(topic);
                final EndOffset endOffset = topicPartitionEndOffsets.get(latestProcessedOffset.asTopicPartition());

                if (latestProcessedOffset.getTopicPartitionOffset() < endOffset.number()) {
                    latestProcessedOffset.incrementTopicPartitionOffset();
                }
            }
        }

        return readLatestProcessedOffsets;
    }

    private List<LatestProcessedOffset> readFromMemory(final TopicAggregationPeriod topicAggregationPeriod) {
        return currentOffsetCache.readCache(topicAggregationPeriod);
    }

    private List<LatestProcessedOffset> readFromDatabase(@NonNull final TopicAggregationPeriod topicAggregationPeriod) {
        final Map<Topic, List<LatestProcessedOffset>> topicLatestProcessedOffsets = persistedOffsetCache.loadPersistedOffsets();
        return topicLatestProcessedOffsets.get(topicAggregationPeriod.topic());
    }

    private List<LatestProcessedOffset> readFromKafka(@NonNull final TopicAggregationPeriod topicAggregationPeriod) {
        final Map<TopicPartition, StartOffset> startOffsetForTopic = messageCounter.getStartOffsetForTopic(topicAggregationPeriod.topic());
        final List<LatestProcessedOffset> latestProcessedOffsets = new ArrayList<>(startOffsetForTopic.size());

        final String startMessage = String.format("Collected start offsets from Kafka to topic '%s':", topicAggregationPeriod.topic().name());
        final StringBuilder logCollector = new StringBuilder(startMessage);

        startOffsetForTopic.forEach((topicPartition, startOffset) -> {
            logCollector.append(System.lineSeparator());
            logCollector.append(String.format("    Partition: '%s' Start offset: '%s'", topicPartition.partition(), startOffset.number()));
            latestProcessedOffsets.add(toLatestProcessedOffset(topicPartition, startOffset));
        });

        log.info(logCollector.toString());

        return latestProcessedOffsets;
    }

    private static LatestProcessedOffset toLatestProcessedOffset(@NonNull final TopicPartition topicPartition, @NonNull final StartOffset startOffset) {
        final LatestProcessedOffsetBuilder builder = LatestProcessedOffset.builder();
        builder.withTopicName(topicPartition.topic());
        builder.withTopicPartition(topicPartition.partition());
        builder.withTopicPartitionOffset(startOffset.number());
        builder.withFromKafka(true);
        return builder.build();
    }
}
