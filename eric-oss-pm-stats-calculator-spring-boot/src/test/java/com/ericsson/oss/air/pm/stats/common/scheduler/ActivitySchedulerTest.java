/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Fail.shouldHaveThrown;
import static org.awaitility.Awaitility.await;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.common.scheduler.utils.ActivityTestUtils;
import com.ericsson.oss.air.pm.stats.common.scheduler.utils.DefaultInterruptableActivity;

import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;

/**
 * Unit tests for {@link ActivityScheduler}.
 */

class ActivitySchedulerTest {
    ActivityScheduler activityScheduler;

    ConditionFactory conditionFactory;

    @BeforeEach
    void setUp() {
        activityScheduler = ActivityScheduler.getInstance();
        conditionFactory = await().atMost(5, TimeUnit.SECONDS);
    }

    @Test
    void whenAnActivityIsScheduled_thenTheActivityAndTheScheduleExistInTheScheduler() throws ActivitySchedulerException {
        final CronSchedule cronSchedule1 = ActivityTestUtils.getCronScheduleWithName("Schedule1");
        final Activity activity = ActivityTestUtils.getActivityWithName("Activity1");

        activityScheduler.addCronScheduleForActivity(activity, cronSchedule1);

        assertThat(activityScheduler.activityExists(activity)).isTrue();
        assertThat(activityScheduler.getScheduleNamesForActivity(activity).contains("Schedule1"));
    }

    @Test
    void whenMultipleCronSchedulesAreAddedForAnActivity_ThenTheActivityExists_andAllSchedulesAreInScheduler()
            throws ActivitySchedulerException {
        final CronSchedule cronSchedule1 = ActivityTestUtils.getCronScheduleWithName("Schedule2A");
        final CronSchedule cronSchedule2 = ActivityTestUtils.getCronScheduleWithName("Schedule2B");
        final Activity activity = ActivityTestUtils.getActivityWithName("Activity2");

        activityScheduler.addCronScheduleForActivity(activity, cronSchedule1);
        activityScheduler.addCronScheduleForActivity(activity, cronSchedule2);

        assertThat(activityScheduler.activityExists(activity)).isTrue();
        assertThat(activityScheduler.getScheduleNamesForActivity(activity).contains("Schedule2A"));
        assertThat(activityScheduler.getScheduleNamesForActivity(activity).contains("Schedule2B"));
    }

    @Test
    void whenAnActivityIsScheduledToRunImmediately_ThenTheSchedulerRunsTheActivity() throws ActivitySchedulerException {
        final Activity activity = ActivityTestUtils.getActivityWithName("Activity3");
        activityScheduler.runActivity(activity);
        conditionFactory.pollInterval(2, TimeUnit.MILLISECONDS)
                        .untilAsserted(() -> assertThat(activityScheduler.isActivityRunning(activity)).isTrue());
    }

    @Test
    void whenAnActivityIsGivenTheSameScheduleTwice_thenTheActivityAndSchedulerExistInTheScheduler_andAnExceptionIsThrownWhenTheSameScheduleIsAddedAgain()
            throws ActivitySchedulerException {
        final CronSchedule cronSchedule = ActivityTestUtils.getCronScheduleWithName("Schedule4");
        final Activity activity = ActivityTestUtils.getActivityWithName("Activity4");

        activityScheduler.addCronScheduleForActivity(activity, cronSchedule);

        assertThat(activityScheduler.activityExists(activity)).isTrue();
        assertThat(activityScheduler.getScheduleNamesForActivity(activity).contains("Schedule4"));

        assertThatThrownBy(() -> activityScheduler.addCronScheduleForActivity(activity, cronSchedule))
                .isInstanceOf(ActivitySchedulerException.class);
    }

