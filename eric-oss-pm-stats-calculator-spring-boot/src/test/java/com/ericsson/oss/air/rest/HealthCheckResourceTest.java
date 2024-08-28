/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckResponse;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckRestResponse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthCheckResourceTest {
    @Mock private HealthCheckMonitor healthCheckMonitorMock;
    @InjectMocks private HealthCheckResource objectUnderTest;

    @Test
    void shouldGetApplicationHealthStatus() {
        final HealthCheckResponse healthCheckRestResponse = new HealthCheckRestResponse();
        when(healthCheckMonitorMock.getCurrentHealth()).thenReturn(healthCheckRestResponse);

        final HealthCheckResponse actual = objectUnderTest.getAppHealthStatus();

        verify(healthCheckMonitorMock).getCurrentHealth();
        Assertions.assertThat(actual).isEqualTo(healthCheckRestResponse);
    }
}