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
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;

/**
 * Manages metrics registry
 */
public interface MetricRegistry {

    /**
     * Get a metric with type {@link Meter} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metricName metric name
     * @return meter object
     */
    Meter meter(String metricName);

    /**
     * Get a metric with type {@link Histogram} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metricName metric name
     * @return histogram object
     * */
    Histogram histogram(String metricName);

    /**
     * Get a metric with type {@link Timer} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param name metric name
     * @return timer object
     */
    Timer timer(String name);

    /**
     * Get a metric with type {@link Timer} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric
     * @param event supplier of a function to be measured
     * @return timer object
     */
    <T> T timer(String metric, Supplier<T> event);

    /**
     * Get a metric with type {@link Counter} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return counter object
     */
    Counter counter(String metric);

    /**
     * Get a metric with type {@link DefaultSettableGauge} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return defaultSettableGauge object
     */
    <T> DefaultSettableGauge<T> defaultSettableGauge(String metric);

    /**
     * Get a metric with type {@link Gauge} from the registry by its name.
     * If the metric does not exist, creates it.
     *
     * @param metric metric name
     * @return gauge object
     */
    <T> Gauge<T> gauge(String metric);

    /**
     * Get the metric registry.
     *
     * @return metric registry
     */
    com.codahale.metrics.MetricRegistry getMetricRegistry();

    /**
     * Get the JMX reporter for this metric registry.
     *
     * @return JMX reporter
     */
    JmxReporter getJmxReporter();
}