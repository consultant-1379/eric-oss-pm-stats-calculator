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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationJobScheduler;
import com.ericsson.oss.air.pm.stats.scheduler.priority.CalculationPriorityRanker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorActivityTest {
    @Mock
    CalculationPriorityRanker calculationPriorityRankerMock;
    @Mock
    KpiCalculationJobScheduler kpiCalculationJobSchedulerMock;

    @InjectMocks
    KpiCalculatorActivity objectUnderTest = KpiCalculatorActivity.of("0 */5 * ? * *");

    @Test
    void whenCreateActivityInstance_thenReturnKpiCalculatorActivity() {
        assertThat(objectUnderTest.getName()).isEqualTo("Activity_kpiScheduler");
        assertThat(objectUnderTest.getContext()).containsEntry("activityName", "Activity_kpiScheduler");
        assertThat(objectUnderTest.getContext()).containsEntry("executionPeriod", "0 */5 * ? * *");
    }

    @Test
    void whenOnDemandIsPrioritized_shouldReturn() {
        when(calculationPriorityRankerMock.isOnDemandPrioritized()).thenReturn(true);

        objectUnderTest.run(createTestJobDataMap());

        verify(calculationPriorityRankerMock).isOnDemandPrioritized();
    }


    @Test
    void whenOnDemandIsNotPrioritizedAndRunning_thenCalculationIsLaunched() {
        when(calculationPriorityRankerMock.isOnDemandPrioritized()).thenReturn(false);

        objectUnderTest.run(createTestJobDataMap());

        verify(kpiCalculationJobSchedulerMock).scheduleCalculations();
    }

    static JobDataMap createTestJobDataMap() {
        return new JobDataMap();
    }
}