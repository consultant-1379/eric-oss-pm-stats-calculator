/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;


/**
 * Class used to report metrics to a JMX Reporter.
 */
public final class HttpPoolMetricReporter {

    private static final MetricRegistry REGISTRY = new MetricRegistry();
    private static final HttpPoolMetricReporter REPORTER = new HttpPoolMetricReporter();
    private static final String DOT = ".";

    static {
        JmxReporter.forRegistry(REGISTRY).build().start();
        REGISTRY.counter(getFullName(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT));
        REGISTRY.counter(getFullName(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT));
        REGISTRY.counter(getFullName(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT));
        REGISTRY.counter(getFullName(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT));
    }

    private HttpPoolMetricReporter() {
    }

    /**
     * Retrieves a @{@link HttpPoolMetricReporter} instance.
     *
     * @return a @{@link HttpPoolMetricReporter} instance
     */
    public static HttpPoolMetricReporter getInstance() {
        return REPORTER;
    }

    /**
     * Increments value of a metric.
     *
     * @param type
     *            the http pool type of the metric to be incremented
     * @param httpPoolMetricNames
     *            the metric name to be incremented
     * @param value
     *            the value to increment by
     */
    public void inc(final HttpPoolType type, final HttpPoolMetrics httpPoolMetricNames, final long value) {
        REGISTRY.counter(getFullName(type, httpPoolMetricNames)).inc(value);
    }

    /**
     * Decrements value of a metric.
     *
     * @param type
     *            the http pool type of the metric to be decremented
     * @param httpPoolMetricNames
     *            the metric name to be decremented
     * @param value
     *            the value to decrement by
     */
    public void dec(final HttpPoolType type, final HttpPoolMetrics httpPoolMetricNames, final long value) {
        REGISTRY.counter(getFullName(type, httpPoolMetricNames)).dec(value);
    }

    /**
     * Retrieves the count of a given metric in the {@link MetricRegistry}.
     *
     * @param type
     *            the http pool type of the metric
     * @param httpPoolMetricNames
     *            the name of the metric
     * @return a {@link long}, the count of the specified metric
     */
    public long getMetricCount(final HttpPoolType type, final HttpPoolMetrics httpPoolMetricNames) {
        return REGISTRY.counter(getFullName(type, httpPoolMetricNames)).getCount();
    }

    private static String getFullName(final HttpPoolType type, final HttpPoolMetrics metric) {
        return HttpPoolMetrics.class.getSimpleName() + DOT + type + DOT + metric;
    }
}