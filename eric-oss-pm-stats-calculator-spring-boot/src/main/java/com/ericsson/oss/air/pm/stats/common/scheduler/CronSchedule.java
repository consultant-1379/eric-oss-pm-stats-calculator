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

import java.text.ParseException;
import java.util.Map;

import lombok.Getter;
import org.quartz.CronExpression;

/**
 * Extension of the {@link Schedule} abstract class with scheduling information based on the CRON format. The CRON expression must be specified with
 * respect to UTC time.
 */
public final class CronSchedule extends Schedule {
    public static final String OSS_TIME_ZONE = "UTC";
    @Getter
    private final String cronExpression;


    /**
     * Extension of {@link Schedule} with support for scheduling in CRON format.
     *
     * @param name
     *            see {@link Schedule#Schedule(String, Map)}
     * @param context
     *            see {@link Schedule#Schedule(String, Map)}
     * @param cronExpression
     *            schedule defined in CRON format specified in UTC time
     * @see CronExpression
     * @throws ActivitySchedulerException
     *             thrown when the provided {@link #cronExpression} is not valid
     */
    public CronSchedule(final String name, final Map<String, Object> context, final String cronExpression) throws ActivitySchedulerException {
        super(name, context);

        if (!DefinedCronSchedule.NEVER.getCronSchedule().equalsIgnoreCase(cronExpression)) {
            validateCronExpression(cronExpression);
        }
        this.cronExpression = cronExpression;
    }

    public static CronSchedule of(final String name, final String cronExpression) throws ActivitySchedulerException {
        return new CronSchedule(name, null, cronExpression);
    }

    private static void validateCronExpression(final String cronExpression) throws ActivitySchedulerException {
        try {
            CronExpression.validateExpression(cronExpression);
        } catch (final ParseException e) {
            throw new ActivitySchedulerException(String.format("CRON expression '%s' is not valid: %s", cronExpression, e.getMessage()), e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s:: {name: '%s', context: '%s', cronExpression: '%s', OSS_TIME_ZONE: '%s'}", getClass().getSimpleName(), getName(),
                getContext(), cronExpression, OSS_TIME_ZONE);
    }
}
