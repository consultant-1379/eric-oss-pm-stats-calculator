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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.util.SqlCreatorUtils;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils;

import org.apache.commons.text.StringSubstitutor;

/**
 * Utility class used to create the SQL used to aggregate the calculated KPIs.
 */
final class AggregationSqlCreator {

    private static final String TIMESTAMP_COLUMNS = "MAX(aggregation_begin_time) AS aggregation_begin_time, ";

    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final int aggregationPeriod;

    AggregationSqlCreator(final SqlProcessorDelegator sqlProcessorDelegator, final int aggregationPeriodInMinutes) {
        this.sqlProcessorDelegator = sqlProcessorDelegator;
        aggregationPeriod = aggregationPeriodInMinutes;
    }

    /**
     * Creates the SQL query to aggregate the KPIs from the current ROP and from the DB (within the correct time period).
     *
     * @param kpiDefinitions
     *            the {@link KpiDefinition} for all KPIs to be aggregated (pre-filtered by alias, datasource and aggregation period)
     * @param alias
     *            the alias for the KPIs
     * @param aggregationElements
     *            the aggregation elements for the datasource table.
     * @return SQL query to aggregate the KPIs
     */
    public String createAggregationSqlQuery(final Collection<KpiDefinition> kpiDefinitions, final String alias, final List<String> aggregationElements) {
        final List<String> aggregationElementsSimplified = aliasOrColumnNames(aggregationElements);

        final String sqlTemplate = "SELECT ${aggregationColumns}, " +
                "${timestampColumns}" +
                "${kpisWithExpressions} " +
                "FROM ${source} " +
                "GROUP BY ${aggregationColumns}${timestampColumnNames}";

        final Set<KpiDefinition> kpisWhichAreNotAggregationElements = SqlCreatorUtils.filterKpiByName(kpiDefinitions, aggregationElementsSimplified);
        final String commmaSeparatedAggregationElements = aggregationElementsSimplified.stream().sorted().collect(joining(COMMA_AND_SPACE_SEPARATOR));

        final Map<String, String> values = new HashMap<>(6);
        values.put("aggregationColumns", commmaSeparatedAggregationElements);
        values.put("timestampColumns", createTimestampColumns());
        values.put("kpisWithExpressions",
                   kpisWhichAreNotAggregationElements.stream()
                                                     .map(kpi -> KpiDefinitionUtils.makeKpiNameAsSql(kpi.getName(), kpi.getAggregationType()))
                                                     .collect(joining(COMMA_AND_SPACE_SEPARATOR))
        );
        values.put("source", createTemporaryDatasetViewName(alias));
        values.put("timestampColumnNames", createTimestampColumnNames());

        return StringSubstitutor.replace(sqlTemplate, values);
    }

    private List<String> aliasOrColumnNames(final List<String> aggregationElements) {
        return sqlProcessorDelegator.aggregationElements(aggregationElements)
                                    .stream()
                                    .map(Reference::aliasOrColumnName)
                                    .distinct()
                                    .collect(toList());
    }

    private String createTimestampColumns() {
        if (aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT) {
            return "";
        } else {
            return TIMESTAMP_COLUMNS;
        }
    }

    private String createTimestampColumnNames() {
        if (aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT) {
            return "";
        } else {
            return COMMA_AND_SPACE_SEPARATOR + AGGREGATION_BEGIN_TIME_COLUMN;
        }
    }
}
