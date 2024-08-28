/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.facade.ReadinessLogFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.CalculationService;
import com.ericsson.oss.air.pm.stats.calculator.service.SparkServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.logging.mdc.MDCRegistrarImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.reliability.OnDemandReliabilityRegister;
import com.ericsson.oss.air.pm.stats.calculator.util.MessageChecker;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import io.prometheus.client.Summary.Builder;
import io.prometheus.client.Summary.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorSparkHandlerTest {
    final UUID dummyUUId = UUID.fromString("c94cef30-4e7e-4004-a59e-ec0f4dcf5a63");

    @Mock KpiCalculatorSpark kpiCalculatorSparkMock;
    @Mock ReadinessLogFacadeImpl readinessLogFacadeMock;
    @Mock SparkServiceImpl sparkServiceMock;
    @Mock CalculationService calculationServiceMock;
    @Mock MDCRegistrarImpl mdcRegistrarMock;
    @Mock OnDemandReliabilityRegister onDemandReliabilityRegisterMock;
    @Mock MessageChecker messageCheckerMock;

    @InjectMocks KpiCalculatorSparkHandler objectUnderTest;

    @Test
    void shouldReturn_whenProvidedKpiDefinitionsAreEmpty() {
        when(sparkServiceMock.getKpisToCalculate()).thenReturn(Collections.emptyList());

        objectUnderTest.startCalculation();

        verify(calculationServiceMock).updateCurrentCalculationState(KpiCalculationState.IN_PROGRESS);
        verify(sparkServiceMock).getKpisToCalculate();
        verify(calculationServiceMock).updateCurrentCalculationStateAndTime(eq(KpiCalculationState.NOTHING_CALCULATED), any(LocalDateTime.class));

        verifyNoMoreInteractions(calculationServiceMock);
        verifyNoMoreInteractions(kpiCalculatorSparkMock);
        verifyNoMoreInteractions(sparkServiceMock);
    }

    @Test
    void shouldNotCalculateSimple() {
        when(sparkServiceMock.getKpisToCalculate()).thenReturn(List.of("simpleKpi"));
        when(sparkServiceMock.isScheduledSimple()).thenReturn(true);
        when(messageCheckerMock.hasNewMessage()).thenReturn(false);

        objectUnderTest.startCalculation();

        verify(calculationServiceMock).updateCurrentCalculationState(KpiCalculationState.IN_PROGRESS);
        verify(sparkServiceMock).getKpisToCalculate();
        verify(sparkServiceMock).isScheduledSimple();
        verify(messageCheckerMock).hasNewMessage();
        verify(calculationServiceMock).updateCurrentCalculationStateAndTime(eq(KpiCalculationState.NOTHING_CALCULATED), any(LocalDateTime.class));
    }

    @Test
    void shouldCalculateSimple(@Mock final Builder builderMock,
                               @Mock final Summary summaryMock,
                               @Mock final Timer timerMock) {
        try (final MockedStatic<Summary> summaryMockedStatic = mockStatic(Summary.class)) {
            final List<String> kpiDefinition = Collections.singletonList("kpiDefinition1");

            when(sparkServiceMock.isScheduledSimple()).thenReturn(true);
            when(messageCheckerMock.hasNewMessage()).thenReturn(true);

            when(sparkServiceMock.getCalculationId()).thenReturn(dummyUUId);
            when(sparkServiceMock.getKpisToCalculate()).thenReturn(kpiDefinition);
            when(sparkServiceMock.getExecutionGroup()).thenReturn("executionGroup");

            summaryMockedStatic.when(Summary::build).thenReturn(builderMock);

            when(builderMock.name("kpi_calculation_time")).thenReturn(builderMock);
            when(builderMock.help("Time to complete executionGroup KPI calculation")).thenReturn(builderMock);
            when(builderMock.register(any(CollectorRegistry.class))).thenReturn(summaryMock);

            when(summaryMock.startTimer()).thenReturn(timerMock);

            when(sparkServiceMock.getApplicationId()).thenReturn("applicationId");
            when(readinessLogFacadeMock.persistReadinessLogs()).thenReturn(Collections.emptyList());

            objectUnderTest.startCalculation();

            verify(sparkServiceMock, times(3)).getCalculationId();
            verify(calculationServiceMock).updateCurrentCalculationState(KpiCalculationState.IN_PROGRESS);
            verify(sparkServiceMock).getKpisToCalculate();
            verify(sparkServiceMock, times(2)).isScheduledSimple();
            verify(messageCheckerMock).hasNewMessage();
            verify(sparkServiceMock, times(3)).getExecutionGroup();

            summaryMockedStatic.verify(Summary::build);

            verify(builderMock).name("kpi_calculation_time");
            verify(builderMock).help("Time to complete executionGroup KPI calculation");
            verify(builderMock).register(any(CollectorRegistry.class));

            verify(summaryMock).startTimer();

            verify(sparkServiceMock).getApplicationId();
            verify(mdcRegistrarMock).registerLoggingKeys();
            verify(kpiCalculatorSparkMock).calculate();
            verify(readinessLogFacadeMock).persistReadinessLogs();
            verify(calculationServiceMock).updateCurrentCalculationStateAndTime(eq(KpiCalculationState.NOTHING_CALCULATED), any(LocalDateTime.class));

            verify(timerMock).observeDuration();

            verifyNoInteractions(onDemandReliabilityRegisterMock);
        }
    }

    @Test
    void shouldCalculateOnDemand(@Mock final Builder builderMock,
                                 @Mock final Summary summaryMock,
                                 @Mock final Timer timerMock) {
        try (final MockedStatic<Summary> summaryMockedStatic = mockStatic(Summary.class)) {
            final List<String> kpiDefinition = Collections.singletonList("kpiDefinition1");

            when(sparkServiceMock.getCalculationId()).thenReturn(dummyUUId);
            when(sparkServiceMock.getKpisToCalculate()).thenReturn(kpiDefinition);
            when(sparkServiceMock.getExecutionGroup()).thenReturn("executionGroup");

            summaryMockedStatic.when(Summary::build).thenReturn(builderMock);

            when(builderMock.name("kpi_calculation_time")).thenReturn(builderMock);
            when(builderMock.help("Time to complete executionGroup KPI calculation")).thenReturn(builderMock);
            when(builderMock.register(any(CollectorRegistry.class))).thenReturn(summaryMock);

            when(summaryMock.startTimer()).thenReturn(timerMock);

            when(sparkServiceMock.getApplicationId()).thenReturn("applicationId");
            when(sparkServiceMock.isOnDemand()).thenReturn(true);
            objectUnderTest.startCalculation();

            verify(sparkServiceMock, times(3)).getCalculationId();
            verify(calculationServiceMock).updateCurrentCalculationState(KpiCalculationState.IN_PROGRESS);
            verify(sparkServiceMock).getKpisToCalculate();
            verify(sparkServiceMock, times(3)).getExecutionGroup();

            summaryMockedStatic.verify(Summary::build);

            verify(builderMock).name("kpi_calculation_time");
            verify(builderMock).help("Time to complete executionGroup KPI calculation");
            verify(builderMock).register(any(CollectorRegistry.class));

            verify(summaryMock).startTimer();

            verify(sparkServiceMock).getApplicationId();
            verify(kpiCalculatorSparkMock).calculate();
            verify(mdcRegistrarMock).registerLoggingKeys();
            verify(sparkServiceMock, times(2)).isScheduledSimple();
            verify(sparkServiceMock).isOnDemand();
            verify(onDemandReliabilityRegisterMock).persistOnDemandReliabilities();

            verify(timerMock).observeDuration();

            verifyNoInteractions(readinessLogFacadeMock);
            verifyNoMoreInteractions(calculationServiceMock);
        }
    }
}