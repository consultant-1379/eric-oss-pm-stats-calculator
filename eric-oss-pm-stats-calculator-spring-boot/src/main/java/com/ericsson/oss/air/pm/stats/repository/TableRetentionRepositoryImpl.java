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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity.RetentionPeriodTableEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;

@Stateless
public class TableRetentionRepositoryImpl implements TableRetentionRepository {
    @Override
    public void saveAll(final Connection connection, final List<RetentionPeriodTableEntity> retentionPeriodTableEntities) throws SQLException {
        final String sql =
                "INSERT INTO kpi_service_db.kpi.retention_configurations_table_level(kpi_collection_id, table_name, retention_period_in_days) " +
                        "VALUES (?,?,?) ON CONFLICT (kpi_collection_id, table_name) DO UPDATE SET retention_period_in_days = ?";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (final RetentionPeriodTableEntity retentionPeriodTableEntity : retentionPeriodTableEntities) {
                preparedStatement.setObject(1, retentionPeriodTableEntity.getKpiCollectionId());
                preparedStatement.setString(2, retentionPeriodTableEntity.getTableName());
                preparedStatement.setInt(3, retentionPeriodTableEntity.getRetentionPeriodInDays());
                preparedStatement.setInt(4, retentionPeriodTableEntity.getRetentionPeriodInDays());

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        }
    }

    @Override
    public List<RetentionPeriodTableEntity> findByCollectionId(final UUID collectionId) {
        final String sql = "SELECT * FROM kpi_service_db.kpi.retention_configurations_table_level WHERE kpi_collection_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, collectionId);

            try (final ResultSet rs = preparedStatement.executeQuery()) {
                final List<RetentionPeriodTableEntity> entities = new ArrayList<>();

                while (rs.next()) {
                    final long id = rs.getLong("id");
                    final UUID kpiCollectionId = rs.getObject("kpi_collection_id", UUID.class);
                    final String tableName = rs.getString("table_name");
                    final int retentionPeriodInDays = rs.getInt("retention_period_in_days");

                    entities.add(retentionPeriodTableEntity(id, kpiCollectionId, tableName, retentionPeriodInDays));
                }

                return entities;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void deleteRetentionForTables(final Connection connection, final String tableName) throws SQLException {
        final String sql = "DELETE FROM kpi_service_db.kpi.retention_configurations_table_level WHERE table_name = ?";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
            preparedStatement.executeUpdate();
        }
    }


    private RetentionPeriodTableEntity retentionPeriodTableEntity(final long id, final UUID kpiCollectionId, final String tableName, final int retentionPeriodInDays) {
        final RetentionPeriodTableEntityBuilder builder = RetentionPeriodTableEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(kpiCollectionId);
        builder.withTableName(tableName);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }
}
