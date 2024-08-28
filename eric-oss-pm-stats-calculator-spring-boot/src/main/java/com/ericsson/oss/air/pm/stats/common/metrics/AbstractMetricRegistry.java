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

import java.util.function.Supplier;

import com.codahale.metrics.Counter;
import com.codahale.metrics.DefaultSettableGauge;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import lombok.Getter;

/**
 * Shared metric registry code.
 */
@Getter
public abstract class AbstractMetricRegistry implements com.ericsson.oss.air.pm.stats.common.metrics.MetricRegistry {

    // TODO: the underlying metricRegistry shouldn't be exposed via a getter, needs bigger refactoring.
    private final MetricRegistry metricRegistry;
    private final JmxReporter jmxReporter;

    protected AbstractMetricRegistry(final MetricRegistry metricRegistry, final String metricDomain) {
        this.metricRegistry = metricRegistry;
        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(metricDomain).build();
    }

    /**
     * Get a metric with type {@link Meter} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metricName metric name
     * @return meter object
     */
    @Override
    public Meter meter(final String metricName) {
        return metricRegistry.meter(metricName);
    }

    /**
     * Get a metric with type {@link Histogram} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metricName metric name
     * @return histogram object
     * */
    @Override
    public Histogram histogram(final String metricName) {
        return metricRegistry.histogram(metricName);
    }

    /**
     * Get a metric with type {@link Timer} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param name metric name
     * @return timer object
     */
    @Override
    public Timer timer(final String name) {
        return metricRegistry.timer(name);
    }

    /**
     * Get a metric with type {@link Timer} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric
     * @param event supplier of a function to be measured
     * @return timer object
     */
    @Override
    public <T> T timer(final String metric, final Supplier<T> event) {
        return metricRegistry.timer(metric).timeSupplier(event);
    }

    /**
     * Get a metric with type {@link Counter} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return counter object
     */
    @Override
    public Counter counter(final String metric) {
        return metricRegistry.counter(metric);
    }

    /**
     * Get a metric with type {@link DefaultSettableGauge} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return defaultSettableGauge object
     */
    @Override
    public <T> DefaultSettableGauge<T> defaultSettableGauge(final String metric) {
        return metricRegistry.gauge(metric, DefaultSettableGauge::new);
    }

    /**
     * Get a metric with type {@link Gauge} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return gauge object
     */
    @Override
    public <T> Gauge<T> gauge(final String metric) {
        return metricRegistry.gauge(metric);
    }
}