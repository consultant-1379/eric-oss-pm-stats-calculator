/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.service.util.TabularParameterUtils.makeUniqueTableNamesForListOfTabularParameters;
import static lombok.AccessLevel.PUBLIC;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.DimensionTablesService;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class TabularParameterFacade {
    @Inject
    private DatabaseService databaseService;
    @Inject
    private DimensionTablesService dimensionTablesService;

    static final String UNABLE_TO_SAVE_TABULAR_PARAMETERS = "Unable to save tabular parameters";

    public void deleteTables(final KpiCalculationJob kpiCalculationJob) {
        if (kpiCalculationJob.isOnDemand()) {

            try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
                final List<String> tableNames = dimensionTablesService.findTableNamesForCalculation(kpiCalculationJob.getCalculationId());

                if (!tableNames.isEmpty()) {
                    databaseService.deleteTables(connection, tableNames);
                }
            } catch (final SQLException e) {
                log.error("Unable to delete tabular parameter tables for calculation :'{}'.", kpiCalculationJob.getCalculationId());
                throw new UncheckedSqlException(e);
            }
        }
    }

    public void saveTabularParameterTables(final UUID calculationId, final List<TabularParameters> tabularParameters) {
        if (!tabularParameters.isEmpty()) {
            try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
                databaseService.createTabularParameterTables(connection, getTabularParameterNames(tabularParameters), calculationId);

                saveTabularParameterValues(connection, tabularParameters, calculationId);

            } catch (final SQLException e) {
                log.error(UNABLE_TO_SAVE_TABULAR_PARAMETERS);
                throw new KpiPersistenceException(UNABLE_TO_SAVE_TABULAR_PARAMETERS, e);
            }
        }
    }

    public void saveTabularParameterDimensions(final UUID calculationId, final List<TabularParameters> tabularParameters) {
        if (!tabularParameters.isEmpty()) {
            try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
                final Collection<String> uniqueTableNames =
                        makeUniqueTableNamesForListOfTabularParameters(getTabularParameterNames(tabularParameters), calculationId);

                dimensionTablesService.save(connection, uniqueTableNames, calculationId);

            } catch (final SQLException e) {
                log.error(UNABLE_TO_SAVE_TABULAR_PARAMETERS);
                throw new KpiPersistenceException(UNABLE_TO_SAVE_TABULAR_PARAMETERS, e);
            }
        }
    }

    public void deleteLostTables() {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            final Set<String> tableNames = dimensionTablesService.findLostTableNames();

            databaseService.deleteTables(connection, tableNames);
        } catch (final SQLException e) {
            log.error("Unable to delete tabular parameters tables");
            throw new UncheckedSqlException(e);
        }
    }

    private Collection<String> getTabularParameterNames(final Collection<TabularParameters> tabularParameters) {
        return CollectionHelpers.transform(tabularParameters, TabularParameters::getName);
    }

    private void saveTabularParameterValues(final Connection connection, final List<TabularParameters> tabularParameters, final UUID calculationId) {
        for (final TabularParameters data : tabularParameters) {
            try {
                databaseService.saveTabularParameter(connection, data, calculationId);
            } catch (final SQLException | IOException e) {
                deleteTabularParameterTables(connection, calculationId, tabularParameters);
                throw new TabularParameterValidationException(UNABLE_TO_SAVE_TABULAR_PARAMETERS, e);
            }
        }
    }

    private void deleteTabularParameterTables(final Connection connection, final UUID calculationId, final List<TabularParameters> tabularParameters) {
        try {
            final Collection<String> uniqueTableNames = makeUniqueTableNamesForListOfTabularParameters(getTabularParameterNames(tabularParameters), calculationId);
            databaseService.deleteTables(connection, uniqueTableNames);
        } catch (final SQLException e) {
            log.error("Unable to delete tabular parameter tables");
            throw new UncheckedSqlException(e);
        }
    }
}
