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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.adp.mgmt.bro.api.exception.FailedToTransferBackupException;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;
import com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupHelper;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculatorDatabaseBackupImplTest {
    @Mock
    BackupHelper backupHelperMock;
    @Mock
    BackupAndRestoreProcessHandler backupAndRestoreProcessHandlerMock;

    @InjectMocks
    CalculatorDatabaseBackupImpl objectUnderTest;

    @Test
    void shouldPrepareForBackup(@Mock final BackupPreparationActions backupPreparationActionsMock) {
        final AgentReturnState actual = objectUnderTest.prepareForBackup(backupPreparationActionsMock);
        assertThat(actual).isEqualTo(AgentReturnState.success("Successful backup preparation"));
    }

    @Test
    @SneakyThrows
    void shouldSuccessfullyExecuteBackup(@Mock final BackupExecutionActions backupExecutionActionsMock) {
        try (final MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            final Path backupFilePath = BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE);

            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("host");
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getAgentIdProperty).thenReturn("CalculatorAgent");

            when(backupExecutionActionsMock.getBackupName()).thenReturn("testBackup");
            when(backupExecutionActionsMock.getBackupType()).thenReturn("PLATFORM");

            doNothing().when(backupHelperMock).createDatabaseBackupFile(backupFilePath);
            doNothing().when(backupExecutionActionsMock).sendBackup(assertArg(argument -> {
                assertThat(argument.getFragmentId()).isEqualTo("CalculatorAgent_1");
                assertThat(argument.getVersion()).isEqualTo("0.0.0");
                assertThat(argument.getSizeInBytes()).isEqualTo("0");
                assertThat(argument.getBackupFilePath()).isEqualTo(backupFilePath.toString());
            }));

            final AgentReturnState actual = objectUnderTest.executeBackup(backupExecutionActionsMock);

            assertThat(actual).isEqualTo(AgentReturnState.success("The backup and restore agent has completed a backup for testBackup"));
        }
    }

    @Test
    @SneakyThrows
    void shouldFailWithTransferException(@Mock final BackupExecutionActions backupExecutionActionsMock) {
        try (final MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            final Path backupFilePath = BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE);

            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("host");

            when(backupExecutionActionsMock.getBackupName()).thenReturn("testBackup");
            when(backupExecutionActionsMock.getBackupType()).thenReturn("PLATFORM");
            doNothing().when(backupHelperMock).createDatabaseBackupFile(backupFilePath);
            doThrow(FailedToTransferBackupException.class).when(backupExecutionActionsMock).sendBackup(any());

            final AgentReturnState actual = objectUnderTest.executeBackup(backupExecutionActionsMock);

            assertThat(actual).isEqualTo(AgentReturnState.failure(
                    "The backup and restore agent has failed to complete a backup testBackup. Cause: null. " +
                            "The backup and restore agent will not retry to send the backup"
            ));
        }
    }

    @Test
    @SneakyThrows
    void shouldExecutePostBackup(@Mock final PostBackupActions postBackupActionsMock) {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            final Path path = BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE);
            filesMockedStatic.when(() -> Files.deleteIfExists(path)).thenAnswer(invocation -> null);

            final AgentReturnState actual = objectUnderTest.postBackup(postBackupActionsMock);

            filesMockedStatic.verify(() -> Files.deleteIfExists(path));

            assertThat(actual).isEqualTo(AgentReturnState.success("Post backup succeeded"));
        }
    }

    @Test
    @SneakyThrows
    void shouldFailPostBackup(@Mock final PostBackupActions postBackupActionsMock) {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            final Path path = BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE);
            filesMockedStatic.when(() -> Files.deleteIfExists(path)).thenThrow(IOException.class);

            final AgentReturnState actual = objectUnderTest.postBackup(postBackupActionsMock);

            filesMockedStatic.verify(() -> Files.deleteIfExists(path));

            assertThat(actual).isEqualTo(AgentReturnState.failure("Post backup failed"));
        }
    }

    @Test
    @SneakyThrows
    void shouldCancelBackup(@Mock final CancelActions cancelActionsMock) {
        doNothing().when(backupAndRestoreProcessHandlerMock).cancelProcess();
        final AgentReturnState actual = objectUnderTest.cancel(cancelActionsMock);

        assertThat(actual).isEqualTo(AgentReturnState.success("The backup has been canceled successfully"));
    }
}