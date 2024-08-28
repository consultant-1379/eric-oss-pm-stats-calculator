/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DimensionTablesService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionAdapterImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.OnDemandParameterCastUtils;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "definitions-to-calculate")
public class KpiDefinitionServiceImpl implements KpiDefinitionService {
    private static final int DEFAULT_AGGREGATION_PERIOD = -1;

    private final KpiDefinitionAdapterImpl kpiDefinitionAdapter;
    private final KpiDefinitionRepository kpiDefinitionDatabaseRepository;
    private final SparkService sparkService;
    private final DimensionTablesService dimensionTablesService;
    private final OnDemandParameterCastUtils onDemandParameterCastUtils;

    @Override
    @Cacheable
    public Set<KpiDefinition> loadDefinitionsToCalculate() {
        List<KpiDefinition> kpiDefinitions = kpiDefinitionAdapter.convertKpiDefinitions(
                kpiDefinitionDatabaseRepository.findByNameIn(sparkService.getKpisToCalculate()));

        if (sparkService.isOnDemand()) {
            final List<String> dimensionTableEntities = dimensionTablesService.getTabularParameterTableNamesByCalculationId(sparkService.getCalculationId());
            kpiDefinitions = KpiDefinitionUtils.changeTableNameIfTabularParameterIsPresent(kpiDefinitions, dimensionTableEntities);
            return onDemandParameterCastUtils.applyParametersToKpiDefinitions(sparkService.getKpiDefinitionParameters(), kpiDefinitions);
        }

        return new HashSet<>(kpiDefinitions);
    }

    @Override
    public boolean areScheduled(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream().anyMatch(KpiDefinition::isScheduled);
    }

    @Override
    public boolean areScheduledSimple(final Collection<KpiDefinition> definitions) {
        return KpiDefinitionHandler.areKpiDefinitionsSimpleKpis(definitions);
    }

    @Override
    public boolean areNonScheduledSimple(final Collection<KpiDefinition> definitions) {
        return !areScheduledSimple(definitions);
    }

    @Override
    public boolean isDefaultAggregationPeriod(@NonNull final Integer aggregationPeriod) {
        return aggregationPeriod.equals(DEFAULT_AGGREGATION_PERIOD);
    }
}
