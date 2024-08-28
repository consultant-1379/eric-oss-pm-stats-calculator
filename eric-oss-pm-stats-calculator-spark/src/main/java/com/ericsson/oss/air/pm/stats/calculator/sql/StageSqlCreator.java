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

import static com.ericsson.oss.air.pm.stats.calculator.sql.SqlDateUtils.truncateToAggregationPeriod;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_BEGIN_TIME_COLUMN;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;

/**
 * Utility class used to create the SQL to calculate a single KPI.
 */
@RequiredArgsConstructor
public class StageSqlCreator {

    private static final String SELECT_SPACE = "SELECT ";

    private final SqlExpressionHelperImpl sqlExpressionHelper;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final int aggregationPeriodInMinutes;

    @Setter private SqlProcessorDelegator sqlProcessorDelegator;

    /**
     * Creates the SQL statement used to calculate the KPIs for given stage.
     *
     * @param kpis
     *            KPIs for which to generate a SQL statement. They have the same from-expression
     * @param aggregationElements
     *            the aggregation elements for the datasource table
     * @param stage
     *            the stage of the KPIs
     * @return the SQL statement used to calculate the supplied KPIs
     */
    String createSqlForKpis(final List<KpiDefinition> kpis, final List<String> aggregationElements, final int stage) {
        final Map<String, String> sqlTemplateValues = new HashMap<>(6);
        sqlTemplateValues.put("aggregationColumns", String.join(", ", aggregationElements));
        sqlTemplateValues.put("selectExpression", sqlExpressionHelper.collectSelectExpressionsAsSql(kpis));
        sqlTemplateValues.put("fromExpression", sqlExpressionHelper.getCleanedFromExpressionOrSchema(kpis.get(0)));
        sqlTemplateValues.put("aggregationColumnsGroupBy", createAggregationColumnsForGroupBy(aggregationElements));

        //received kpis were already grouped in DatasetExecutor by data source types
        final boolean isFactDatasource = kpiDefinitionHelper.isKpiDataSourcesContainType(kpis.get(0), DatasourceType.FACT);
        final boolean notDefaultAggPeriod = aggregationPeriodInMinutes != DEFAULT_AGGREGATION_PERIOD_INT;
        final boolean isTimedSql = (kpis.get(0).isSimple() || isFactDatasource) && notDefaultAggPeriod;
        final String sqlTemplate;
        if (isTimedSql) {
            //TODO simplify this logic to get proper table names
            final String fromExpression = sqlExpressionHelper.getFromExpression(kpis.get(0));
            final String tablesInFromExpression = sqlExpressionHelper.getFromExpressionWithoutSource(fromExpression);
            final String tableName = sqlExpressionHelper.getTableNameOnFromExpression(tablesInFromExpression);
            sqlTemplateValues.put("aggregationBeginTime", createTimestamp(tableName, stage));
            sqlTemplateValues.put("aggregationBeginTimeForGroupBy", createTimestampForGroupBy(tableName, stage));
            sqlTemplate = SELECT_SPACE
                    + "${aggregationColumns}, "
                    + "${aggregationBeginTime}, "
                    + "${selectExpression} "
                    + "FROM ${fromExpression} "
                    + "GROUP BY ${aggregationColumnsGroupBy}, ${aggregationBeginTimeForGroupBy}";
        } else {
            sqlTemplate = SELECT_SPACE
                    + "${aggregationColumns}, "
                    + "${selectExpression} "
                    + "FROM ${fromExpression} "
                    + "GROUP BY ${aggregationColumnsGroupBy}";
        }
        return StringSubstitutor.replace(sqlTemplate, sqlTemplateValues);
    }

    private String createAggregationColumnsForGroupBy(final List<String> aggregationElements) {
        final Set<String> columns = new LinkedHashSet<>();

        for (final Reference reference : sqlProcessorDelegator.aggregationElements(aggregationElements)) {
            final Column column = reference.requiredColumn();

            if (reference.isParameterizedAggregationElement()) {
                // Parameterized aggregation element does not have table attribute --> '${param.execution_id}' AS execution_id
                //  At this point the parameter is already resolved --> '294a7366-4e95-48e8-8861-24cbd7c7939d' AS execution_id
                // From `'294a7366-4e95-48e8-8861-24cbd7c7939d' AS execution_id` the '294a7366-4e95-48e8-8861-24cbd7c7939d' is the column what is
                // required here
                columns.add(column.getName());
            } else if (reference.isFunctionAggregationElement()) {
                columns.add(reference.parsedFunctionSql());
            } else {
                final String tableName = reference.requiredTable().getName();
                columns.add(tableName + '.' + column.getName());
            }
        }

        return String.join(", ", columns);
    }

    private String createTimestamp(final String tableName, final int stage) {
        final String aggregationBeginTime = aggregationBeginTime(tableName);
        if (stage == 1) {
            return String.format("MAX(%s) AS aggregation_begin_time", truncateToAggregationPeriod(aggregationBeginTime, aggregationPeriodInMinutes));
        } else {
            return String.format("FIRST(%s) AS %s", aggregationBeginTime, AGGREGATION_BEGIN_TIME_COLUMN);
        }
    }

    private String createTimestampForGroupBy(final String tableName, final int stage) {
        final String aggregationBeginTime = aggregationBeginTime(tableName);
        if (stage == 1) {
            return truncateToAggregationPeriod(aggregationBeginTime, aggregationPeriodInMinutes);
        } else {
            return aggregationBeginTime;
        }
    }

    private static String aggregationBeginTime(final String tableName) {
        return String.format("%s.%s", tableName, AGGREGATION_BEGIN_TIME_COLUMN);
    }
}
