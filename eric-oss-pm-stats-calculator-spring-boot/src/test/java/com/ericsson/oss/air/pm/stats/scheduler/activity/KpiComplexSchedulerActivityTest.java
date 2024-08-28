/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationExecutionController;
import com.ericsson.oss.air.pm.stats.service.facade.RunningCalculationDetectorFacade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;

@ExtendWith(MockitoExtension.class)
class KpiComplexSchedulerActivityTest {
    private static final String CRON_EXPRESSION = "0/30 * * * * *";
    private static final String ACTIVITY_NAME = "Activity_complexKpiScheduler";

    @Mock
    RunningCalculationDetectorFacade runningCalculationDetectorFacadeMock;
    @Mock
    KpiCalculationExecutionController kpiCalculationExecutionControllerMock;

    @InjectMocks
    KpiComplexSchedulerActivity objectUnderTest = KpiComplexSchedulerActivity.of(CRON_EXPRESSION);

    @Test
    void whenCreateActivityInstance_thenReturnKpiComplexSchedulerActivity() {
        assertThat(objectUnderTest.getName()).isEqualTo(ACTIVITY_NAME);
        assertThat(objectUnderTest.getContext()).containsEntry("activityName", ACTIVITY_NAME);
        assertThat(objectUnderTest.getContext()).containsEntry("activityRunFrequency", CRON_EXPRESSION);
    }

    @Test
    void whenActivityIsRun_andComplexQueueEmpty_thenCalculationIsNotLaunched() {
        when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(true);

        objectUnderTest.run(new JobDataMap());

        verify(kpiCalculationExecutionControllerMock, never()).executeComplexCalculation();
    }

    @Test
    void whenActivityIsRun_andSimpleKpiQueueNotEmpty_thenCalculationIsNotLaunched() {
        when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(false);
        when(kpiCalculationExecutionControllerMock.isSimpleQueueEmpty()).thenReturn(false);

        objectUnderTest.run(new JobDataMap());

        verify(kpiCalculationExecutionControllerMock, never()).executeComplexCalculation();
    }

    @Test
    void whenActivityIsRun_andSimpleJobRunning_thenCalculationIsNotLaunched() {
        when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(false);
        when(kpiCalculationExecutionControllerMock.isSimpleQueueEmpty()).thenReturn(true);
        when(runningCalculationDetectorFacadeMock.isAnySimpleCalculationRunning()).thenReturn(true);

        objectUnderTest.run(new JobDataMap());

        verify(kpiCalculationExecutionControllerMock, never()).executeComplexCalculation();
    }

    @Test
    void whenActivityIsRun_andComplexJobRunning_thenCalculationIsNotLaunched() {
        when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(false);
        when(kpiCalculationExecutionControllerMock.isSimpleQueueEmpty()).thenReturn(true);
        when(runningCalculationDetectorFacadeMock.isAnySimpleCalculationRunning()).thenReturn(false);
        when(runningCalculationDetectorFacadeMock.isAnyComplexCalculationRunning()).thenReturn(true);

        objectUnderTest.run(new JobDataMap());

        verify(kpiCalculationExecutionControllerMock, never()).executeComplexCalculation();
    }

    @Test
    void whenActivityIsRun_andAllConditionsAreMet_thenCalculationIsLaunched() {
        when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(false);
        when(kpiCalculationExecutionControllerMock.isSimpleQueueEmpty()).thenReturn(true);
        when(runningCalculationDetectorFacadeMock.isAnySimpleCalculationRunning()).thenReturn(false);
        when(runningCalculationDetectorFacadeMock.isAnyComplexCalculationRunning()).thenReturn(false);

        objectUnderTest.run(new JobDataMap());

        verify(kpiCalculationExecutionControllerMock).executeComplexCalculation();
    }

}