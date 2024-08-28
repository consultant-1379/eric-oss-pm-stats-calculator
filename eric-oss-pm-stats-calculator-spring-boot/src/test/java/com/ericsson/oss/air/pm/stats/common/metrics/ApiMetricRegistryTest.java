/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiMetricRegistryTest {

    private final ApiMetricRegistry objectUnderTest = new ApiMetricRegistry(new MetricRegistry());

    @BeforeEach
    void init() {
        objectUnderTest.getMetricRegistry().removeMatching(MetricFilter.ALL);
    }

    @Test
    void getMetricRegistry() {
        final MetricRegistry result = objectUnderTest.getMetricRegistry();
        assertThat(result.getNames()).isEmpty();
    }

    @Test
    void getTimer() {
        objectUnderTest.timer(ApiTimer.DEFINITION_GET_SERVICE_TIME, () -> null);
        assertThat(objectUnderTest.getMetricRegistry().getNames()).containsExactly("definition_get_endpoint_duration_ms");
    }

    @Test
    void getCounter() {
        objectUnderTest.counter(ApiCounter.DEFINITION_PERSISTED_KPI);
        assertThat(objectUnderTest.getMetricRegistry().getNames()).containsExactly("definition_persisted_kpi");
    }

    @Test
    void getMeter() {
        objectUnderTest.meter("Test meter");
        assertThat(objectUnderTest.getMetricRegistry().getNames()).containsExactly("Test meter");
    }

    @Test
    void getHistogram() {
        objectUnderTest.histogram("Test histogram");
        assertThat(objectUnderTest.getMetricRegistry().getNames()).containsExactly("Test histogram");
    }

    @Test
    void getDefaultSettableGauge() {
        objectUnderTest.defaultSettableGauge(ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES);
        assertThat(objectUnderTest.getMetricRegistry().getNames()).containsExactly("calculation_post_compressed_payload_size_in_bytes");
    }
}