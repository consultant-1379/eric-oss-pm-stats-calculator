/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class KpiExposureStartupServiceTest {
    private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    private static final String EXPOSURE_TOPIC_NAME = "kpi-exposure-topic";

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));

    @Spy Properties kafkaAdminProperties;
    @Mock CalculatorProperties calculatorPropertiesMock;
    @Spy Properties kpiExposureTopicProperties;

    KpiExposureStartupService objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new KpiExposureStartupService(kafkaAdminProperties, calculatorPropertiesMock, kpiExposureTopicProperties);

        when(calculatorPropertiesMock.getKafkaExposureTopicName()).thenReturn(EXPOSURE_TOPIC_NAME);

        kafkaAdminProperties.put(BOOTSTRAP_SERVERS, KAFKA_CONTAINER.getBootstrapServers());

        kpiExposureTopicProperties.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
        kpiExposureTopicProperties.put(TopicConfig.DELETE_RETENTION_MS_CONFIG, "100");
        kpiExposureTopicProperties.put(TopicConfig.SEGMENT_MS_CONFIG, "100");
        kpiExposureTopicProperties.put(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0.01");
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldCreateKafkaTopic_onServiceStart() throws Exception {
        objectUnderTest.onServiceStart();

        final Properties adminProperties = new Properties();
        adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        try (final Admin admin = Admin.create(adminProperties)) {
            assertThat(admin.listTopics().names().get()).contains(calculatorPropertiesMock.getKafkaExposureTopicName());
        }
    }
}
