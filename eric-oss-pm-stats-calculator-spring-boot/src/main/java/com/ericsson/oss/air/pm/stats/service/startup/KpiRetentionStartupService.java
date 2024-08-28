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

import java.time.Duration;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.scheduler.Activity;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.common.scheduler.CronSchedule;
import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.configuration.scheduler.SchedulerProducer.Scheduler;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorRetentionActivity;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used to manage schedulers for {@link KpiCalculatorRetentionActivity} instance. Can create new activities that will be scheduled to run.
 */
@Slf4j
@Startup
@Singleton
@NoArgsConstructor
@AllArgsConstructor
@DependsOn({"KpiStartupService", "FlywayIntegration"})
public class KpiRetentionStartupService {
    private static final String KPI_CALCULATOR_RETENTION_SCHEDULER = "kpi_calculator_retention_scheduler";

    @Inject
    private DatabaseService databaseService;

    @Inject
    @Scheduler
    private ActivityScheduler activityScheduler;

    @Inject
    private EnvironmentValue<Duration> retentionPeriodDays;

    @Inject
    private EnvironmentValue<String> cronRetentionPeriodCheck;

    /**
     * On initialization, creates the scheduler for retention period.
     *
     * @implNote If {@link StartupException} is thrown then the deployment fails not allowing
     * to deploy the application without proper retention control.
     */
    @PostConstruct
    public void init() {
        try {
            scheduleKpiCalculatorRetentionPeriod();
        } catch (final ActivitySchedulerException e) {
            throw new StartupException(String.format("Error while scheduling retention period for cron: '%s' tables:%n%s",
                    cronRetentionPeriodCheck.value(),
                    databaseService.findAllCalculationOutputTables()
                            .stream()
                            .collect(Collectors.joining(System.lineSeparator()))),
                    e);
        }
    }

    /**
     * PM Stats Calculator retention activity based on the clean up cron schedule time from the environment variable. Creates a {@link CronSchedule} object
     * with the scheduler cron time to trigger an PM Stats Calculator retention period job. Use the {@link ActivityScheduler} to add a Cron schedule for
     * the created {@link Activity}
     *
     * @throws ActivitySchedulerException thrown if there is an error scheduling the activity.
     */
    private void scheduleKpiCalculatorRetentionPeriod() throws ActivitySchedulerException {
        log.info("Scheduling PM Stats Calculator retention period");

        final Activity activity = KpiCalculatorRetentionActivity.of((int) retentionPeriodDays.value().toDays());

        if (activityScheduler.activityExists(activity)) {
            log.info("Removing existing scheduled activity '{}'", activity.getName());
            activityScheduler.removeActivity(activity);
        }

        final CronSchedule cronSchedule = CronSchedule.of(KPI_CALCULATOR_RETENTION_SCHEDULER, cronRetentionPeriodCheck.value());
        log.info("Adding activity '{}' to CRON scheduler job at: '{}'", activity.getName(), cronSchedule);
        activityScheduler.addCronScheduleForActivity(activity, cronSchedule);
    }

}
