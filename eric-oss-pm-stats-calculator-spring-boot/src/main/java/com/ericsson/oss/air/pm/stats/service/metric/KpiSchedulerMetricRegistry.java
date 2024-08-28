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

import java.util.Arrays;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.common.metrics.AbstractMetricRegistry;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class KpiSchedulerMetricRegistry extends AbstractMetricRegistry {
    public static final String METRIC_DOMAIN = "pm_stats_calculator_scheduler";

    // @TODO: Remove when DI injection is implemented for the 3PP classes.
    public KpiSchedulerMetricRegistry() {
        this(new MetricRegistry());
    }

    public KpiSchedulerMetricRegistry(final MetricRegistry metricRegistry) {
        super(metricRegistry, METRIC_DOMAIN);
        initializeMetrics();
    }

    private void initializeMetrics() {
        Stream.of(KpiMetric.values(), com.ericsson.oss.air.pm.stats.common.metrics.KpiMetric.values())
                .flatMap(Arrays::stream)
                .forEach(metric -> metric.register(this));
    }

    /**
     * Increments a metric counter with the value specified.
     *
     * @param metric The name of the counter to increment
     * @param value  The value to increment the counter by
     */
    public void incrementKpiMetric(final KpiMetric metric, final long value) {
        final Counter counter = getMetricRegistry().counter(metric.name());
        log.debug("Increment counter {} with value {}...", metric, value);
        counter.inc(value);
    }

    /**
     * Increments a metric counter by 1.
     *
     * @param metric The name of the counter to increment
     */
    public void incrementKpiMetric(final KpiMetric metric) {
        incrementKpiMetric(metric, 1);
    }

    /**
     * Decrements a metric counter with the value specified.
     *
     * @param metric The name of the counter to decrement
     * @param value  The value to decrement the counter by
     */
    public void decrementKpiMetric(final KpiMetric metric, final long value) {
        final Counter counter = getMetricRegistry().counter(metric.name());
        log.debug("Decrement counter {} with value {}...", metric, value);
        counter.dec(value);
    }

    /**
     * Decrements a metric counter by 1.
     *
     * @param metric The name of the counter to increment
     */
    public void decrementKpiMetric(final KpiMetric metric) {
        decrementKpiMetric(metric, 1);
    }
}