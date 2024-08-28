/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityRunner;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorActivity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorSchedulerBeanTest {
    private static final String ACTIVITY_NAME = "activityName";
    private static final String CONTEXT_ENTRY = "kpiCalculator";
    private static final String EXECUTION_PERIOD = "executionPeriod";

    @Mock
    ActivityScheduler activitySchedulerMock;
    @Mock
    ActivityRunner activityRunnerMock;

    @InjectMocks
    private KpiCalculatorSchedulerBean objectUnderTest;

    @Test
    void shouldNotScheduleKpiCalculation_whenItIsAlreadyScheduled() throws ActivitySchedulerException {
        try (final MockedStatic<KpiCalculatorActivity> kpiCalculatorActivityMockedStatic = mockStatic(KpiCalculatorActivity.class)) {
            final KpiCalculatorActivity kpiCalculatorActivityMock = mock(KpiCalculatorActivity.class);

            final Verification kpiCalculatorActivityVerification = () -> KpiCalculatorActivity.of(EXECUTION_PERIOD);

            kpiCalculatorActivityMockedStatic.when(kpiCalculatorActivityVerification).thenReturn(kpiCalculatorActivityMock);
            when(kpiCalculatorActivityMock.getName()).thenReturn(ACTIVITY_NAME);
            when(activitySchedulerMock.activityExists(kpiCalculatorActivityMock)).thenReturn(true);

            objectUnderTest.scheduleKpiCalculation(EXECUTION_PERIOD);

            kpiCalculatorActivityMockedStatic.verify(kpiCalculatorActivityVerification);
            verify(kpiCalculatorActivityMock).getName();
            verify(activitySchedulerMock).activityExists(kpiCalculatorActivityMock);
        }
    }

    @Test
    void shouldScheduleKpiCalculation_whenItIsNotScheduledAlready() throws ActivitySchedulerException {
        try (final MockedStatic<KpiCalculatorActivity> kpiCalculatorActivityMockedStatic = mockStatic(KpiCalculatorActivity.class)) {
            final KpiCalculatorActivity kpiCalculatorActivityMock = mock(KpiCalculatorActivity.class);

            final Verification kpiCalculatorActivityVerification = () -> KpiCalculatorActivity.of(EXECUTION_PERIOD);
            kpiCalculatorActivityMockedStatic.when(kpiCalculatorActivityVerification).thenReturn(kpiCalculatorActivityMock);
            when(kpiCalculatorActivityMock.getName()).thenReturn(ACTIVITY_NAME);
            when(activitySchedulerMock.activityExists(kpiCalculatorActivityMock)).thenReturn(false);

            objectUnderTest.scheduleKpiCalculation(EXECUTION_PERIOD);

            kpiCalculatorActivityMockedStatic.verify(kpiCalculatorActivityVerification);
            verify(kpiCalculatorActivityMock).getName();
            verify(activitySchedulerMock).activityExists(kpiCalculatorActivityMock);
            verify(activityRunnerMock).scheduleWithCronExpression(kpiCalculatorActivityMock, EXECUTION_PERIOD, CONTEXT_ENTRY);
        }

    }
}