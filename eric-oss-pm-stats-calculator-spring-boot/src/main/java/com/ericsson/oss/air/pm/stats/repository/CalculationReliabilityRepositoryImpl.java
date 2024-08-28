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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationReliabilityRepository;

@Stateless
public class CalculationReliabilityRepositoryImpl implements CalculationReliabilityRepository {
    @Override
    public Map<String, LocalDateTime> findReliabilityThresholdByCalculationId(final UUID calculationId) {
        return projection("reliability_threshold", calculationId);
    }

    @Override
    public Map<String, LocalDateTime> findCalculationStartByCalculationId(final UUID calculationId) {
        return projection("calculation_start_time", calculationId);
    }

    @Override
    public void save(final Connection connection, final List<CalculationReliability> calculationReliabilities) {
        final String sql = "INSERT INTO kpi_service_db.kpi.calculation_reliability(" +
                "calculation_start_time, reliability_threshold, calculation_id, kpi_definition_id) " +
                "VALUES(?,?,?,?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (final CalculationReliability calculationReliability : calculationReliabilities) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(calculationReliability.getCalculationStartTime()));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(calculationReliability.getReliabilityThreshold()));
                preparedStatement.setObject(3, calculationReliability.getKpiCalculationId());
                preparedStatement.setInt(4, calculationReliability.getKpiDefinitionId());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Map<String, LocalDateTime> findMaxReliabilityThresholdByKpiName() {
        final String sql = "SELECT def.name, max(reliability.reliability_threshold) AS threshold " +
                "FROM kpi_service_db.kpi.calculation_reliability reliability " +
                "INNER JOIN kpi_service_db.kpi.kpi_definition def " +
                "ON def.id = reliability.kpi_definition_id " +
                "GROUP BY def.name";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {

            final Map<String, LocalDateTime> result = new HashMap<>();

            while (resultSet.next()) {
                final String name = resultSet.getString("name");
                final LocalDateTime threshold = resultSet.getTimestamp("threshold").toLocalDateTime();
                result.put(name, threshold);
            }

            return result;
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private Map<String, LocalDateTime> projection(final String columnName, final UUID calculationId) {
        final String sql = String.format(
                "SELECT reliability.%s, definitions.name " +
                        "FROM kpi_service_db.kpi.calculation_reliability reliability " +
                        "INNER JOIN kpi_service_db.kpi.kpi_definition definitions " +
                        "ON definitions.id = reliability.kpi_definition_id " +
                        "WHERE calculation_id = ?", columnName);

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final Map<String, LocalDateTime> reliabilities = new HashMap<>();

                while (resultSet.next()) {
                    final LocalDateTime reliability = resultSet.getTimestamp(columnName).toLocalDateTime();
                    final String kpiName = resultSet.getString("name");
                    reliabilities.put(kpiName, reliability);
                }
                return reliabilities;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}