    @Test
    void whenTwoActivitiesAreGivenTheSameSchedule_thenBothActivitiesExistInTheScheduler_andTheScheduleExistsForBoth()
            throws ActivitySchedulerException {
        final CronSchedule cronSchedule = ActivityTestUtils.getCronScheduleWithName("Schedule5");
        final Activity activityA = ActivityTestUtils.getActivityWithName("Activity5A");
        final Activity activityB = ActivityTestUtils.getActivityWithName("Activity5B");

        activityScheduler.addCronScheduleForActivity(activityA, cronSchedule);
        activityScheduler.addCronScheduleForActivity(activityB, cronSchedule);

        assertThat(activityScheduler.activityExists(activityA)).isTrue();
        assertThat(activityScheduler.activityExists(activityB)).isTrue();
        assertThat(activityScheduler.getScheduleNamesForActivity(activityA).contains("Schedule5"));
        assertThat(activityScheduler.getScheduleNamesForActivity(activityB).contains("Schedule5"));
    }

    @Test
    void whenActivityExists_andTheActivityIsRemoved_thenTheActivityDoesNotExistInTheScheduler() throws ActivitySchedulerException {
        final Activity activity = ActivityTestUtils.getActivityWithName("Activity");
        final CronSchedule cronSchedule = ActivityTestUtils.getCronScheduleWithName("Schedule");

        activityScheduler.addCronScheduleForActivity(activity, cronSchedule);
        assertThat(activityScheduler.activityExists(activity)).isTrue();

        activityScheduler.removeActivity(activity);
        assertThat(activityScheduler.activityExists(activity)).isFalse();
    }

    @Test
    void whenCheckIsActivityIsRunning_andActivityIsNotRunning_thenFalseIsReturned() throws ActivitySchedulerException {
        final String activityName = "invalid";
        final Activity activity = new Activity(activityName, Collections.emptyMap()) {

            @Override
            public void run(final JobDataMap activityContext) {

            }
        };

        final boolean result = activityScheduler.isActivityRunning(activity);
        assertThat(result).isFalse();
    }

    @Test
    void whenGetAllJobNames_thenAllScheduledJobNamesAreReturned() throws ActivitySchedulerException {
        final CronSchedule cronSchedule = ActivityTestUtils.getCronScheduleWithName("Schedule6");
        final Activity activityA = ActivityTestUtils.getActivityWithName("Activity6A");
        final Activity activityB = ActivityTestUtils.getActivityWithName("Activity6B");
        final Activity activityC = ActivityTestUtils.getActivityWithName("Activity6C");

        activityScheduler.addCronScheduleForActivity(activityA, cronSchedule);
        activityScheduler.addCronScheduleForActivity(activityB, cronSchedule);
        activityScheduler.addCronScheduleForActivity(activityC, cronSchedule);

        assertThat(activityScheduler.getAllJobNames().containsAll(List.of("Activity6A", "Activity6B", "Activity6C")));

        activityScheduler.removeActivity(activityB);

        assertThat(activityScheduler.getAllJobNames().containsAll(List.of("Activity6A", "Activity6C")));
    }

    @Test
    void whenAnActivityIsRunning_andTheSchedulerInterruptsTheActivity_thenAnExceptionIsThrown() throws ActivitySchedulerException {
        final Activity activity = ActivityTestUtils.getActivityWithName("Activity7");
        activityScheduler.runActivity(activity);
        conditionFactory.pollInterval(2, TimeUnit.MILLISECONDS)
                        .untilAsserted(() -> assertThat(activityScheduler.isActivityRunning(activity)).isTrue());
        try {
            activityScheduler.interruptActivity(activity);
            shouldHaveThrown(ActivitySchedulerException.class);
        } catch (final ActivitySchedulerException e) {
            assertThat(e.getMessage()).isEqualTo("Unable to interrupt activity 'Activity7' from the scheduler");
        }
    }

    @Test
    void whenAnInterruptableActivityIsRunning_andTheSchedulerInterruptsTheActivity_thenActivityIsInterrupted()
            throws ActivitySchedulerException {
        final DefaultInterruptableActivity activity = ActivityTestUtils.getInterruptableActivityWithName("Activity8");
        activityScheduler.runActivity(activity);
        conditionFactory.pollInterval(2, TimeUnit.MILLISECONDS)
                        .untilAsserted(() -> assertThat(activityScheduler.isActivityRunning(activity)).isTrue());
        activityScheduler.interruptActivity(activity);

        conditionFactory.pollInterval(2, TimeUnit.MILLISECONDS)
                        .untilAsserted(() -> assertThat(activity.isCompletelyExecuted()).isFalse());
    }
}
