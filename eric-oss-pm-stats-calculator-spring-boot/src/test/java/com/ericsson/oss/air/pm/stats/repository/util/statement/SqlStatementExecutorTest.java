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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class SqlStatementExecutorTest {
    @Test
    void shouldExecuteUpdate_legacy() throws Exception {
        final Connection connectionMock = mock(Connection.class);
        final Statement statementMock = mock(Statement.class);

        when(connectionMock.createStatement()).thenReturn(statementMock);

        SqlStatementExecutor.executeUpdate(connectionMock, "sql");

        verify(connectionMock).createStatement();
        verify(statementMock).executeUpdate("sql");
    }

    @Test
    void shouldExecuteUpdate() throws Exception {
        final Connection connectionMock = mock(Connection.class);
        final Statement statementMock = mock(Statement.class);

        when(connectionMock.createStatement()).thenReturn(statementMock);

        SqlStatementExecutor.executeUpdate(connectionMock, SqlStatement.of("sql"));

        verify(connectionMock).createStatement();
        verify(statementMock).executeUpdate("sql");
    }

    @Test
    void shouldExecuteBatch() throws Exception {
        final Connection connectionMock = mock(Connection.class);
        final Statement statementMock = mock(Statement.class);

        when(connectionMock.createStatement()).thenReturn(statementMock);

        SqlStatementExecutor.executeBatch(connectionMock, Arrays.asList(SqlStatement.of("sql1"), SqlStatement.of("sql2")));

        verify(connectionMock).createStatement();
        verify(statementMock).addBatch("sql1");
        verify(statementMock).addBatch("sql2");
        verify(statementMock).executeBatch();
    }
}