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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class KafkaReaderTest {

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));
    KafkaConsumer<String, String> testConsumer;

    KafkaReader objectUnderTest;

    @BeforeEach
    void init() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", KAFKA_CONTAINER.getBootstrapServers());
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());

        testConsumer = new KafkaConsumer<>(properties);
        try (AdminClient admin = AdminClient.create(properties)) {
            admin.createTopics(List.of(new NewTopic("topic1", 3, (short) 1)));
            //TODO: figure out a way to delete messages from kafka while offset stays. retention did not work
        }
        objectUnderTest = new KafkaReader(testConsumer);
    }


    @Test
    void whenTopicIsBrandNew() {
        final Map<TopicPartition, Long> actual = objectUnderTest.gatherEndOffsetsForTopic("topic1");

        assertThat(actual).containsOnly(entry(new TopicPartition("topic1", 0), 0L),
                entry(new TopicPartition("topic1", 1), 0L),
                entry(new TopicPartition("topic1", 2), 0L)
        );
    }
}