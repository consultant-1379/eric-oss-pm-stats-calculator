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

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.STARTED;
import static lombok.AccessLevel.PUBLIC;

import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.api.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculation.readiness.ReadinessWindowCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.calculation.readiness.window.ReadinessWindowPrinter;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation.CalculationBuilder;
import com.ericsson.oss.air.pm.stats.scheduler.finalize.HangingCalculationFinalizer;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.metric.KpiMetric;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;
import com.ericsson.oss.air.pm.stats.utils.KpiCalculationJobUtil;
import com.ericsson.oss.air.pm.stats.utils.KpiCalculatorJmxPortPool;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This Class manages the execution of KPI Calculations.
 */
@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiCalculationExecutionController {
    private final Map<UUID, KpiCalculationJob> runningCalculations = new HashMap<>();
    private Queue<String> sparkJmxPortQueue;
    private int onDemandCalculationCount;

    @Value("${pm-stats-calculator.queue-weight-on-demand-calculation}")
    private Integer queueWeightOnDemandCalculation;
    @Value("${pm-stats-calculator.queue-weight-scheduled-calculation}")
    private Integer queueWeightScheduledCalculation;
    @Value("${pm-stats-calculator.maximum-concurrent-calculations}")
    private Integer maximumConcurrentCalculations;
    @Value("${pm-stats-calculator.spark-executor-starting-port}")
    private Integer sparkExecutorStartingPort;

    @Delegate
    private KpiCalculationQueue kpiCalculationQueue;
    @Inject
    private KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry;
    @Inject
    private KpiCalculator kpiCalculator;
    @Inject
    private CalculationService calculationService;
    @Inject
    private ReadinessWindowCollector readinessWindowCollector;
    @Inject
    private ReadinessWindowPrinter readinessWindowPrinter;
    @Inject
    private HangingCalculationFinalizer hangingCalculationFinalizer;

    @PostConstruct
    void init() {
        kpiCalculationQueue = new KpiCalculationQueue(queueWeightOnDemandCalculation, queueWeightScheduledCalculation, kpiSchedulerMetricRegistry);
        sparkJmxPortQueue = KpiCalculatorJmxPortPool.createJmxPortMapping(sparkExecutorStartingPort, maximumConcurrentCalculations);
    }

    /**
     * To get current on demand calculations.
     *
     * @return current on demand calculations
     */
    @Lock(LockType.READ)
    public int getCurrentOnDemandCalculationCount() {
        return onDemandCalculationCount;
    }

    /**
     * Schedules new kpi calculation. After putting the calculation job on the queue, if maximum concurrent calculations limit is not reached, new
     * calculation will be started.
     *
     * @param job {@link KpiCalculationJob} job to be scheduled
     */
    @Lock(LockType.WRITE)
    public void scheduleCalculation(final KpiCalculationJob job) {
        if (job.isOnDemand()) {
            onDemandCalculationCount++;
            kpiSchedulerMetricRegistry.incrementKpiMetric(KpiMetric.CURRENT_ON_DEMAND_CALCULATION);
        }
        kpiCalculationQueue.add(job);
        executeCalculation(() -> kpiCalculationQueue.poll());
    }

    /**
     * Removes a calculation from running calculations list.
     *
     * @param calculationId Calculation to be removed.
     */
    @Lock(LockType.WRITE)
    public void removeRunningCalculationAndStartNext(final UUID calculationId) {
        removeCalculation(calculationId);
        executeCalculation(() -> kpiCalculationQueue.poll());
    }

    private void removeCalculation(UUID calculationId) {
        log.info("Remove Running Calculation: {}", calculationId);
        if (runningCalculations.get(calculationId).isOnDemand()) {
            onDemandCalculationCount--;
            kpiSchedulerMetricRegistry.decrementKpiMetric(KpiMetric.CURRENT_ON_DEMAND_CALCULATION);
        }
        final KpiCalculationJob job = runningCalculations.remove(calculationId);
        sparkJmxPortQueue.add(job.getJmxPort());
        runningCalculations.remove(calculationId);
    }

    @Lock(LockType.WRITE)
    public void executeComplexCalculation() {
        executeCalculation(() -> kpiCalculationQueue.getScheduledComplexCalculationJob());
    }

    /**
     * Executes a kpi calculation job polling it from the queue, if maximum concurrent calculations limit is not reached.
     *
     * @param jobSupplier Supplier of calculation job
     */
    public void executeCalculation(final Supplier<KpiCalculationJob> jobSupplier) {
        if (runningCalculations.size() == maximumConcurrentCalculations) {
            log.info("Could not start calculation as maximum allowed number of calculations were reached");
            return;
        }
        final KpiCalculationJob job = jobSupplier.get();

        if (job == null) {
            return;
        }

        if (job.isComplex()) {
            final Map<DefinitionName, List<ReadinessWindow>> readinessLogs = readinessWindowCollector.collect(job.getExecutionGroup(), job.getKpiDefinitionNames());
            readinessWindowPrinter.logReadinessWindows(job.getExecutionGroup(), readinessLogs);
        }

        if (!job.isOnDemand()) { //we have already persisted ON_DEMAND at KpiCalculationRequestHandler.java
            final Calculation calculation = calculation(job, STARTED);
            try {
                calculationService.save(calculation);
            } catch (final SQLException e) {
                throw new KpiPersistenceException(String.format("Unable to persist state job '%s'", job), e);
            }
        }

        log.info("Start executing calculation: {} with kpis: {}", job.getCalculationId(), job.getKpiDefinitionNames());
        job.setJmxPort(getNextSparkJmxPort());
        runningCalculations.put(job.getCalculationId(), job);
        kpiCalculator.calculateKpis(job);
    }

    @Lock(LockType.READ)
    public boolean isSimpleQueuedOrRunning(final String executionGroup) {
        final boolean isRunning = KpiCalculationJobUtil.doesContainExecutionGroup(runningCalculations.values(), executionGroup);
        final boolean isQueued = isSimpleQueued(executionGroup);
        return isRunning || isQueued;
    }

    @Lock(LockType.WRITE)
    public void checkHangingCalculations() {
        final List<UUID> toFinalize = hangingCalculationFinalizer.finalizeHangingCalculationsAfter(Duration.ofHours(1));
        toFinalize.forEach(this::removeCalculation);
    }

    private String getNextSparkJmxPort() {
        return sparkJmxPortQueue.remove();
    }

    private Calculation calculation(final KpiCalculationJob job, final KpiCalculationState kpiCalculationState) {
        final CalculationBuilder builder = Calculation.builder();
        builder.withCalculationId(job.getCalculationId());
        builder.withTimeCreated(job.getTimeCreated().toLocalDateTime());
        builder.withKpiCalculationState(kpiCalculationState);
        builder.withExecutionGroup(job.getExecutionGroup());
        builder.withKpiType(job.getJobType());
        return builder.build();
    }
}