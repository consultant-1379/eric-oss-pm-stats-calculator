/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup.health;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor.Indicator;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent.Component;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckResponse;
import com.ericsson.oss.itpf.sdk.microhealthcheck.HealthCheckResponse.State;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HealthCheckMonitorTest {
    private static final String APPLICATION_NAME = "eric-oss-pm-stats-calculator";

    private HealthCheckMonitor objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new HealthCheckMonitor();
    }

    @Test
    void shouldSetHealthyIndicator() {
        objectUnderTest.markHealthy(Component.SPARK_MASTER);

        final Map<Component, Object> actual = getHealthIndicators();

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(Collections.singletonMap(Component.SPARK_MASTER, Indicator.HEALTHY));
    }

    @Test
    void shouldSetUnHealthyIndicator() {
        objectUnderTest.markUnHealthy(Component.SPARK_MASTER);

        final Map<Component, Object> actual = getHealthIndicators();

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(Collections.singletonMap(Component.SPARK_MASTER, Indicator.UN_HEALTHY));
    }

    @Test
    void shouldReturnDown_WhenServiceIsNotHealthy() {
        objectUnderTest.markUnHealthy(Component.SPARK_MASTER);
        objectUnderTest.markUnHealthy(Component.KPI_DATABASE);
        final HealthCheckResponse actual = objectUnderTest.getCurrentHealth();

        Assertions.assertThat(actual)
                .satisfies(healthCheckResponse -> {
                    Assertions.assertThat(healthCheckResponse.getState()).isEqualTo(State.DOWN);
                    Assertions.assertThat(healthCheckResponse.getAppName()).isEqualTo(APPLICATION_NAME);

                    final Map<String, Object> expected = new HashMap<>(2);
                    expected.put("SPARK_MASTER", Indicator.UN_HEALTHY);
                    expected.put("KPI_DATABASE", Indicator.UN_HEALTHY);
                    Assertions.assertThat(healthCheckResponse.getAdditionalData()).containsExactlyInAnyOrderEntriesOf(expected);
                });
    }

    @Test
    void shouldReturnUp_WhenServiceIsHealthy() {
        objectUnderTest.markHealthy(Component.SPARK_MASTER);
        objectUnderTest.markHealthy(Component.KPI_DATABASE);
        final HealthCheckResponse actual = objectUnderTest.getCurrentHealth();

        Assertions.assertThat(actual)
                .satisfies(healthCheckResponse -> {
                    Assertions.assertThat(healthCheckResponse.getState()).isEqualTo(State.UP);
                    Assertions.assertThat(healthCheckResponse.getAppName()).isEqualTo(APPLICATION_NAME);

                    final Map<String, Object> expected = new HashMap<>(2);
                    expected.put("SPARK_MASTER", Indicator.HEALTHY);
                    expected.put("KPI_DATABASE", Indicator.HEALTHY);
                    Assertions.assertThat(healthCheckResponse.getAdditionalData()).containsExactlyInAnyOrderEntriesOf(expected);
                });
    }

    @Test
    void shouldChangeHealthiness() {
        objectUnderTest.markUnHealthy(Component.SPARK_MASTER);
        Assertions.assertThat(objectUnderTest.getCurrentHealth())
                .extracting(HealthCheckResponse::getState)
                .isEqualTo(State.DOWN);

        objectUnderTest.markHealthy(Component.SPARK_MASTER);
        Assertions.assertThat(objectUnderTest.getCurrentHealth())
                .extracting(HealthCheckResponse::getState)
                .isEqualTo(State.UP);

        objectUnderTest.markUnHealthy(Component.SPARK_MASTER);
        Assertions.assertThat(objectUnderTest.getCurrentHealth())
                .extracting(HealthCheckResponse::getState)
                .isEqualTo(State.DOWN);
    }

    @SneakyThrows
    private Map<Component, Object> getHealthIndicators() {
        final Field field = FieldUtils.getDeclaredField(HealthCheckMonitor.class, "healthIndicators", true);

        @SuppressWarnings("unchecked") final Map<Component, Object> healthIndicators = (Map<Component, Object>) field.get(objectUnderTest);
        return healthIndicators;
    }
}