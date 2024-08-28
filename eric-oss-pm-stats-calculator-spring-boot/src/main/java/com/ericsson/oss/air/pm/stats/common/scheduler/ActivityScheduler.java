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

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.core.jmx.JobDataMapSupport;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

/**
 * Scheduler for executing instances of the {@link Activity} class. {@link Activity}s are run based on the associated {@link Schedule}s. Only one
 * instance of an {@link ActivityScheduler} is allowed.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActivityScheduler {

    private static class Holder {
        static final ActivityScheduler INSTANCE = new ActivityScheduler();
        static final Scheduler SCHEDULER = createScheduler();
        @SneakyThrows
        private static Scheduler createScheduler() {
            return ActivitySchedulerFactory.createAndStart();
        }
    }

    /**
     * Singleton instance of {@link ActivityScheduler}.
     *
     * @return instance of {@link ActivityScheduler}
     */
    public static ActivityScheduler getInstance(){
        return Holder.INSTANCE;
    }

    public void setJobFactory(final JobFactory factory) throws SchedulerException {
        Holder.SCHEDULER.setJobFactory(factory);
    }

    /**
     * Method that takes an {@link Activity} implementation and links it to a {@link CronSchedule}. The {@link Activity} will be triggered based on
     * the configuration of the {@link CronSchedule}. All instances of {@link Activity} are identified by a group and an identity. The instance is
     * durable, in that it is stored in the scheduler even when there are no associated {@link CronSchedule}s.
     *
     * @param activity
     *            implementation of the {@link Activity} class containing the business logic of the process
     * @param cronSchedule
     *            instance of the {@link CronSchedule} containing information on when to trigger the associated {@link Activity}
     * @throws ActivitySchedulerException
     *             thrown if there is an issue adding the {@link CronSchedule}
     */
    public void addCronScheduleForActivity(final Activity activity, final CronSchedule cronSchedule) throws ActivitySchedulerException {
        if (!activityExists(activity)) {
            addActivity(activity);
        }
        addCronScheduleForActivity(cronSchedule, activity.getName());
    }

    private void addCronScheduleForActivity(final CronSchedule cronSchedule, final String activityName)
            throws ActivitySchedulerException {
        final Trigger trigger = newTrigger()
                .withIdentity(cronSchedule.getName(), activityName)
                .usingJobData(JobDataMapSupport.newJobDataMap(cronSchedule.getContext()))
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(cronSchedule.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone(CronSchedule.OSS_TIME_ZONE)))
                .forJob(activityName)
                .build();
        try {
            Holder.SCHEDULER.scheduleJob(trigger);
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(
                    String.format("Error adding schedule '%s' for activity '%s'", cronSchedule.getCronExpression(), activityName), e);
        }
    }

    /**
     * Method that triggers an {@link Activity} to run immediately. If the {@link Activity} does not already exist in the {@link ActivityScheduler}
     * then it will be added.
     *
     * @param activity
     *            implementation of the {@link Activity} class
     * @throws ActivitySchedulerException
     *             if there is an issue adding the {@link Activity} or triggering the {@link Activity}
     */
    public void runActivity(final Activity activity) throws ActivitySchedulerException {
        if (!activityExists(activity)) {
            addActivity(activity);
        }
        triggerActivity(activity);
    }

    /**
     * Checks if the specified {@link Activity} exists in the {@link ActivityScheduler}.
     *
     * @param activity
     *            the {@link Activity} to check
     * @return true if the {@link Activity} already exists
     * @throws ActivitySchedulerException
     *             thrown if there is any issue checking if the activity exists
     */
    public boolean activityExists(final Activity activity) throws ActivitySchedulerException {
        return activityExists(activity.getName());
    }

    /**
     * Checks if the {@link Activity} with the specified name exists in the {@link ActivityScheduler}.
     *
     * @param activityName
     *            the name of the {@link Activity} to check
     * @return true if the {@link Activity} already exists
     * @throws ActivitySchedulerException
     *             thrown if there is any issue checking if the activity exists
     */
    public boolean activityExists(final String activityName) throws ActivitySchedulerException {
        final JobKey activityKey = new JobKey(activityName);
        try {
            return Holder.SCHEDULER.checkExists(activityKey);
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(String.format("Unable to determine existence of activity '%s' in scheduler", activityName), e);
        }
    }

    private void addActivity(final Activity activity) throws ActivitySchedulerException {
        final JobDetail jobDetail = JobBuilder.newJob(activity.getClass())
                .withIdentity(activity.getName())
                .usingJobData(JobDataMapSupport.newJobDataMap(activity.getContext()))
                .storeDurably()
                .build();
        try {
            Holder.SCHEDULER.addJob(jobDetail, false);
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(
                    String.format("Error adding activity '%s' to scheduler", activity.getName()), e);
        }
    }

    private void triggerActivity(final Activity activity) throws ActivitySchedulerException {
        final JobKey activityKey = new JobKey(activity.getName());
        try {
            Holder.SCHEDULER.triggerJob(activityKey);
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(String.format("Unable to run activity '%s'", activity), e);
        }
    }

    /**
     * Returns the names of {@link Schedule}s currently available in the {@link ActivityScheduler} based on the {@link Activity} provided.
     *
     * @param activity
     *            instance of {@link Activity}
     * @return list of {@link Schedule} names for given {@link Activity}
     * @throws ActivitySchedulerException
     *             if the {@link Schedule}s associated with an {@link Activity} can not be listed
     */
    public List<String> getScheduleNamesForActivity(final Activity activity) throws ActivitySchedulerException {
        try {
            return Holder.SCHEDULER.getTriggersOfJob(new JobKey(activity.getName()))
                    .stream()
                    .map(trigger -> trigger.getKey().getName())
                    .collect(Collectors.toList());
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(String.format("Unable to list schedules for activity '%s'", activity.getName()), e);
        }
    }

    /**
     * Determines whether or not an {@link Activity} is currently running.
     *
     * @param activity
     *            instance of {@link Activity} used to determine if it is running
     * @return true if {@link Activity} is running, false if it is not
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error
     */
    public boolean isActivityRunning(final Activity activity) throws ActivitySchedulerException {
        return isActivityRunning(activity.getName());
    }

    /**
     * Determines whether or not an {@link Activity} is currently running.
     *
     * @param activityName
     *            name of {@link Activity} used to determine if it is running
     * @return true if {@link Activity} is running, false if it is not
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error
     */
    public boolean isActivityRunning(final String activityName) throws ActivitySchedulerException {
        try {
            final List<JobExecutionContext> executingJobs = Holder.SCHEDULER.getCurrentlyExecutingJobs();
            for (final JobExecutionContext jobExecutionContext : executingJobs) {
                final String runningJobName = jobExecutionContext.getJobDetail().getKey().getName();
                if (activityName.equals(runningJobName)) {
                    return true;
                }
            }
            return false;
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(String.format("Unable to determine if activity '%s' is running", activityName), e);
        }
    }

    /**
     * Remove an {@link Activity} that is registered on the scheduler.
     *
     * @param activity
     *            instance of {@link Activity} used to be removed from the scheduler
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error trying to remove the {@link Activity}
     */
    public void removeActivity(final Activity activity) throws ActivitySchedulerException {
        removeActivity(activity.getName());
    }

    /**
     * Remove an {@link Activity} that is registered on the scheduler.
     *
     * @param activityName
     *            the name of the {@link Activity} to be removed from the scheduler
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error trying to remove the {@link Activity}
     */
    public void removeActivity(final String activityName) throws ActivitySchedulerException {
        final JobKey activityKey = new JobKey(activityName);
        try {
            if (Holder.SCHEDULER.checkExists(activityKey)) {
                Holder.SCHEDULER.deleteJob(activityKey);
                log.info("Activity '{}' has been deleted from the scheduler", activityName);
            } else {
                log.warn("Activity '{}' does not exist and does not need to be deleted from the scheduler", activityName);
            }
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(String.format("Unable to remove activity '%s' from the scheduler", activityName), e);
        }
    }

    /**
     * Interrupt an {@link Activity} that is registered on the scheduler.
     *
     * @param activity
     *            instance of {@link Activity} used to be interrupted by the scheduler
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error trying to interrupt the {@link Activity}
     */
    public void interruptActivity(final Activity activity) throws ActivitySchedulerException {
        interruptActivity(activity.getName());
    }

    /**
     * Interrupt an {@link Activity} that is registered on the scheduler.
     *
     * @param activityName
     *            the name of the {@link Activity} used to be interrupted by the scheduler
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error trying to interrupt the {@link Activity}
     */
    public void interruptActivity(final String activityName) throws ActivitySchedulerException {
        final JobKey activityKey = new JobKey(activityName);
        try {
            if (Holder.SCHEDULER.checkExists(activityKey)) {
                Holder.SCHEDULER.interrupt(activityKey);
                log.info("Activity '{}' has been interrupted by the scheduler", activityName);
            } else {
                log.warn("Activity '{}' does not exist and cannot be interrupted", activityName);
            }
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException(String.format("Unable to interrupt activity '%s' from the scheduler", activityName), e);
        }
    }

    /**
     * Returns a {@code List} of all job names.
     *
     * @return a {@code List} of all job names
     * @throws ActivitySchedulerException
     *             if there is an internal scheduler error
     */
    public List<String> getAllJobNames() throws ActivitySchedulerException {
        final List<String> results = new ArrayList<>();
        try {
            for (final String groupName : Holder.SCHEDULER.getJobGroupNames()) {
                for (final JobKey jobKey : Holder.SCHEDULER.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    results.add(jobKey.getName());
                }
            }
        } catch (final SchedulerException e) {
            throw new ActivitySchedulerException("Unable to retrieve groupNames", e);
        }
        return results;
    }
}
