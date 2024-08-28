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

import java.io.IOException;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@DependsOn("kpiStartupService")
public class SchemaRegistryHealthCheckComponent implements HealthCheckComponent {

    private CalculatorProperties calculatorProperties;
    private RestExecutor restExecutor;
    private HealthCheckMonitor healthCheckMonitor;

    @Override
    public Component getComponent() {
        return Component.SCHEMA_REGISTRY;
    }

    @Override
    @Scheduled(fixedRate = 30_000, initialDelay = 5_000)
    public void execute() {
        log.debug("Checking health of Schema Registry");
        final HttpGet httpGetRequest = new HttpGet(calculatorProperties.getSchemaRegistryUrl());

        try (final RestResponse<String> response = restExecutor.sendGetRequest(httpGetRequest)) {
            final int status = response.getStatus();

            log.debug("Schema Registry HTTP request returns status {}", status);
            if (status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST) {
                healthCheckMonitor.markHealthy(getComponent());
            } else {
                healthCheckMonitor.markUnHealthy(getComponent());
            }
        } catch (final IOException e) { //NOSONAR Exception suitably logged
            log.warn("Schema Registry HTTP request failed");
            healthCheckMonitor.markUnHealthy(getComponent());
        }
    }
}
