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

import static com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils.createTemporaryDatasetViewName;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistency;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistencyPostgres;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.registry.OffsetPersistencyRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.util.DatasetExecutor;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionParser;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Class used to calculate KPIs with default filter. Calculation of KPIs is agnostic to the aggregation period.
 */
@Slf4j
public class KpiCalculatorDefaultFilter extends KpiCalculator {

    private final Collection<KpiDefinition> postAggregationKpiDefinitions;
    private final OffsetPersistency offsetPersistency;
    private final int aggregationPeriod;
    private final SparkSession sparkSession;

    public KpiCalculatorDefaultFilter(
            final SparkService sparkService,
            final int aggregationPeriodInMinutes,
            final Collection<KpiDefinition> defaultFilterKpis,
            final SqlProcessorDelegator sqlProcessorDelegator,
            final SparkSession sparkSession,
            final OffsetPersistencyRegistryFacadeImpl offsetPersistencyRegistryFacade) {
        this.sparkService = sparkService;
        aggregationPeriod = aggregationPeriodInMinutes;
        this.sqlProcessorDelegator = sqlProcessorDelegator;
        kpiDefinitions = new ArrayList<>(defaultFilterKpis);
        this.sparkSession = sparkSession;
        datasetExecutor = new DatasetExecutor(
                sparkService,
                new KpiDefinitionHierarchy(kpiDefinitions),
                sqlProcessorDelegator,
                aggregationPeriodInMinutes,
                sparkSession);
        postAggregationKpiDefinitions = KpiDefinitionHandler.getPostAggregationKpiDefinitions(kpiDefinitions);
        kpiDefinitions.removeAll(postAggregationKpiDefinitions);
        offsetPersistency = offsetPersistencyRegistryFacade.offsetPersistency(kpiDefinitions);
    }

    /**
     * Calculates the KPIs for a given aggregation period (e.g. 1 day or 1 hour) based on the {@link KpiDefinition}s. The current time period in which
     * to calculate the new KPIs is defined by the start and end {@link Timestamp}s provided. KPIs are calculated using the following flow:
     * <ol>
     * <li><b>Determine Max Timestamps Per Datasource</b>
     * <p>
     * For each datasource (which has already been filtered by start and end time), the most recent timestamp is determined. This is done to ensure
     * that the next time KPIs are calculated, input data that has already been processed isn't reprocessed, which would lead to incorrect KPIs.
     * </p>
     * </li>
     * <li><b>Calculate KPIs</b>
     * <p>
     * The {@link KpiDefinition}s are grouped by alias and then by stage. The stage is determined in {@link KpiDefinitionHandler} and is defined as
     * the order in which KPIs should be calculated. This is extremely important as some KPIs are based on others. The calculator iterates through
     * each stage, and within each stage KPIs are calculated per alias. The alias is essentially a name given to the element to which KPIs should be
     * aggregated to. KPIs are calculated one by one within the alias and appended back to the base table. At this point in the flow, there is no
     * aggregation done on the input rows. The {@link Dataset}s which exist at this stage are simply KPIs at a ROP level.
     * </p>
     * </li>
     * <li><b>Union With Output Table</b>
     * <p>
     * At this point in the flow, the {@link Dataset}s per alias are 'unioned' with the target table. The output tables are named using
     * &lt;alias&gt;_&lt;aggregation_period&gt;. Once the union is created the current are simply aggregated with the existing based on the
     * local_timestamp.
     * </p>
     * </li>
     * <li><b>Persist KPIs to Output Table</b>
     * <p>
     * The final tables once aggregated are written to the output tables.
     * </p>
     * </li>
     * <li><b>Persist Max Timestamps Per Datasource</b>
     * <p>
     * Once all KPIs have been calculated and successfully persisted, the max timestamps determined in Step 1 are written. It is important not to do
     * this until the end as we may have an error in calculation. This would mean the next time KPIs are calculated, the calculator would assume that
     * certain input data has already been processed.
     * </p>
     * </li>
     * </ol>
     *
     */
    @Override
    public TableDatasets calculate() {
        log.info("Starting calculation of KPIs which use a default filter");
        final Map<Integer, List<KpiDefinition>> kpisByStage = kpiDefinitionHelper.groupKpisByStage(kpiDefinitions);
        final Set<String> aliases = kpiDefinitionHelper.extractAliases(kpiDefinitions);
        //TODO this variable is used only to send it as a parameter. Check whether we need to calculate here
        final DatasourceTables datasourceTables = kpiDefinitionHelper.extractNonInMemoryDatasourceTables(kpiDefinitions);
        if (offsetPersistency instanceof OffsetPersistencyPostgres) {
            offsetPersistency.calculateMaxOffsetsForSourceTables(kpisByStage, datasourceTables, aggregationPeriod);
        }
        TableDatasets calculatedKpisByAlias = calculateKpis(aliases, kpisByStage, new HashMap<>(), TableDatasets.of());
        if (calculatedKpisByAlias.isEmpty()) {
            return TableDatasets.of();
        }

        if (sparkService.isScheduledSimple()) {
            final TableDatasets aggregatedKpisByAlias = unionWithDatabaseAndAggregate(kpiDefinitions);
            aggregatedKpisByAlias.cacheDatasets();
            calculatedKpisByAlias = aggregatedKpisByAlias;
        }

        final TableDatasets calculatedKpisByAliasWithPostAggregationKpis = calculatePostAggregationKpisIfRequired(kpiDefinitions,
                calculatedKpisByAlias);

        final TableDatasets finalTables = writeResults(aliases, kpisByStage, calculatedKpisByAliasWithPostAggregationKpis);

        if (sparkService.isScheduledComplex()) {
            offsetPersistency.persistMaxOffsets(aggregationPeriod);
        }

        return finalTables;
    }

