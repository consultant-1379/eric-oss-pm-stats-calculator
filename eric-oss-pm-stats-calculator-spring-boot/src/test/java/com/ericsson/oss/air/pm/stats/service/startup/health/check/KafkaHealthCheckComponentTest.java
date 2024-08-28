/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup.health.check;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent.Component;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class KafkaHealthCheckComponentTest {

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));

    @Mock CalculatorProperties calculatorPropertiesMock;
    @Mock HealthCheckMonitor healthCheckMonitorMock;

    @InjectMocks KafkaHealthCheckComponent objectUnderTest;

    @Test
    void shouldGetComponent() {
        Assertions.assertThat(objectUnderTest.getComponent()).isEqualTo(Component.KAFKA);
    }

    @Test
    void shouldExecute_andIndicateKafkaIsHealthy() {
        when(calculatorPropertiesMock.getKafkaBootstrapServers()).thenReturn(KAFKA_CONTAINER.getBootstrapServers());

        objectUnderTest.execute();

        verify(calculatorPropertiesMock).getKafkaBootstrapServers();
        verify(healthCheckMonitorMock).markHealthy(Component.KAFKA);
    }

    @Test
    void shouldExecute_andIndicateKafkaIsUnHealthy() {
        when(calculatorPropertiesMock.getKafkaBootstrapServers()).thenReturn("unknownHost");

        objectUnderTest.execute();

        verify(calculatorPropertiesMock).getKafkaBootstrapServers();
        verify(healthCheckMonitorMock).markUnHealthy(Component.KAFKA);
    }
}