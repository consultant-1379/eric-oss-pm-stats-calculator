/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.consumer;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final KafkaConsumer<String, String> kafkaConsumer;

    public List<TopicPartition> loadTopicPartitions(final Topic topic) {
        return loadTopicPartitions(topic.name());
    }

    public List<TopicPartition> loadTopicPartitions(final String topic) {
        return kafkaConsumer.partitionsFor(topic).stream().map(KafkaConsumerService::topicPartition).collect(toList());
    }

    private static TopicPartition topicPartition(final PartitionInfo partitionInfo) {
        return new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
    }
}
