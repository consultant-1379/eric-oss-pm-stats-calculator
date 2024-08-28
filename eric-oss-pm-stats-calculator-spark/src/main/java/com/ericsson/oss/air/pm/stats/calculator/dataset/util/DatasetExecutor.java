/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.util;

import static com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils.NAME_TOKEN_SEPARATOR;
import static com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils.createTemporaryDatasetViewName;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlCreator;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils;
import com.ericsson.oss.air.pm.stats.calculator.util.SparkUtils;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import lombok.AllArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Utility class used to execute SQL commands to generate {@link Dataset}s for different phases of the KPI calculation flow.
 */
@AllArgsConstructor
public final class DatasetExecutor {

    private final SparkService sparkService;
    private final SqlCreator sqlCreator;
    private final SqlExpressionHelperImpl sqlExpressionHelper;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final int aggregationPeriodInMinutes;
    private final SparkSession sparkSession;

    public DatasetExecutor(final SparkService sparkService,
                           final KpiDefinitionHierarchy kpiDefinitionHierarchy,
                           final SqlProcessorDelegator sqlProcessorDelegator,
                           final int aggregationPeriodInMinutes,
                           final SparkSession sparkSession) {
        this.sparkService = sparkService;
        this.sparkSession = sparkSession;
        this.sqlExpressionHelper = new SqlExpressionHelperImpl(sqlProcessorDelegator);
        this.kpiDefinitionHelper = new KpiDefinitionHelperImpl(kpiDefinitionHierarchy, sqlProcessorDelegator, this.sqlExpressionHelper);
        this.sqlCreator = new SqlCreator(aggregationPeriodInMinutes, sqlProcessorDelegator, kpiDefinitionHelper);
        this.aggregationPeriodInMinutes = aggregationPeriodInMinutes;
    }

    /**
     * Retrieve the maximum timestamp from proposed datasource table.
     *
     * @param datasourceTable
     *            the datasource table from which records will be retrieved.
     * @return the {@link Dataset} with maximum timestamp
     */
    public Dataset<Row> getMaxUtcTimestamp(final String datasourceTable) {
        final String sql = sqlCreator.createMaxTimestampSql(datasourceTable);
        return sparkSession.sql(sql);
    }

    /**
     * Calculates the KPIs for given KPI definitions and alias.
     * <p>
     * Simple KPIs (without a fromExpression) are calculated grouped by schema and joined backed to the current table for
     * that alias.
     * Complex and ondemand KPIs (with their own fromExpression) are calculated afterward and joined separately.
     *
     * @param currentStageKpiDefinitions
     *            the current stage's {@link KpiDefinition}s, filtered by alias and datasource
     * @param alreadyCalculatedKpisByAlias
     *            a {@link Map} of {@link List} of {@link KpiDefinition}s that have already been calculated keyed by alias
     * @param alias
     *            the alias for KPIs
     * @param aggregationElements
     *            the aggregation elements for joining KPIs.
     * @param stage
     *            the stage of the KPIs
     * @return the {@link Dataset} with the current stage's KPIs
     */
    public Dataset<Row> calculateKpisForAlias(final Collection<KpiDefinition> currentStageKpiDefinitions,
            final Map<String, List<KpiDefinition>> alreadyCalculatedKpisByAlias, final String alias, final List<String> aggregationElements,
            final int stage) {

        //TODO extract grouping into a method
        final List<KpiDefinition> factKpis = currentStageKpiDefinitions.stream()
                .filter(kpiDefinition -> kpiDefinitionHelper.isKpiDataSourcesContainType(kpiDefinition, DatasourceType.FACT))
                .collect(toList());

        final List<KpiDefinition> dimKpis = new ArrayList<>(currentStageKpiDefinitions);
        dimKpis.removeAll(factKpis);

        final Map<String, List<KpiDefinition>> definitionsForCurrentStageByFromExpression = new LinkedHashMap<>();
        definitionsForCurrentStageByFromExpression.putAll(factKpis.stream()
                .collect(groupingBy(sqlExpressionHelper::getCleanedFromExpressionOrSchema)));
        definitionsForCurrentStageByFromExpression.putAll(dimKpis.stream()
                .collect(groupingBy(sqlExpressionHelper::getCleanedFromExpressionOrSchema)));

        Dataset<Row> calculatedKpis = sparkSession.emptyDataFrame();
        final String viewNameForAlias = createTemporaryDatasetViewName(alias);
        for (final Map.Entry<String, List<KpiDefinition>> entry : definitionsForCurrentStageByFromExpression.entrySet()) {
            final List<KpiDefinition> kpiDefinitions = entry.getValue();

            //TODO check whether we can do this in the SqlCreator (needs alias)
            final List<String> aggregationElementsForCurrentKpis = kpiDefinitionHelper.getKpiAggregationElementsForAlias(kpiDefinitions, alias);

            final String multipleKpiSql = sqlCreator.createKpiSql(kpiDefinitions, aggregationElementsForCurrentKpis, stage);
            final Dataset<Row> kpisForCurrentFromExpression = sparkSession.sql(multipleKpiSql);

            final boolean isFirstTimeAliasCalculation = alreadyCalculatedKpisByAlias.get(viewNameForAlias).isEmpty();
            if (isFirstTimeAliasCalculation) {
                calculatedKpis = kpisForCurrentFromExpression;
            } else {
                final String currentDatasetName = getTemporaryDatasetNameForStageAndAggregation(alias, stage);
                sparkService.cacheView(currentDatasetName, kpisForCurrentFromExpression);
                calculatedKpis = joinKpisToViewForAlias(alias, kpiDefinitions, alreadyCalculatedKpisByAlias.get(viewNameForAlias), currentDatasetName,
                        aggregationElements);
            }

            sparkService.cacheView(viewNameForAlias, calculatedKpis);
            alreadyCalculatedKpisByAlias.get(viewNameForAlias).addAll(kpiDefinitions);
        }
        return calculatedKpis;
    }

