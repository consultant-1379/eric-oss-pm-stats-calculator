/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Utility class for executing loads/joins/etc on multiple {@link Dataset} instances.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatasetUtils {

    private static final String ERROR_LOADING_TABLE_CONN = "Error loading '{}' from '{}'";

    private static final String PROPERTY_DATABASE_TABLE = "dbtable";
    private static final String PROPERTY_DATABASE_QUERY = "query";
    private static final String PROPERTY_DRIVER = "driver";
    private static final String FORMAT_JDBC = "jdbc";
    private static final String PROPERTY_PASSWORD = "password";
    private static final String PROPERTY_URL = "url";
    private static final String PROPERTY_USER = "user";
    private static final String NUM_PARTITIONS = "numPartitions";
    private static final String PARTITION_COLUMN = "partitionColumn";
    private static final String LOWER_BOUND = "lowerBound";
    private static final String UPPER_BOUND = "upperBound";

    /**
     * Loads a {@link Dataset} instance based on the specified columns and primary keys of the input data.
     *
     * @param sparkSession   the {@link SparkSession}
     * @param tableName      the name of the table to be loaded into a {@link Dataset}
     * @param columns        the columns to load
     * @param jdbcConnection the JDBC connection URL
     * @param jdbcProperties the JDBC connection {@link Properties}
     * @return the requested {@link Dataset}, or an empty {@link Dataset} if one does not exist
     * @see SparkSession#emptyDataFrame()
     */
    public static Dataset<Row> loadDatasetWithSpecificColumnsAndPrimaryKeys(
            final SparkSession sparkSession,
            final String tableName,
            final Set<String> columns,
            final String jdbcConnection,
            final Properties jdbcProperties,
            final ReadingOptions readingOptions
    ) {
        try {
            return loadDatasetWithErrorIfItDoesNotExist(sparkSession, tableName, columns, jdbcConnection, jdbcProperties, readingOptions);
        } catch (final Exception e) {
            log.warn(ERROR_LOADING_TABLE_CONN, tableName, jdbcConnection, e);
            return sparkSession.emptyDataFrame();
        }
    }

    /**
     * Loads a partitioned {@link Dataset} instance based on the specified columns and primary keys of the input data using the min and max values of
     * partitionColumn as the partition limits.
     *
     * @param sparkSession    the {@link SparkSession}
     * @param tableName       the name of the table to be loaded into a {@link Dataset}
     * @param columns         the columns to load
     * @param jdbcConnection  the JDBC connection URL
     * @param jdbcProperties  the JDBC connection {@link Properties}
     * @param numPartitions   the number of partitions expected in the {@link Dataset}
     * @param partitionColumn the column to partition the {@link Dataset} on
     * @return the requested {@link Dataset}, or an empty {@link Dataset} if one does not exist
     * @see SparkSession#emptyDataFrame()
     */
    public static Dataset<Row> loadDatasetWithSpecificColumnsAndPrimaryKeys(
            final SparkSession sparkSession,
            final String tableName,
            final Set<String> columns,
            final String jdbcConnection,
            final Properties jdbcProperties,
            final long numPartitions,
            final String partitionColumn,
            final ReadingOptions readingOptions
    ) {
        try {
            return loadDatasetWithErrorIfItDoesNotExist(
                    sparkSession, tableName, columns, jdbcConnection, jdbcProperties, numPartitions, partitionColumn, readingOptions
            );
        } catch (final IllegalArgumentException e) {
            log.warn("Error loading dataset '{}' with partition column '{}'", tableName, partitionColumn);
            throw e;
        } catch (final Exception e) {
            log.info(ERROR_LOADING_TABLE_CONN, tableName, jdbcConnection, e);
            return sparkSession.emptyDataFrame();
        }
    }

    /**
     * Loads a {@link Dataset} instance based on the specified columns and the primary keys of the the input data.
     * <p>
     * If the {@link Dataset} does not exist, then an exception will be propagated.
     *
     * @param sparkSession   the {@link SparkSession}
     * @param tableName      the name of the table to be loaded into a {@link Dataset}
     * @param columns        the columns to load
     * @param jdbcConnection the JDBC connection URL
     * @param jdbcProperties the JDBC connection {@link Properties}
     * @return the requested {@link Dataset}
     */
    private static Dataset<Row> loadDatasetWithErrorIfItDoesNotExist(
            final SparkSession sparkSession,
            final String tableName,
            final Set<String> columns,
            final String jdbcConnection,
            final Properties jdbcProperties,
            final ReadingOptions readingOptions
    ) {
        final Properties properties = setProperty(PROPERTY_URL, jdbcConnection, jdbcProperties);

        columns.addAll(primaryKeys(sparkSession, properties, tableName));

        final String sqlTemplate = "SELECT ${columns} FROM ${table} ${filter}";
        final String query = StringSubstitutor.replace(sqlTemplate, Map.of(
                "columns", columns(columns),
                "table", tableName,
                "filter", deduceFilter(readingOptions)
        ));

        return query(sparkSession, properties, query).load();
    }

    /**
     * Loads the provided columns and the primary keys of a partitioned {@link Dataset} instance data using the min and max value of the partition
     * column as the partition limits.
     * <p>
     * If the {@link Dataset} does not exist, then an exception will be propagated.
     *
     * @param sparkSession    the {@link SparkSession}
     * @param tableName       the name of the table to be loaded into a {@link Dataset}
     * @param columns         the columns to load
     * @param jdbcConnection  the JDBC connection URL
     * @param jdbcProperties  the JDBC connection {@link Properties}
     * @param numPartitions   the number of partitions expected in the {@link Dataset}
     * @param partitionColumn the column to partition the {@link Dataset} on
     * @return the requested {@link Dataset}
     * @throws IllegalStateException
     *             if the Dataset cannot be loaded
     */
    private static Dataset<Row> loadDatasetWithErrorIfItDoesNotExist(
            final SparkSession sparkSession,
            final String tableName,
            final Set<String> columns,
            final String jdbcConnection,
            final Properties jdbcProperties,
            final long numPartitions,
            final String partitionColumn,
            final ReadingOptions readingOptions
    ) {
        //  TODO: add parameters to the ReadingOptions to reduce method signature complexity
        try {
            final Properties properties = setProperty(PROPERTY_URL, jdbcConnection, jdbcProperties);
            columns.addAll(primaryKeys(sparkSession, properties, tableName));
            columns.add(partitionColumn);

            final ColumnValueLimits partitionLimits = partitionLimits(sparkSession, tableName, partitionColumn, properties);

            final String sqlTemplate = "(SELECT ${columns} FROM ${table} ${filter}) AS ${table}";
            final String dbTable = StringSubstitutor.replace(sqlTemplate, Map.of(
                    "columns", columns(columns),
                    "table", tableName,
                    "filter", deduceFilter(readingOptions)
            ));

            return dbtable(sparkSession, properties, dbTable)
                    .option(NUM_PARTITIONS, numPartitions)
                    .option(PARTITION_COLUMN, partitionColumn)
                    .option(LOWER_BOUND, computeLowerBound(readingOptions, partitionLimits).toString())
                    .option(UPPER_BOUND, String.valueOf(partitionLimits.getMax()))
                    .load();
        } catch (final SQLException e) {
            log.warn("Partition Column '{}' provided is not exist in '{}'", partitionColumn, tableName);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Find the max and min non zero values of a partition column provided.
     *
     * @param sparkSession    the {@link SparkSession}
     * @param tableName       the name of the table to be loaded into a {@link Dataset}
     * @param partitionColumn the column of which to find the min and max values
     * @return the requested {@link ColumnValueLimits}
     * @throws SQLException if the partition column cannot read the min and max values of the partition column
     */
    public static ColumnValueLimits partitionLimits(
            final SparkSession sparkSession,
            final String tableName,
            final String partitionColumn,
            final Properties properties
    ) throws SQLException { //NOSONAR spark call can throw SQLException
        final String dbTable = String.format("(SELECT MIN(%1$s) AS min, MAX(%1$s) AS max FROM %2$s) as e", partitionColumn, tableName);

        final Row minAndMax = dbtable(sparkSession, properties, dbTable).load().head();

        final Object min;
        final Object max;
        if (minAndMax.get(0) == null || minAndMax.get(1) == null) {
            min = Timestamp.valueOf(LocalDateTime.MIN);
            max = Timestamp.valueOf(LocalDateTime.MAX);
        } else {
            min = minAndMax.get(0);
            max = minAndMax.get(1);
        }
        return new ColumnValueLimits(min, max);
    }

    /**
     * Retrieves a {@link List} of all the primary keys for the table 'tableName'.
     *
     * @param sparkSession the {@link SparkSession}
     * @param properties   the JDBC connection {@link Properties}
     * @param tableName    the name of the table from which to retrieve the primary keys
     * @return a {@link List} of all the primary keys from the table 'tableName'
     */
    private static List<String> primaryKeys(final SparkSession sparkSession, final Properties properties, final String tableName) {
        final List<String> primaryKeyList = new ArrayList<>(2);
        final String dbTable = String.format(
                "(SELECT a.attname FROM   pg_index i JOIN   pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                        "WHERE  i.indrelid = '%s'::regclass AND    i.indisprimary) as e",
                tableName);

        final Dataset<Row> primaryKeys = dbtable(sparkSession, properties, dbTable).load();
        for (final Row row : primaryKeys.collectAsList()) {
            primaryKeyList.add(row.getString(0));
        }
        return primaryKeyList;
    }

    private static DataFrameReader query(final SparkSession sparkSession, final Properties properties, final String query) {
        log.info("Executing read query '{}'", query);
        return readJdbc(sparkSession, properties).option(PROPERTY_DATABASE_QUERY, query);
    }

    private static DataFrameReader dbtable(final SparkSession sparkSession, final Properties properties, final String dbTable) {
        log.info("Executing read dbTable '{}'", dbTable);
        return readJdbc(sparkSession, properties).option(PROPERTY_DATABASE_TABLE, dbTable);
    }

    private static Properties setProperty(final String key, final String value, final Properties properties) {
        final Properties result = new Properties();
        result.putAll(properties);
        result.setProperty(key, value);
        return result;
    }

    private static DataFrameReader readJdbc(@NonNull final SparkSession sparkSession, @NonNull final Properties properties) {
        final String url = requireNonNull(properties.getProperty(PROPERTY_URL), PROPERTY_URL);
        final String user = requireNonNull(properties.getProperty(PROPERTY_USER), PROPERTY_USER);
        final String password = requireNonNull(properties.getProperty(PROPERTY_PASSWORD), PROPERTY_PASSWORD);
        final String driver = requireNonNull(properties.getProperty(PROPERTY_DRIVER), PROPERTY_DRIVER);

        return sparkSession.read().format(FORMAT_JDBC)
                .option(PROPERTY_URL, url)
                .option(PROPERTY_USER, user)
                .option(PROPERTY_PASSWORD, password)
                .option(PROPERTY_DRIVER, driver);
    }

    private static LocalDateTime computeLowerBound(final ReadingOptions readingOptions, final ColumnValueLimits partitionLimits) {
        return readingOptions.getAggregationBeginTimeLimit().orElseGet(() -> {
            final Timestamp partitionLimitMinimum = (Timestamp) partitionLimits.getMin();
            return partitionLimitMinimum.toLocalDateTime();
        });
    }


    private static String deduceFilter(final ReadingOptions readingOptions) {
        return readingOptions.getAggregationBeginTimeLimit().map(limit -> " WHERE aggregation_begin_time >= '" + limit + "' ").orElse(EMPTY);
    }

    private static String columns(final Collection<String> columns) {
        return columns.stream().map(DatasetUtils::enquote).collect(joining(", "));
    }

    private static String enquote(final String value) {
        return '"' + value + '"';
    }
}
