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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.pm.stats.utils.LocalDateTimes.isAfterOrEqual;
import static java.util.Collections.max;
import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.AggregationPeriodSupporter;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ReadinessBoundCalculator {
    @Inject
    private AggregationPeriodSupporter aggregationPeriodSupporter;
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private ReadinessBoundCollector readinessBoundCollector;
    @Inject
    private CalculationReliabilityService calculationReliabilityService;

    public boolean isReliablyCalculableGroup(final String complexExecutionGroup) {
        final List<KpiDefinitionEntity> kpiDefinitions = kpiDefinitionService.findKpiDefinitionsByExecutionGroup(complexExecutionGroup);
        final Map<String, LocalDateTime> reliabilityThresholds = calculationReliabilityService.findMaxReliabilityThresholdByKpiName();
        final Integer maxDataLookback = max(CollectionHelpers.transform(kpiDefinitions, KpiDefinitionEntity::dataLookbackLimit));

        log.info("Complex execution group '{}' has maximum data lookback limit '{}", complexExecutionGroup, maxDataLookback);

        return readinessBoundCollector.calculateReadinessBounds(complexExecutionGroup, kpiDefinitions)
                .entrySet()
                .stream()
                .anyMatch(entry -> isReliablyCalculableKpi(entry, reliabilityThresholds, maxDataLookback));
    }

    private boolean isReliablyCalculableKpi(final Entry<? extends KpiDefinitionEntity, ? extends ReadinessBound> kpiDefinitionReadinessEntry,
                                            final Map<String, LocalDateTime> reliabilities,
                                            final Integer dataLookback) {
        final long aggregationPeriod = kpiDefinitionReadinessEntry.getKey().aggregationPeriod();

        if (aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT) {
            return true;
        }

        final ReadinessBound readinessBound = kpiDefinitionReadinessEntry.getValue();
        final LocalDateTime lowerReadinessBound = readinessBound.lowerReadinessBound();
        final LocalDateTime upperReadinessBound = readinessBound.upperReadinessBound();

        final String kpiName = kpiDefinitionReadinessEntry.getKey().name();
        if (reliabilities.containsKey(kpiName)) {
            final LocalDateTime reliability = reliabilities.get(kpiName);
            if (upperReadinessBound.isBefore(reliability) && isAfterOrEqual(lowerReadinessBound.plusMinutes(dataLookback), reliability)) {
                return true;
            }
        }

        return aggregationPeriodSupporter.areBoundsInDifferentAggregationPeriods(lowerReadinessBound, upperReadinessBound, aggregationPeriod);
    }

}
