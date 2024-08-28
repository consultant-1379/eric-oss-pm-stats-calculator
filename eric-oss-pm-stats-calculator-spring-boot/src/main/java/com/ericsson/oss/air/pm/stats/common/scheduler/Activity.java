/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import java.util.Collections;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.common.scheduler.logging.ContextLogCreators;

import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * Abstract class which should be extended when scheduling a process to run. The logic for the process must be implemented in the run method. Any
 * class extending {@link Activity} <b>must</b> have a no parameter constructor in order to support the Quartz {@link org.quartz.Scheduler}. Any
 * activity related data should be passed in the activityContext {@link Map} in the constructor.
 * <p>
 * An {@link Activity} is internally identified by the name field. {@link Activity}s are durable within the {@link ActivityScheduler} in that they can
 * exist without an associated {@link Schedule}.
 * </p>
 */
@Slf4j
public abstract class Activity implements InterruptableJob {

    private final JobDataMap activityContext = new JobDataMap();

    private String name;

    /**
     * Any class extending {@link Activity} <b>must</b> have a no parameter constructor.
     */
    public Activity() {
    }

    /**
     * Any class extending {@link Activity} must have a constructor with a unique name for the {@link Activity}. Note that the name field is not
     * accessible in the run method as the Quartz {@link org.quartz.Scheduler} instantiates the {@link Activity} with the no parameter constructor.
     * Any values required by an {@link Activity} at run time should be supplied in the context {@link Map}.
     *
     * @param name
     *            a uniquely identifiable name for the given {@link Activity} extension
     * @param context
     *            a {@link Map} containing information required when the {@link Activity} is run. Values can be accessed within the #run method of the
     *            extending class
     */
    public Activity(final String name, final Map<String, Object> context) {
        this.name = name;
        activityContext.putAll(context == null ? Collections.emptyMap() : context);
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(activityContext.getWrappedMap());
    }

    /**
     * The business logic for a scheduled process should be implemented here. Any activity related data can be accessed from the activityContext
     * {@link JobDataMap}. Note that the name field is not accessible in the run method as the Quartz {@link org.quartz.Scheduler} instantiates the
     * {@link Activity} with the no parameter constructor.
     *
     * @param activityContext
     *            contains combined data from the activity data in the {@link Activity} implementation and the schedule specific data in the
     *            associated {@link CronSchedule}
     * @throws ActivityException
     *             exception to be thrown by the {@link Activity} implementation if the client wants the {@link ActivityScheduler} to handle the
     *             exception gracefully. A boolean can be provided in the exception to determine whether or not to re-fire the failed {@link Activity}
     */
    public abstract void run(JobDataMap activityContext) throws ActivityException;

    /**
     * This method is overridden from the Quartz {@link Job} interface which is invoked by the Quartz scheduler. It simply calls the
     * {@link Activity#run} method with the context {@link JobDataMap} provided in the {@link Activity} constructor.
     *
     * @param context
     *            contains the activity and schedule related information provided in the {@link Activity} and {@link Schedule} instances.
     * @throws JobExecutionException
     *             Quartz specific exception class handled by the Quartz {@link org.quartz.Scheduler}. Wraps the {@link ActivityException} class
     */
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final JobDataMap mergedActivityAndScheduleContext = context.getMergedJobDataMap();
        log.info(ContextLogCreators.createContextLog(mergedActivityAndScheduleContext));
        try {
            run(mergedActivityAndScheduleContext);
        } catch (final ActivityException e) {
            throw new JobExecutionException(e, e.isRefire());
        }
    }

    /**
     * This method is overridden from the Quartz {@link InterruptableJob} interface which is invoked by the Quartz scheduler.
     * By default the method throws an {@link UnableToInterruptJobException} exception.
     *
     * @throws UnableToInterruptJobException
     *             Quartz specific exception class handled by the Quartz {@link org.quartz.Scheduler}.
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        throw new UnableToInterruptJobException("Interrupt logic is not implemented.");
    }

    @Override
    public String toString() {
        return String.format("%s:: {name: '%s', context: '%s'}", getClass().getSimpleName(), name, activityContext);
    }
}
