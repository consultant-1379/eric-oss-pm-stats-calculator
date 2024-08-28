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
import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity.RetentionPeriodCollectionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;

@Stateless
public class CollectionRetentionRepositoryImpl implements CollectionRetentionRepository {

    @Override
    public long save(final Connection connection, final RetentionPeriodCollectionEntity retentionPeriodCollectionEntity) throws SQLException {
        final String sql =
                "INSERT INTO kpi_service_db.kpi.retention_configurations_collection_level(kpi_collection_id, retention_period_in_days) " +
                        "VALUES (?,?) ON CONFLICT (kpi_collection_id) DO UPDATE SET retention_period_in_days = ?";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql, RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, retentionPeriodCollectionEntity.getKpiCollectionId());
            preparedStatement.setInt(2, retentionPeriodCollectionEntity.getRetentionPeriodInDays());
            preparedStatement.setInt(3, retentionPeriodCollectionEntity.getRetentionPeriodInDays());

            preparedStatement.executeUpdate();

            try (final ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getLong(1);
            }
        }
    }

    @Override
    public Optional<RetentionPeriodCollectionEntity> findByCollectionId(final UUID collectionId) {
        final String sql = "SELECT * FROM kpi_service_db.kpi.retention_configurations_collection_level WHERE kpi_collection_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, collectionId);

            try (final ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    final long id = rs.getLong("id");
                    final UUID kpiCollectionId = (UUID) rs.getObject("kpi_collection_id");
                    final int retentionPeriodInDays = rs.getInt("retention_period_in_days");

                    return Optional.of(retentionPeriodCollectionEntity(id, kpiCollectionId, retentionPeriodInDays));
                }
                return Optional.empty();
            }

        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private RetentionPeriodCollectionEntity retentionPeriodCollectionEntity(final long id, final UUID kpiCollectionId, final int retentionPeriodInDays) {
        final RetentionPeriodCollectionEntityBuilder builder = RetentionPeriodCollectionEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(kpiCollectionId);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }
}
