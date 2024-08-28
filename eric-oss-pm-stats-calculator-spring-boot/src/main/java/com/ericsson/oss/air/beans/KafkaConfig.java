/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.beans;

import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.util.KafkaJsonSerializer;
import com.ericsson.oss.air.pm.stats.service.util.KpiExposureConfigSerializer;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public Properties kafkaAdminProperties(final CalculatorProperties calculatorProperties) {
        final Properties adminProperties = new Properties();
        adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, calculatorProperties.getKafkaBootstrapServers());
        adminProperties.put(AdminClientConfig.RECONNECT_BACKOFF_MS_CONFIG, String.valueOf(1000));
        adminProperties.put(AdminClientConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, String.valueOf(10000));
        return adminProperties;
    }

    @Bean
    public Properties kpiExposureKafkaTopicProperties() {
        final Properties topicProperties = new Properties();
        topicProperties.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
        topicProperties.put(TopicConfig.DELETE_RETENTION_MS_CONFIG, "100");
        topicProperties.put(TopicConfig.SEGMENT_MS_CONFIG, "100");
        topicProperties.put(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0.01");
        return topicProperties;
    }

    @Bean
    public Properties kpiExecutionReportProducerProperties(final CalculatorProperties calculatorProperties) {
        final Properties producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, calculatorProperties.getKafkaBootstrapServers());
        producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class.getName());
        return producerProperties;
    }

    @Bean
    public Properties kpiExposureKafkaProducerProperties(final CalculatorProperties calculatorProperties) {
        final Properties producerProperties = new Properties();

        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, calculatorProperties.getKafkaBootstrapServers());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KpiExposureConfigSerializer.class.getName());
        return producerProperties;
    }

    @Bean
    public Properties kafkaConsumerProperties(final CalculatorProperties calculatorProperties) {
        final Properties consumerProperties = new Properties();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, calculatorProperties.getKafkaBootstrapServers());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return consumerProperties;
    }

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer(final Properties kafkaConsumerProperties) {
        return new KafkaConsumer<>(kafkaConsumerProperties);
    }

    @Bean
    public KafkaProducer<String, ExecutionReport> kafkaProducer(final Properties kpiExecutionReportProducerProperties) {
        return new KafkaProducer<>(kpiExecutionReportProducerProperties);
    }

    @Bean
    public Producer<String, KpiExposureConfig> kafkaExposureProducer(final Properties kpiExposureKafkaProducerProperties) {
        return new KafkaProducer<>(kpiExposureKafkaProducerProperties);
    }
}