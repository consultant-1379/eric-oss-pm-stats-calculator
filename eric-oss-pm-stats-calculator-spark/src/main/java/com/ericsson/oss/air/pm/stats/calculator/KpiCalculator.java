/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils.createOutputTable;
import static com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils.createTemporaryDatasetViewName;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_BEGIN_TIME_COLUMN;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_END_TIME_COLUMN;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.calculator.dataset.util.DatasetExecutor;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry.api.DatasetWriterRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.util.SparkUtils;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

/**
 * Abstract class used to calculate KPIs. Specific implementations can be used depending on if the KPI uses a default or a custom filter.
 */
@Slf4j
public abstract class KpiCalculator {

    DatasetExecutor datasetExecutor;
    Collection<KpiDefinition> kpiDefinitions;
    final List<KpiDefinition> definitionsToExclude = new ArrayList<>();
    private final Map<String, Integer> stagesPerAlias = new HashMap<>();
    private final Map<String, List<KpiDefinition>> kpisPerAlias = new HashMap<>();

    @Setter protected KpiDefinitionHelperImpl kpiDefinitionHelper;
    @Setter protected SparkService sparkService;
    @Setter protected DatasetWriterRegistryFacade datasetWriterRegistryFacade;
    @Setter protected SqlProcessorDelegator sqlProcessorDelegator;

    public abstract TableDatasets calculate();

    TableDatasets calculateKpis(final Set<String> aliases, final Map<Integer, List<KpiDefinition>> kpisByStage,
            final Map<String, List<KpiDefinition>> alreadyCalculatedKpisByAlias, final TableDatasets kpiTablesByAlias) {

        final Map<String, List<KpiDefinition>> alreadyCalculatedKpisByAliasViewName =
                calculatedKpisByAliasViewName(aliases, alreadyCalculatedKpisByAlias);

        for (int stage = 1; stage <= kpisByStage.keySet().size(); stage++) {
            for (final String alias : aliases) {
                log.info("Calculating stage '{}' KPIs for alias '{}' ", stage, alias);

                final Set<KpiDefinition> kpisToCalculate = KpiDefinitionHandler.getKpisForAGivenStageAndAliasFromStagedKpis(kpisByStage, stage,
                        alias);

                final List<String> aggregationElements = kpiDefinitionHelper.getKpiAggregationElementsForAlias(kpisToCalculate, alias);

                if (kpisToCalculate.isEmpty()) {
                    log.debug("No stage {} KPIs found for alias '{}' ", stage, alias);
                    continue;
                }
                stagesPerAlias.put(alias, stage);
                kpisPerAlias.computeIfAbsent(alias, list -> new ArrayList<>()).addAll(kpisToCalculate);

                final Dataset<Row> kpiTableForAliasWithCurrentStageKpis = datasetExecutor
                        .calculateKpisForAlias(kpisToCalculate, alreadyCalculatedKpisByAliasViewName, alias, aggregationElements, stage);
                log.debug("Finished calculating stage {} KPIs for alias '{}'", stage, alias);
                kpiTablesByAlias.put(Table.of(alias), kpiTableForAliasWithCurrentStageKpis);
            }
        }
        return kpiTablesByAlias;
    }

    private Map<String, List<KpiDefinition>> calculatedKpisByAliasViewName(Set<String> aliases, Map<String, List<KpiDefinition>> alreadyCalculatedKpisByAlias) {
        final Map<String, List<KpiDefinition>> alreadyCalculatedKpisByAliasViewName = new HashMap<>();
        for (final String alias : aliases) {
            if (Objects.isNull(alreadyCalculatedKpisByAlias.get(alias))) {
                alreadyCalculatedKpisByAliasViewName.put(createTemporaryDatasetViewName(alias), new ArrayList<>());
            } else {
                alreadyCalculatedKpisByAliasViewName.put(createTemporaryDatasetViewName(alias), alreadyCalculatedKpisByAlias.get(alias));
            }
        }
        return alreadyCalculatedKpisByAliasViewName;
    }

    TableDatasets writeResults(final Set<String> aliases,
                                           final Map<Integer, List<KpiDefinition>> kpisByStage,
                                           final TableDatasets calculatedKpisByAlias) {
        final TableDatasets outputKpisByTableName = TableDatasets.of();

        for (final String alias : aliases) {
            final Set<KpiDefinition> dailyKpisForAlias = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(kpisByStage, alias);

            Dataset<Row> outputKpis = calculatedKpisByAlias.get(Table.of(alias));
            final Integer aggregationPeriodInMinutes = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);
            if (!Objects.equals(aggregationPeriodInMinutes, DEFAULT_AGGREGATION_PERIOD_INT)) {
                outputKpis = outputKpis.withColumn(AGGREGATION_END_TIME_COLUMN, functions.col(AGGREGATION_BEGIN_TIME_COLUMN)
                        .plus(Duration.ofMinutes(aggregationPeriodInMinutes)));
            }
            final Table outputTable = createOutputTable(alias, aggregationPeriodInMinutes);
            outputKpisByTableName.put(outputTable, outputKpis);

            final List<String> aggregationElements = kpiDefinitionHelper.getKpiAggregationElementsForAlias(dailyKpisForAlias, alias);
            final List<String> aggregationElementsSimplified = aliasOrColumnNames(aggregationElements);

            String targetTablePrimaryKeys = String.join(",", aggregationElementsSimplified);
            if (!Objects.equals(aggregationPeriodInMinutes, DEFAULT_AGGREGATION_PERIOD_INT)) {
                targetTablePrimaryKeys = String.join(",", targetTablePrimaryKeys, AGGREGATION_BEGIN_TIME_COLUMN);
            }

            final String description = SparkUtils.buildJobDescription(
                    "CALCULATE",
                    outputTable.getName(),
                    String.valueOf(aggregationPeriodInMinutes),
                    String.valueOf(stagesPerAlias.get(alias)),
                    String.valueOf(kpisPerAlias.get(alias).size())
            );

            datasetWriterRegistryFacade.datasetWriter(aggregationPeriodInMinutes).writeToTargetTableWithCache(
                    description,
                    outputKpis,
                    outputTable.getName(),
                    targetTablePrimaryKeys
            );
        }
        return outputKpisByTableName;
    }

    private List<String> aliasOrColumnNames(final List<String> aggregationElements) {
        return sqlProcessorDelegator.aggregationElements(aggregationElements)
                                    .stream()
                                    .map(Reference::aliasOrColumnName)
                                    .distinct()
                                    .collect(toList());
    }
}
