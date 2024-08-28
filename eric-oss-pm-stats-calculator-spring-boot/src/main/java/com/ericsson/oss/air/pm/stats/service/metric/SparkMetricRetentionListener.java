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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import lombok.Getter;

// TODO Remove when migrating over micrometer.
public class SparkMetricRetentionListener implements MetricRegistryListener {

    @Getter
    private MetricRegistry metricRegistry;

    // Time value after which metrics for ended calculations should be removed.
    @Getter
    private final Long expirationTimeInSeconds;

    private final Map<String, Long> metricTimeMap = new ConcurrentHashMap<>();

    private final String nameRegex = String.format(".*%s$|.*%s$|.*%s$", SparkMetricName.START_TIME, SparkMetricName.END_TIME, SparkMetricName.DURATION);

    public SparkMetricRetentionListener(final Long expirationTimeInSeconds) {
        this.expirationTimeInSeconds = expirationTimeInSeconds;
    }

    public void registerAsListener(final MetricRegistry metricRegistry) {
        metricRegistry.addListener(this);
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void onGaugeAdded(final String name, final Gauge<?> gauge) {
        addMetric(name);
    }

    @Override
    public void onGaugeRemoved(final String name) {
        // Not action is needed on remove.
    }

    @Override
    public void onCounterAdded(final String name, final Counter counter) {
        addMetric(name);
    }

    @Override
    public void onCounterRemoved(final String name) {
        // Not action is needed on remove.
    }

    @Override
    public void onHistogramAdded(final String name, final Histogram histogram) {
        addMetric(name);
    }

    @Override
    public void onHistogramRemoved(final String name) {
        // Not action is needed on remove.
    }

    @Override
    public void onMeterAdded(final String name, final Meter meter) {
        addMetric(name);
    }

    @Override
    public void onMeterRemoved(final String name) {
        // Not action is needed on remove.
    }

    @Override
    public void onTimerAdded(final String name, final Timer timer) {
        addMetric(name);
    }

    @Override
    public void onTimerRemoved(final String name) {
        // Not action is needed on remove.
    }

    private void addMetric(final String name) {
        if (name.matches(nameRegex)) {
            metricTimeMap.putIfAbsent(name, System.currentTimeMillis());
            purgeMetrics();
        }
    }

    /**
     * Purge all metrics from the registry that reached timeout threshold.
     */
    private void purgeMetrics() {
        for (final Map.Entry<String, Long> metricTime : metricTimeMap.entrySet()) {
            if (System.currentTimeMillis() - metricTime.getValue() >= expirationTimeInSeconds * 1000) {
                metricTimeMap.remove(metricTime.getKey());
                if (metricRegistry.getNames().contains(metricTime.getKey())) {
                    metricRegistry.remove(metricTime.getKey());
                }
            }
        }
    }
}
