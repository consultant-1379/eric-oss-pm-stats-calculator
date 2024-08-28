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
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationExecutionController;
import com.ericsson.oss.air.pm.stats.service.facade.RunningCalculationDetectorFacade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;

/**
 * Implementation of abstract class {@link Activity}, used with {@link ActivityScheduler} to schedule Complex KPIs.
 */
@Slf4j
@ApplicationScoped
@AllArgsConstructor(access = PUBLIC)
@DisallowConcurrentExecution
public class KpiComplexSchedulerActivity extends ScheduleActivity {

    private static final String ACTIVITY_NAME_CONTEXT_KEY = "activityName";
    private static final String ACTIVITY_RUN_FREQ_CONTEXT_KEY = "activityRunFrequency";
    private static final String ACTIVITY_NAME = "Activity_complexKpiScheduler";

    @Inject
    private KpiCalculationExecutionController kpiCalculationExecutionController;
    @Inject
    private RunningCalculationDetectorFacade runningCalculationDetector;

    /**
     * Default constructor. Any class extending {@link Activity} must have a no parameter constructor in order to support the Quartz
     * {@link org.quartz.Scheduler}. Any activity related data should be passed in the activityContext {@link Map} in the constructor.
     */
    public KpiComplexSchedulerActivity() {
    }

    /**
     * Constructor with the params.
     *
     * @param name {@link String} unique name of the activity.
     * @param map  {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    private KpiComplexSchedulerActivity(final String name, final Map<String, Object> map) {
        super(name, map);
    }

    /**
     * Creates an instance.
     *
     * @param activityRunFrequency activity run frequency
     * @return an instance
     */
    public static KpiComplexSchedulerActivity of(final String activityRunFrequency) {
        final Map<String, Object> paramsMap = new HashMap<>(4);
        paramsMap.put(ACTIVITY_NAME_CONTEXT_KEY, ACTIVITY_NAME);
        paramsMap.put(ACTIVITY_RUN_FREQ_CONTEXT_KEY, activityRunFrequency);

        return new KpiComplexSchedulerActivity(ACTIVITY_NAME, paramsMap);
    }

    @Override
    public void run(final JobDataMap activityContext) {
        if (isComplexKpiExecutable()) {
            kpiCalculationExecutionController.executeComplexCalculation();
        }
    }

    private boolean isComplexKpiExecutable() {
        if (kpiCalculationExecutionController.isComplexQueueEmpty()) {
            log.info("No scheduled Complex KPI has been found in the queue to start");
            return false;
        }

        if (!kpiCalculationExecutionController.isSimpleQueueEmpty()) {
            log.info("Scheduled simple KPI has been found in the queue, no complex KPI calculation has started");
            return false;
        }

        if (runningCalculationDetector.isAnySimpleCalculationRunning()) {
            log.info("Simple KPI has been found that is started or running, no complex KPI calculation has started");
            return false;
        }

        if (runningCalculationDetector.isAnyComplexCalculationRunning()) {
            log.info("Complex KPI has been found that is started or running, no complex KPI calculation has started to avoid change in order");
            return false;
        }

        return true;
    }
}