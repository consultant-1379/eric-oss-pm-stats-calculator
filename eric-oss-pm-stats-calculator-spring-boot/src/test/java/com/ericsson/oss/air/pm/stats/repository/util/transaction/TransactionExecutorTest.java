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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class TransactionExecutorTest {

    @Nested
    class ExecuteSilently {
        @Test
        void shouldVerifyExecuteSilently() {
            DriverManagerMock.prepare(connectionMock -> {
                final OperationRunner operationRunnerMock = mock(OperationRunner.class);

                Assertions.assertThatNoException().isThrownBy(() -> TransactionExecutor.executeSilently(operationRunnerMock));

                verify(operationRunnerMock).run(connectionMock);
            });
        }

        @Test
        void shouldVerifyExecuteInTransactionSilentlySilently() {
            DriverManagerMock.prepare(connectionMock -> {
                final OperationRunner operationRunnerMock = mock(OperationRunner.class);

                Assertions.assertThatNoException().isThrownBy(() -> TransactionExecutor.executeInTransactionSilently(operationRunnerMock));

                verify(operationRunnerMock).run(connectionMock);
            });
        }

        @Test
        void shouldRaiseException_whenSomethingGoesWrongDuringExecuteSilently() {
            DriverManagerMock.prepare(connectionMock -> {
                final OperationRunner operationRunnerMock = mock(OperationRunner.class);

                doThrow(SQLException.class).when(operationRunnerMock).run(connectionMock);

                Assertions.assertThatThrownBy(() -> TransactionExecutor.executeSilently(operationRunnerMock))
                        .isInstanceOf(UncheckedSqlException.class);

                verify(operationRunnerMock).run(connectionMock);
            });
        }

        @Test
        void shouldRaiseException_whenSomethingGoesWrongDuringExecuteInTransactionSilently() {
            DriverManagerMock.prepare(connectionMock -> {
                final OperationRunner operationRunnerMock = mock(OperationRunner.class);

                doThrow(SQLException.class).when(operationRunnerMock).run(connectionMock);

                Assertions.assertThatThrownBy(() -> TransactionExecutor.executeInTransactionSilently(operationRunnerMock))
                        .isInstanceOf(UncheckedSqlException.class);

                verify(operationRunnerMock).run(connectionMock);
            });
        }
    }

    @Test
    void shouldExecute() {
        DriverManagerMock.prepare(connectionMock -> {
            final OperationRunner operationRunnerMock = mock(OperationRunner.class);

            Assertions.assertThatNoException().isThrownBy(() -> TransactionExecutor.execute(operationRunnerMock));

            verify(operationRunnerMock).run(connectionMock);
        });
    }

    @Test
    void shouldExecuteInTransaction() {
        DriverManagerMock.prepare(connectionMock -> {
            final OperationRunner operationRunnerMock = mock(OperationRunner.class);

            Assertions.assertThatNoException().isThrownBy(() -> TransactionExecutor.executeInTransaction(operationRunnerMock));

            final InOrder inOrder = Mockito.inOrder(connectionMock, operationRunnerMock);
            inOrder.verify(connectionMock).setAutoCommit(false);
            inOrder.verify(operationRunnerMock).run(connectionMock);
            inOrder.verify(connectionMock).commit();
            inOrder.verify(connectionMock).setAutoCommit(true);
            inOrder.verify(connectionMock).close();
        });
    }

    @Test
    void shouldNotRollback_AndNotCloseConnection_whenConnectionIsNotCreated_onExecuteInTransaction() {
        DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
            final OperationRunner operationRunnerMock = mock(OperationRunner.class);

            Assertions.assertThatThrownBy(() -> TransactionExecutor.executeInTransaction(operationRunnerMock))
                    .isInstanceOf(SQLException.class);
        });
    }

    @Test
    void shouldThrowSQLException_andRollback_whenSomethingGoesWrong_onExecuteInTransaction() {
        DriverManagerMock.prepare(connectionMock -> {
            final OperationRunner operationRunnerMock = mock(OperationRunner.class);

            doThrow(SQLException.class).when(connectionMock).setAutoCommit(false);

            Assertions.assertThatThrownBy(() -> TransactionExecutor.executeInTransaction(operationRunnerMock))
                    .isInstanceOf(SQLException.class);

            final InOrder inOrder = Mockito.inOrder(connectionMock, operationRunnerMock);
            inOrder.verify(connectionMock).setAutoCommit(false);
            inOrder.verify(connectionMock, never()).commit();
            inOrder.verify(connectionMock, never()).setAutoCommit(true);
            inOrder.verify(connectionMock).close();
        });
    }
}