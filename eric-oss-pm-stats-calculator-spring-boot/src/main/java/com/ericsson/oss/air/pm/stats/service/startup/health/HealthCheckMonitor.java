/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup.health;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent.Component;
import com.ericsson.oss.itpf.sdk.microhealthcheck.DefaultHealthCheckResponseBuilder;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckResponse;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckResponse.State;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@AccessTimeout(value = 10000)
public class HealthCheckMonitor {
    private static final String APPLICATION_NAME = "eric-oss-pm-stats-calculator";

    private final Map<Component, Object> healthIndicators = new EnumMap<>(Component.class);

    /**
     * Returns the current states of the dependent applications.
     *
     * @return an implementation of {@link HealthCheckResponse} containing the service health status
     */
    @Lock(LockType.READ)
    public HealthCheckResponse getCurrentHealth() {
        logUnhealthyComponents();

        return anyUnhealthyComponent()
                ? getHealthCheckResponse(State.DOWN)
                : getHealthCheckResponse(State.UP);

    }

    @Lock(LockType.WRITE)
    public void markUnHealthy(final Component component) {
        healthIndicators.put(component, Indicator.UN_HEALTHY);
    }

    @Lock(LockType.WRITE)
    public void markHealthy(final Component component) {
        healthIndicators.put(component, Indicator.HEALTHY);
    }

    private HealthCheckResponse getHealthCheckResponse(final State state) {
        return new DefaultHealthCheckResponseBuilder(state, APPLICATION_NAME, getAdditionalData()).build();
    }

    private HashMap<String, Object> getAdditionalData() {
        final HashMap<String, Object> result = new HashMap<>(healthIndicators.size());

        //  for-each used to avoid testing MergeFunction on Collectors.toMap()
        for (final Entry<Component, Object> entry : healthIndicators.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }

        return result;
    }

    private boolean anyUnhealthyComponent() {
        return healthIndicators.containsValue(Indicator.UN_HEALTHY);
    }

    private void logUnhealthyComponents() {
        healthIndicators.entrySet()
                .stream()
                .filter(this::isUnhealthy)
                .map(Entry::getKey)
                .forEach(unhealthyComponent -> log.warn("Component '{}' is unhealthy.", unhealthyComponent));
    }

    private boolean isUnhealthy(final Entry<Component, Object> entry) {
        return entry.getValue() == Indicator.UN_HEALTHY;
    }

    public enum Indicator {
        HEALTHY,
        UN_HEALTHY
    }

}
