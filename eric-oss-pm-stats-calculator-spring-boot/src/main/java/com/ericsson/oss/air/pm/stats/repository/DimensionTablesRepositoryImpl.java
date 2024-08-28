/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy.COLLECTION_ID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.DimensionTablesRepository;

@Stateless
public class DimensionTablesRepositoryImpl implements DimensionTablesRepository {

    @Override
    public void save(final Connection connection, final Collection<String> tableNames, final UUID calculationId) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.dimension_tables(calculation_id, table_name, collection_id) VALUES (?, ?, ?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (final String name : tableNames) {
                preparedStatement.setObject(1, calculationId);
                preparedStatement.setString(2, name);
                preparedStatement.setObject(3, COLLECTION_ID);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    @Override
    public List<String> findTableNamesForCalculation(final UUID calculationId) {
        final String sql = "SELECT table_name " +
                "FROM kpi_service_db.kpi.dimension_tables " +
                "WHERE calculation_id = ? " +
                "AND collection_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);
            preparedStatement.setObject(2, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<String> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(resultSet.getString(1));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Set<String> findLostTableNames() {
        final String sql =
                "SELECT table_name " +
                        "FROM kpi_service_db.kpi.dimension_tables dimension_tables " +
                        "INNER JOIN kpi_service_db.kpi.kpi_calculation calculation " +
                        "   ON calculation.calculation_id = dimension_tables.calculation_id " +
                        "WHERE calculation.state = ? " +
                        "AND calculation.collection_id = ? ";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, KpiCalculationState.LOST.name());
            preparedStatement.setObject(2, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final Set<String> result = new HashSet<>();

                while (resultSet.next()) {
                    result.add(resultSet.getString(1));
                }

                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
