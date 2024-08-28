/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util.sql;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.KPI_DEFINITION_TABLE_NAME;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.LATEST_SOURCE_DATA;
import static com.ericsson.oss.air.pm.stats.test.util.sql.SqlQueryExecutor.cleanContentFromGivenTable;
import static com.ericsson.oss.air.pm.stats.test.util.sql.SqlQueryExecutor.cleanTablesByTableName;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to clean up the database after integration tests.
 */
public final class DatabaseCleaner {

    private DatabaseCleaner() {

    }

    /**
     * Deletes all data from the kpi_definition, latest_source_data and kpi_calculation tables added by
     * <code>eric-oss-pm-stats-calculator</code> during the integration tests.
     */

    public static void cleanKpiInputTables() {
        final List<String> tablesToClean = new ArrayList<>(3);
        tablesToClean.add(KPI_DEFINITION_TABLE_NAME);
        tablesToClean.add(LATEST_SOURCE_DATA);

        cleanTablesByTableName(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties(), tablesToClean);
    }

    /**
     * Deletes all data from the kpi aggregation tables added by <code>eric-oss-pm-stats-calculator</code> during the integration tests.
     */
    public static void cleanKpiOutputTables() {
        final List<String> tablesToClean = new ArrayList<>(7);
        tablesToClean.add("kpi_cell_guid_1440");
        tablesToClean.add("kpi_cell_guid_60");
        tablesToClean.add("kpi_relation_guid_source_guid_target_guid_60");
        tablesToClean.add("kpi_area_1440");
        tablesToClean.add("kpi_cell_sector_1440");
        tablesToClean.add("kpi_complex_60");
        cleanTablesByTableName(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties(), tablesToClean);
    }

    public static void cleanKpiDefinitionTable(final List<String> kpiNames) {
        cleanContentFromGivenTable(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties(), "kpi_definition", kpiNames);
    }
}