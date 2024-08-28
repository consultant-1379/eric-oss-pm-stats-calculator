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

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityRunner;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiComplexSchedulerActivity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Startup bean for {@code eric-oss-pm-stats-calculator} Complex KPI scheduler.
 * <p>
 * Schedules the activity responsible to schedule Complex KPIs .
 */
@Slf4j
@Startup
@Singleton
@NoArgsConstructor
@AllArgsConstructor
@DependsOn({"KpiStartupService", "KpiExposureStartupService", "KpiRetentionStartupService", "FlywayIntegration"})
public class KpiComplexSchedulerStartupService {
    @Inject
    private ActivityRunner activityRunner;

    /**
     * On initialization, creates the complex KPI scheduler
     */
    @PostConstruct
    public void onServiceStart() {
        log.info("Starting eric-oss-pm-stats-calculator Complex Kpi Scheduling Service");

        final String ACTIVITY_CRON = "0/5 * * * * ?";
        final KpiComplexSchedulerActivity kpiComplexSchedulerActivity = KpiComplexSchedulerActivity.of(ACTIVITY_CRON);

        scheduleActivity(kpiComplexSchedulerActivity, ACTIVITY_CRON);

        log.info("Kpi Complex Scheduler activity scheduled: {}", kpiComplexSchedulerActivity.getName());
    }

    @SneakyThrows(ActivitySchedulerException.class)
    private void scheduleActivity(final KpiComplexSchedulerActivity kpiComplexSchedulerActivity, final String activityCron) {
        final String ACTIVITY_CONTEXT_ENTRY = "complexKpiScheduler";
        activityRunner.scheduleWithCronExpression(kpiComplexSchedulerActivity, activityCron,
                ACTIVITY_CONTEXT_ENTRY);
    }
}
