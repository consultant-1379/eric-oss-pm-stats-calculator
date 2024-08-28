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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_BEGIN_TIME_COLUMN;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.COMMA_AND_SPACE_SEPARATOR;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static java.util.stream.Collectors.joining;
import static org.apache.spark.sql.functions.coalesce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.util.SqlCreatorUtils;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.functions;

/**
 * Utility class used to create the SQL to join KPI tables for the same alias.
 */
@RequiredArgsConstructor
final class JoinSqlCreator {

    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final int aggregationPeriod;

    /**
     * Creates the SQL query to join a list of KPIs to their alias dataset.
     *
     * @param alias
     *            the alias for the KPIs
     * @param newKpiDefinitions
     *            the {@link KpiDefinition} for the alias
     * @param alreadyCalculatedKpis
     *            the previous stage KPIs
     * @param secondDatasource
     *            the name of the dataset which the KPIs are currently stored in
     * @param aggregationElements
     *            the aggregation elements for the datasource table
     * @return SQL query to merge calculated KPIs for the same alias
     */
    String createJoinSql(final String alias, final Collection<KpiDefinition> newKpiDefinitions, final Collection<KpiDefinition> alreadyCalculatedKpis,
            final String secondDatasource, final List<String> aggregationElements) {

        final List<String> aggregationElementsSimplified = aliasOrColumnNames(aggregationElements);

        final boolean isFactDatasource = kpiDefinitionHelper.isKpiDataSourcesContainType(new ArrayList<>(newKpiDefinitions).get(0),
                DatasourceType.FACT);

        String sqlTemplate = "SELECT " +
                "${coalescedAggregationElements}, " +
                "${coalescedTimestampColumns}" +
                "${firstDatasourceKpis}" +
                "${kpiColumns} " +
                "FROM " +
                "${firstDatasource} " +
                "FULL OUTER JOIN " +
                "${secondDatasource} " +
                "ON ${joinAggregationColumns}";

        final Set<KpiDefinition> alreadyCalculatedKpisWhichAreNotAggregationElements = SqlCreatorUtils.filterKpiByName(alreadyCalculatedKpis,
                aggregationElementsSimplified);

        final Map<String, String> values = new HashMap<>(8);
        values.put("coalescedAggregationElements",
                createCoalesceAggregationElementExpression(alias, secondDatasource, aggregationElementsSimplified));
        values.put("coalescedTimestampColumns", aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT ? ""
                : createCoalesceTimestampElementExpression(alias, secondDatasource, isFactDatasource));
        values.put("firstDatasourceKpis", createAlreadyCalculatedKpiExpression(alreadyCalculatedKpisWhichAreNotAggregationElements, alias));
        values.put("kpiColumns", createNewKpiExpression(newKpiDefinitions, secondDatasource)); //iterate over kpiDefinitions
        values.put("firstDatasource", String.format("%1$s_kpis %1$s", alias));
        values.put("secondDatasource", secondDatasource);
        values.put("joinAggregationColumns", createJoinAggregationColumns(alias, secondDatasource, aggregationElementsSimplified));

        if (isFactDatasource && aggregationPeriod != DEFAULT_AGGREGATION_PERIOD_INT) {
            sqlTemplate += " AND ${joinTimestampColumns}";
            values.put("joinTimestampColumns", createJoinTimestampColumns(alias, secondDatasource));
        }

        return StringSubstitutor.replace(sqlTemplate, values);
    }

    private List<String> aliasOrColumnNames(final List<String> aggregationElements) {
        return sqlProcessorDelegator.aggregationElements(aggregationElements)
                                    .stream()
                                    .map(Reference::aliasOrColumnName)
                                    .distinct()
                                    .collect(java.util.stream.Collectors.toList());
    }

    private static String createCoalesceAggregationElementExpression(
            final String firstDatasource, final String secondDatasource, final List<String> aggregationElements
    ) {
        return aggregationElements.stream().map(aggregationElement -> {
            final Column firstColumn = col(firstDatasource, aggregationElement);
            final Column secondColumn = col(secondDatasource, aggregationElement);
            return coalesce(firstColumn, secondColumn).as(aggregationElement).toString();
        }).collect(joining(COMMA_AND_SPACE_SEPARATOR));
    }

    private static String createCoalesceTimestampElementExpression(
            final String firstDatasource, final String secondDatasource, final boolean isFactDatasource
    ) {
        if (isFactDatasource) {
            final Column firstColumn = col(firstDatasource, AGGREGATION_BEGIN_TIME_COLUMN);
            final Column secondColumn = col(secondDatasource, AGGREGATION_BEGIN_TIME_COLUMN);
            return coalesce(firstColumn, secondColumn).as(AGGREGATION_BEGIN_TIME_COLUMN).toString() + ", ";
        }

        return col(firstDatasource, AGGREGATION_BEGIN_TIME_COLUMN).as(AGGREGATION_BEGIN_TIME_COLUMN).toString() + ", ";
    }

    private static String createAlreadyCalculatedKpiExpression(final Set<KpiDefinition> kpiDefinitions, final String source) {
        if (CollectionUtils.isEmpty(kpiDefinitions)) {
            return StringUtils.EMPTY;
        }

        return createNewKpiExpression(kpiDefinitions, source) + ", ";
    }

    private static String createNewKpiExpression(final Collection<KpiDefinition> kpiDefinitions, final String source) {
        if (CollectionUtils.isEmpty(kpiDefinitions)) {
            return StringUtils.EMPTY;
        }

        return kpiDefinitions.stream()
                .map(kpiDefinition -> col(source, kpiDefinition.getName()))
                .map(Column::toString)
                .sorted()
                .collect(joining(COMMA_AND_SPACE_SEPARATOR));
    }

    private static String createJoinAggregationColumns(
            final String firstDatasource, final String secondDatasource, final List<String> aggregationElements
    ) {
        return aggregationElements.stream().map(aggregationElement -> {
            final Column left = col(firstDatasource, aggregationElement);
            final Column right = col(secondDatasource, aggregationElement);
            return left.equalTo(right);
        }).reduce(Column::and).map(Column::toString).orElseThrow(
                () -> new IllegalArgumentException("'aggregationElements' is empty")
        );
    }

    private static String createJoinTimestampColumns(final String firstDatasourceName, final String secondDatasourceName) {
        final Column left = col(firstDatasourceName, AGGREGATION_BEGIN_TIME_COLUMN);
        final Column right = col(secondDatasourceName, AGGREGATION_BEGIN_TIME_COLUMN);
        return left.equalTo(right).toString();
    }

    private static Column col(final String table, final String column) {
        return functions.col(table + '.' + column);
    }
}
