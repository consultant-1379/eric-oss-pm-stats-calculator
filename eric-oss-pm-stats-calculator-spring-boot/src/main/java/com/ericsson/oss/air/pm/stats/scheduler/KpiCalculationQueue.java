/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import java.util.ArrayDeque;
import java.util.Queue;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.service.metric.KpiMetric;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;
import com.ericsson.oss.air.pm.stats.utils.KpiCalculationJobUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This Class implements queueing strategy for KPI Calculations.
 */
@Slf4j
public class KpiCalculationQueue {

    /*  TODO Consider to review get and peek methods' access properties.
        Consider creating a separate class to get jobs from queues (eg with name KpiCalculationQueueExtractor) to control access to queues
    */

    private final KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry;

    private final Queue<KpiCalculationJob> onDemandKpiCalculationJobs = new ArrayDeque<>();
    private final Queue<KpiCalculationJob> scheduledComplexKpiCalculationJobs = new ArrayDeque<>();
    private final Queue<KpiCalculationJob> scheduledSimpleKpiCalculationJobs = new ArrayDeque<>();

    private Boolean isServingOnDemandNow;
    private int onDemandCalculationCounter;
    private int scheduleSimpleCalculationCounter;
    private final int onDemandCalculationWeight;
    private final int scheduledCalculationWeight;

    public KpiCalculationQueue(final int onDemandCalculationWeight, final int scheduledCalculationWeight, final KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry) {
        this.kpiSchedulerMetricRegistry = kpiSchedulerMetricRegistry;
        this.onDemandCalculationWeight = onDemandCalculationWeight;
        this.scheduledCalculationWeight = scheduledCalculationWeight;
        onDemandCalculationCounter = this.onDemandCalculationWeight;
        scheduleSimpleCalculationCounter = this.scheduledCalculationWeight;

        kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT, onDemandCalculationCounter);
        kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE_REMAINING_WEIGHT, scheduleSimpleCalculationCounter);

        isServingOnDemandNow = this.onDemandCalculationWeight >= this.scheduledCalculationWeight;
    }

    /**
     * Inserts the specified {@link KpiCalculationJob} element into this queue.
     *
     * @param job the element to add
     */
    public void add(final KpiCalculationJob job) {
        if (job.isOnDemand()) {
            onDemandKpiCalculationJobs.add(job);
            log.info("Added calculation to on-demand queue: {} with kpis: {}", job.getCalculationId(), job.getKpiDefinitionNames());
            kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE);
        } else if (job.isComplex()) {
            scheduledComplexKpiCalculationJobs.add(job);
            kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.SCHEDULED_COMPLEX_CALCULATION_QUEUE);
            log.info("Added calculation to complex queue: {} with kpis: {}", job.getCalculationId(), job.getKpiDefinitionNames());
        } else {
            scheduledSimpleKpiCalculationJobs.add(job);
            log.info("Added calculation to simple queue: {} with kpis: {}", job.getCalculationId(), job.getKpiDefinitionNames());
            kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE);
        }
    }

    /**
     * To get next element from the queue. Returns null if queue is empty.
     *
     * @return next {@link KpiCalculationJob} element from the queue
     */
    public KpiCalculationJob poll() {
        if (isServingOnDemandNow) {
            final KpiCalculationJob onDemandCalculationJob = getOnDemandCalculationJob();
            return onDemandCalculationJob == null ? getScheduledSimpleCalculationJob() : onDemandCalculationJob;
        } else {
            final KpiCalculationJob scheduledSimpleCalculationJob = getScheduledSimpleCalculationJob();
            return scheduledSimpleCalculationJob == null ? getOnDemandCalculationJob() : scheduledSimpleCalculationJob;
        }
    }

    private KpiCalculationJob getOnDemandCalculationJob() {
        if (onDemandKpiCalculationJobs.isEmpty()) {
            return null;
        }
        onDemandCalculationCounter -= 1;
        kpiSchedulerMetricRegistry.decrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT);
        kpiSchedulerMetricRegistry.decrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE);
        if (onDemandCalculationCounter == 0) {
            isServingOnDemandNow = false;
            onDemandCalculationCounter = onDemandCalculationWeight;
            kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT, onDemandCalculationCounter);
        }
        return onDemandKpiCalculationJobs.poll();
    }

    private KpiCalculationJob getScheduledSimpleCalculationJob() {
        if (scheduledSimpleKpiCalculationJobs.isEmpty()) {
            return null;
        }
        scheduleSimpleCalculationCounter -= 1;
        kpiSchedulerMetricRegistry.decrementKpiMetric(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE_REMAINING_WEIGHT);
        kpiSchedulerMetricRegistry.decrementKpiMetric(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE);
        if (scheduleSimpleCalculationCounter == 0) {
            isServingOnDemandNow = true;
            scheduleSimpleCalculationCounter = scheduledCalculationWeight;
            kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE_REMAINING_WEIGHT, scheduleSimpleCalculationCounter);
        }
        return scheduledSimpleKpiCalculationJobs.poll();
    }

    public KpiCalculationJob getScheduledComplexCalculationJob() {
        if (!scheduledComplexKpiCalculationJobs.isEmpty()) {
            kpiSchedulerMetricRegistry.decrementKpiMetric(KpiMetric.SCHEDULED_COMPLEX_CALCULATION_QUEUE);
        }

        return scheduledComplexKpiCalculationJobs.poll();
    }

    public KpiCalculationJob peekScheduledComplexCalculationJob() {
        return scheduledComplexKpiCalculationJobs.peek();
    }

    public KpiCalculationJob peekScheduledSimpleCalculationJob() {
        return scheduledSimpleKpiCalculationJobs.peek();
    }

    public boolean isComplexQueueEmpty() {
        return scheduledComplexKpiCalculationJobs.isEmpty();
    }

    public boolean isSimpleQueueEmpty() {
        return scheduledSimpleKpiCalculationJobs.isEmpty();
    }

    public void clearComplexCalculationQueue() {
        scheduledComplexKpiCalculationJobs.clear();
    }

    public boolean isSimpleQueued(final String executionGroup) {
        return KpiCalculationJobUtil.doesContainExecutionGroup(scheduledSimpleKpiCalculationJobs, executionGroup);
    }
}