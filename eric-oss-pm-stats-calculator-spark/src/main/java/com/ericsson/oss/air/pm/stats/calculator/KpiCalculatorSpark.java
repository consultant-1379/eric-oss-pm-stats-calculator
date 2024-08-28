/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.registry.DataLoaderRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.KpiDefinitionsByFilter;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.CalculatorHandlerRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.CalculationExecutorFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.DatasourceRegistryHandlerImp;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiCalculatorSpark {
    private final KpiDefinitionService kpiDefinitionService;
    private final KpiDefinitionHierarchy kpiDefinitionHierarchy;
    private final DatasourceRegistryHandlerImp datasourceRegistryHandlerImpl;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    private final CalculationExecutorFacade calculationExecutorFacade;
    private final CalculatorHandlerRegistryFacadeImpl calculatorHandlerRegistryFacade;
    private final OffsetHandlerRegistryFacade offsetHandlerRegistryFacade;
    private final DataLoaderRegistryFacadeImpl dataLoaderRegistryFacade;

    public void calculate() {
        final Set<KpiDefinition> kpiDefinitions = kpiDefinitionService.loadDefinitionsToCalculate();

        final Set<KpiDefinition> customFilterKpis = kpiDefinitionHelper.getFilterAssociatedKpiDefinitions(kpiDefinitions);
        final Set<KpiDefinition> defaultFilterKpis = new HashSet<>(CollectionUtils.removeAll(kpiDefinitions, customFilterKpis));

        offsetHandlerRegistryFacade.calculateOffsets(defaultFilterKpis);

        if (datasourceRegistryHandlerImpl.isNoDataSourceMissing(defaultFilterKpis)) {
            calculateDefaultFilterKpis(defaultFilterKpis);
        } else {
            log.warn("Default filter KPIs will not be calculated.");
        }

        if (datasourceRegistryHandlerImpl.isNoDataSourceMissing(customFilterKpis)) {
            calculateCustomFilteredKpis(customFilterKpis);
        } else {
            log.warn("Custom filter KPIs will not be calculated.");
        }

        offsetHandlerRegistryFacade.saveOffsets();
    }

    private void calculateDefaultFilterKpis(final Collection<? extends KpiDefinition> defaultFilterKpis) {
        kpiDefinitionHelper.groupByAggregationPeriod(defaultFilterKpis).forEach((aggregationPeriod, kpiDefinitions) -> {
            final KpiCalculator kpiCalculator = calculatorHandlerRegistryFacade.defaultCalculator(kpiDefinitions);

            dataLoaderRegistryFacade.defaultFilterIterator(kpiDefinitions).forEachRemaining(tableDatasets ->
                    calculationExecutorFacade.calculateKpis(kpiCalculator, tableDatasets)
            );

            log.info("All defined KPIs with default filter have been calculated for aggregation period: {}", aggregationPeriod);
        });
    }

    private void calculateCustomFilteredKpis(final Collection<KpiDefinition> kpiDefinitions) {
        kpiDefinitionHelper.extractAggregationPeriods(kpiDefinitions).forEach(aggPeriodInMinutes -> {
            final List<KpiDefinition> kpisForAggregationPeriod = offsetHandlerRegistryFacade.offsetHandler().getKpisForAggregationPeriodWithTimestampParameters(
                    aggPeriodInMinutes,
                    kpiDefinitions
            );

            if (!kpisForAggregationPeriod.isEmpty()) {
                calculateCustomFilteredKpisForAggregationPeriod(kpisForAggregationPeriod);
            }

            log.info("All defined KPIs with custom filter have been calculated for aggregation period: {}", aggPeriodInMinutes);
        });
    }

    private void calculateCustomFilteredKpisForAggregationPeriod(final List<KpiDefinition> kpisForAggregationPeriod) {
        //  TODO: Find a better solution to extract same filters by datasource
        if (kpiDefinitionService.areScheduledSimple(kpisForAggregationPeriod)) {
            kpiDefinitionHelper.groupByDataIdentifier(kpisForAggregationPeriod).forEach((identifier, definitionsByIdentifier) -> {
                final KpiDefinitionsByFilter kpisByFilter = kpiDefinitionHelper.groupByFilter(definitionsByIdentifier);

                kpisByFilter.forEach(this::calculateCustomFilteredKpisForFilters);
            });
        } else {
            final KpiDefinitionsByFilter kpisByFilter = kpiDefinitionHelper.groupFilterAssociatedKpisByFilters(
                    kpisForAggregationPeriod,
                    kpiDefinitionHierarchy
            );

            kpisByFilter.forEach(this::calculateCustomFilteredKpisForFilters);
        }
    }

    private void calculateCustomFilteredKpisForFilters(final Set<Filter> filters, final Collection<KpiDefinition> definitions) {
        final KpiCalculator kpiCalculator = calculatorHandlerRegistryFacade.customCalculator(definitions);
        final TableDatasets tableDatasets = dataLoaderRegistryFacade.customFilterDatasets(definitions, filters);

        calculationExecutorFacade.calculateKpis(kpiCalculator, tableDatasets);
    }

}