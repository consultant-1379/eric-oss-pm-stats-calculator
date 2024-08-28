/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.activity;

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.scheduler.Activity;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionFacade;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionManager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

/**
 * Implementation of abstract class {@link Activity}, used with {@link ActivityScheduler} to create the scheduling of the PM Stats Calculator clean up job.
 * <p>
 * When this activity runs it will clean up all data from <code>RETENTION_PERIOD_TABLES</code> which are beyond <code>RETENTION_PERIOD_DAYS</code>
 * </P>
 */
@Slf4j
@ApplicationScoped
@AllArgsConstructor(access = PUBLIC)
public class KpiCalculatorRetentionActivity extends Activity {
    private static final String RETENTION_ACTIVITY_NAME = "Activity_kpiCalculatorRetention";
    private static final String RETENTION_PERIOD = "retentionPeriod";

    @Inject
    private PartitionRetentionFacade partitionRetentionFacade;
    @Inject
    private CalculationService calculationService;
    @Inject
    private PartitionRetentionManager partitionRetentionManager;

    /**
     * Default constructor. Any class extending {@link Activity} must have a no parameter constructor in order to support the Quartz
     * {@link org.quartz.Scheduler}. Any activity related data should be passed in the activityContext {@link Map} in the constructor.
     */
    public KpiCalculatorRetentionActivity() {
        super();
    }

    /**
     * Constructor with the params.
     *
     * @param name    {@link String} name of the activity.
     * @param context {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    private KpiCalculatorRetentionActivity(final String name, final Map<String, Object> context) {
        super(name, context);
    }

    /**
     * Creates an instance.
     *
     * @param retentionPeriod {@link Integer} of the retention period.
     * @return an instance
     */
    public static KpiCalculatorRetentionActivity of(final Integer retentionPeriod) {
        final Map<String, Object> paramsMap = new HashMap<>(2);
        paramsMap.put(RETENTION_PERIOD, retentionPeriod);
        return new KpiCalculatorRetentionActivity(RETENTION_ACTIVITY_NAME, paramsMap);
    }


    @Override
    public void run(final JobDataMap activityContext) {
        log.info("PM Stats Calculator retention service started");

        partitionRetentionFacade.runRetention();
        cleanUpCalculations(activityContext);

        log.info("PM Stats Calculator retention service concluded");
    }

    private void cleanUpCalculations(JobDataMap activityContext) {
        Integer defaultRetentionPeriodInDays = (Integer) activityContext.get(RETENTION_PERIOD);
        LocalDateTime retentionDate = partitionRetentionManager.getRetentionDate(defaultRetentionPeriodInDays);
        final long deletedCalculations = calculationService.deleteByTimeCreatedLessThen(retentionDate);
        log.info("Deleted calculation: '{}'", deletedCalculations);
    }
}