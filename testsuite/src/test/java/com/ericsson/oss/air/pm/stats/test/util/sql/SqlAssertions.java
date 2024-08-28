/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util.sql;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlAssertions {

    /**
     * Executes a "SELECT" on the given table for the given columns, then checks each row against the input expected rows.
     *
     * @param tableName     the name of the table to verify against
     * @param wantedColumns the names of the columns to be verified
     * @param expectedRows  the expected values of each row in the table, as a comma-separated {@link String}
     */
    public static void assertTableContent(final String tableName, final List<String> wantedColumns, final List<String> expectedRows) {
        final String columns = getColumns(wantedColumns);

        final List<String> actualRows = retrieveRows(tableName, columns);
        Collections.sort(expectedRows);
        Collections.sort(actualRows);

        Assertions.assertThat(actualRows).as("Number of rows does not match the expected number").hasSameSizeAs(expectedRows);
        Assertions.assertThat(actualRows).isEqualTo(expectedRows);
    }

    public static void assertCountQuery(final int expectedCount, final String tableName) {
        final String sqlQuery = "SELECT COUNT(*) FROM " + tableName;

        Assertions.assertThat(executeCountSqlQuery(sqlQuery))
                  .as(String.format("Count query '%s' failed", sqlQuery))
                  .isEqualTo(expectedCount);
    }

    private static int executeCountSqlQuery(final String sqlCountQuery) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sqlCountQuery)) {
            resultSet.next();
            return resultSet.getInt("count");
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    private static List<String> retrieveRows(final String tableName, final String columns) {
        final String sqlQuery = String.format("SELECT %s FROM %s", columns, tableName);

        final List<String> rows = new ArrayList<>();

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sqlQuery)) {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                final String row = getRowAsString(resultSet, metaData);
                rows.add(row);
            }

            return rows;
        } catch (final SQLException e) {
            throw new AssertionError(e);
        }
    }

    private static String getColumns(final List<String> wantedColumns) {
        if (wantedColumns.isEmpty()) {
            return "*";
        }
        return wantedColumns.stream().map(column -> String.format("\"%s\"", column)).collect(Collectors.joining(","));
    }

    private static String getRowAsString(final ResultSet resultSet, final ResultSetMetaData metaData) throws SQLException {
        final StringBuilder rowBuilder = new StringBuilder();

        for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
            final String cell = String.valueOf(resultSet.getObject(columnIndex));

            if (isStringWithComma(cell)) {
                rowBuilder.append('"').append(cell).append("\",");
            } else {
                rowBuilder.append(cell).append(',');
            }
        }

        rowBuilder.setLength(rowBuilder.length() - 1); // Remove trailing comma
        return rowBuilder.toString();
    }

    private static boolean isStringWithComma(final String cell) {
        return cell.contains(",") && !cell.startsWith("{") && !cell.startsWith("[");
    }
}