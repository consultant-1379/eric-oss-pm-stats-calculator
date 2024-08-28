/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import static lombok.AccessLevel.PUBLIC;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.CalculationLimitCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.CalculationLimit;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.ComplexExecutionGroupOrderFacade;
import com.ericsson.oss.air.pm.stats.scheduler.heartbeatmanager.HeartbeatManager;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.facade.KafkaOffsetCheckerFacade;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements methods related to scheduling calculation jobs in <code>eric-oss-pm-stats-calculator</code>.
 */
@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiCalculationJobScheduler {

    @Inject
    private CalculationLimitCalculator calculationLimitCalculator;
    @Inject
    private ComplexExecutionGroupOrderFacade complexExecutionGroupOrderFacade;
    @Inject
    private KpiCalculationExecutionController kpiCalculationExecutionController;
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private HeartbeatManager heartbeatManager;
    @Inject
    private KafkaOffsetCheckerFacade kafkaOffsetCheckerFacade;
    @Inject
    private EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples;

    /**
     * Schedule simple and complex calculation jobs in <code>eric-oss-pm-stats-calculator</code>.
     */
    public void scheduleCalculations() {
        kpiCalculationExecutionController.checkHangingCalculations();

        int heartbeatCounter = heartbeatManager.getHeartbeatCounter().get();
        log.info("Start heartbeat processing with maxHeartbeatToWaitToRecalculateSimples: {}, heartbeat counter: {}", maxHeartbeatToWaitToRecalculateSimples, heartbeatCounter);

        if (heartbeatManager.canSimplesWait()) {
            if (kpiCalculationExecutionController.isComplexQueueEmpty()) {
                scheduleSimpleCalculations();
                scheduleComplexCalculations();
            } else {
                log.info("Complex calculations took longer than the heartbeat, heartbeat is skipped now");
                heartbeatManager.incrementHeartbeatCounter();
            }
        } else {
            log.info("Max heartbeat to wait to recalculate simples has reached. Simple calculations will start after the current complex job");
            kpiCalculationExecutionController.clearComplexCalculationQueue();
            scheduleSimpleCalculations();
        }
    }

    private void scheduleSimpleCalculations() {
        final Map<String, List<String>> simpleKpisByExecGroups = kpiDefinitionService.findAllSimpleKpiNamesGroupedByExecGroups();
        for (final Map.Entry<String, List<String>> entry : new TreeMap<>(simpleKpisByExecGroups).entrySet()) {
            String executionGroup = entry.getKey();
            List<String> simpleKpiNames = entry.getValue();

            if (kpiCalculationExecutionController.isSimpleQueuedOrRunning(executionGroup)) {
                log.info("Execution group: '{}' already in the queue or running, skipping from scheduling", executionGroup);
                continue;
            }

            if (kafkaOffsetCheckerFacade.hasNewMessage(executionGroup)) {
                log.info("Start scheduling simple KPIs. KPIs: '{}', executionGroup: '{}'", simpleKpiNames, executionGroup);
                scheduleCalculationJobs(executionGroup, simpleKpiNames, KpiType.SCHEDULED_SIMPLE);
            } else {
                log.info("Simple KPIs are not scheduled for executionGroup: '{}', as there is no data read from Kafka", executionGroup);
            }
        }
        heartbeatManager.resetHeartBeatCounter();
    }

    private void scheduleComplexCalculations() {
        if (kpiDefinitionService.countComplexKpi() == 0) {
            log.info("Complex KPIs are not scheduled, as there are not Complex KPIs in the database");
            return;
        }

        final Map<String, List<String>> complexKpisByExecGroups = kpiDefinitionService.findAllComplexKpiNamesGroupedByExecGroups();
        final Queue<String> complexCalculableGroupOrderList = complexExecutionGroupOrderFacade.sortCalculableComplexExecutionGroups();
        log.info("Order of the calculable complex group's: '{}'", complexCalculableGroupOrderList);

        complexCalculableGroupOrderList.forEach(complexGroup -> {
            final List<String> complexKpiNames = complexKpisByExecGroups.get(complexGroup);
            log.info("Start scheduling complex KPIs. KPIs: '{}', executionGroup: '{}'", complexKpiNames, complexGroup);
            scheduleCalculationJobs(complexGroup, complexKpiNames, KpiType.SCHEDULED_COMPLEX);
        });
    }

    private void scheduleCalculationJobs(final String executionGroup, final List<String> kpiNames, final KpiType jobType) {
        kpiCalculationExecutionController.scheduleCalculation(
                KpiCalculationJob.defaultsBuilder()
                        .withExecutionGroup(executionGroup)
                        .withKpiDefinitionNames(kpiNames)
                        .withJobType(jobType)
                        .withCalculationLimit(findCalculationLimit(executionGroup, jobType))
                        .build());
    }

    private CalculationLimit findCalculationLimit(final String executionGroup, final KpiType jobType) {
        return jobType == KpiType.SCHEDULED_COMPLEX
                ? calculationLimitCalculator.calculateLimit(executionGroup)
                : CalculationLimit.DEFAULT;
    }

}
