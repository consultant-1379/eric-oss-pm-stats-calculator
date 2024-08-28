/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.sink;

import static com.ericsson.oss.air.pm.stats.common.spark.sink.OnConflictQueryBuilder.buildUpsertQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.metrics.KpiMetric;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.spark.TaskContext;
import org.apache.spark.executor.OutputMetrics;
import org.apache.spark.executor.TaskMetrics;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.execution.datasources.jdbc.JdbcOptionsInWrite;
import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.jdbc.JdbcDialects;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.CreatableRelationProvider;
import org.apache.spark.sql.sources.DataSourceRegister;
import scala.Serializable;
import scala.collection.immutable.Map;

/**
 * Implementation of Spark {@link CreatableRelationProvider} and {@link DataSourceRegister} which acts as a JDBC sink. This takes a {@link Dataset},
 * reads it row by row, creates an 'INSERT ON CONFLICT' statement for each of them and persists them into the database using a JDBC connection.
 */
@Slf4j
public class JdbcUpsertSink implements CreatableRelationProvider, DataSourceRegister, Serializable {
    public static final String OPTION_KPI_NAMES_TO_CALCULATE = "kpiNamesToCalculate";

    private static final long serialVersionUID = 1L;

    private static final Pattern PRIMARY_KEY_PATTERN = Pattern.compile("\\s*,\\s*");

    @Override
    public String shortName() {
        return "jdbc-sink";
    }

    @Override
    @Nullable
    public BaseRelation createRelation(final SQLContext sqlContext, final SaveMode mode, final Map<String, String> parameters, final Dataset<Row> data) {
        final JdbcOptionsInWrite options = new JdbcOptionsInWrite(parameters);
        final JdbcDialect dialect = JdbcDialects.get(options.url());
        //  TODO: Can we obtain primary keys from the actual database? - be careful as the primary keys might be simple unique indexes for partitioned
        //        tables (see the database).
        final List<String> primaryKeys = primaryKeys(options);
        final List<String> columnNames = columnNames(data);
        final Set<String> kpiNamesToCalculate = kpiNamesToCalculate(options);

        final int changedKpiColumnCount = getChangedKpiColumnCount(columnNames, kpiNamesToCalculate);
        if (changedKpiColumnCount <= 0) {
            log.warn("No KPI column changes detected: table '{}', received columns '{}', expected columns '{}'!",
                    options.table(), columnNames,
                    String.join(",", kpiNamesToCalculate));
        }

        data.foreachPartition(iterator -> {
            final long changedRowCount = upsertPartition(options, dialect, iterator, columnNames, primaryKeys);
            final long changedKpiCount = changedKpiColumnCount * changedRowCount;

            // TODO: This metric update shouldn't be here, it should be in an event handler.
            if (changedKpiColumnCount > 0) {
                try {
                    updatePersistedKpiValueCount(options, changedKpiCount);
                } catch (final Exception e) {
                    // Not breaking the calculation if metric update fails, rather have bad statistic than failing calculations (edge case).
                    log.error("Incrementing '{}' metric with value '{}' failed!", KpiMetric.PERSISTED_CALCULATION_RESULTS, changedKpiCount, e);
                }
            }
        });

        return null;
    }

    private void updatePersistedKpiValueCount(
        final JdbcOptionsInWrite options,
        final long totalUpdatedColumns
    ) throws SQLException {
        final JdbcDialect dialect = JdbcDialects.get(options.url());

        final String sql =
                "INSERT INTO metric (name, \"value\") VALUES (?, ?) ON CONFLICT (name) DO UPDATE SET value = metric.value + excluded.value";

        try (final Connection connection = connection(dialect, options);
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, KpiMetric.PERSISTED_CALCULATION_RESULTS.toString());
            preparedStatement.setLong(2, totalUpdatedColumns);
            preparedStatement.executeUpdate();
        }
    }

    private static long upsertPartition(
            final JdbcOptionsInWrite options,
            final JdbcDialect dialect,
            final Iterator<Row> iterator,
            final List<String> columnNames,
            final List<String> primaryKeys
    ) throws SQLException {
        final OutputMetrics outputMetrics = outputMetrics();
        long totalRowCount = 0L;
        int batchRowCount = 0;

        boolean isCommitted = false;

        final int batchSize = options.batchSize();

        final java.util.Map<String, Object> columnNamesWithColumnValues = new HashMap<>();

        final Connection connection = connection(dialect, options);
        try {
            connection.setAutoCommit(false);

            try (final Statement statement = connection.createStatement()) {
                while (iterator.hasNext()) {
                    final Row row = iterator.next();

                    for (final String columnName : columnNames) {
                        columnNamesWithColumnValues.put(columnName, row.getAs(columnName));
                    }

                    totalRowCount++;
                    batchRowCount++;

                    //  TODO: Provide prepared statements for updates to align with security
                    statement.addBatch(buildUpsertQuery(options.table(), columnNamesWithColumnValues, primaryKeys, columnNames));

                    if(batchRowCount % batchSize == 0){
                        statement.executeBatch();
                        batchRowCount = 0;
                    }
                }

                if (batchRowCount > 0) {
                    statement.executeBatch();
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
            isCommitted = true;
        } catch (final SQLException e) {
            rollback(connection);
            throw e;
        } finally {
            closeConnection(connection);
            if (isCommitted) {
                outputMetrics.setRecordsWritten(totalRowCount);
            }
        }

        return totalRowCount;
    }

    private int getChangedKpiColumnCount(final List<String> columnNames, final Set<String> kpiNamesToCalculate) {
        return SetUtils.intersection(kpiNamesToCalculate, Set.copyOf(columnNames)).size();
    }

    private static void rollback(final Connection connection) throws SQLException {
        if (connection == null) {
            return;
        }

        connection.rollback();
    }

    private static void closeConnection(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (final SQLException e) {
            log.error("Could not close connection quietly, exception is being ignored as transaction succeeded", e);
        }
    }

    private static Connection connection(final JdbcDialect dialect, final JdbcOptionsInWrite options) {
        final int createConnectionOnDriver = -1;
        return dialect.createConnectionFactory(options).apply(createConnectionOnDriver);
    }

    private static OutputMetrics outputMetrics() {
        final TaskContext taskContext = TaskContext.get();
        final TaskMetrics taskMetrics = taskContext.taskMetrics();
        return taskMetrics.outputMetrics();
    }

    private static Set<String> kpiNamesToCalculate(final JdbcOptionsInWrite options) {
        return Set.copyOf(options.parameters().get(OPTION_KPI_NAMES_TO_CALCULATE).map(PRIMARY_KEY_PATTERN::split).map(Arrays::asList).getOrElse(() -> {
            throw new IllegalArgumentException(String.format("'%s' is required", OPTION_KPI_NAMES_TO_CALCULATE));
        }));
    }

    private static List<String> primaryKeys(final JdbcOptionsInWrite options) {
        return options.parameters().get("primaryKey").map(PRIMARY_KEY_PATTERN::split).map(Arrays::asList).getOrElse(() -> {
            throw new IllegalArgumentException("'primaryKey' is required");
        });
    }

    private static List<String> columnNames(final Dataset<Row> dataset) {
        return Arrays.asList(dataset.schema().fieldNames());
    }

}
