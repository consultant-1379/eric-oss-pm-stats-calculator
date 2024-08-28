/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.listener;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.metrics.CustomMetric;
import com.ericsson.oss.air.pm.stats.calculator.metrics.CustomMetricSupplier;
import com.ericsson.oss.air.pm.stats.calculator.metrics.SettableGauge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;


/**
 * Utility class used to push metrics to prometheus.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MetricHelper {
    private static final String PER_STAGE = "_PER_STAGE";
    private static final String PER_KPI = "_PER_KPI";

    /**
     * Gets JDBC properties for a named database.
     *
     * @param jobDescription
     *            the description of the spark job which the metric measures
     * @param executionGroup
     *            the execution group of the current calculation
     * @param executionTimeInMillis
     *            the execution time of all spark jobs with the job description in milliseconds
     */
    //TODO check possible null values and update Javadoc
    static void pushSparkJobExecutionTimeMetric(final String jobDescription, final String executionGroup, final double executionTimeInMillis) {
        log.info("Pushing metric of description: {} and executionGroup: {}", jobDescription, executionGroup);

        final String[] metricInformation = jobDescription.split(":");
        final String metricName = metricInformation[0].trim();
        final String table = metricInformation[1].trim();

        //TODO why we are filling this Map?
        final Map<String, String> groupingKeys = new HashMap<>();
        groupingKeys.put("executionGroup", executionGroup);
        groupingKeys.put("table", table);

        final SparkSession sparkSession = SparkSession.builder().getOrCreate();

        final CustomMetric customMetric = new CustomMetric(metricInformation[1]);
        sparkSession.sparkContext().env().metricsSystem().registerSource(customMetric);
        setGauge(customMetric, metricInformation[1], (long) executionTimeInMillis);

        if (metricInformation.length > 3) {
            final String metric = metricName + PER_STAGE;
            final int numberOfStages = Integer.parseInt(metricInformation[3].trim());
            groupingKeys.put("stages", metricInformation[3].trim());

            final CustomMetric metricPerStage = new CustomMetric(metricName);
            sparkSession.sparkContext().env().metricsSystem().registerSource(metricPerStage);
            setGauge(metricPerStage, metric, (long) (executionTimeInMillis / numberOfStages));

            groupingKeys.remove("stages");
        }

        if (metricInformation.length > 4) {
            final String metric = metricName + PER_KPI;

            final int numberOfKpis = Integer.parseInt(metricInformation[4].trim());

            final CustomMetric metricPerKPI = new CustomMetric(metricName);
            sparkSession.sparkContext().env().metricsSystem().registerSource(metricPerKPI);
            setGauge(metricPerKPI, metric, (long) (executionTimeInMillis / numberOfKpis));

            groupingKeys.remove("kpis");
            groupingKeys.put("kpis", metricInformation[4].trim());
        }
    }

    public static SettableGauge<Long> setGauge(final CustomMetric customMetric, final String gaugeName, final long value) {
        @SuppressWarnings("unchecked")
        SettableGauge<Long> gauge = (SettableGauge<Long>) customMetric.metricRegistry().gauge(gaugeName, new CustomMetricSupplier());
        gauge.setValue(value);
        return gauge;
    }

}
