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

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;

import com.codahale.metrics.Counter;
import com.codahale.metrics.DefaultSettableGauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Manages metrics registry for API.
 */
@ApplicationScoped
public class ApiMetricRegistry extends AbstractMetricRegistry {

    public static final String METRIC_DOMAIN = "pm_stats_calculator_api";

    // @TODO: Remove when DI injection is implemented for the 3PP classes.
    public ApiMetricRegistry() {
        this(new MetricRegistry());
    }

    public ApiMetricRegistry(final MetricRegistry metricRegistry) {
        super(metricRegistry, METRIC_DOMAIN);

        initializeMetrics();
    }

    private void initializeMetrics() {
        Stream.of(ApiCounter.values(), ApiGauge.values(), ApiTimer.values()).flatMap(Arrays::stream).forEach(
                metric -> metric.register(this)
        );
    }

    /**
     * Get a metric with type {@link Timer} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric
     * @param event  supplier of a function to be measured
     * @return timer object
     */
    public <T> T timer(final ApiTimer metric, final Supplier<T> event) {
        return getMetricRegistry().timer(metric.getName()).timeSupplier(event);
    }

    /**
     * Get a metric with type {@link Counter} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return counter object
     */
    public Counter counter(final ApiCounter metric) {
        return getMetricRegistry().counter(metric.getName());
    }

    /**
     * Get a metric with type {@link Meter} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return meter object
     */
    public DefaultSettableGauge defaultSettableGauge(final ApiGauge metric) {
        return getMetricRegistry().gauge(metric.getName(), DefaultSettableGauge::new);
    }
}