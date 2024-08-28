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

import static com.ericsson.oss.air.pm.stats.common.scheduler.DefinedCronSchedule.EVERY_FIFTEEN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.scheduler.DefinedCronSchedule.EVERY_HOUR;
import static com.ericsson.oss.air.pm.stats.common.scheduler.DefinedCronSchedule.IMMEDIATE;
import static com.ericsson.oss.air.pm.stats.common.scheduler.DefinedCronSchedule.NEVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActivityRunnerTest {
    static final String TEST_SCHEDULE = "TEST_SCHEDULE";
    static final String TEST_ACTIVITY = "testActivity";
    static final String TEST_ACTIVITY_SCHEDULER = "testActivity_scheduler";

    ActivityScheduler activityScheduler;

    ActivityRunner objectUnderTest;

    @BeforeEach
    void setUp() throws ActivitySchedulerException {
        activityScheduler = ActivityScheduler.getInstance();
        activityScheduler.removeActivity(getTestActivity());
        objectUnderTest = new ActivityRunner(activityScheduler);
    }

    @Test
    void whenActivityScheduledWithSpecificEnvironmentVariableValueForCronExpression_thenScheduledActivityCreated()
            throws ActivitySchedulerException {
        System.setProperty(TEST_SCHEDULE, EVERY_FIFTEEN_MINUTES.getCronSchedule());
        objectUnderTest.schedule(getTestActivity(), TEST_SCHEDULE, TEST_ACTIVITY);
        final List<String> scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertEquals(1, scheduleNames.size());
        assertThat(scheduleNames).containsExactly(TEST_ACTIVITY_SCHEDULER);
    }

    @Test
    void whenActivityScheduledWithSpecificCronExpression_thenScheduledActivityCreated()
            throws ActivitySchedulerException {

        objectUnderTest.scheduleWithCronExpression(getTestActivity(), EVERY_HOUR.getCronSchedule(), TEST_ACTIVITY);
        final List<String> scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertEquals(1, scheduleNames.size());
        assertThat(scheduleNames).containsExactly(TEST_ACTIVITY_SCHEDULER);
    }

    @Test
    void whenActivityScheduledWithEmptyCronExpression_thenScheduledActivityCreated()
            throws ActivitySchedulerException {
        objectUnderTest.scheduleWithCronExpression(getTestActivity(), IMMEDIATE.getCronSchedule(), TEST_ACTIVITY);
        final List<String> scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertEquals(1, scheduleNames.size());
    }

    @Test
    void whenActivityScheduledWithEmptyEnvironmentVariableValueForCronExpression_thenScheduledActivityCreated()
            throws ActivitySchedulerException {
        System.setProperty(TEST_SCHEDULE, IMMEDIATE.getCronSchedule());
        objectUnderTest.schedule(getTestActivity(), TEST_SCHEDULE, TEST_ACTIVITY);
        final List<String> scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertEquals(1, scheduleNames.size());
    }

    @Test
    void whenActivityScheduledTwiceWithSameCronExpression_thenJustOneScheduledActivityCreated()
            throws ActivitySchedulerException {
        System.setProperty(TEST_SCHEDULE, "0 15 * * * ? *");
        objectUnderTest.schedule(getTestActivity(), TEST_SCHEDULE, TEST_ACTIVITY);
        List<String> scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertEquals(1, scheduleNames.size());
        assertThat(scheduleNames).containsExactly(TEST_ACTIVITY_SCHEDULER);

        objectUnderTest.schedule(getTestActivity(), TEST_SCHEDULE, TEST_ACTIVITY);
        scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertEquals(1, scheduleNames.size());
        assertThat(scheduleNames).containsExactly(TEST_ACTIVITY_SCHEDULER);
    }

    @Test
    void whenActivityScheduledWithCronExpressionSetToNever_thenNoScheduledActivityIsCreated()
            throws ActivitySchedulerException {
        System.setProperty(TEST_SCHEDULE, NEVER.getCronSchedule());
        objectUnderTest.schedule(getTestActivity(), TEST_SCHEDULE, TEST_ACTIVITY);
        final List<String> scheduleNames = activityScheduler.getScheduleNamesForActivity(getTestActivity());

        assertTrue(scheduleNames.isEmpty());
    }

    ScheduleActivity getTestActivity() {
        return new ScheduleActivity().createInstance(TEST_ACTIVITY);
    }
}
