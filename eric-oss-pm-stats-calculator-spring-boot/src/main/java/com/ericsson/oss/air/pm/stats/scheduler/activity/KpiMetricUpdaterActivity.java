/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.activity;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.common.metrics.KpiMetric;
import com.ericsson.oss.air.pm.stats.common.scheduler.Activity;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.repository.api.MetricRepository;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

/**
 * Implementation of abstract class {@link Activity}, used with {@link ActivityScheduler} to create the scheduling of the PM Stats Calculator metric updater job.
 * <p>
 * When this activity runs it will update various kpi calculation metrics.
 * </p>
 */
@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor
public class KpiMetricUpdaterActivity extends Activity {
    private static final String ACTIVITY_NAME_CONTEXT_KEY = "activityName";
    private static final String UPDATE_PERIOD_CONTEXT_KEY = "updatePeriod";
    private static final String ACTIVITY_NAME = "Activity_kpiMetricUpdater";
    private static final String PERSISTED_CALCULATIONS = "persisted_calculations";

    @Inject
    private CalculationRepository calculationRepository;
    @Inject
    private KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry;
    @Inject
    private MetricRepository metricRepository;

    /**
     * Constructor with the params.
     *
     * @param name {@link String} unique identifier of the activity.
     * @param map  {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    private KpiMetricUpdaterActivity(final String name, final Map<String, Object> map) {
        super(name, map);
    }

    /**
     * Factory method.
     *
     * @param updatePeriod {@link Integer} of the metric update period.
     * @return an instance
     */
    public static KpiMetricUpdaterActivity of(final String updatePeriod) {
        final Map<String, Object> paramsMap = new HashMap<>(3);
        paramsMap.put(ACTIVITY_NAME_CONTEXT_KEY, ACTIVITY_NAME);
        paramsMap.put(UPDATE_PERIOD_CONTEXT_KEY, updatePeriod);

        return new KpiMetricUpdaterActivity(ACTIVITY_NAME, paramsMap);
    }

    @Override
    public void run(final JobDataMap activityContext) {
        updateKpiCalculationStateMetrics();
        updateKpiCalculatedResultsMetric();
    }

    private void updateKpiCalculationStateMetrics() {
        final Map<KpiCalculationState, Long> countByState = calculationRepository.countByStates();

        for (final KpiCalculationState state : KpiCalculationState.values()) {
            final Long count = countByState.getOrDefault(state, 0L);

            final String metricName = String.format("%s_%s", PERSISTED_CALCULATIONS, state);
            kpiSchedulerMetricRegistry.defaultSettableGauge(metricName).setValue(count);

            // TODO Obsolete metric for backwards compatibility, need to retire at some point.
            if (state == KpiCalculationState.IN_PROGRESS) {
                kpiSchedulerMetricRegistry.defaultSettableGauge(
                        com.ericsson.oss.air.pm.stats.service.metric.KpiMetric.ONGOING_CALCULATIONS.toString()
                ).setValue(count);
            }

            logUpdatedMetricValue(metricName, count);
        }
    }

    private void updateKpiCalculatedResultsMetric() {
        metricRepository.findByName(KpiMetric.PERSISTED_CALCULATION_RESULTS.name()).ifPresentOrElse(
                metric -> {
                    kpiSchedulerMetricRegistry.defaultSettableGauge(KpiMetric.PERSISTED_CALCULATION_RESULTS.name()).setValue(metric.getValue());
                    logUpdatedMetricValue(KpiMetric.PERSISTED_CALCULATION_RESULTS.name(), metric.getValue());
                }, () -> log.error("Metric '{}' not found", KpiMetric.PERSISTED_CALCULATION_RESULTS.name()));
    }

    private static void logUpdatedMetricValue(final String metricName, final Long count) {
        log.info("KPI metric '{}' updated to value '{}'", metricName, count);
    }
}
