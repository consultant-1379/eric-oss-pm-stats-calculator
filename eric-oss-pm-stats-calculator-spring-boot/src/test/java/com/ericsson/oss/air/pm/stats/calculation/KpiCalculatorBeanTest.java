/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FAILED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINALIZING;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.IN_PROGRESS;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.NOTHING_CALCULATED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.ON_DEMAND;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_COMPLEX;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob.KpiCalculationJobBuilder;
import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculationMediator;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.facade.ComplexReliabilityThresholdFacade;
import com.ericsson.oss.air.pm.stats.service.facade.ReadinessLogManagerFacade;
import com.ericsson.oss.air.pm.stats.service.helper.CalculationLauncher;
import com.ericsson.oss.air.pm.stats.service.metric.SparkMeterService;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorBeanTest {
    private static final Timestamp TEST_TIME = Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 5), LocalTime.NOON));
    private static final UUID CALCULATION_ID = UUID.fromString("f5ec4601-264c-42f3-a753-a4861304e2ce");
    private static final String ERROR_MESSAGE = "error message";

    @Mock
    ComplexReliabilityThresholdFacade complexReliabilityThresholdFacadeMock;
    @Mock
    ReadinessLogManagerFacade readinessLogManagerFacadeMock;
    @Mock
    CalculationLauncher calculationLauncherMock;
    @Mock
    CalculationService calculationServiceMock;
    @Mock
    KpiCalculationMediator kpiCalculationMediatorMock;
    @Mock
    ExecutionReportEventPublisher executionReportEventPublisherMock;
    @Mock
    TabularParameterFacade tabularParameterFacadeMock;
    @Mock
    SparkMeterService sparkMeterServiceMock;

    @InjectMocks
    KpiCalculatorBean objectUnderTest;

    @Test
    @SneakyThrows
    void shouldCalculateKpis_WithOnDemandJobType() {
        final KpiCalculationJob kpiCalculationJob = job(ON_DEMAND);
        objectUnderTest.calculateKpis(kpiCalculationJob);

        verify(sparkMeterServiceMock).meterCalculationStart("execution_group", CALCULATION_ID);
        verify(calculationLauncherMock).launchCalculation(kpiCalculationJob);
        verify(readinessLogManagerFacadeMock).persistComplexReadinessLog(kpiCalculationJob);
        verify(kpiCalculationMediatorMock).removeRunningCalculation(CALCULATION_ID);
        verify(calculationServiceMock).updateCompletionState(CALCULATION_ID, FINALIZING);
        verify(sparkMeterServiceMock).meterCalculationDuration(anyLong(), eq("execution_group"), eq(CALCULATION_ID), eq(FINALIZING));
        verify(executionReportEventPublisherMock).pushEvent(CALCULATION_ID);
        verify(tabularParameterFacadeMock).deleteTables(kpiCalculationJob);
        verifyNoInteractions(complexReliabilityThresholdFacadeMock);
    }

    @Test
    @SneakyThrows
    void shouldCalculateKpis_WithSimpleJobType_whenCalculated() {
        final KpiCalculationJob kpiCalculationJob = job(KpiType.SCHEDULED_SIMPLE);
        when(calculationServiceMock.forceFindByCalculationId(CALCULATION_ID)).thenReturn(IN_PROGRESS);

        objectUnderTest.calculateKpis(kpiCalculationJob);

        verify(sparkMeterServiceMock).meterCalculationStart("execution_group", CALCULATION_ID);
        verify(calculationLauncherMock).launchCalculation(kpiCalculationJob);
        verify(calculationServiceMock).forceFindByCalculationId(CALCULATION_ID);
        verify(readinessLogManagerFacadeMock).persistComplexReadinessLog(kpiCalculationJob);
        verify(kpiCalculationMediatorMock).removeRunningCalculation(CALCULATION_ID);
        verify(calculationServiceMock).updateCompletionState(CALCULATION_ID, FINALIZING);
        verify(sparkMeterServiceMock).meterCalculationDuration(anyLong(), eq("execution_group"), eq(CALCULATION_ID), eq(FINALIZING));
        verify(executionReportEventPublisherMock).pushEvent(CALCULATION_ID);
        verify(tabularParameterFacadeMock).deleteTables(kpiCalculationJob);
        verifyNoInteractions(complexReliabilityThresholdFacadeMock);
    }

    @Test
    @SneakyThrows
    void shouldCalculateKpis_WithSimpleJobType_whenNothingCalculated() {
        final KpiCalculationJob kpiCalculationJob = job(KpiType.SCHEDULED_SIMPLE);
        when(calculationServiceMock.forceFindByCalculationId(CALCULATION_ID)).thenReturn(NOTHING_CALCULATED);

        objectUnderTest.calculateKpis(kpiCalculationJob);

        verify(sparkMeterServiceMock).meterCalculationStart("execution_group", CALCULATION_ID);
        verify(calculationLauncherMock).launchCalculation(kpiCalculationJob);
        verify(kpiCalculationMediatorMock).removeRunningCalculation(CALCULATION_ID);
        verify(sparkMeterServiceMock).meterCalculationDuration(anyLong(), eq("execution_group"), eq(CALCULATION_ID), eq(NOTHING_CALCULATED));
        verifyNoInteractions(readinessLogManagerFacadeMock, complexReliabilityThresholdFacadeMock, executionReportEventPublisherMock,
                tabularParameterFacadeMock);
        verifyNoMoreInteractions(calculationServiceMock);
    }

    @Test
    @SneakyThrows
    void shouldCalculateKpis_WithComplexJobType_whenCalculated() {
        final KpiCalculationJob kpiCalculationJob = job(SCHEDULED_COMPLEX);

        objectUnderTest.calculateKpis(kpiCalculationJob);

        verify(sparkMeterServiceMock).meterCalculationStart("execution_group", CALCULATION_ID);
        verify(calculationLauncherMock).launchCalculation(kpiCalculationJob);
        verify(complexReliabilityThresholdFacadeMock).persistComplexReliabilityThreshold(kpiCalculationJob);
        verify(readinessLogManagerFacadeMock).persistComplexReadinessLog(kpiCalculationJob);
        verify(kpiCalculationMediatorMock).removeRunningCalculation(CALCULATION_ID);
        verify(calculationServiceMock).updateCompletionState(CALCULATION_ID, FINALIZING);
        verify(sparkMeterServiceMock).meterCalculationDuration(anyLong(), eq("execution_group"), eq(CALCULATION_ID), eq(FINALIZING));
        verify(executionReportEventPublisherMock).pushEvent(CALCULATION_ID);
        verify(tabularParameterFacadeMock).deleteTables(kpiCalculationJob);
    }

    @Test
    @SneakyThrows
    void shouldCalculateKpis_WithComplexJobType_whenNothingCalculated() {
        final KpiCalculationJob kpiCalculationJob = job(SCHEDULED_COMPLEX);
        when(calculationServiceMock.forceFindByCalculationId(kpiCalculationJob.getCalculationId())).thenReturn(NOTHING_CALCULATED);

        objectUnderTest.calculateKpis(kpiCalculationJob);

        verify(sparkMeterServiceMock).meterCalculationStart("execution_group", CALCULATION_ID);
        verify(calculationLauncherMock).launchCalculation(kpiCalculationJob);
        verify(complexReliabilityThresholdFacadeMock).persistComplexReliabilityThreshold(kpiCalculationJob);
        verify(calculationServiceMock).forceFindByCalculationId(kpiCalculationJob.getCalculationId());
        verify(sparkMeterServiceMock).meterCalculationDuration(anyLong(), eq("execution_group"), eq(CALCULATION_ID), eq(NOTHING_CALCULATED));
        verifyNoInteractions(readinessLogManagerFacadeMock, executionReportEventPublisherMock, tabularParameterFacadeMock);
        verifyNoMoreInteractions(calculationServiceMock);
    }

    @Test
    @SneakyThrows
    void shouldThrowKpiCalculatorException_whenCalculationCannotBeLaunched_onCalculateKpis() {
        final KpiCalculationJob kpiCalculationJob = job(KpiType.SCHEDULED_SIMPLE);

        doThrow(new RuntimeException(ERROR_MESSAGE)).when(calculationLauncherMock).launchCalculation(kpiCalculationJob);

        Assertions.assertThatThrownBy(() -> objectUnderTest.calculateKpis(kpiCalculationJob))
                .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                .isInstanceOf(KpiCalculatorException.class)
                .hasMessage("java.lang.RuntimeException: error message");

        verify(sparkMeterServiceMock).meterCalculationStart("execution_group", CALCULATION_ID);
        verify(calculationServiceMock).updateCompletionState(CALCULATION_ID, FAILED);
        verify(sparkMeterServiceMock).meterCalculationDuration(anyLong(), eq("execution_group"), eq(CALCULATION_ID), eq(FAILED));
        verify(kpiCalculationMediatorMock).removeRunningCalculation(CALCULATION_ID);
        verify(tabularParameterFacadeMock).deleteTables(kpiCalculationJob);

        verifyNoInteractions(executionReportEventPublisherMock);
    }

    static KpiCalculationJob job(final KpiType kpiType) {
        final KpiCalculationJobBuilder builder = KpiCalculationJob.builder();
        builder.withCalculationId(CALCULATION_ID);
        builder.withTimeCreated(TEST_TIME);
        builder.withJobType(kpiType);
        builder.withExecutionGroup("execution_group");
        return builder.build();
    }
}