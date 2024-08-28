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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent.Component;

import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparkMasterHealthCheckComponentTest {

    @Mock CalculatorProperties calculatorPropertiesMock;
    @Mock RestExecutor restExecutorMock;
    @Mock HealthCheckMonitor healthCheckMonitorMock;

    @InjectMocks
    SparkMasterHealthCheckComponent objectUnderTest;

    @Test
    void shouldGetComponent() {
        Assertions.assertThat(objectUnderTest.getComponent()).isEqualTo(Component.SPARK_MASTER);
    }

    @Nested
    @DisplayName("Given RestExecutor initialized")
    class GivenRestExecutorInitialized {
        private static final String SPARK_URL = "sparkUrl";

        @Mock
        RestResponse<String> responseMock;

        @Test
        @SneakyThrows
        void shouldMarkHealthy() {
            when(calculatorPropertiesMock.getSparkMasterUrl()).thenReturn(SPARK_URL);
            when(restExecutorMock.sendGetRequest(any(HttpGet.class))).thenReturn(responseMock);
            when(responseMock.getStatus()).thenReturn(HttpStatus.SC_OK);

            objectUnderTest.execute();

            verify(calculatorPropertiesMock).getSparkMasterUrl();
            verify(restExecutorMock).sendGetRequest(any(HttpGet.class));
            verify(responseMock).getStatus();
            verify(healthCheckMonitorMock).markHealthy(Component.SPARK_MASTER);
        }

        @Test
        @SneakyThrows
        void shouldMarkUnhealthy() {
            when(calculatorPropertiesMock.getSparkMasterUrl()).thenReturn(SPARK_URL);
            when(restExecutorMock.sendGetRequest(any(HttpGet.class))).thenReturn(responseMock);
            when(responseMock.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

            objectUnderTest.execute();

            verify(calculatorPropertiesMock).getSparkMasterUrl();
            verify(restExecutorMock).sendGetRequest(any(HttpGet.class));
            verify(responseMock).getStatus();
            verify(healthCheckMonitorMock).markUnHealthy(Component.SPARK_MASTER);
        }

        @Test
        @SneakyThrows
        void shouldMarkUnhealthy_whenSparkIsNotReachable() {
            when(calculatorPropertiesMock.getSparkMasterUrl()).thenReturn(SPARK_URL);
            doThrow(IOException.class).when(restExecutorMock).sendGetRequest(any(HttpGet.class));

            objectUnderTest.execute();

            verify(calculatorPropertiesMock).getSparkMasterUrl();
            verify(restExecutorMock).sendGetRequest(any(HttpGet.class));
            verify(healthCheckMonitorMock).markUnHealthy(Component.SPARK_MASTER);
        }

    }
}
