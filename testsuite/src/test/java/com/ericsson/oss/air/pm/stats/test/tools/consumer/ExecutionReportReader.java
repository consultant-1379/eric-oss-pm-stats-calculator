/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.test.tools.consumer.serializer.ExecutionReportDeserializer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Reads execution reports from Kafka topic.
 */
public class ExecutionReportReader implements Closeable {

    private static final String BOOTSTRAP_SERVERS = "eric-data-message-bus-kf:9092";

    private static final String GROUP_ID = "calc_grp";
    private static final String EXECUTION_REPORT_TOPIC = "pm-stats-calculator-json-execution-report";
    private final KafkaConsumer<String, ExecutionReport> consumer;

    public ExecutionReportReader() {
        final Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ExecutionReportDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(List.of(EXECUTION_REPORT_TOPIC));
    }

    /**
     * Reads execution reports from Kafka topic.
     * @return list of {@link ExecutionReport}
     */
    public List<ExecutionReport> readExecutionReports() {
        return StreamSupport.stream(consumer.poll(Duration.ofSeconds(10)).spliterator(), false)
            .map(ConsumerRecord::value).collect(Collectors.toList());
    }

    /**
     * Closes the underlying kafka consumer.
     */
    @Override
    public void close() {
        consumer.close();
    }
}
