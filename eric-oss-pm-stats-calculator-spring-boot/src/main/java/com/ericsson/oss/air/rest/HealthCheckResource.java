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

import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.itpf.sdk.microhealthcheck.DefaultHealthCheckProvider;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that exposes the health status of the service.
 */
@RestController
@RequestMapping("/calculator-service/health")
@RequiredArgsConstructor
public class HealthCheckResource extends DefaultHealthCheckProvider {

    private final HealthCheckMonitor healthCheckMonitor;

    @Override
    @GetMapping
    public HealthCheckResponse getAppHealthStatus() {
        return healthCheckMonitor.getCurrentHealth();
    }
}