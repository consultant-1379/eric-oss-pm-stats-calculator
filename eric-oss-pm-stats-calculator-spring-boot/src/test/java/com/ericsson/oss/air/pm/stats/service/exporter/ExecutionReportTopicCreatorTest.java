/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;

import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(MockitoExtension.class)
@DisplayName("Execution report topic creater test")
class ExecutionReportTopicCreatorTest {
    static final String EXECUTION_REPORT_TOPIC_NAME = "pm-stats-exporter-json-execution-report-test";

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));

    @Mock CalculatorProperties calculatorPropertiesMock;
    @Spy Properties kafkaAdminProperties;

    @InjectMocks ExecutionReportTopicCreator objectUnderTest;

    @BeforeEach
    void setup() {
        kafkaAdminProperties.put("bootstrap.servers", KAFKA_CONTAINER.getBootstrapServers());
    }

    @Test
    @SneakyThrows
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldInitKafka() {
        doReturn(EXECUTION_REPORT_TOPIC_NAME).when(calculatorPropertiesMock).getKafkaExecutionReportTopicName();
        objectUnderTest.createTopic();

        try (final Admin admin = Admin.create(kafkaAdminProperties)) {
            assertThat(admin.listTopics().names().get()).contains(EXECUTION_REPORT_TOPIC_NAME);
        }
    }

    @Test
    @SneakyThrows
    void whenTopicExists_shouldNoErrorAppear() {
        doReturn(EXECUTION_REPORT_TOPIC_NAME).when(calculatorPropertiesMock).getKafkaExecutionReportTopicName();

        try (final Admin admin = Admin.create(kafkaAdminProperties)) {
            if (!admin.listTopics().names().get(5, TimeUnit.SECONDS).contains(EXECUTION_REPORT_TOPIC_NAME)) {
                final short replicas = 1;
                admin.createTopics(Collections.singletonList(
                                new NewTopic(EXECUTION_REPORT_TOPIC_NAME, 1, replicas)))
                        .values().get(EXECUTION_REPORT_TOPIC_NAME).get(10, TimeUnit.SECONDS);
            }
            assertThatNoException().isThrownBy(() -> objectUnderTest.createTopic());

            assertThat(admin.listTopics().names().get()).contains(EXECUTION_REPORT_TOPIC_NAME);
        }
    }

    @Test
    @SneakyThrows
    void whenExceptionThrown_shouldInitFailWithStartupException() {
        final Admin adminMock = mock(Admin.class);
        final ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        final KafkaFuture<List<String>> listFutureMock = mock(KafkaFuture.class);

        try (MockedStatic<Admin> adminMockedStatic = mockStatic(Admin.class)) {
            adminMockedStatic.when(() -> Admin.create(any(Properties.class))).thenReturn(adminMock);
            doReturn(listTopicsResult).when(adminMock).listTopics();
            doReturn(listFutureMock).when(listTopicsResult).names();
            doThrow(TimeoutException.class).when(listFutureMock).get(anyLong(), any(TimeUnit.class));
            assertThatThrownBy(() -> objectUnderTest.createTopic())
                    .isInstanceOf(StartupException.class)
                    .hasMessage("Error occurred while creating kafka topic for execution report.");
            adminMockedStatic.verify(() -> Admin.create(any(Properties.class)));
        }
    }
}
