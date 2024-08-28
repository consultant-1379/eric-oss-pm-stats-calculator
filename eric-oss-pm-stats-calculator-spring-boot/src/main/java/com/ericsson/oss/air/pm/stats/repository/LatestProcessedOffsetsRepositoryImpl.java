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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.LatestProcessedOffsetsRepository;

@Stateless
public class LatestProcessedOffsetsRepositoryImpl implements LatestProcessedOffsetsRepository {

    @Override
    public List<LatestProcessedOffset> findAll() {
        final String sql = "SELECT offsets.id, " +
                "       topic_name, " +
                "       topic_partition, " +
                "       topic_partition_offset, " +
                "       execution_group_id, " +
                "       from_kafka, " +
                "       exe.execution_group, " +
                "       collection_id " +
                "FROM kpi.latest_processed_offsets offsets " +
                "INNER JOIN kpi.kpi_execution_groups exe ON offsets.execution_group_id = exe.id " +
                "WHERE collection_id = ? ";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, CollectionIdProxy.COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<LatestProcessedOffset> latestProcessedOffsets = new ArrayList<>();
                while (resultSet.next()) {
                    latestProcessedOffsets.add(getLatestProcessedOffset(resultSet));
                }
                return latestProcessedOffsets;
            }
        } catch (final SQLException exception) {
            throw new UncheckedSqlException(exception);
        }
    }

    @Override
    public List<LatestProcessedOffset> findAllForExecutionGroup(final String executionGroup) {
        final String sql = "SELECT offsets.id, " +
                "       topic_name, " +
                "       topic_partition, " +
                "       topic_partition_offset, " +
                "       execution_group_id, " +
                "       from_kafka, " +
                "       exe.execution_group, " +
                "       collection_id " +
                "FROM kpi.latest_processed_offsets offsets " +
                "INNER JOIN kpi.kpi_execution_groups exe ON offsets.execution_group_id = exe.id " +
                "WHERE exe.execution_group = ? AND collection_id = ? ";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, executionGroup);
            statement.setObject(2, CollectionIdProxy.COLLECTION_ID);

            final ResultSet resultSet = statement.executeQuery();

            final List<LatestProcessedOffset> latestProcessedOffsets = new ArrayList<>();
            while (resultSet.next()) {
                latestProcessedOffsets.add(getLatestProcessedOffset(resultSet));
            }
            return latestProcessedOffsets;
        } catch (final SQLException exception) {
            throw new UncheckedSqlException(exception);
        }
    }

    @Override
    public void deleteOffsetsByTopic(final Collection<String> topics) {
        final String sql = "DELETE FROM kpi.latest_processed_offsets WHERE topic_name = ANY(?)";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setArray(1, connection.createArrayOf("VARCHAR", topics.toArray()));

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new UncheckedSqlException(exception);
        }
    }

    private ExecutionGroup getExecutionGroup(final ResultSet resultSet) throws SQLException {
        return ExecutionGroup.builder()
                .withId(resultSet.getInt("execution_group_id"))
                .withName(resultSet.getString("execution_group"))
                .build();
    }

    private LatestProcessedOffset getLatestProcessedOffset(final ResultSet resultSet) throws SQLException {
        return LatestProcessedOffset.builder()
                .withId(resultSet.getInt("id"))
                .withTopicName(resultSet.getString("topic_name"))
                .withTopicPartition(resultSet.getInt("topic_partition"))
                .withTopicPartitionOffset(resultSet.getLong("topic_partition_offset"))
                .withFromKafka(resultSet.getBoolean("from_kafka"))
                .withExecutionGroup(getExecutionGroup(resultSet))
                .withCollectionId(resultSet.getObject("collection_id", UUID.class))
                .build();
    }
}