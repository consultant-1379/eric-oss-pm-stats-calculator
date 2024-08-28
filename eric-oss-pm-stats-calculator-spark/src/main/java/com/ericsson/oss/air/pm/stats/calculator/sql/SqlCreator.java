/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import org.apache.commons.text.StringSubstitutor;

/**
 * Utility class used to generate SQL statements for the different phases for the KPI calculation flow.
 */
public final class SqlCreator {

    private final AggregationSqlCreator aggregationSqlCreator;
    private final JoinSqlCreator joinSqlCreator;
    private final StageSqlCreator stageSqlCreator;
    private final UnionSqlCreator unionSqlCreator;

    public SqlCreator(final int aggregationPeriodInMinutes, final SqlProcessorDelegator sqlProcessorDelegator, final KpiDefinitionHelperImpl kpiDefinitionHelper) {
        aggregationSqlCreator = new AggregationSqlCreator(sqlProcessorDelegator, aggregationPeriodInMinutes);
        joinSqlCreator = new JoinSqlCreator(sqlProcessorDelegator, kpiDefinitionHelper, aggregationPeriodInMinutes);
        unionSqlCreator = new UnionSqlCreator(sqlProcessorDelegator, aggregationPeriodInMinutes);
        stageSqlCreator = new StageSqlCreator(new SqlExpressionHelperImpl(sqlProcessorDelegator), kpiDefinitionHelper, aggregationPeriodInMinutes);
        stageSqlCreator.setSqlProcessorDelegator(sqlProcessorDelegator);
    }

    /**
     * Creates the SQL statement used to select the maximum timestamp from the proposed datasource table.
     *
     * @param datasourceTable
     *            the datasource table from which records will be retrieved.
     * @return the SQL statement used to calculate maximum timestamp
     */
    public String createMaxTimestampSql(final String datasourceTable) {
        final String sqlTemplate = "SELECT " + "MAX(aggregation_begin_time) as latest_time_collected" + " FROM ${source}";

        final Map<String, String> values = new HashMap<>(1);
        values.put("source", datasourceTable);
        return StringSubstitutor.replace(sqlTemplate, values);
    }

    /**
     * Creates the SQL statement used to calculate a KPI for given stage and source table.
     *
     * @param kpis
     *            KPIs for which to generate a SQL statement. They have the same from-expression
     * @param aggregationElements
     *            the aggregation elements for the alias
     * @param stage
     *            the stage of the KPIs
     * @return the SQL statement used to calculate the supplied KPI
     * @see StageSqlCreator#createSqlForKpis(List, List, int)
     */
    public String createKpiSql(final List<KpiDefinition> kpis, final List<String> aggregationElements, final int stage) {
        return stageSqlCreator.createSqlForKpis(kpis, aggregationElements, stage);
    }

    /**
     * Creates the SQL query to execute a union of the KPIs calculated from the current ROP, against the KPIs already calculated in the DB for the
     * required time period.
     *
     * @param kpiDefinitions
     *            the {@link KpiDefinition} for all KPIs to be calculated (pre-filtered by alias, datasource and aggregation period)
     * @param aggregationElements
     *            the aggregation elements for the datasource table.
     * @param alias
     *            the alias for the KPIs
     * @param aggregationPeriodInMinutes
     *            the aggregation period of the KPI in minutes
     * @param timestamps
     *            the timestamps over which to apply the union
     * @return the SQL query to execute a union of the current KPIs with the DB KPIs
     * @see UnionSqlCreator#createUnionWithOutputTargetTableQuery(Collection, List, String, int, Set)
     */
    public String createUnionSql(final Collection<KpiDefinition> kpiDefinitions, final List<String> aggregationElements, final String alias,
            final int aggregationPeriodInMinutes, final Set<Timestamp> timestamps) {
        return unionSqlCreator.createUnionWithOutputTargetTableQuery(kpiDefinitions, aggregationElements, alias, aggregationPeriodInMinutes,
                timestamps);
    }

    /**
     * Creates the SQL query to aggregate the KPIs from the current ROP and from the DB (within the correct time period).
     *
     * @param kpiDefinitions
     *            the {@link KpiDefinition} for all KPIs to be calculated (pre-filtered by alias, datasource and aggregation period)
     * @param alias
     *            the alias for the KPIs
     * @param aggregationElements
     *            the aggregation elements for the datasource table.
     * @return the SQL query to aggregate KPIs
     * @see AggregationSqlCreator#createAggregationSqlQuery(Collection, String, List)
     */
    public String createAggregationSql(final Collection<KpiDefinition> kpiDefinitions, final String alias, final List<String> aggregationElements) {
        return aggregationSqlCreator.createAggregationSqlQuery(kpiDefinitions, alias, aggregationElements);
    }

    /**
     * Creates the SQL query to join a single KPI to the dataset for its alias.
     *
     * @param alias
     *            the alias for the KPIs
     * @param kpisToCalculate
     *            a {@link List} of the new {@link KpiDefinition}s to join back to the alias dataset
     * @param alreadyCalculatedKpis
     *            a {@link List} of the {@link KpiDefinition}s that have already been calculated to join back to the alias dataset
     * @param secondDatasource
     *            the name of the dataset which the KPI is stored in
     * @param aggregationElements
     *            the aggregation elements for the datasource table
     * @return SQL query to merge calculated KPIs for the same alias
     * @see JoinSqlCreator#createJoinSql(String, Collection, Collection, String, List)
     */
    public String createJoinSql(final String alias, final Collection<KpiDefinition> kpisToCalculate,
            final Collection<KpiDefinition> alreadyCalculatedKpis, final String secondDatasource, final List<String> aggregationElements) {
        return joinSqlCreator.createJoinSql(alias, kpisToCalculate, alreadyCalculatedKpis, secondDatasource, aggregationElements);
    }
}
