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

import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.FILE_FORMAT;
import static lombok.AccessLevel.PUBLIC;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.bragent.exception.FailedToCreateBackupException;
import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class BackupHelper {
    @Inject
    private BackupAndRestoreProcessHandler backupAndRestoreProcessHandler;

    public void createDatabaseBackupFile(final Path backupFilePath) {
        final String command = OptionsHandler.getBackupCommand();
        final List<String> options = OptionsHandler.getBackupCommandOptions(FILE_FORMAT, backupFilePath);
        final List<String> createdCommand = new ArrayList<>(List.of(command));
        createdCommand.addAll(options);
        final Map<String, String> env = OptionsHandler.getExtraEnv();

        final ProcessResult processResult = backupAndRestoreProcessHandler.processHandler(createdCommand, env);

        if (processResult.getExitCode() != 0) {
            if (RestoreLogAnalyser.isLogContainsFailedConnectionError(processResult.getResultLog())) {
                throw new FailedToCreateBackupException(processResult.getResultLog());
            }
            throw new FailedToCreateBackupException("Backup failed");
        }
    }
}
