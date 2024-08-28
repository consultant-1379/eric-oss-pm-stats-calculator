/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.agent;

import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.API_VERSION_PROPERTY;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SCOPE_PROPERTY;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SOFTWARE_VERSION_DESCRIPTION_PROPERTY;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SOFTWARE_VERSION_PRODUCTION_DATE_PROPERTY;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SOFTWARE_VERSION_PRODUCT_NAME;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SOFTWARE_VERSION_PRODUCT_NUMBER;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SOFTWARE_VERSION_REVISION_PROPERTY;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.SOFTWARE_VERSION_TYPE_PROPERTY;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getAgentIdProperty;
import static lombok.AccessLevel.PUBLIC;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.adp.mgmt.bro.api.agent.AgentBehavior;
import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostRestoreActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;
import com.ericsson.adp.mgmt.bro.api.registration.RegistrationInformation;
import com.ericsson.adp.mgmt.bro.api.registration.SoftwareVersion;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseBackup;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseRestore;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class BackupAndRestoreAgentBehavior implements AgentBehavior {
    @Inject
    private CalculatorDatabaseBackup calculatorDatabaseBackup;
    @Inject
    private CalculatorDatabaseRestore calculatorDatabaseRestore;

    @Override
    public RegistrationInformation getRegistrationInformation() {
        final RegistrationInformation registrationInfo = new RegistrationInformation();
        registrationInfo.setAgentId(getAgentIdProperty());
        registrationInfo.setApiVersion(API_VERSION_PROPERTY);
        registrationInfo.setScope(SCOPE_PROPERTY);
        registrationInfo.setSoftwareVersion(getSoftwareVersion());
        return registrationInfo;
    }

    @Override
    public void prepareForBackup(final BackupPreparationActions backupPreparationActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseBackup.prepareForBackup(backupPreparationActions);
        backupPreparationActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    @Override
    public void executeBackup(final BackupExecutionActions backupExecutionActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseBackup.executeBackup(backupExecutionActions);
        backupExecutionActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    @Override
    public void postBackup(final PostBackupActions postBackupActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseBackup.postBackup(postBackupActions);
        postBackupActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    @Override
    public void prepareForRestore(final RestorePreparationActions restorePreparationActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseRestore.prepareForRestore(restorePreparationActions);
        restorePreparationActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    @Override
    public void executeRestore(final RestoreExecutionActions restoreExecutionActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseRestore.executeRestore(restoreExecutionActions);
        restoreExecutionActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    @Override
    public void postRestore(final PostRestoreActions postRestoreActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseRestore.postRestore(postRestoreActions);
        postRestoreActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    @Override
    public void cancelAction(final CancelActions cancelActions) {
        final AgentReturnState agentReturnState = calculatorDatabaseBackup.cancel(cancelActions);
        cancelActions.sendStageComplete(agentReturnState.isSuccessful(), agentReturnState.getMessage());
    }

    private static SoftwareVersion getSoftwareVersion() {
        final SoftwareVersion softwareVersion = new SoftwareVersion();
        softwareVersion.setDescription(SOFTWARE_VERSION_DESCRIPTION_PROPERTY);
        softwareVersion.setProductionDate(SOFTWARE_VERSION_PRODUCTION_DATE_PROPERTY);
        softwareVersion.setProductName(SOFTWARE_VERSION_PRODUCT_NAME);
        softwareVersion.setProductNumber(SOFTWARE_VERSION_PRODUCT_NUMBER);
        softwareVersion.setType(SOFTWARE_VERSION_TYPE_PROPERTY);
        softwareVersion.setRevision(SOFTWARE_VERSION_REVISION_PROPERTY);
        return softwareVersion;
    }
}
