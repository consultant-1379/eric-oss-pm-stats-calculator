/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.utils;

import com.ericsson.oss.air.pm.stats.bragent.exception.FailedToCreateBackupException;
import com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants;
import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupHelperTest {
    @Mock
    BackupAndRestoreProcessHandler backupAndRestoreProcessHandlerMock;

    @InjectMocks
    BackupHelper objectUnderTest;

    @Test
    @SneakyThrows
    void shouldCreateBackupSuccess() {
        try (final MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("eric-pm-kpi-data-v2");

            when(backupAndRestoreProcessHandlerMock.processHandler(any(), any())).thenReturn(ProcessResult.of(0, ""));

            assertThatNoException().isThrownBy(() -> objectUnderTest.createDatabaseBackupFile(Path.of("/")));
        }
    }

    @Test
    @SneakyThrows
    void shouldFailedBackupResult() {
        try (final MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("host");

            when(backupAndRestoreProcessHandlerMock.processHandler(any(), any())).thenReturn(ProcessResult.of(255, "connection to database \"db\" failed"));
            assertThatThrownBy(() -> objectUnderTest.createDatabaseBackupFile(Path.of("/")))
                    .isInstanceOf(FailedToCreateBackupException.class)
                    .hasMessage("connection to database \"db\" failed");
        }
    }
}