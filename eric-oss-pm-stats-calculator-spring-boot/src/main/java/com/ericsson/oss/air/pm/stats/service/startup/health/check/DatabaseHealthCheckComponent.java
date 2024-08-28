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

import com.ericsson.oss.air.pm.stats.bragent.model.RestoreStatus;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@DependsOn("kpiStartupService")
public class DatabaseHealthCheckComponent implements HealthCheckComponent {

    private HealthCheckMonitor healthCheckMonitor;
    private DatabaseService databaseService;
    private RestoreStatus restoreStatus;

    @Override
    public Component getComponent() {
        return Component.KPI_DATABASE;
    }

    @Override
    @Scheduled(fixedRate = 30_000, initialDelay = 5_000)
    public void execute() {
        log.debug("Checking health of database");
        if (restoreStatus.isRestoreOngoing()) {
            log.debug("Restore is ongoing, DB healthcheck is skipped");
            healthCheckMonitor.markHealthy(getComponent());
        } else if (databaseService.isAvailable()) {
            log.debug("{} is healthy", getComponent());
            healthCheckMonitor.markHealthy(getComponent());
        } else {
            log.warn("{} is not healthy", getComponent());
            healthCheckMonitor.markUnHealthy(getComponent());
        }
    }
}
