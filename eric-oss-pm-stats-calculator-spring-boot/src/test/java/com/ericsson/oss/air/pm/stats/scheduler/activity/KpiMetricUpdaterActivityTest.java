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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.common.metrics.KpiMetric;
import com.ericsson.oss.air.pm.stats.model.entity.Metric;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.repository.api.MetricRepository;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;

@ExtendWith(MockitoExtension.class)
class KpiMetricUpdaterActivityTest {
    @Mock
    JobDataMap jobDataMapMock;
    @Mock
    CalculationRepository calculationRepositoryMock;
    @Mock
    MetricRepository metricRepositoryMock;

    @Spy
    KpiSchedulerMetricRegistry kpiSchedulerMetricRegistryMock = new KpiSchedulerMetricRegistry();

    @InjectMocks
    KpiMetricUpdaterActivity objectUnderTest;

    @Test
    void whenRun_shouldUpdateMetrics() {
        final Map<KpiCalculationState, Long> countByStateResult = Map.of(
                KpiCalculationState.STARTED, 1L,
                KpiCalculationState.IN_PROGRESS, 2L,
                KpiCalculationState.FINALIZING, 3L,
                KpiCalculationState.NOTHING_CALCULATED, 4L,
                KpiCalculationState.FINISHED, 5L,
                KpiCalculationState.FAILED, 6L,
                KpiCalculationState.LOST, 7L);

        when(calculationRepositoryMock.countByStates()).thenReturn(countByStateResult);
        when(metricRepositoryMock.findByName(KpiMetric.PERSISTED_CALCULATION_RESULTS.name())).thenReturn(
                Optional.of(Metric.builder().withName(KpiMetric.PERSISTED_CALCULATION_RESULTS.name()).withValue(6L).build()));

        objectUnderTest.run(jobDataMapMock);

        Stream.of(KpiCalculationState.values()).forEach(state -> {
            final String metricName = String.format("%s_%s", "persisted_calculations", state);
            verify(kpiSchedulerMetricRegistryMock).defaultSettableGauge(metricName);
        });
        verify(kpiSchedulerMetricRegistryMock).defaultSettableGauge(KpiMetric.PERSISTED_CALCULATION_RESULTS.name());
    }
}