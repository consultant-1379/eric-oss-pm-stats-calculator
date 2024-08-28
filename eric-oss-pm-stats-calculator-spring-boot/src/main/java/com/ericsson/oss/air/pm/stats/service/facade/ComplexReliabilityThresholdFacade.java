/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.ReadinessBoundCollector;
import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.AggregationPeriodCreatorRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.ComplexAggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability.CalculationReliabilityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ComplexReliabilityThresholdFacade {
    @Inject
    private CalculationReliabilityService calculationReliabilityService;
    @Inject
    private ReadinessBoundCollector readinessBoundCollector;
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private AggregationPeriodCreatorRegistryFacade aggregationPeriodCreatorRegistry;

    public void persistComplexReliabilityThreshold(@NonNull final KpiCalculationJob kpiCalculationJob) {
        if (kpiCalculationJob.isComplex()) {
            final UUID calculationId = kpiCalculationJob.getCalculationId();
            final String complexExecutionGroup = kpiCalculationJob.getExecutionGroup();
            final List<CalculationReliability> calculationReliabilities = getCalculationReliabilities(calculationId, complexExecutionGroup);
            if (calculationReliabilities.isEmpty()) {
                calculationService.updateCompletionState(calculationId, KpiCalculationState.NOTHING_CALCULATED);
                return;
            }
            calculationReliabilityService.save(calculationReliabilities);
        }
    }

    private List<CalculationReliability> getCalculationReliabilities(final UUID complexCalculationId, final String complexExecutionGroup) {
        final List<KpiDefinitionEntity> kpiDefinitions = kpiDefinitionService.findKpiDefinitionsByExecutionGroup(complexExecutionGroup);
        final Map<KpiDefinitionEntity, ReadinessBound> calculatedReadinessBound = readinessBoundCollector.calculateReadinessBounds(complexExecutionGroup, kpiDefinitions);
        final List<CalculationReliability> calculationReliabilities = new ArrayList<>();

        for (final Map.Entry<KpiDefinitionEntity, ReadinessBound> readinessEntry : calculatedReadinessBound.entrySet()) {
            final KpiDefinitionEntity kpiDefinition = readinessEntry.getKey();
            final int aggregationPeriod = kpiDefinition.aggregationPeriod();
            final LocalDateTime lowerReadinessBound = readinessEntry.getValue().lowerReadinessBound();
            final LocalDateTime upperReadinessBound = readinessEntry.getValue().upperReadinessBound();

            LocalDateTime calculationStart;
            LocalDateTime reliabilityThreshold;

            if (aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT) {
                calculationStart = lowerReadinessBound;
                reliabilityThreshold = upperReadinessBound;
            } else {
                final AggregationPeriodCreator aggregationPeriodCreator = aggregationPeriodCreatorRegistry.aggregationPeriod(aggregationPeriod);
                final ComplexAggregationPeriod complexAggregationPeriod = aggregationPeriodCreator.createComplexAggregation(lowerReadinessBound, upperReadinessBound);
                calculationStart = complexAggregationPeriod.getCalculationStartTime();
                reliabilityThreshold = complexAggregationPeriod.getReliabilityThreshold();
            }

            calculationReliabilities.add(calculationReliability(complexCalculationId, calculationStart, reliabilityThreshold, kpiDefinition));
        }

        return calculationReliabilities;
    }

    private static CalculationReliability calculationReliability(
            final UUID complexCalculationId,
            final LocalDateTime calculationStart,
            final LocalDateTime reliabilityThreshold,
            final KpiDefinitionEntity kpiDefinition
    ) {
        final CalculationReliabilityBuilder builder = CalculationReliability.builder();
        builder.withCalculationStartTime(calculationStart);
        builder.withReliabilityThreshold(reliabilityThreshold);
        builder.withKpiCalculationId(complexCalculationId);
        builder.withKpiDefinitionId(kpiDefinition.id());
        return builder.build();
    }

}
