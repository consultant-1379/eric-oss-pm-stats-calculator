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
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.FILE_FORMAT;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getBackupPath;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgClientUser;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgDatabase;
import static java.nio.file.Files.deleteIfExists;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.io.FileUtils.listFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.adp.mgmt.bro.api.agent.PostRestoreActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;
import com.ericsson.adp.mgmt.bro.api.exception.FailedToDownloadException;
import com.ericsson.adp.mgmt.bro.api.fragment.FragmentInformation;
import com.ericsson.adp.mgmt.bro.api.registration.SoftwareVersion;
import com.ericsson.oss.air.pm.stats.bragent.agent.BackupAndRestoreQueryExecutor;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseRestore;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;
import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;
import com.ericsson.oss.air.pm.stats.bragent.model.RestoreStatus;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.OptionsHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.RestoreLogAnalyser;
import com.ericsson.oss.air.pm.stats.model.exception.CalculationStateCorrectionException;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionFacade;

import io.github.resilience4j.retry.Retry;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculatorDatabaseRestoreImpl implements CalculatorDatabaseRestore {
    @Inject
    private BackupAndRestoreQueryExecutor backupAndRestoreQueryExecutor;
    @Inject
    private BackupAndRestoreProcessHandler backupAndRestoreProcessHandler;
    @Inject
    private KpiExposureService kpiExposureService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private PartitionRetentionFacade partitionRetentionFacade;
    @Inject
    private RestoreStatus restoreStatus;

    @Inject
    private Retry updateCalculationStateRetry;

    @Override
    public AgentReturnState prepareForRestore(final RestorePreparationActions restorePreparationActions) {
        final String backupName = restorePreparationActions.getBackupName();
        final SoftwareVersion softwareVersion = restorePreparationActions.getSoftwareVersion();

        log.info("Start restore the database from '{}' backup to '{}' version", backupName, softwareVersion);

        final List<FragmentInformation> fragments = restorePreparationActions.getFragmentList();
        try {
            for (final FragmentInformation fragment : fragments) {
                restorePreparationActions.downloadFragment(fragment, BACKUP_PATH.toString());
            }
            return AgentReturnState.success("The backup content is stored to the disk");
        } catch (final FailedToDownloadException e) {
            log.info("Unable to store the restore content", e);
            return AgentReturnState.failure(e.getMessage());
        }
    }

    @Override
    public AgentReturnState executeRestore(final RestoreExecutionActions restoreExecutionActions) {
        log.info("Restore process started");

        final String pgDatabase = getPgDatabase();
        final String pgUser = getPgClientUser();

        final ProcessResult resultRevoke = backupAndRestoreQueryExecutor.revokeAccess(pgDatabase, pgUser);
        if (resultRevoke.getExitCode() != 0) {
            return AgentReturnState.failure("Cannot revoke database access");
        }

        final ProcessResult resultTerminate = backupAndRestoreQueryExecutor.terminateBackend(pgDatabase);
        if (resultTerminate.getExitCode() != 0) {
            return AgentReturnState.failure("Cannot terminate backends sessions");
        }
        restoreStatus.setRestoreOngoing(true);
        final String command = OptionsHandler.getRestoreCommand();
        final List<String> options = OptionsHandler.getRestoreCommandOptions(FILE_FORMAT);
        options.add(BACKUP_PATH.resolve(BACKUP_PGDUMP_FILE).toString());
        final List<String> createdCommand = new ArrayList<>(List.of(command));
        createdCommand.addAll(options);

        final Map<String, String> env = OptionsHandler.getExtraEnv();

        final ProcessResult processResult = backupAndRestoreProcessHandler.processHandler(createdCommand, env);


        String positiveResponse = "The database is restored.";
        if (processResult.getExitCode() != 0 && RestoreLogAnalyser.isLogContainsWarning(processResult.getResultLog())) {
            positiveResponse = "The restore process log included WARNING, please take care it";
        } else if (processResult.getExitCode() != 0) {
            return AgentReturnState.failure("Restore Failed");
        }
        if (RestoreLogAnalyser.isLogContainsDuplicateKeyValueError(processResult.getResultLog())) {
            return AgentReturnState.failure("Fail to restore and the log include the message: ERROR:  duplicate key value violates!");
        }

        backupAndRestoreQueryExecutor.grantAccess(pgDatabase, pgUser);
        restoreStatus.setRestoreOngoing(false);
        return AgentReturnState.success(positiveResponse);
    }

    @Override
    public AgentReturnState postRestore(final PostRestoreActions postRestoreActions) {
        kpiExposureService.updateExposure();

        try {
            calculationService.updateStatesAfterRestore(updateCalculationStateRetry);
        } catch (final CalculationStateCorrectionException e) {
            log.warn("Unsuccessful Calculation State update", e);
            return AgentReturnState.failure("Post restore failed");
        }

        partitionRetentionFacade.runRetention();

        for (final File file : listFiles(getBackupPath().toFile(), null, false)) {
            try {
                log.info("Tyring to delete file '{}'", file);
                deleteIfExists(file.toPath());
            } catch (final IOException e) {
                log.info("Unsuccessfully storage clean", e);
                return AgentReturnState.failure("Post restore failed");
            }
        }
        return AgentReturnState.success("Post restore succeeded");
    }
}
