/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class used to construct the name of columns/datasets based on KPI data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiNameUtils {

    public static final String NAME_TOKEN_SEPARATOR = "_";
    public static final String TEMP_DATASET_VIEW_SUFFIX = "kpis";

    /**
     * Creates the name for the temporary dataset view to allow Spark SQL statements to be executed against it. Will be in the form:
     *
     * <pre>
     *     &lt;alias&gt;_&lt;datasource&gt;_kpis
     * </pre>
     *
     * @param alias
     *            the alias of the KPI
     * @param datasource
     *            the datasource of the KPI
     * @return the temporary view name
     */
    public static String createTemporaryDatasetViewName(final String alias, final String datasource) {
        return String.join(NAME_TOKEN_SEPARATOR, alias, datasource, TEMP_DATASET_VIEW_SUFFIX);
    }

    /**
     * Creates the name for the temporary dataset view to allow Spark SQL statements to be executed against it. Will be in the form:
     *
     * <pre>
     *     &lt;alias&gt;_kpis
     * </pre>
     *
     * @param alias
     *            the alias of the KPI
     * @return the temporary view name
     */
    public static String createTemporaryDatasetViewName(final String alias) {
        return String.join(NAME_TOKEN_SEPARATOR, alias, TEMP_DATASET_VIEW_SUFFIX);
    }

    /**
     * Creates the name for the output table of KPIs, to be persisted to the DB. Will be in the form:
     *
     * <pre>
     *     kpi_&lt;alias&gt;_&lt;aggregationPeriodInMinutes&gt;
     * </pre>
     *
     * @param alias
     *            the alias of the KPI
     * @param aggregationPeriodInMinutes
     *            the aggregation period of the KPI in minutes
     * @return the name of the output table to be persisted
     */
    public static String createOutputTableName(final String alias, final int aggregationPeriodInMinutes) {
        if (aggregationPeriodInMinutes == DEFAULT_AGGREGATION_PERIOD_INT) {
            return String.join(NAME_TOKEN_SEPARATOR, "kpi", alias, "");
        } else {
            return String.join(NAME_TOKEN_SEPARATOR, "kpi", alias, String.valueOf(aggregationPeriodInMinutes));
        }
    }

    public static Table createOutputTable(final String alias, final int aggregationPeriodInMinutes) {
        return Table.of(createOutputTableName(alias, aggregationPeriodInMinutes));
    }
}
