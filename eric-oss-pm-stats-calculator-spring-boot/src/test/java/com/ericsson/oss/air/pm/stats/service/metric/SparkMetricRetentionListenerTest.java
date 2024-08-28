/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.metric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparkMetricRetentionListenerTest {

    private static final MetricRegistry metricRegistry = new MetricRegistry();

    private static SparkMetricRetentionListener objectUnderTest;

    @BeforeAll()
    static void beforeAll() {
        objectUnderTest = spy(new SparkMetricRetentionListener(0L));
        objectUnderTest.registerAsListener(metricRegistry);
    }

    @Test
    void shouldFireOnGaugeAdded() {
        metricRegistry.gauge("test_gauge");
        verify(objectUnderTest).onGaugeAdded(eq("test_gauge"), any());
    }

    @Test
    void shouldFireOnCounterAdded() {
        metricRegistry.counter("test_counter");
        verify(objectUnderTest).onCounterAdded(eq("test_counter"), any());
    }

    @Test
    void shouldFireOnHistogramAdded() {
        metricRegistry.histogram("test_histogram");
        verify(objectUnderTest).onHistogramAdded(eq("test_histogram"), any());
    }

    @Test
    void shouldFireOnMeterAdded() {
        metricRegistry.meter("test_meter");
        verify(objectUnderTest).onMeterAdded(eq("test_meter"), any());
    }

    @Test
    void shouldFireOnTimerAdded() {
        metricRegistry.timer("test_timer");
        verify(objectUnderTest).onTimerAdded(eq("test_timer"), any());
    }

    @Test
    void shouldClearExpiredMetrics() {
        String executionGroup = "executionGroup";
        UUID calculationId = UUID.randomUUID();
        String sparkMetricName = SparkMetricName.createStartTimeName(executionGroup, calculationId).toString();

        // Because the timeout is set to 0, it should be removed immediately after being added.
        metricRegistry.gauge(sparkMetricName);
        assertFalse(metricRegistry.getNames().contains(sparkMetricName));
    }
}