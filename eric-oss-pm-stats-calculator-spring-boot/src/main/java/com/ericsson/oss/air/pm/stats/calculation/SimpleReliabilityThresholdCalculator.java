/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.ReliabilityThresholdTemporalAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ReadinessLogRepository;

import com.google.common.collect.Iterators;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class used to calculate Reliability Threshold for simple KPIs.
 */
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SimpleReliabilityThresholdCalculator implements ReliabilityThresholdCalculator {
    @Inject
    private ReadinessLogRepository readinessLogRepository;
    @Inject
    private CalculationRepository calculationRepository;
    @Inject
    private KpiDefinitionRepository kpiDefinitionRepository;
    @Inject
    private ReliabilityThresholdTemporalAdjuster reliabilityThresholdTemporalAdjuster;

    @Override
    public Map<String, LocalDateTime> calculateReliabilityThreshold(final UUID kpiCalculationId) {
        final List<ReadinessLog> readinessLogs = readinessLogRepository.findByCalculationId(kpiCalculationId);
        final String executionGroup = calculationRepository.forceFetchExecutionGroupByCalculationId(kpiCalculationId);
        final List<KpiDefinitionEntity> kpiDefinitions = kpiDefinitionRepository.findKpiDefinitionsByExecutionGroup(executionGroup);

        return kpiDefinitions.stream().collect(Collectors.toMap(
                KpiDefinitionEntity::name,
                kpiDefinition -> reliabilityThresholdTemporalAdjuster.calculate(
                        deduceReadinessLog(readinessLogs, kpiDefinition),
                        kpiDefinition)));
    }

    @Override
    public boolean doesSupport(final KpiType kpiType) {
        return kpiType == KpiType.SCHEDULED_SIMPLE;
    }

    @Override
    public boolean supports(final KpiType kpiType) {
        return kpiType == KpiType.SCHEDULED_SIMPLE;
    }

    private static ReadinessLog deduceReadinessLog(@NonNull final List<? extends ReadinessLog> readinessLogs, final KpiDefinitionEntity kpiDefinition) {
        /* We expect to find matching readiness log for the KPI Definition - Java Stream API would require to handle Optional */
        return Iterators.find(readinessLogs.iterator(), readinessLog -> readinessLog.hasSameDatasource(kpiDefinition));
    }
}
