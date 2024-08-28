/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;

@Local
public interface DimensionTablesService {
    /**
     * Inserts the table names related to the calculation ID to table.
     *
     * @param connection    {@link Connection} connection to the database.
     * @param tableNames    the names of the tables.
     * @param calculationId {@link UUID} ID of the calculation.
     * @throws SQLException If there are any errors creating the table.
     */
    void save(Connection connection, Collection<String> tableNames, UUID calculationId) throws SQLException;

    /**
     * Finds the tabular parameter names belonging to a calculation request.
     *
     * @param calculationId the {@link UUID} of calculation ID of request.
     * @return {@link List<String>} containing the tabular parameter names.
     */
    List<String> findTableNamesForCalculation(UUID calculationId);

    /**
     * Finds tabular parameter table names that has attached {@link Calculation} in {@link KpiCalculationState#LOST} state.
     *
     * @return {@link Set} containing tabular parameter table names.
     */
    Set<String> findLostTableNames();
}
