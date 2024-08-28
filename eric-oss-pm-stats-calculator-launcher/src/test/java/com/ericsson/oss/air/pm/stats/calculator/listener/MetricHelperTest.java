/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.metrics.CustomMetric;

import org.apache.spark.SparkContext;
import org.apache.spark.SparkEnv;
import org.apache.spark.metrics.MetricsSystem;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricHelperTest {
    @Mock SparkSession sparkSessionMock;
    @Mock SparkSession.Builder builderMock;
    @Mock SparkContext sparkContextMock;
    @Mock SparkEnv sparkEnvMock ;
    @Mock MetricsSystem metricsSystemMock ;

    final String FREQUENCY_STRING = "seconds_0__minutes_0_5_10_15_20_25_30_35_40_45_50_55";
    final double EXECUTION_TIME = 1000;
    final String DATA_SOURCE_TABLE = "kpi_cell_guid_simple_1440";
    final String METRIC_NAME = "CALCULATE";

    @Test
    void whenSetGaugeIsCalled_thenGaugeValueIsSetInTheSelectedMetricRegistry(){
        final String sourceName = "source_2";
        final CustomMetric customMetric = new CustomMetric(sourceName);

        assertEquals(0, customMetric.metricRegistry().getGauges().size());

        final long gaugeValue = 0L;
        final String gaugeName = "gauge_1";
        MetricHelper.setGauge(customMetric, gaugeName, gaugeValue);

        assertEquals(1, customMetric.metricRegistry().getGauges().size());
        assertEquals(gaugeValue,customMetric.metricRegistry().getGauges().get(gaugeName).getValue());
    }

    @Test
    void whenJobDescriptionHasOnlyThreeValues_thenOnlyOneNewMetricIsCreated(){
        try(MockedStatic<SparkSession> sparkSessionMockedStatic = Mockito.mockStatic(SparkSession.class)){
            final String jobDescription = METRIC_NAME + ":" + DATA_SOURCE_TABLE + ":" + "1440";
            List<CustomMetric> customMetrics = new LinkedList<>();

            sparkSessionMockedStatic.when(SparkSession::builder).thenReturn(builderMock);
            doReturn(sparkSessionMock).when(builderMock).getOrCreate();
            doReturn(sparkContextMock).when(sparkSessionMock).sparkContext();
            doReturn(sparkEnvMock).when(sparkContextMock).env();
            doReturn(metricsSystemMock).when(sparkEnvMock).metricsSystem();
            doAnswer(invocation -> {
                CustomMetric customMetric = invocation.getArgument(0);
                customMetrics.add(customMetric);
                return null;
            }).when(metricsSystemMock).registerSource(any(CustomMetric.class));

            MetricHelper.pushSparkJobExecutionTimeMetric(jobDescription, FREQUENCY_STRING, EXECUTION_TIME);

            assertEquals(1, customMetrics.size());
            assertEquals(DATA_SOURCE_TABLE, customMetrics.get(0).sourceName());
            assertEquals((long) EXECUTION_TIME, customMetrics.get(0).metricRegistry().getGauges().get(customMetrics.get(0).sourceName()).getValue());
        }
    }

    @Test
    void whenJobDescriptionHasFiveValues_thenThreeNewMetricsAreCreated(){
        try(MockedStatic<SparkSession> sparkSessionMockedStatic = Mockito.mockStatic(SparkSession.class)) {
            final long numberOfStages = 1;
            final long numberOfKpis = 2;
            final String jobDescription = METRIC_NAME + ":" + DATA_SOURCE_TABLE + ":" + "1440" + ":" + numberOfStages + ":" + numberOfKpis;
            List<CustomMetric> customMetrics = new LinkedList<>();

            sparkSessionMockedStatic.when(SparkSession::builder).thenReturn(builderMock);
            doReturn(sparkSessionMock).when(builderMock).getOrCreate();
            doReturn(sparkContextMock).when(sparkSessionMock).sparkContext();
            doReturn(sparkEnvMock).when(sparkContextMock).env();
            doReturn(metricsSystemMock).when(sparkEnvMock).metricsSystem();
            doAnswer(invocation -> {
                CustomMetric customMetric = invocation.getArgument(0);
                customMetrics.add(customMetric);
                return null;
            }).when(metricsSystemMock).registerSource(any(CustomMetric.class));

            MetricHelper.pushSparkJobExecutionTimeMetric(jobDescription, FREQUENCY_STRING, EXECUTION_TIME);

            assertEquals(3, customMetrics.size());
            List<String> customMetricSources = customMetrics.stream().map(CustomMetric::sourceName).collect(Collectors.toList());
            assertEquals(Arrays.asList(DATA_SOURCE_TABLE, METRIC_NAME, METRIC_NAME), customMetricSources);
            assertEquals((long) EXECUTION_TIME, customMetrics.get(0).metricRegistry().getGauges().get(customMetrics.get(0).sourceName()).getValue());
            assertEquals((long) EXECUTION_TIME / numberOfStages, customMetrics.get(1).metricRegistry().getGauges().get(METRIC_NAME+"_PER_STAGE").getValue());
            assertEquals((long) EXECUTION_TIME / numberOfKpis, customMetrics.get(2).metricRegistry().getGauges().get(METRIC_NAME+"_PER_KPI").getValue());
        }
    }
}
