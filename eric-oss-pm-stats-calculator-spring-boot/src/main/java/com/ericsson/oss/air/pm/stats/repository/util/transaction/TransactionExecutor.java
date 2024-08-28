/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.transaction;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionExecutor {

    public static void executeSilently(final OperationRunner operationRunner) {
        try {
            execute(operationRunner);
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static void executeInTransactionSilently(final OperationRunner operationRunner) {
        try {
            executeInTransaction(operationRunner);
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static void execute(final OperationRunner operationRunner) throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            operationRunner.run(connection);
        }
    }

    public static void executeInTransaction(final OperationRunner operationRunner) throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
            connection.setAutoCommit(false);

            operationRunner.run(connection);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            if (Objects.nonNull(connection)) {
                connection.rollback();
            }

            throw e;
        } finally {
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }
}
