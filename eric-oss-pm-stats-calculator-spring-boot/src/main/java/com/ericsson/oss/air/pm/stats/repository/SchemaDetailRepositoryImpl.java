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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.SchemaDetailRepository;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class SchemaDetailRepositoryImpl implements SchemaDetailRepository {

    @Override
    public Optional<SchemaDetail> findById(final Integer id) {
        final String sql = "SELECT id, topic, namespace " +
                           "FROM kpi_service_db.kpi.schema_details " +
                           "WHERE id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (final ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(allRowMapper(resultSet))
                        : Optional.empty();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Optional<SchemaDetail> findBy(final String topic, final String namespace) {
        final String sql = "SELECT id, topic, namespace " +
                           "FROM kpi_service_db.kpi.schema_details " +
                           "WHERE topic = ? " +
                           "AND namespace = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, topic);
            statement.setString(2, namespace);

            try (final ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(allRowMapper(resultSet))
                        : Optional.empty();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Integer save(final Connection connection, @NonNull final SchemaDetail schemaDetail) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.schema_details(topic, namespace) " +
                "VALUES (?,?)";

        try (final PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, schemaDetail.getTopic());
            statement.setString(2, schemaDetail.getNamespace());
            statement.executeUpdate();

            try (final ResultSet resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private SchemaDetail allRowMapper(@NonNull final ResultSet resultSet) throws SQLException {
        return SchemaDetail.builder()
                           .withId(resultSet.getInt("id"))
                           .withTopic(resultSet.getString("topic"))
                           .withNamespace(resultSet.getString("namespace"))
                           .build();
    }
}