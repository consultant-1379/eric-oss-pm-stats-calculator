/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent._utils;

import java.nio.file.Path;

import com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackupAndRestoreConstantPrepares {

    @SneakyThrows
    public static void prepareBackupPath(final Path backup, @NonNull final TestRunner testRunner) {
        try (final MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getBackupPath).thenReturn(backup);

            testRunner.run();
        }
    }

    @SneakyThrows
    public static void prepareConstants(final Boolean ssl, @NonNull final TestRunner testRunner) {
        try (final MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgPass).thenReturn(ssl == null ? "" : ssl.toString());
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("eric-pm-kpi-data-v2");
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgPort).thenReturn(5_432);
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgClientUser).thenReturn("kpi_service_user");
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgDatabase).thenReturn("calculator-database");

            testRunner.run();
        }
    }

    @FunctionalInterface
    public interface TestRunner {
        void run() throws Exception;
    }
}
