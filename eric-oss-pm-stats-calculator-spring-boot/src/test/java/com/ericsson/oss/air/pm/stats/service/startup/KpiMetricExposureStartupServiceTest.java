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

import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.common.scheduler.CronSchedule;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiMetricUpdaterActivity;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;

import com.codahale.metrics.jmx.JmxReporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiMetricExposureStartupServiceTest {
    static final String KPI_METRIC_UPDATE_SCHEDULER = "kpi_metric_update_scheduler";
    static final String KPI_METRIC_CRON = "0/15 * * * * ?";

    @Mock
    JmxReporter jmxReporterMock;

    @Mock
    ActivityScheduler activitySchedulerMock;
    @Mock
    KpiSchedulerMetricRegistry kpiSchedulerMetricRegistryMock;
    @Mock
    ApiMetricRegistry apiMetricRegistryMock;

    @InjectMocks
    KpiMetricExposureStartupService objectUnderTest;

    @Test
    void whenServiceIsStarting_shouldMetricReportersAndMetricUpdaterActivity() throws Exception {
        when(activitySchedulerMock.activityExists(isA(KpiMetricUpdaterActivity.class))).thenReturn(false);
        when(kpiSchedulerMetricRegistryMock.getJmxReporter()).thenReturn(jmxReporterMock);
        when(apiMetricRegistryMock.getJmxReporter()).thenReturn(jmxReporterMock);

        Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.onServiceStart());

        verify(activitySchedulerMock).addCronScheduleForActivity(assertArg(actualActivity -> {
            Assertions.assertThat(actualActivity).usingRecursiveComparison().isEqualTo(KpiMetricUpdaterActivity.of(KPI_METRIC_CRON));
        }), assertArg(actualCronSchedule -> {
            Assertions.assertThat(actualCronSchedule).usingRecursiveComparison().isEqualTo(CronSchedule.of(KPI_METRIC_UPDATE_SCHEDULER, KPI_METRIC_CRON));
        }));
        verify(jmxReporterMock, times(2)).start();
    }

    @Test
    void shouldThrowException_WhenSchedulingMetricUpdateActivityFails() throws Exception {
        when(activitySchedulerMock.activityExists(isA(KpiMetricUpdaterActivity.class))).thenReturn(false);
        when(kpiSchedulerMetricRegistryMock.getJmxReporter()).thenReturn(jmxReporterMock);
        when(apiMetricRegistryMock.getJmxReporter()).thenReturn(jmxReporterMock);

        doThrow(new ActivitySchedulerException("Error while scheduling KPI metric update cron activity"))
                .when(activitySchedulerMock)
                .addCronScheduleForActivity(isA(KpiMetricUpdaterActivity.class), isA(CronSchedule.class));

        Assertions.assertThatThrownBy(() -> objectUnderTest.onServiceStart())
                .isInstanceOf(StartupException.class)
                .hasRootCauseInstanceOf(ActivitySchedulerException.class);
    }
}
