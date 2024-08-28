/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.metric;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.metrics.SettableGauge;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

//  TODO: This is a temporal solution to enable characteristics measurements, revisit this later on
@Slf4j
public class SparkMeterService {

    @Getter
    private final MetricRegistry metricRegistry;

    /**
     * Constructor.
     */
    public SparkMeterService(final Long metricRetentionInSeconds) {
        metricRegistry = new MetricRegistry();

        // TODO: Quick fix to remove metrics based on retention period (15 mins by default), refactor when migrating to micrometer.
        // Constantly adding metrics cause memory leak and jmx exporter issues.
        new SparkMetricRetentionListener(metricRetentionInSeconds).registerAsListener(metricRegistry);

        JmxReporter.forRegistry(metricRegistry).inDomain(SparkMetricName.DOMAIN).build().start();

        log.info("Started '{}' JMX reporter", SparkMetricName.DOMAIN);
    }

    public long meterCalculationStart(final String executionGroup, @NonNull final UUID calculationId) {
        final long epochMillis = System.currentTimeMillis();

        final String metricName = SparkMetricName.createStartTimeName(executionGroup, calculationId);

        metricRegistry.gauge(metricName, SettableGauge::new).setValue(epochMillis);

        log.info("For metric '{}' time '{}' registered", metricName, toLocalDateTime(epochMillis));

        return epochMillis;
    }

    public long meterCalculationEnd(
            final String executionGroup, @NonNull final UUID calculationId, @NonNull final KpiCalculationState kpiCalculationState
    ) {
        final long epochMillis = System.currentTimeMillis();

        final String metricName = SparkMetricName.createEndTimeName(executionGroup, calculationId, kpiCalculationState);

        metricRegistry.gauge(metricName, SettableGauge::new).setValue(epochMillis);

        log.info("For metric '{}' time '{}' registered", metricName, toLocalDateTime(epochMillis));

        return epochMillis;
    }

    public void meterCalculationDuration(
            final long startTimeMillis, final String executionGroup, final UUID calculationId, final KpiCalculationState kpiCalculationState
    ) {
        final long endTimeMillis = meterCalculationEnd(executionGroup, calculationId, kpiCalculationState);

        final Duration calculationDuration = Duration.ofMillis(endTimeMillis - startTimeMillis);
        final String metricName = SparkMetricName.createDurationName(executionGroup, calculationId, kpiCalculationState);

        metricRegistry.gauge(metricName, SettableGauge::new).setValue(calculationDuration.toMillis());

        log.info("For metric '{}' duration value '{}' registered", metricName, formatDurationHMS(calculationDuration.toMillis()));
    }

    private static LocalDateTime toLocalDateTime(final long timeValue) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeValue), ZoneId.of("UTC"));
    }
}
