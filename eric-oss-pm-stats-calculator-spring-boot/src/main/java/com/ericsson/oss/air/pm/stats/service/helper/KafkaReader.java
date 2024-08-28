/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.helper;

import static lombok.AccessLevel.PUBLIC;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class KafkaReader {

    @Inject
    private KafkaConsumer<String, String> kafkaConsumer;

    public Map<TopicPartition, Long> gatherEndOffsetsForTopic(final String topic) {
        return kafkaConsumer.endOffsets(loadTopicPartitions(topic));
    }

    private List<TopicPartition> loadTopicPartitions(@NonNull final String topic) {
        return kafkaConsumer.partitionsFor(topic).stream().map(KafkaReader::topicPartition).collect(Collectors.toList());
    }

    private static TopicPartition topicPartition(@NonNull final PartitionInfo partitionInfo) {
        return new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
    }
}