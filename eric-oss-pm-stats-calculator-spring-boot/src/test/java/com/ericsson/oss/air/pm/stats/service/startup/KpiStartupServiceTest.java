/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculation.TabularParameterFacade;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.DatasourceConfigLoader;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculatorScheduler;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import io.github.resilience4j.retry.Retry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiStartupServiceTest {
    private static final String EXECUTION_PERIOD = "executionPeriod";

    @Mock CalculationService calculationServiceMock;
    @Mock TabularParameterFacade tabularParameterFacadeMock;
    @Mock KpiCalculatorScheduler kpiCalculatorSchedulerMock;
    @Mock DatasourceConfigLoader datasourceConfigLoaderMock;
    @Mock CalculatorProperties calculatorPropertiesMock;

    @Mock(name = "updateRunningCalculationsToLostRetry") private Retry updateRunningCalculationsToLostRetryMock;

    @InjectMocks
    private KpiStartupService objectUnderTest;

    @BeforeEach
    void setUp() {
        Assertions.assertThat(updateRunningCalculationsToLostRetryMock).as("updateRunningCalculationsToLostRetry").isNotNull();
    }

    @Test
    void shouldThrowException_whenSchedulingFails_onServiceStart() throws Exception {
        final ActivitySchedulerException activitySchedulerException = new ActivitySchedulerException("Cannot schedule", new RuntimeException());

        when(calculatorPropertiesMock.getKpiExecutionPeriod()).thenReturn(EXECUTION_PERIOD);
        doThrow(activitySchedulerException).when(kpiCalculatorSchedulerMock).scheduleKpiCalculation(EXECUTION_PERIOD);

        Assertions.assertThatThrownBy(() -> objectUnderTest.onServiceStart())
                  .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                  .isInstanceOf(StartupException.class)
                  .hasMessage("Cannot schedule");

        verify(calculationServiceMock).updateRunningCalculationsToLost(updateRunningCalculationsToLostRetryMock);
        verify(tabularParameterFacadeMock).deleteLostTables();
        verify(datasourceConfigLoaderMock).populateDatasourceRegistry();
        verify(calculatorPropertiesMock).getKpiExecutionPeriod();
        verify(kpiCalculatorSchedulerMock).scheduleKpiCalculation(EXECUTION_PERIOD);
    }

    @Test
    void shouldVerifyOnServiceStart() throws Exception {
        when(calculatorPropertiesMock.getKpiExecutionPeriod()).thenReturn(EXECUTION_PERIOD);

        objectUnderTest.onServiceStart();

        verify(calculationServiceMock).updateRunningCalculationsToLost(updateRunningCalculationsToLostRetryMock);
        verify(tabularParameterFacadeMock).deleteLostTables();
        verify(datasourceConfigLoaderMock).populateDatasourceRegistry();
        verify(calculatorPropertiesMock).getKpiExecutionPeriod();
        verify(kpiCalculatorSchedulerMock).scheduleKpiCalculation(EXECUTION_PERIOD);
    }
}