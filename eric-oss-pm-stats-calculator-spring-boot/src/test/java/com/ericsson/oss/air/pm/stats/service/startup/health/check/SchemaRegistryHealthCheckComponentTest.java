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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaRegistryHealthCheckComponentTest {

    @Mock CalculatorProperties calculatorPropertiesMock;
    @Mock RestExecutor restExecutorMock;
    @Mock HealthCheckMonitor healthCheckMonitorMock;

    @InjectMocks SchemaRegistryHealthCheckComponent objectUnderTest;

    @Test
    void shouldGetComponent() {
        Assertions.assertThat(objectUnderTest.getComponent()).isEqualTo(Component.SCHEMA_REGISTRY);
    }


    @Nested
    @DisplayName("Given RestExecutor initialized")
    class GivenRestExecutorInitialized {
        private static final String SCHEMA_URL = "schemaUrl";

        @Mock
        RestResponse<String> responseMock;

        @ValueSource(ints = {
                HttpStatus.SC_OK,
                HttpStatus.SC_BAD_REQUEST
        })
        @SneakyThrows
        @ParameterizedTest(name = "[{index}] Marks healthy with response code: ''{0}''")
        void shouldMarkHealthy(final int statusCode) {
            when(calculatorPropertiesMock.getSchemaRegistryUrl()).thenReturn(SCHEMA_URL);
            when(restExecutorMock.sendGetRequest(any(HttpGet.class))).thenReturn(responseMock);
            when(responseMock.getStatus()).thenReturn(statusCode);

            objectUnderTest.execute();

            verify(calculatorPropertiesMock).getSchemaRegistryUrl();
            verify(restExecutorMock).sendGetRequest(any(HttpGet.class));
            verify(responseMock).getStatus();
            verify(healthCheckMonitorMock).markHealthy(Component.SCHEMA_REGISTRY);
        }

        @Test
        @SneakyThrows
        void shouldMarkUnhealthy() {
            when(calculatorPropertiesMock.getSchemaRegistryUrl()).thenReturn(SCHEMA_URL);
            when(restExecutorMock.sendGetRequest(any(HttpGet.class))).thenReturn(responseMock);
            when(responseMock.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

            objectUnderTest.execute();

            verify(calculatorPropertiesMock).getSchemaRegistryUrl();
            verify(restExecutorMock).sendGetRequest(any(HttpGet.class));
            verify(responseMock).getStatus();
            verify(healthCheckMonitorMock).markUnHealthy(Component.SCHEMA_REGISTRY);
        }

        @Test
        @SneakyThrows
        void shouldMarkUnhealthy_whenSchemaRegistryIsNotReachable() {
            when(calculatorPropertiesMock.getSchemaRegistryUrl()).thenReturn(SCHEMA_URL);
            doThrow(IOException.class).when(restExecutorMock).sendGetRequest(any(HttpGet.class));

            objectUnderTest.execute();

            verify(calculatorPropertiesMock).getSchemaRegistryUrl();
            verify(restExecutorMock).sendGetRequest(any(HttpGet.class));
            verify(healthCheckMonitorMock).markUnHealthy(Component.SCHEMA_REGISTRY);
        }

    }
}