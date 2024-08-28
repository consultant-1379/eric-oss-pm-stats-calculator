/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.activity;

import static lombok.AccessLevel.PUBLIC;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.scheduler.Activity;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.scheduler.ScheduleActivity;
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationJobScheduler;
import com.ericsson.oss.air.pm.stats.scheduler.priority.CalculationPriorityRanker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

/**
 * Implementation of abstract class {@link Activity}, used with {@link ActivityScheduler} to create the scheduling of the KPI calculation.
 */
@Slf4j
@ApplicationScoped
@AllArgsConstructor(access = PUBLIC)
public class KpiCalculatorActivity extends ScheduleActivity {
    private static final String ACTIVITY_NAME_CONTEXT_KEY = "activityName";
    private static final String EXECUTION_PERIOD_CONTEXT_KEY = "executionPeriod";
    private static final String ACTIVITY_NAME = "Activity_kpiScheduler";

    @Inject
    private KpiCalculationJobScheduler calculationJobScheduler;
    @Inject
    private CalculationPriorityRanker calculationPriorityRanker;

    /**
     * Default constructor. Any class extending {@link Activity} must have a no parameter constructor in order to support the Quartz
     * {@link org.quartz.Scheduler}. Any activity related data should be passed in the activityContext {@link Map} in the constructor.
     */
    public KpiCalculatorActivity() {
    }

    /**
     * Constructor with the params.
     *
     * @param name {@link String} unique identifier of the activity.
     * @param map  {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    private KpiCalculatorActivity(final String name, final Map<String, Object> map) {
        super(name, map);
    }

    /**
     * Creates an instance.
     *
     * @param executionPeriod the calculation frequency
     * @return an instance
     */
    public static KpiCalculatorActivity of(final String executionPeriod) {
        final Map<String, Object> paramsMap = new HashMap<>(3);
        paramsMap.put(ACTIVITY_NAME_CONTEXT_KEY, ACTIVITY_NAME);
        paramsMap.put(EXECUTION_PERIOD_CONTEXT_KEY, executionPeriod);

        return new KpiCalculatorActivity(ACTIVITY_NAME, paramsMap);
    }

    @Override
    public void run(final JobDataMap activityContext) {
        if (calculationPriorityRanker.isOnDemandPrioritized()) {
            log.warn("Skipping calculation scheduling, a prioritized on demand KPI calculation is in progress");
            return;
        }
        calculationJobScheduler.scheduleCalculations();
    }

}