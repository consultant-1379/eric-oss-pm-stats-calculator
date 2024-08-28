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
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.FRAGMENT_NUMBER;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.FRAGMENT_VERSION;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getAgentIdProperty;
import static java.nio.file.Files.deleteIfExists;
import static lombok.AccessLevel.PUBLIC;

import java.io.IOException;
import java.nio.file.Path;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.adp.mgmt.bro.api.exception.FailedToTransferBackupException;
import com.ericsson.adp.mgmt.bro.api.fragment.BackupFragmentInformation;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseBackup;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupHelper;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculatorDatabaseBackupImpl implements CalculatorDatabaseBackup {
    @Inject
    private BackupHelper backupHelper;
    @Inject
    private BackupAndRestoreProcessHandler backupAndRestoreProcessHandler;

    @Override
    public AgentReturnState prepareForBackup(final BackupPreparationActions backupPreparationActions) {
        //stop calculations
        return AgentReturnState.success("Successful backup preparation");
    }

    @Override
    public AgentReturnState executeBackup(final BackupExecutionActions backupExecutionActions) {
        final String backupName = backupExecutionActions.getBackupName();
        log.info("Backup name '{}' and type '{}'", backupName, backupExecutionActions.getBackupType());

        try {
            final Path backupFilePath = BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE);
            backupHelper.createDatabaseBackupFile(backupFilePath);
            backupExecutionActions.sendBackup(collectFragmentInfo(backupFilePath));
            return AgentReturnState.success(String.format("The backup and restore agent has completed a backup for %s", backupName));
        } catch (final FailedToTransferBackupException e) {
            log.error("Backup Failed due to exception:", e);
            return AgentReturnState.failure(String.format(
                    "The backup and restore agent has failed to complete a backup %s. Cause: %s. " +
                            "The backup and restore agent will not retry to send the backup",
                    backupName, e.getMessage()
            ));
        }
    }

    @Override
    public AgentReturnState postBackup(final PostBackupActions postBackupActions) {
        try {
            deleteIfExists(BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE));
            return AgentReturnState.success("Post backup succeeded");
        } catch (final IOException e) {
            log.info("Unsuccessfully storage clean", e);
            return AgentReturnState.failure("Post backup failed");
        }
    }

    @Override
    public AgentReturnState cancel(final CancelActions cancelActions) {
        backupAndRestoreProcessHandler.cancelProcess();
        return AgentReturnState.success("The backup has been canceled successfully");
    }

    private static BackupFragmentInformation collectFragmentInfo(final Path backupDestination) {
        final BackupFragmentInformation fragmentInformation = new BackupFragmentInformation();

        fragmentInformation.setFragmentId(String.format("%s_%s", getAgentIdProperty(), FRAGMENT_NUMBER));
        fragmentInformation.setVersion(FRAGMENT_VERSION);
        fragmentInformation.setSizeInBytes(Long.toString(backupDestination.toFile().length()));
        fragmentInformation.setBackupFilePath(backupDestination.toString());

        return fragmentInformation;
    }
}
