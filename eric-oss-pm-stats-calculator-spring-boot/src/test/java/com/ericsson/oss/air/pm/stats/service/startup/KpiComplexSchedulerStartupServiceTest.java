/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityRunner;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiComplexSchedulerActivity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiComplexSchedulerStartupServiceTest {
    private static final String ACTIVITY_CRON = "0/5 * * * * ?";
    private static final String ACTIVITY_CONTEXT_ENTRY = "complexKpiScheduler";

    @Mock
    KpiComplexSchedulerActivity kpiComplexSchedulerActivityMock;
    @Mock
    ActivityRunner activityRunnerMock;

    @InjectMocks
    KpiComplexSchedulerStartupService objectUnderTest;

    @Test
    void shouldVerifyOnServiceStart() throws Exception {
        try (final MockedStatic<KpiComplexSchedulerActivity> complexSchedulerActivityMockedStatic = mockStatic(KpiComplexSchedulerActivity.class)) {
            final Verification activityCreateFunction = () -> KpiComplexSchedulerActivity.of(ACTIVITY_CRON);

            when(kpiComplexSchedulerActivityMock.getName()).thenReturn("Activity_complexKpiScheduler");
            complexSchedulerActivityMockedStatic.when(activityCreateFunction).thenReturn(kpiComplexSchedulerActivityMock);

            objectUnderTest.onServiceStart();

            complexSchedulerActivityMockedStatic.verify(activityCreateFunction);
            verify(activityRunnerMock).scheduleWithCronExpression(kpiComplexSchedulerActivityMock, ACTIVITY_CRON, ACTIVITY_CONTEXT_ENTRY);
        }
    }
}
