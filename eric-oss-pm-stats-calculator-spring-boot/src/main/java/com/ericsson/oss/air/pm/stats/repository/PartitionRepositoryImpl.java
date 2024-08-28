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
import static com.ericsson.oss.air.pm.stats.repository.util.PartitionUtils.createPartitionSql;
import static com.ericsson.oss.air.pm.stats.repository.util.TableUtils.createUniqueIndexSql;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.PartitionRepository;
import com.ericsson.oss.air.pm.stats.repository.util.ColumnUtils;
import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.statement.SqlStatementExecutor;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlTableModifier;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class PartitionRepositoryImpl implements PartitionRepository {
    @Inject
    private SqlTableModifier sqlTableModifier;

    @Override
    public void createPartitions(final Connection connection, final List<? extends Partition> partitions) throws SQLException {
        final List<String> sqlStatements = new ArrayList<>(partitions.size() << 1);

        for (final Partition partition : partitions) {
            log.info("Create partition for '{}' with name: '{}' with '{}' index from columns",
                    partition.getTableName(),
                    partition.getPartitionName(),
                    partition.getUniqueIndexColumns());

            sqlStatements.add(createPartitionSql(partition));
            sqlStatements.add(createUniqueIndexSql(partition));
        }

        try (final Statement statement = connection.createStatement()) {
            for (final String sqlStatement : sqlStatements) {
                statement.addBatch(sqlStatement);
            }
            statement.executeBatch();
        }
    }

    @Override
    public void dropPartitions(final Connection connection, final List<String> partitionNames) throws SQLException {
        try (final Statement statement = connection.createStatement()) {

            for (final String partition : partitionNames) {
                final String sql = prepareSqlScript(partition);
                statement.addBatch(sql);
            }

            statement.executeBatch();
            log.info("Deleted partitions '{}' ", partitionNames);
        }
    }

    //TODO: After uplift to PostgreSQl 14 or higher please change DETACH PARTITION ... CONCURRENTLY
    private String prepareSqlScript(String partition) {
        String tableName = deduceTableName(partition);
        return String.format(
                "DO $$\n" +
                        "BEGIN\n" +
                        "    IF EXISTS (SELECT relname FROM pg_class WHERE relname='%s' ) THEN\n" +
                        "        EXECUTE 'ALTER TABLE %s DETACH PARTITION %s';\n" +
                        "        EXECUTE 'DROP TABLE IF EXISTS %s';\n" +
                        "    END if;\n" +
                        "END\n" +
                        "$$;",
                partition, tableName, partition, partition);
    }

    @Override
    public List<String> getPartitionNamesForTable(final String tableName) {
        final String sql = "SELECT child.relname as partition_name " +
                "FROM pg_inherits " +
                "JOIN pg_class parent " +
                "     ON pg_inherits.inhparent = parent.oid " +
                "JOIN pg_class child " +
                "     ON pg_inherits.inhrelid = child.oid " +
                "JOIN pg_namespace nmsp_parent " +
                "     ON nmsp_parent.oid = parent.relnamespace " +
                "JOIN pg_namespace nmsp_child " +
                "     ON nmsp_child.oid = child.relnamespace " +
                "WHERE parent.relname=?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<String> partitions = new ArrayList<>();
                while (resultSet.next()) {
                    partitions.add(resultSet.getString("partition_name"));
                }
                return partitions;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void dropUniqueIndex(final Connection connection, final PartitionUniqueIndex partitionUniqueIndex) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.dropUniqueIndex(partitionUniqueIndex));
    }

    @Override
    public void createUniqueIndex(final Connection connection, final PartitionUniqueIndex uniqueIndex) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.createUniqueIndex(uniqueIndex));
    }

    @Override
    public List<PartitionUniqueIndex> findAllPartitionUniqueIndexes(@NonNull final Table table) {
        final String sql = "SELECT * FROM pg_indexes WHERE tablename LIKE ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, String.format("%s_p_%%", table.getName()));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<PartitionUniqueIndex> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(new PartitionUniqueIndex(resultSet.getString("tablename"),
                            resultSet.getString("indexname"),
                            ColumnUtils.parseUniqueIndexColumns(resultSet.getString("indexdef"))));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private String deduceTableName(String partition) {
        return partition.substring(0, partition.indexOf("_p_"));
    }

}
