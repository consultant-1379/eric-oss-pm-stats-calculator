/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Utility class used to execute SQL queries for integration tests.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlQueryExecutor {

    /**
     * Cleans the supplied tables by deleting all entries using the provided {@link Statement}. An SQL query will be created for each table names, and
     * used to clean the table.
     * <p>
     * If any of the SQL queries fail, the error will be logged, and the next table will be cleaned.
     *
     * @param jdbcConnection
     *            the JDBC connection URL
     * @param jdbcProperties
     *            the JDBC connection properties
     * @param tableNames
     *            the names of the tables to be cleaned
     * @see SqlQueryExecutor#createCleanTableSqlQuery(String)
     */
    public static void cleanTablesByTableName(final String jdbcConnection, final Properties jdbcProperties, final List<String> tableNames) {
        try (final Connection connection = DriverManager.getConnection(jdbcConnection, jdbcProperties);
                final Statement statement = connection.createStatement()) {
            for (final String tableName : tableNames) {
                cleanTable(statement, tableName);
            }
        } catch (final SQLException e) {
            log.warn("Error opening connection to clean tables: {}", tableNames, e);
        }
    }

    public static void cleanContentFromGivenTable(final String jdbcConnection, final Properties jdbcProperties, final String tableName, final List<String> kpiNames) {
        try (final Connection connection = DriverManager.getConnection(jdbcConnection, jdbcProperties);
                final Statement statement = connection.createStatement()) {
            cleanTableContent(statement, tableName, kpiNames);
        } catch (final SQLException e) {
            log.warn("Error opening connection to clean table contents: {}", tableName, e);
        }
    }

    private static void cleanTable(final Statement statement, final String tableToClean) {
        if (!tableExists(statement, tableToClean)) {
            return;
        }

        final String cleanTableCommand = createCleanTableSqlQuery(tableToClean);
        try {
            statement.execute(cleanTableCommand);
        } catch (final Exception e) {
            log.warn("Error cleaning table with command: '{}'", cleanTableCommand, e);
        }
    }

    private static void cleanTableContent(final Statement statement, final String tableToClean, final List<String> kpiNames) {
        if (!tableExists(statement, tableToClean)) {
            return;
        }

        final String cleanTableContentCommand = createCleanTableContentSqlQuery(tableToClean, kpiNames);
        try {
            statement.execute(cleanTableContentCommand);
        } catch (final Exception e) {
            log.warn("Error cleaning table content with command: '{}'", cleanTableContentCommand, e);
        }
    }

    private static boolean tableExists(final Statement statement, final String tableName) {
        final String tableExistsQuery = String.format("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '%s'", tableName);

        try (final ResultSet resultSet = statement.executeQuery(tableExistsQuery)) {
            resultSet.next();
            final int count = resultSet.getInt("count");

            if (count != 1) {
                log.warn("Found {} instances of table '{}'", count, tableName);
                return false;
            }

            return true;
        } catch (final Exception e) {
            log.warn("Unable to check if table '{}' exists", tableName, e);
            return false;
        }
    }

    /**
     * Creates an SQL query to clean a table by deleting all entries given the input table name. The output query will be in the form:
     *
     * <pre>
     *     DELETE FROM "tableName"
     * </pre>
     *
     * @param tableName
     *            the name of the table for which the SQL query will be created
     * @return the SQL query to clean the table
     */
    private static String createCleanTableSqlQuery(final String tableName) {
        return String.format("DELETE FROM \"%s\"", tableName);
    }

    /**
     * Creates an SQL query to clean a table by deleting given entries of the input table. The output query will be in the form:
     *
     * <pre>
     *     DELETE FROM "tableName"
     *     WHERE name = "kpiName1" OR name = "kpiName2" OR ...
     * </pre>
     *
     * @param tableName
     *            the name of the table for which the SQL query will be created
     * @param kpiNames
     *            the kpi definition names which will be deleted from the given input table
     * @return the SQL query to clean the table
     */

    private static String createCleanTableContentSqlQuery(final String tableName, final List<String> kpiNames) {
        String query = kpiNames.stream()
                                .map(kpiName -> String.format("%s.name = '%s'", tableName, kpiName))
                                .collect(Collectors.joining(" OR "));

        return String.format("DELETE FROM \"%s\" WHERE %s", tableName, query);
    }
}
