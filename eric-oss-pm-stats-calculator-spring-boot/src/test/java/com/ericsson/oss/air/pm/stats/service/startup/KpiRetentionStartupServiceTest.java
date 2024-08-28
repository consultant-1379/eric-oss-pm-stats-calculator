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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.common.scheduler.CronSchedule;
import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorRetentionActivity;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiRetentionStartupServiceTest {
    private static final Integer RETENTION_DAYS = 5;

    private static final String ACTIVITY_NAME = "ACTIVITY_NAME";
    private static final String CRON = "CRON";
    private static final String KPI_CALCULATOR_RETENTION_SCHEDULER = "kpi_calculator_retention_scheduler";

    @Mock
    private DatabaseService databaseServiceMock;

    private KpiRetentionStartupService objectUnderTest;

    private MockedStatic<KpiCalculatorRetentionActivity> retentionActivityMockedStatic;

    @Mock
    private KpiCalculatorRetentionActivity kpiCalculatorRetentionActivityMock;
    @Mock
    ActivityScheduler activitySchedulerMock;

    @Mock(name = "retentionPeriod")
    EnvironmentValue<Duration> retentionPeriodMock;

    @Mock(name = "cronRetentionPeriodCheck")
    EnvironmentValue<String> cronRetentionPeriodCheckMock;

    private Verification retentionVerification;

    @BeforeEach
    void setUp() {
        objectUnderTest = new KpiRetentionStartupService(databaseServiceMock, activitySchedulerMock, retentionPeriodMock, cronRetentionPeriodCheckMock);
        retentionActivityMockedStatic = mockStatic(KpiCalculatorRetentionActivity.class);

        retentionVerification = () -> KpiCalculatorRetentionActivity.of(RETENTION_DAYS);
        retentionActivityMockedStatic.when(retentionVerification).thenReturn(kpiCalculatorRetentionActivityMock);

        when(retentionPeriodMock.value()).thenReturn(Duration.ofDays(5));
        when(cronRetentionPeriodCheckMock.value()).thenReturn(CRON);
    }

    @AfterEach
    void tearDown() {
        retentionActivityMockedStatic.close();
    }

    @Test
    void shouldThrowStartupException_whenSomethingGoesWrong_onInit() throws Exception {
        final String message = "Something went wrong...";
        doThrow(new ActivitySchedulerException(message, new RuntimeException())).when(activitySchedulerMock).activityExists(kpiCalculatorRetentionActivityMock);

        final List<String> outputTableNames = Arrays.asList("tableName1", "tableName2");
        when(databaseServiceMock.findAllCalculationOutputTables()).thenReturn(outputTableNames);

        Assertions.assertThatThrownBy(() -> objectUnderTest.init())
                .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                .hasCauseExactlyInstanceOf(ActivitySchedulerException.class)
                .isInstanceOf(StartupException.class)
                .hasMessage("Error while scheduling retention period for cron: '%s' tables:%n%s",
                        CRON,
                        outputTableNames.stream().collect(Collectors.joining(System.lineSeparator())));

        retentionActivityMockedStatic.verify(retentionVerification);
        verify(activitySchedulerMock).activityExists(kpiCalculatorRetentionActivityMock);
        verify(databaseServiceMock).findAllCalculationOutputTables();
        verify(retentionPeriodMock).value();
    }

    @Test
    void shouldRemove_andCreateNewSchedulerForRetention_onInit() throws Exception {

        try (final MockedStatic<CronSchedule> cronScheduleMockedStatic = mockStatic(CronSchedule.class)) {
            final CronSchedule cronScheduleMock = mock(CronSchedule.class);
            final Verification cronScheduleVerification = () -> CronSchedule.of(eq(KPI_CALCULATOR_RETENTION_SCHEDULER), eq(CRON));
            cronScheduleMockedStatic.when(cronScheduleVerification).thenReturn(cronScheduleMock);

            when(activitySchedulerMock.activityExists(kpiCalculatorRetentionActivityMock)).thenReturn(true);
            when(kpiCalculatorRetentionActivityMock.getName()).thenReturn(ACTIVITY_NAME, ACTIVITY_NAME);

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.init());

            retentionActivityMockedStatic.verify(retentionVerification);
            verify(activitySchedulerMock).activityExists(kpiCalculatorRetentionActivityMock);
            verify(kpiCalculatorRetentionActivityMock, times(2)).getName();
            verify(activitySchedulerMock).removeActivity(kpiCalculatorRetentionActivityMock);
            cronScheduleMockedStatic.verify(cronScheduleVerification);
            verify(activitySchedulerMock).addCronScheduleForActivity(kpiCalculatorRetentionActivityMock, cronScheduleMock);
            verify(retentionPeriodMock).value();
        }
    }

    @Test
    void shouldCreateNewSchedulerForRetention_onInit() throws Exception {
        try (final MockedStatic<CronSchedule> cronScheduleMockedStatic = mockStatic(CronSchedule.class)) {
            when(activitySchedulerMock.activityExists(kpiCalculatorRetentionActivityMock)).thenReturn(false);
            when(kpiCalculatorRetentionActivityMock.getName()).thenReturn(ACTIVITY_NAME);

            final CronSchedule cronScheduleMock = mock(CronSchedule.class);

            final Verification cronScheduleVerification = () -> CronSchedule.of(eq(KPI_CALCULATOR_RETENTION_SCHEDULER), eq(CRON));
            cronScheduleMockedStatic.when(cronScheduleVerification).thenReturn(cronScheduleMock);

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.init());

            retentionActivityMockedStatic.verify(retentionVerification);
            verify(activitySchedulerMock).activityExists(kpiCalculatorRetentionActivityMock);
            verify(kpiCalculatorRetentionActivityMock).getName();
            cronScheduleMockedStatic.verify(cronScheduleVerification);
            verify(activitySchedulerMock).addCronScheduleForActivity(kpiCalculatorRetentionActivityMock, cronScheduleMock);
            verify(retentionPeriodMock).value();
        }
    }
}