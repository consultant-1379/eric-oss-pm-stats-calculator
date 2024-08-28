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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.bragent.model.RestoreStatus;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent.Component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthCheckComponentTest {
    @Mock
    private HealthCheckMonitor healthCheckMonitorMock;
    @Mock
    private DatabaseService databaseServiceMock;
    @Mock
    private RestoreStatus restoreStatusMock;

    @InjectMocks
    private DatabaseHealthCheckComponent objectUnderTest;

    @Test
    void shouldExecute_andIndicateApplicationIsHealthy() {
        when(restoreStatusMock.isRestoreOngoing()).thenReturn(false);
        when(databaseServiceMock.isAvailable()).thenReturn(true);

        objectUnderTest.execute();

        verify(restoreStatusMock).isRestoreOngoing();
        verify(databaseServiceMock).isAvailable();
        verify(healthCheckMonitorMock).markHealthy(Component.KPI_DATABASE);
    }

    @Test
    void shouldExecute_andIndicateApplicationIsUnHealthy() {
        when(restoreStatusMock.isRestoreOngoing()).thenReturn(false);
        when(databaseServiceMock.isAvailable()).thenReturn(false);

        objectUnderTest.execute();

        verify(restoreStatusMock).isRestoreOngoing();
        verify(databaseServiceMock).isAvailable();
        verify(healthCheckMonitorMock).markUnHealthy(Component.KPI_DATABASE);
    }

    @Test
    void shouldExecute_andRestoreOngoing() {
        when(restoreStatusMock.isRestoreOngoing()).thenReturn(true);

        objectUnderTest.execute();

        verify(restoreStatusMock).isRestoreOngoing();
        verify(healthCheckMonitorMock).markHealthy(Component.KPI_DATABASE);
        verifyNoInteractions(databaseServiceMock);
    }

}