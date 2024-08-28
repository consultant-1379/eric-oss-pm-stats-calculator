/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import static com.ericsson.oss.air.pm.stats.common.scheduler.DefinedCronSchedule.EVERY_FIFTEEN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.scheduler.DefinedCronSchedule.NEVER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.common.env.Environment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Class used to manage schedulers for {@link Activity} instance. Can create new activities that will be schedule to run.
 */
@Slf4j
@RequiredArgsConstructor
public class ActivityRunner {

    private static final Map<String, Object> DEFAULT_ACTIVITY_DATA = new HashMap<>(0);
    private static final String CRON_SCHEDULER_APPENDER = "_scheduler";

    private final ActivityScheduler activityScheduler;

    /**
     * Schedule or run the activity. Use the {@link ActivityScheduler} to add a Cron schedule for the created {@link Activity}
     *
     * @param scheduleActivity
     *            activity that will be run
     * @param cronEnvironmentVariable
     *            environment variable name
     * @param contextEntry
     *            context of the activity
     * @throws ActivitySchedulerException
     *             thrown if there is error in scheduling the activity
     */
    public void schedule(final ScheduleActivity scheduleActivity, final String cronEnvironmentVariable, final String contextEntry)
            throws ActivitySchedulerException {
        final String cronExpression = Environment.getEnvironmentValue(cronEnvironmentVariable, EVERY_FIFTEEN_MINUTES.getCronSchedule());
        final Optional<CronSchedule> cronSchedule = createCronSchedule(cronExpression, contextEntry);

        handleActivity(cronSchedule.orElse(null), scheduleActivity);

    }

    /**
     * Schedule or run the activity. Use the {@link ActivityScheduler} to add a Cron schedule for the created {@link Activity}
     *
     * @param scheduleActivity
     *            activity that will be run
     * @param cronExpression
     *            cron expression
     * @param contextEntry
     *            context of the activity
     * @throws ActivitySchedulerException
     *             thrown if there is error in scheduling the activity
     */
    public void scheduleWithCronExpression(final ScheduleActivity scheduleActivity, final String cronExpression, final String contextEntry)
            throws ActivitySchedulerException {
        final Optional<CronSchedule> cronSchedule = createCronSchedule(cronExpression, contextEntry);
        handleActivity(cronSchedule.orElse(null), scheduleActivity);
    }

    private void handleActivity(final CronSchedule cronSchedule, final ScheduleActivity scheduleActivity) throws ActivitySchedulerException {
        try {
            if (cronSchedule != null) {
                if (NEVER.getCronSchedule().equals(cronSchedule.getCronExpression())) {
                    log.warn("Cron expression set to 'NEVER', activity {} is not scheduled", scheduleActivity.getName());
                    return;
                }
                scheduleActivity(cronSchedule, scheduleActivity);
                return;
            }
            runActivity(scheduleActivity);
        } catch (final ActivitySchedulerException e) {
            log.error("Fail to schedule the activity '{}'", scheduleActivity.getName(), e);
            throw new ActivitySchedulerException("Fail to schedule the activity '{}'", e);
        }
    }

    private void scheduleActivity(final CronSchedule cronSchedule, final ScheduleActivity scheduleActivity)
            throws ActivitySchedulerException {
        if (activityScheduler.activityExists(scheduleActivity)) {
            log.info("Removing existing scheduled activity '{}'", scheduleActivity.getName());
            activityScheduler.removeActivity(scheduleActivity);
        }

        log.info("Adding activity '{}' to CRON scheduler job at: '{}'", scheduleActivity.getName(), cronSchedule);
        activityScheduler.addCronScheduleForActivity(scheduleActivity, cronSchedule);
    }

    private void runActivity(final ScheduleActivity scheduleActivity) throws ActivitySchedulerException {
        log.info("Running activity '{}' now", scheduleActivity.getName());
        activityScheduler.runActivity(scheduleActivity);
    }

    private Optional<CronSchedule> createCronSchedule(final String cronExpression, final String contextEntry)
            throws ActivitySchedulerException {

        if (StringUtils.isEmpty(cronExpression)) {
            return Optional.empty();
        } else if (NEVER.getCronSchedule().equals(cronExpression)) {
            return Optional.of(new CronSchedule(
                    contextEntry + CRON_SCHEDULER_APPENDER, DEFAULT_ACTIVITY_DATA, NEVER.getCronSchedule()));
        }

        return Optional.of(new CronSchedule(
                contextEntry + CRON_SCHEDULER_APPENDER, DEFAULT_ACTIVITY_DATA, cronExpression));
    }
}