    private TableDatasets calculatePostAggregationKpisIfRequired(final Collection<KpiDefinition> kpiDefinitions,
                                                                 final TableDatasets calculatedKpisByAlias) {
        if (!postAggregationKpiDefinitions.isEmpty()) {
            return calculatePostAggregationKpis(kpiDefinitions, calculatedKpisByAlias);
        }
        return calculatedKpisByAlias;
    }

    private TableDatasets calculatePostAggregationKpis(final Collection<KpiDefinition> alreadyCalculatedKpis,
                                                       final TableDatasets kpiTablesByAlias) {
        log.info("Calculating post aggregation KPIs");
        final Set<String> aliases = kpiDefinitionHelper.extractAliases(postAggregationKpiDefinitions);

        for (final String alias : aliases) {
            kpiTablesByAlias.get(Table.of(alias)).createOrReplaceTempView(createTemporaryDatasetViewName(alias));
        }

        final Map<Integer, List<KpiDefinition>> postAggregationKpisByStage
                = kpiDefinitionHelper.groupKpisByStage(postAggregationKpiDefinitions);
        final Map<String, List<KpiDefinition>> alreadyCalculatedKpisByAlias = KpiDefinitionParser.groupKpisByAlias(alreadyCalculatedKpis);

        calculateKpis(aliases, postAggregationKpisByStage, alreadyCalculatedKpisByAlias, kpiTablesByAlias);

        return kpiTablesByAlias;
    }

    private TableDatasets unionWithDatabaseAndAggregate(final Collection<KpiDefinition> kpiDefinitions) {
        final Set<String> aliases = kpiDefinitionHelper.extractAliases(kpiDefinitions);
        //TODO remove this call. The caller method has already calculated this
        final Map<Integer, List<KpiDefinition>> kpisByStage = kpiDefinitionHelper.groupKpisByStage(kpiDefinitions);
        final TableDatasets calculatedKpisByAlias = TableDatasets.of();
        log.info("Performing union with output tables and aggregating");
        for (final String alias : aliases) {
            final Set<KpiDefinition> aliasKpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(kpisByStage, alias);

            if (aliasKpis.isEmpty()) {
                log.debug("No KPIs found to union/aggregate for alias '{}'", alias);
                continue;
            }

            /*TODO this variable is used only to send it as a parameter. Check whether we need this assignment here
            Review also the calculation method. We calculate aggregationElements as aliasOrColumnNames here, however
            in every SQL query we `simplify` them again. Note that in SqlCreatorTest we just use
            kpiDefinitionHelper.getKpiAggregationElementsForAlias() to simulate this calculation.
            */
            final List<String> aggregationElements = removeAggregationElementsWhichAreNotInAliasTable(
                    kpiDefinitionHelper.getKpiAggregationElementsForAlias(aliasKpis, alias), alias);

            definitionsToExclude.forEach(aliasKpis::remove);
            datasetExecutor.unionWithDatabase(aliasKpis, aggregationElements, alias);

            final Dataset<Row> calculatedKpis = datasetExecutor.aggregateKpis(aliasKpis, alias, aggregationElements);

            final String temporaryDatasetViewName = createTemporaryDatasetViewName(alias);

            sparkService.cacheView(temporaryDatasetViewName, calculatedKpis);
            calculatedKpisByAlias.put(Table.of(alias), calculatedKpis);
        }
        return calculatedKpisByAlias;
    }

    /**
     * Given an aggregation elements list and an alias, this method returns a filtered list of aggregation elements that exists in alias database
     * table. This covers the case where we calculate a KPI (lets name it as 'A') and then there are other KPIs (lets name them as 'B') which have "A"
     * as an aggregation element. For the KPI named as "A": we only need its aggregation element for staging query where "A" is involved. "A"'s
     * aggregation element doesn't need to be joined nor aggregated because it is "A" itself but with a different name. As "A"'s aggregation element
     * won't be part of the resulting database table, it has to be taken out of the joining and aggregation queries.
     *
     * @param aggregationElements
     *            {@link List} of aggregation elements to be filtered
     * @param alias
     *            alias whose database table needs to be used for filtering
     * @return {@link List} of aggregation elements from @param aggregationElements which are column of alias database table
     */
    private List<String> removeAggregationElementsWhichAreNotInAliasTable(final List<String> aggregationElements, final String alias) {
        final String tableName = createTemporaryDatasetViewName(alias);
        final List<String> columns = columns(tableName);
        final List<String> result = new ArrayList<>();

        //  TODO: This is a simple intersection with the Spark table columns for the `alias` parameter and the aggregation elements aliasOrColumnName
        //        I can imagine a less long but more descriptive name for this method. Also the JavaDoc is hard to understand, instead of long
        //        description an example would be better.
        log.info("The Spark table '{}' contains the following columns: '{}'", tableName, columns);
        for (final Reference reference : sqlProcessorDelegator.aggregationElements(aggregationElements)) {
            final String column = reference.aliasOrColumnName();
            if (columns.contains(column)) {
                result.add(column);
            } else {
                log.info("The Spark table '{}' does not contain column '{}', it is not considered", tableName, column);
            }
        }

        return result;
    }

    private List<String> columns(final String tableName) {
        final Dataset<Row> table = sparkSession.table(tableName);
        return Arrays.asList(table.columns());
    }

}
