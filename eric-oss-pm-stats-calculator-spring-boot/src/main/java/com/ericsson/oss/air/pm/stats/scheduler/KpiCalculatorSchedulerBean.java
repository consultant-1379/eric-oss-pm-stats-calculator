/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityRunner;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.configuration.scheduler.SchedulerProducer.Scheduler;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorActivity;
import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculatorScheduler;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link KpiCalculatorScheduler}.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Stateless(name = "kpiCalculatorSchedulerBean")
public class KpiCalculatorSchedulerBean implements KpiCalculatorScheduler {
    private static final String KPI_CALCULATOR_ACTIVITY_CONTEXT_ENTRY = "kpiCalculator";

    @Inject
    @Scheduler
    private ActivityScheduler activityScheduler;
    @Inject
    private ActivityRunner activityRunner;

    /**
     * Schedule the PM Stats Calculator based on the execution group if new type of calculation is added,
     * it should be put to map.
     *
     * @param executionPeriod the execution period for KPI calculation.
     */
    @Override
    @Asynchronous
    public void scheduleKpiCalculation(final String executionPeriod) throws ActivitySchedulerException {
        final KpiCalculatorActivity kpiCalculationActivity = KpiCalculatorActivity.of(executionPeriod);
        final String activityName = kpiCalculationActivity.getName();
        final boolean activityExists = activityScheduler.activityExists(kpiCalculationActivity);

        if (!activityExists) {
            log.info("Scheduling Activity: {}", activityName);
            activityRunner.scheduleWithCronExpression(kpiCalculationActivity, executionPeriod,
                    KPI_CALCULATOR_ACTIVITY_CONTEXT_ENTRY);
        }
    }
}
