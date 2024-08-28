/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import java.sql.Timestamp;

/**
 * Utility class to build an SQL filter string.
 */
public class SqlFilterCreator {
    private final StringBuilder builder = new StringBuilder();

    public static SqlFilterCreator create() {
        return new SqlFilterCreator();
    }

    /**
     * Create a filter string that filters between 2 aggregation begin times.
     *
     * @param startTimestamp
     *            start timestamp to filter on
     * @param endTimestamp
     *            end timestamp to filter on
     * @return {@link SqlFilterCreator} instance containing an SQL String
     */
    public SqlFilterCreator aggregationBeginTimeBetween(final Timestamp startTimestamp, final Timestamp endTimestamp) {
        builder.append(String.format("aggregation_begin_time BETWEEN TO_TIMESTAMP('%s')" +
                " AND TO_TIMESTAMP('%s')", startTimestamp, endTimestamp));
        return this;
    }

    /**
     * Appends a where clause to the filter query.
     *
     * @return {@link SqlFilterCreator} instance containing an SQL String
     */
    public SqlFilterCreator where() {
        builder.append(" WHERE ");
        return this;
    }

    /**
     * Appends a filter to the sql query.
     *
     * @param filter
     *            a string representing the where clause of the sql filter
     * @return {@link SqlFilterCreator} instance containing an SQL String
     */
    public SqlFilterCreator addFilter(final String filter) {
        builder.append(filter);
        return this;
    }

    /**
     * Appends an and clause to the filter query.
     *
     * @return {@link SqlFilterCreator} instance containing an SQL String
     */
    public SqlFilterCreator and() {
        builder.append(" AND ");
        return this;
    }

    /**
     * Creates a select all statement on the tableName passed in.
     *
     * @param tableName
     *            the table to query from
     * @return {@link SqlFilterCreator} instance containing an SQL String
     */
    public SqlFilterCreator selectAll(final String tableName) {
        builder.append(String.format("SELECT * FROM %s", tableName));
        return this;
    }

    /**
     * Builds and returns the computed SQL filter query.
     *
     * @return the computed SQL filter query
     */
    public String build() {
        final String result = builder.toString();
        builder.setLength(0);
        return result;
    }

}
