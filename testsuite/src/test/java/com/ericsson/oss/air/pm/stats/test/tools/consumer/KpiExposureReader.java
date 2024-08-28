/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.consumer;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;
import com.ericsson.oss.air.pm.stats.test.tools.consumer.serializer.KpiExposureConfigDeserializer;

import com.google.common.collect.Iterables;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.opentest4j.AssertionFailedError;

/**
 * Reads execution reports from Kafka topic.
 */
public class KpiExposureReader implements Closeable {

    private static final String BOOTSTRAP_SERVERS = "eric-data-message-bus-kf:9092";

    private static final String GROUP_ID = "calc_grp";
    private static final String TOPIC_NAME = "pm-stats-calculator-json-exposure-control";
    private static final int PARTITION = 0;
    private final KafkaConsumer<String, KpiExposureConfig> consumer;

    public KpiExposureReader() {
        final Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KpiExposureConfigDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_uncommitted");
        consumer = new KafkaConsumer<>(properties);
        TopicPartition partition0 = new TopicPartition(TOPIC_NAME, PARTITION);
        consumer.assign(List.of(partition0));
    }

    /**
     * Reads kpi exposure configs from Kafka topic at a specific offset. Throws {@link AssertionFailedError} if the assigned
     * partition or the record at the targeted offset does not exist or is not available.
     *
     * @param targetOffset
     *             offset to seek before polling for records
     * @return {@link KpiExposureConfig}
     */
    public KpiExposureConfig readKpiExposures(long targetOffset) {
        final Set<TopicPartition> partitions = consumer.assignment();
        final TopicPartition topicPartition = Iterables.getOnlyElement(partitions);
        Assertions.assertThat(topicPartition.partition()).isEqualTo(PARTITION);
        consumer.seek(topicPartition, targetOffset);
        final ConsumerRecords<String, KpiExposureConfig> records = consumer.poll(Duration.ofSeconds(10));
        final ConsumerRecord<String, KpiExposureConfig> record = Iterables.getOnlyElement(records.records(topicPartition));
        Assertions.assertThat(record.offset()).isEqualTo(targetOffset);
        return record.value();
    }

    /**
     * Closes the underlying kafka consumer.
     */
    @Override
    public void close() {
        consumer.close();
    }
}
