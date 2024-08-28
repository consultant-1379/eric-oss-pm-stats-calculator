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

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.common.scheduler.CronSchedule;
import com.ericsson.oss.air.pm.stats.configuration.scheduler.SchedulerProducer.Scheduler;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiMetricUpdaterActivity;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Start reporting Api metrics via JMX.
 */
@Slf4j
@Startup
@Singleton
@DependsOn("KpiStartupService")
@AllArgsConstructor
@NoArgsConstructor
public class KpiMetricExposureStartupService {
    private static final String KPI_METRIC_UPDATE_SCHEDULER = "kpi_metric_update_scheduler";
    // TODO: This should be configurable.
    private static final String KPI_METRIC_CRON = "0/15 * * * * ?";

    @Inject
    private ApiMetricRegistry apiMetricRegistry;
    @Inject
    private KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry;

    @Inject
    @Scheduler
    private ActivityScheduler activityScheduler;

    /**
     * Start reporting Api metrics via JMX.
     */
    @PostConstruct
    public void onServiceStart() {
        log.info("KPI metric exposure service initialization started");

        startApiMetricReporter();
        startKpiSchedulerMetricReporter();
        scheduleKpiMetricUpdaterActivity();

        log.info("KPI metric exposure service initialization complete");
    }

    private void scheduleKpiMetricUpdaterActivity() {
        log.info("Scheduling KPI metric updater activity");

        try {
            final KpiMetricUpdaterActivity activity = KpiMetricUpdaterActivity.of(KPI_METRIC_CRON);
            final String activityName = activity.getName();
            final boolean activityExists = activityScheduler.activityExists(activity);

            if (!activityExists) {
                final CronSchedule cronSchedule = CronSchedule.of(KPI_METRIC_UPDATE_SCHEDULER, KPI_METRIC_CRON);
                log.info("Adding activity '{}' to CRON scheduler job at: '{}'", activityName, cronSchedule);
                activityScheduler.addCronScheduleForActivity(activity, cronSchedule);
            }
        } catch (final ActivitySchedulerException e) {
            throw new StartupException(e);
        }
    }

    private void startApiMetricReporter() {
        log.info("Starting API metric reporter");
        apiMetricRegistry.getJmxReporter().start();
    }

    private void startKpiSchedulerMetricReporter() {
        log.info("Starting KPI scheduler metric reporter");
        kpiSchedulerMetricRegistry.getJmxReporter().start();
    }
}
