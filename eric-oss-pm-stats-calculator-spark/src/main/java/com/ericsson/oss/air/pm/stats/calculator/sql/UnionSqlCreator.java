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

import static com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils.createTemporaryDatasetViewName;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_BEGIN_TIME_COLUMN;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.COMMA_AND_SPACE_SEPARATOR;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils;
import com.ericsson.oss.air.pm.stats.calculator.util.SqlCreatorUtils;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import org.apache.commons.text.StringSubstitutor;

/**
 * Utility class used to create the SQL to merge the results of the current ROP's calculated KPIs with the previously calculated ROP's in the DB.
 */
final class UnionSqlCreator {
    private static final String AGGREGATION_COLUMNS_PLACEHOLDER = "${aggregationColumns}, ";

    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final int aggregationPeriodInMinutes;

    UnionSqlCreator(final SqlProcessorDelegator sqlProcessorDelegator, final int aggregationPeriodInMinutes) {
        this.sqlProcessorDelegator = sqlProcessorDelegator;
        this.aggregationPeriodInMinutes = aggregationPeriodInMinutes;
    }

    /**
     * Creates the SQL query to execute a union of the KPIs calculated from the current ROP, against the KPIs already calculated in the DB for the
     * required time period.
     *
     * @param kpis
     *            the {@link KpiDefinition} for all KPIs to be calculated (pre-filtered by alias, datasource and aggregation period)
     * @param aggregationElements
     *            the aggregation elements for the datasource table.
     * @param alias
     *            the alias for the KPIs
     * @param aggregationPeriodInMinutes
     *            the aggregation period of the KPI in minutes
     * @param timestamps
     *            the timestamps over which to apply the union
     * @return SQL query to execute a union of the current KPIs with the DB KPIs
     */
    String createUnionWithOutputTargetTableQuery(final Collection<KpiDefinition> kpis, final List<String> aggregationElements, final String alias,
            final int aggregationPeriodInMinutes, final Set<Timestamp> timestamps) {

        final List<String> aggregationElementsSimplified = aliasOrColumnNames(aggregationElements);

        final String sqlTemplate = "SELECT " +
                AGGREGATION_COLUMNS_PLACEHOLDER +
                "${timestampColumnNames}" +
                "${kpiNames} " +
                "FROM ${temporaryTableName} " +
                "UNION ALL SELECT " +
                AGGREGATION_COLUMNS_PLACEHOLDER +
                "${timestampColumnNames}" +
                "${kpiNames} " +
                "FROM ${targetTableName}" +
                "${timePeriodWhereClause}";

        final Set<KpiDefinition> kpisWhichAreNotAggregationElements = SqlCreatorUtils.filterKpiByName(kpis, aggregationElementsSimplified);
        final String commaSeparatedAggregationElements = aggregationElementsSimplified.stream().sorted().collect(joining(COMMA_AND_SPACE_SEPARATOR));
        final String tempDatasetViewName = createTemporaryDatasetViewName(alias);

        final Map<String, String> values = new HashMap<>(7);
        values.put("aggregationColumns", commaSeparatedAggregationElements);
        values.put("timestampColumnNames",
                this.aggregationPeriodInMinutes == DEFAULT_AGGREGATION_PERIOD_INT ? "" : (AGGREGATION_BEGIN_TIME_COLUMN + COMMA_AND_SPACE_SEPARATOR));
        values.put("kpiNames", getKpiNames(kpisWhichAreNotAggregationElements));
        values.put("targetTableName", KpiNameUtils.createOutputTableName(alias, aggregationPeriodInMinutes));
        values.put("timePeriodWhereClause",
                this.aggregationPeriodInMinutes == DEFAULT_AGGREGATION_PERIOD_INT ? ""
                        : createWhereClauseForTimePeriod(timestamps));
        values.put("temporaryTableName", tempDatasetViewName);
        return StringSubstitutor.replace(sqlTemplate, values);
    }

    private List<String> aliasOrColumnNames(final List<String> aggregationElements) {
        return sqlProcessorDelegator.aggregationElements(aggregationElements)
                                    .stream()
                                    .map(Reference::aliasOrColumnName)
                                    .distinct()
                                    .collect(toList());
    }

    private static String createWhereClauseForTimePeriod(final Set<Timestamp> timestamps) {
        return timestamps.isEmpty() ? "" : String.format(" WHERE aggregation_begin_time in (%s)" , wrapTimestampValueInToTimestamp(timestamps));
    }

    private static String wrapTimestampValueInToTimestamp(final Set<Timestamp> timestamps) {
        return timestamps.stream().map(timestamp -> String.format("TO_TIMESTAMP('%s')", timestamp)).collect(joining(COMMA_AND_SPACE_SEPARATOR));
    }

    private static String getKpiNames(final Collection<KpiDefinition> kpis) {
        return kpis.stream()
                .map(KpiDefinition::getName)
                .collect(joining(COMMA_AND_SPACE_SEPARATOR));
    }
}
