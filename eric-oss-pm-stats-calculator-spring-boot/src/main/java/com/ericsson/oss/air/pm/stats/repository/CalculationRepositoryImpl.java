/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.repository.util.ArrayUtils;
import com.ericsson.oss.air.pm.stats.repository.util.ColumnUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class CalculationRepositoryImpl implements CalculationRepository {

    @Override
    public void save(final Connection connection, final Calculation calculation) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.kpi_calculation(calculation_id, " +
                "                                               time_created, " +
                "                                               time_completed, " +
                "                                               state, " +
                "                                               execution_group, " +
                "                                               parameters, " +
                "                                               kpi_type, " +
                "                                               collection_id) " +
                "VALUES (?, ? ,? ,? ,?, ?, ?, ?)";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculation.getCalculationId());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(calculation.getTimeCreated()));
            preparedStatement.setTimestamp(3, ColumnUtils.nullableTimestamp(calculation.getTimeCompleted()));
            preparedStatement.setString(4, calculation.getKpiCalculationState().name());
            preparedStatement.setString(5, calculation.getExecutionGroup());
            preparedStatement.setString(6, calculation.getParameters());
            preparedStatement.setString(7, calculation.getKpiType().name());
            preparedStatement.setObject(8, COLLECTION_ID);

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void updateTimeCompletedAndStateByCalculationId(final Connection connection,
                                                           final LocalDateTime timeCompleted,
                                                           final KpiCalculationState kpiCalculationState,
                                                           final UUID calculationId) throws SQLException {
        final String sql = "UPDATE kpi_service_db.kpi.kpi_calculation " +
                "SET time_completed = ?, " +
                "state = ? " +
                "WHERE calculation_id = ? " +
                "AND collection_id = ?";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(timeCompleted));
            preparedStatement.setString(2, kpiCalculationState.name());
            preparedStatement.setObject(3, calculationId);
            preparedStatement.setObject(4, COLLECTION_ID);

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void updateStateByCalculationId(final Connection connection,
                                           final KpiCalculationState kpiCalculationState,
                                           final UUID calculationId) throws SQLException {
        final String sql = "UPDATE kpi_service_db.kpi.kpi_calculation " +
                "SET state = ? " +
                "WHERE calculation_id = ? " +
                "  AND collection_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, kpiCalculationState.name());
            preparedStatement.setObject(2, calculationId);
            preparedStatement.setObject(3, COLLECTION_ID);

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void updateStateByStates(final Connection connection,
                                    final KpiCalculationState newState,
                                    final Collection<KpiCalculationState> statesToModify) throws SQLException {
        final String sql = "UPDATE kpi_service_db.kpi.kpi_calculation " +
                "SET state = ? " +
                "WHERE state = ANY(?) " +
                "  AND collection_id = ?";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newState.name());
            preparedStatement.setArray(2, createArray(connection, statesToModify));
            preparedStatement.setObject(3, COLLECTION_ID);

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<UUID> updateStateByStateAndTimeCreated(final Connection connection,
                                                       final KpiCalculationState newState,
                                                       final KpiCalculationState oldState,
                                                       final LocalDateTime timeCreated) throws SQLException {
        final String sql = "UPDATE kpi_service_db.kpi.kpi_calculation " +
                "SET state = ? " +
                "WHERE state = ? " +
                "  AND execution_group != ? " +
                "  AND time_created < ? " +
                "  AND collection_id = ?";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, newState.name());
            preparedStatement.setString(2, oldState.name());
            preparedStatement.setString(3, KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(timeCreated));
            preparedStatement.setObject(5, COLLECTION_ID);

            preparedStatement.executeUpdate();

            final List<UUID> updatedRows = new ArrayList<>();
            try (final ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    updatedRows.add(resultSet.getObject(1, UUID.class));
                }
                return updatedRows;
            }
        }
    }

    @Override
    public long countByExecutionGroupAndStates(final String executionGroup, final Collection<KpiCalculationState> kpiCalculationStates) {
        final String sql = "SELECT COUNT(*) AS count " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE execution_group = ? " +
                "  AND state = ANY(?) " +
                "  AND collection_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, executionGroup);
            preparedStatement.setArray(2, createArray(connection, kpiCalculationStates));
            preparedStatement.setObject(3, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong("count");
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public long countByExecutionGroupsAndStates(@NonNull final Collection<String> executionGroups, final Collection<KpiCalculationState> kpiCalculationStates) {
        final String sql = "SELECT COUNT(*) AS count " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE execution_group = ANY(?) " +
                "  AND state = ANY(?) " +
                "  AND collection_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setArray(1, connection.createArrayOf("VARCHAR", executionGroups.toArray()));
            preparedStatement.setArray(2, createArray(connection, kpiCalculationStates));
            preparedStatement.setObject(3, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong("count");
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }


    @Override
    public Map<KpiCalculationState, Long> countByStates() {
        return countByStates(List.of(KpiCalculationState.values()));
    }

    @Override
    public Map<KpiCalculationState, Long> countByStates(final Collection<KpiCalculationState> kpiCalculationStates) {
        final String sql = "SELECT state, COUNT(*) AS count " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE state = ANY(?) " +
                "GROUP BY state";

        final Map<KpiCalculationState, Long> result = new EnumMap<>(KpiCalculationState.class);

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setArray(1, createArray(connection, kpiCalculationStates));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.put(KpiCalculationState.valueOf(resultSet.getString("state")), resultSet.getLong("count"));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Optional<KpiCalculationState> findByCalculationId(final UUID calculationId) {
        final String sql = "SELECT state " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE calculation_id = ? " +
                "  AND collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);
            preparedStatement.setObject(2, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(KpiCalculationState.valueOf(resultSet.getString("state")))
                        : Optional.empty();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<Calculation> findAllByState(@NonNull final KpiCalculationState kpiCalculationState) {
        final String sql = "SELECT * " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE state = ? " +
                "  AND collection_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, kpiCalculationState.name());
            preparedStatement.setObject(2, COLLECTION_ID);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<Calculation> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(parseAllColumns(resultSet));
                }

                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Optional<Calculation> findCalculationToSendToExporter(final UUID calculationId) {
        final String sql = "SELECT * " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE state = ? " +
                "  AND calculation_id = ? " +
                "  AND collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, KpiCalculationState.FINALIZING.name());
            preparedStatement.setObject(2, calculationId);
            preparedStatement.setObject(3, COLLECTION_ID);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(parseAllColumns(resultSet))
                        : Optional.empty();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<Calculation> findCalculationsByTimeCreatedIsAfter(final LocalDateTime target) {
        final String sql = "SELECT * " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE ? <= kpi_calculation.time_created " +
                "  AND collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(target));
            preparedStatement.setObject(2, COLLECTION_ID);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<Calculation> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(parseAllColumns(resultSet));
                }

                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public long deleteByTimeCreatedLessThen(final Connection connection, final LocalDateTime localDateTime) throws SQLException {
        final String sql = "DELETE FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE kpi_calculation.time_created < ? " +
                "  AND collection_id = ?";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(localDateTime));
            preparedStatement.setObject(2, COLLECTION_ID);
            return preparedStatement.executeUpdate();
        }
    }

    @Override
    public Optional<String> findExecutionGroupByCalculationId(final UUID calculationId) {
        final String sql = "SELECT execution_group " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE calculation_id = ? " +
                "  AND collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);
            preparedStatement.setObject(2, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("execution_group"));
                }
            }
            return Optional.empty();
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public LocalDateTime getLastComplexCalculationReliability(final String complexExecutionGroup) {
        final String sql = "SELECT CASE count(*) WHEN 0 THEN to_timestamp('1970-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') ELSE max(reliability.reliability_threshold) END AS time_completed " +
                "FROM kpi_service_db.kpi.kpi_calculation calculation " +
                "INNER JOIN kpi_service_db.kpi.calculation_reliability reliability on calculation.calculation_id = reliability.calculation_id " +
                "WHERE execution_group = ?" +
                "AND state = ANY(?) " +
                "AND calculation.collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final Array successfullyFinishedStates = ArrayUtils.successfullyFinishedStates(connection);
            preparedStatement.setObject(1, complexExecutionGroup);
            preparedStatement.setArray(2, successfullyFinishedStates);
            preparedStatement.setObject(3, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getTimestamp("time_completed").toLocalDateTime();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Optional<KpiType> findKpiTypeByCalculationId(final UUID calculationId) {
        final String sql = "SELECT kpi_type " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE calculation_id = ? " +
                "  AND collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);
            preparedStatement.setObject(2, COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(KpiType.valueOf(resultSet.getString("kpi_type")))
                        : Optional.empty();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private Calculation parseAllColumns(@NonNull final ResultSet resultSet) throws SQLException {
        return Calculation.builder()
                .withCalculationId(resultSet.getObject("calculation_id", UUID.class))
                .withTimeCreated(resultSet.getTimestamp("time_created").toLocalDateTime())
                .withTimeCompleted(Optional.ofNullable(resultSet.getTimestamp("time_completed")).map(Timestamp::toLocalDateTime).orElse(null))
                .withKpiCalculationState(KpiCalculationState.valueOf(resultSet.getString("state")))
                .withParameters(resultSet.getString("parameters"))
                .withExecutionGroup(resultSet.getString("execution_group"))
                .withKpiType(KpiType.valueOf(resultSet.getString("kpi_type")))
                .withCollectionId(resultSet.getObject("collection_id", UUID.class))
                .build();
    }

    private Array createArray(final Connection connection, final Collection<KpiCalculationState> kpiCalculationStates) throws SQLException {
        return connection.createArrayOf("VARCHAR", kpiCalculationStates.stream().map(Enum::name).toArray());
    }
}
