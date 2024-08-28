/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PUBLIC;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.repository.api.LatestProcessedOffsetsRepository;
import com.ericsson.oss.air.pm.stats.service.helper.KafkaReader;
import com.ericsson.oss.air.pm.stats.service.helper.model.TopicDetail;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KafkaOffsetCheckerFacade {

    @Inject
    private KafkaReader kafkaReader;
    @Inject
    private LatestProcessedOffsetsRepository latestProcessedOffsetsRepository;

    public void compareDatabaseToKafka() {
        final List<LatestProcessedOffset> latestProcessedOffsets = latestProcessedOffsetsRepository.findAll();
        final Map<String, List<LatestProcessedOffset>> offsetsByTopic = latestProcessedOffsets.stream().collect(groupingBy(LatestProcessedOffset::getTopicName, toList()));

        final Map<String, TopicDetail> databaseOffsetsByTopic = offsetsByTopic.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> TopicDetail.of(value.getValue())));

        final Set<String> toDelete = new HashSet<>(databaseOffsetsByTopic.keySet().size());

        databaseOffsetsByTopic.forEach((topic, details) -> {
            Map<TopicPartition, Long> kafkaEnds = kafkaReader.gatherEndOffsetsForTopic(topic);
            kafkaEnds.forEach((partition, endOffset) -> {
                if (details.getOffset(partition.partition()) > endOffset) {
                    toDelete.add(topic);
                    log.warn("Topic: '{}' has lower offsets in Kafka than in the database, database cache will be truncated! " +
                            "Next iteration will read starting offsets from Kafka", topic);
                    //TODO: stop checking the topic after the first found difference
                }
            });
        });

        latestProcessedOffsetsRepository.deleteOffsetsByTopic(toDelete);
    }

    public boolean hasNewMessage(final String executionGroup) {
        final List<LatestProcessedOffset> latestProcessedOffsets = latestProcessedOffsetsRepository.findAllForExecutionGroup(executionGroup);

        if (latestProcessedOffsets.isEmpty()) {
            return true;
        }

        final Set<String> topicNames = latestProcessedOffsets.stream().map(LatestProcessedOffset::getTopicName).collect(Collectors.toSet());

        for (final String topic : topicNames) {
            final Map<TopicPartition, Long> kafkaEnds = kafkaReader.gatherEndOffsetsForTopic(topic);

            final long totalMessages = CollectionHelpers.sumExact(kafkaEnds.values().stream());
            final long computedMessages = computedMessages(topic, latestProcessedOffsets);

            log.info("Processed messages: {}, read messages: {} for topic '{}'", totalMessages, computedMessages, topic);

            if (totalMessages != computedMessages) {
                return true;
            }
        }
        return false;
    }

    private static long computedMessages(final String topic, final List<LatestProcessedOffset> latestProcessedOffsets) {
        long readMessages = 0;

        for (final LatestProcessedOffset latestProcessedOffset : latestProcessedOffsets) {
            if (latestProcessedOffset.getTopicName().equals(topic)) {
                final long offset = latestProcessedOffset.getTopicPartitionOffset();
                if (offset > 0) {
                    //  If offset is 0, then nothing has been read, so we need not increment
                    //  If offset is not 0, then it is saved exclusively, so we have to increment
                    readMessages += (offset + 1);
                }
            }
        }

        return readMessages;
    }
}