    private static String getTemporaryDatasetNameForStageAndAggregation(final String alias, final int stage) {
        return alias + NAME_TOKEN_SEPARATOR + stage;
    }

    /**
     * Executes a union of the KPIs calculated for the current ROP against the existing KPIs in the database.
     * <p>
     * Does not return a {@link Dataset}, instead creates a temp view using {@link Dataset#createOrReplaceTempView(String)} with a name defined by
     * {@link KpiNameUtils#createTemporaryDatasetViewName(String, String)}.
     *
     * @param kpis
     *            the {@link KpiDefinition} for all KPIs to be calculated (pre-filtered by alias, datasource and aggregation period)
     * @param aggregationElements
     *            the {@link List} of aggregation columns
     * @param alias
     *            the alias for the KPIs
     */
    public void unionWithDatabase(final Set<KpiDefinition> kpis, final List<String> aggregationElements,
            final String alias) {
        final Set<Timestamp> timestampSet = getTimestampsFromInMemoryTable(alias);
        if (timestampSet.isEmpty() && aggregationPeriodInMinutes != -1) {
            return;
        }

        final String unionOfKpisSql = sqlCreator.createUnionSql(kpis, aggregationElements, alias, aggregationPeriodInMinutes, timestampSet);
        final Dataset<Row> unionOfKpis = sparkSession.sql(unionOfKpisSql);

        sparkService.cacheView(createTemporaryDatasetViewName(alias), unionOfKpis);
    }

    private Set<Timestamp> getTimestampsFromInMemoryTable(final String alias) {
        final String tempTable = KpiNameUtils.createTemporaryDatasetViewName(alias);
        final Dataset<Row> timestamps = aggregationPeriodInMinutes == -1 ? sparkSession.emptyDataFrame()
                : sparkSession.sql("SELECT DISTINCT(aggregation_begin_time) FROM " + tempTable);

        sparkService.registerJobDescription(SparkUtils.buildJobDescription("GET_TIMESTAMPS", tempTable, String.valueOf(aggregationPeriodInMinutes)));
        final Set<Timestamp> collect = timestamps.collectAsList().stream().map(row -> row.getTimestamp(0)).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        sparkService.unregisterJobDescription();
        return collect;
    }

    /**
     * Aggregates the union of KPIs from the current ROP and from the DB (within the correct time period). This will be the output for that time
     * period and could be updated as further ROPs within this time period are processed.
     *
     * @param kpiDefinitions
     *            the {@link KpiDefinition} for all KPIs to be aggregated (pre-filtered by alias, datasource and aggregation period)
     * @param alias
     *            the alias for the KPIs
     * @param aggregationElements
     *            the {@link List} of aggregation columns
     * @return the final aggregated KPIs containing the KPIs from the current ROP and with existing KPIs in the DB
     */
    public Dataset<Row> aggregateKpis(final Collection<KpiDefinition> kpiDefinitions, final String alias, final List<String> aggregationElements) {
        final String aggregatedKpisSql = sqlCreator.createAggregationSql(kpiDefinitions, alias, aggregationElements);
        return sparkSession.sql(aggregatedKpisSql);
    }

    /**
     * Merges a calculated KPI across datasources, from the same alias. For example, cell-based KPIs can be from multiple datasources: PM stats data,
     * or PM events data. These two are calculated and aggregated independently, and joined together at the end of the calculation flow.
     *
     * @param alias
     *            the alias for the KPIs
     * @param kpiDefinitions
     *            the {@link KpiDefinition} for the alias
     * @param alreadyCalculatedKpis
     *            the {@link KpiDefinition}s that have already been calculated for the alias
     * @param secondDatasource
     *            the name of the dataset which the KPI is stored in
     * @param aggregationElements
     *            the {@link List} of aggregation columns
     * @return the merged KPIs for all datasources for the given alias
     */
    public Dataset<Row> joinKpisToViewForAlias(final String alias, final Collection<KpiDefinition> kpiDefinitions,
            final Collection<KpiDefinition> alreadyCalculatedKpis, final String secondDatasource, final List<String> aggregationElements) {
        final String joinSql = sqlCreator.createJoinSql(alias, kpiDefinitions, alreadyCalculatedKpis, secondDatasource, aggregationElements);
        return sparkSession.sql(joinSql);
    }
}
