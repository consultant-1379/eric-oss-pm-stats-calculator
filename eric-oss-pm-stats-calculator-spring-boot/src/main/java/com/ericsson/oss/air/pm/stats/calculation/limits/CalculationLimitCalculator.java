/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits;

import static java.util.Collections.max;
import static java.util.Collections.min;
import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.CalculationLimit;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;
import com.ericsson.oss.air.pm.stats.utils.LocalDateTimes;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculationLimitCalculator {

    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private ReadinessBoundCollector readinessBoundCollector;
    @Inject
    private CalculationRepository calculationRepository;

    public CalculationLimit calculateLimit(final String complexExecutionGroup) {
        final List<KpiDefinitionEntity> kpiDefinitions = kpiDefinitionService.findKpiDefinitionsByExecutionGroup(complexExecutionGroup);

        final Map<KpiDefinitionEntity, ReadinessBound> calculatedReadinessBound = readinessBoundCollector.calculateReadinessBounds(complexExecutionGroup, kpiDefinitions);

        final Collection<LocalDateTime> upperReadinessBounds = CollectionHelpers.transform(calculatedReadinessBound.values(), ReadinessBound::upperReadinessBound);
        final Collection<LocalDateTime> lowerReadinessBounds = CollectionHelpers.transform(calculatedReadinessBound.values(), ReadinessBound::lowerReadinessBound);
        final Collection<Integer> dataLookBackLimits = CollectionHelpers.transform(kpiDefinitions, KpiDefinitionEntity::dataLookbackLimit);

        final LocalDateTime lastComplexTimeCompleted = calculationRepository.getLastComplexCalculationReliability(complexExecutionGroup);

        final LocalDateTime calculationEndTime = LocalDateTimes.max(max(upperReadinessBounds), lastComplexTimeCompleted);

        final LocalDateTime earliestAllowedStartTime = lastComplexTimeCompleted.minusMinutes(max(dataLookBackLimits));

        final LocalDateTime calculationStartTime = LocalDateTimes.max(min(lowerReadinessBounds), earliestAllowedStartTime);

        return CalculationLimit.builder()
                .calculationStartTime(calculationStartTime)
                .calculationEndTime(calculationEndTime)
                .build();
    }
}