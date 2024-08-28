/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent;

import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.BACKUP_PATH;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.BACKUP_PGDUMP_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.adp.mgmt.bro.api.agent.PostRestoreActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;
import com.ericsson.adp.mgmt.bro.api.exception.FailedToDownloadException;
import com.ericsson.adp.mgmt.bro.api.fragment.FragmentInformation;
import com.ericsson.oss.air.pm.stats.bragent._utils.BackupAndRestoreConstantPrepares;
import com.ericsson.oss.air.pm.stats.bragent.agent.BackupAndRestoreQueryExecutor;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;
import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;
import com.ericsson.oss.air.pm.stats.bragent.model.RestoreStatus;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.OptionsHandler;
import com.ericsson.oss.air.pm.stats.model.exception.CalculationStateCorrectionException;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionFacade;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculatorDatabaseRestoreImplTest {
    @Mock
    RestorePreparationActions restorePreparationActionsMock;
    @Mock
    RestoreExecutionActions restoreExecutionActionsMock;

    @Mock
    BackupAndRestoreProcessHandler backupAndRestoreProcessHandlerMock;
    @Mock
    BackupAndRestoreQueryExecutor backupAndRestoreQueryExecutorMock;
    @Mock
    KpiExposureService kpiExposureServiceMock;
    @Mock
    CalculationService calculationServiceMock;
    @Mock
    PartitionRetentionFacade partitionRetentionFacadeMock;
    @Mock
    RestoreStatus restoreStatusMock;

    @InjectMocks
    CalculatorDatabaseRestoreImpl objectUnderTest;

    @Nested
    class PostRestore {
        @TempDir
        Path backupPath;

        @Test
        @SneakyThrows
        void shouldExecutePostRestore(@Mock final PostRestoreActions postRestoreActionsMock) {
            doNothing().when(kpiExposureServiceMock).updateExposure();
            doNothing().when(calculationServiceMock).updateStatesAfterRestore(any());
            doNothing().when(partitionRetentionFacadeMock).runRetention();

            BackupAndRestoreConstantPrepares.prepareBackupPath(backupPath, () -> {
                Files.createFile(backupPath.resolve("fragment_1.sql"));
                Files.createFile(backupPath.resolve("fragment_2.sql"));

                final AgentReturnState actual = objectUnderTest.postRestore(postRestoreActionsMock);
                assertThat(actual).isEqualTo(AgentReturnState.success("Post restore succeeded"));

                assertThat(backupPath).isEmptyDirectory();
            });
            verify(kpiExposureServiceMock).updateExposure();
            verify(calculationServiceMock).updateStatesAfterRestore(any());
        }

        @Test
        @SneakyThrows
        void shouldFailOnPostRestore(@Mock final PostRestoreActions postRestoreActionsMock) {
            doNothing().when(kpiExposureServiceMock).updateExposure();
            doThrow(new CalculationStateCorrectionException("State update failed")).when(calculationServiceMock).updateStatesAfterRestore(any());
            BackupAndRestoreConstantPrepares.prepareBackupPath(backupPath, () -> {
                Files.createFile(backupPath.resolve("fragment_1.sql"));
                Files.createFile(backupPath.resolve("fragment_2.sql"));

                final AgentReturnState actual = objectUnderTest.postRestore(postRestoreActionsMock);
                assertThat(actual).isEqualTo(AgentReturnState.failure("Post restore failed"));

                assertThat(backupPath).isNotEmptyDirectory();
            });
            verify(kpiExposureServiceMock).updateExposure();
            verify(calculationServiceMock).updateStatesAfterRestore(any());
        }
    }

    @Test
    @SneakyThrows
    void shouldFailPrepareForRestore() {
        final FragmentInformation fragmentInformation = new FragmentInformation();

        when(restorePreparationActionsMock.getFragmentList()).thenReturn(List.of(fragmentInformation));
        doThrow(new FailedToDownloadException("Cannot download ...")).when(restorePreparationActionsMock).downloadFragment(
                fragmentInformation, BACKUP_PATH.toString()
        );

        final AgentReturnState actual = objectUnderTest.prepareForRestore(restorePreparationActionsMock);

        assertThat(actual).isEqualTo(AgentReturnState.failure("Cannot download ..."));
    }

    @Test
    @SneakyThrows
    void shouldSucceedPrepareForStore() {
        final FragmentInformation fragmentInformation = new FragmentInformation();

        when(restorePreparationActionsMock.getFragmentList()).thenReturn(List.of(fragmentInformation));
        doNothing().when(restorePreparationActionsMock).downloadFragment(fragmentInformation, BACKUP_PATH.toString());

        final AgentReturnState actual = objectUnderTest.prepareForRestore(restorePreparationActionsMock);

        assertThat(actual).isEqualTo(AgentReturnState.success("The backup content is stored to the disk"));
    }

    @Test
    void shouldExecuteRestore_andFailOnRevokeAccess() {
        BackupAndRestoreConstantPrepares.prepareConstants(null, () -> {
            when(backupAndRestoreQueryExecutorMock.revokeAccess("calculator-database", "kpi_service_user")).thenReturn(ProcessResult.of(255, "Error"));

            final AgentReturnState actual = objectUnderTest.executeRestore(restoreExecutionActionsMock);

            assertThat(actual).isEqualTo(AgentReturnState.failure("Cannot revoke database access"));
        });
    }

    @Test
    void shouldExecuteRestore_andFailOnTerminateBackend() {
        BackupAndRestoreConstantPrepares.prepareConstants(null, () -> {
            when(backupAndRestoreQueryExecutorMock.revokeAccess("calculator-database", "kpi_service_user")).thenReturn(ProcessResult.success(""));
            when(backupAndRestoreQueryExecutorMock.terminateBackend("calculator-database")).thenReturn(ProcessResult.of(255, "Error"));

            final AgentReturnState actual = objectUnderTest.executeRestore(restoreExecutionActionsMock);

            assertThat(actual).isEqualTo(AgentReturnState.failure("Cannot terminate backends sessions"));
        });
    }

    @Test
    void shouldExecuteRestore_andFailWithNoAccess() {
        BackupAndRestoreConstantPrepares.prepareConstants(null, () -> {
            when(backupAndRestoreQueryExecutorMock.revokeAccess("calculator-database", "kpi_service_user")).thenReturn(ProcessResult.success(""));
            when(backupAndRestoreQueryExecutorMock.terminateBackend("calculator-database")).thenReturn(ProcessResult.success(""));
            when(backupAndRestoreProcessHandlerMock.processHandler(
                    assertArg(createdCommand -> assertThat(createdCommand).containsExactly(
                            OptionsHandler.getRestoreCommand(),
                            "--verbose",
                            "--clean",
                            "-d host=eric-pm-kpi-data-v2 port=5432 user=postgres dbname=calculator-database",
                            "--format=c",
                            "--jobs=10",
                            BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE).toString()
                    )),
                    assertArg(env -> assertThat(env).containsEntry("PGPASSWORD", ""))
            )).thenReturn(ProcessResult.of(255, "Conn failed"));

            final AgentReturnState actual = objectUnderTest.executeRestore(restoreExecutionActionsMock);

            assertThat(actual).isEqualTo(AgentReturnState.failure("Restore Failed"));
        });
    }

    @SneakyThrows
    @MethodSource("provideDatasourceRestoreProcess")
    @ParameterizedTest(name = "[{index}] Datasource with exit code: ''{0}'' has message: ''{1}'' ")
    void shouldExecuteRestore(final ProcessResult processResult, final AgentReturnState expected) {
        BackupAndRestoreConstantPrepares.prepareConstants(null, () -> {
            when(backupAndRestoreQueryExecutorMock.revokeAccess("calculator-database", "kpi_service_user")).thenReturn(ProcessResult.success(""));
            when(backupAndRestoreQueryExecutorMock.terminateBackend("calculator-database")).thenReturn(ProcessResult.success(""));
            when(backupAndRestoreProcessHandlerMock.processHandler(
                    assertArg(createdCommand -> assertThat(createdCommand).containsExactly(
                            OptionsHandler.getRestoreCommand(),
                            "--verbose",
                            "--clean",
                            "-d host=eric-pm-kpi-data-v2 port=5432 user=postgres dbname=calculator-database",
                            "--format=c",
                            "--jobs=10",
                            BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE).toString()
                    )),
                    assertArg(env -> assertThat(env).containsEntry("PGPASSWORD", ""))
            )).thenReturn(processResult);

            final AgentReturnState actual = objectUnderTest.executeRestore(restoreExecutionActionsMock);

            assertThat(actual).isEqualTo(expected);
        });
    }

    private static Stream<Arguments> provideDatasourceRestoreProcess() {
        return Stream.of(
                arguments(
                        ProcessResult.of(0, ""),
                        AgentReturnState.success("The database is restored.")
                ),
                arguments(
                        ProcessResult.of(0, "ERROR:  duplicate key value violates"),
                        AgentReturnState.failure("Fail to restore and the log include the message: ERROR:  duplicate key value violates!")
                ),
                arguments(
                        ProcessResult.of(1, "warning: errors ignored"),
                        AgentReturnState.success("The restore process log included WARNING, please take care it")
                )
        );
    }
}