/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlStatementExecutor {

    public static void executeUpdate(final Connection connection, final String sql) throws SQLException {
        executeUpdate(connection, SqlStatement.of(sql));
    }

    public static void executeUpdate(final @NonNull Connection connection, @NonNull final SqlStatement sqlStatement) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlStatement.statement());
        }
    }

    public static void executeBatch(final Connection connection, @NonNull final Collection<SqlStatement> statements) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            for (final SqlStatement sqlStatement : statements) {
                statement.addBatch(sqlStatement.statement());
            }

            statement.executeBatch();
        }
    }
}
