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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostRestoreActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;
import com.ericsson.adp.mgmt.bro.api.registration.RegistrationInformation;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseBackup;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseRestore;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackupAndRestoreAgentBehaviorTest {
    static final AgentReturnState AGENT_RETURN_STATE = AgentReturnState.of(true, "Success");

    @Mock
    CalculatorDatabaseBackup calculatorDatabaseBackupMock;
    @Mock
    CalculatorDatabaseRestore calculatorDatabaseRestoreMock;

    @InjectMocks
    BackupAndRestoreAgentBehavior objectUnderTest;

    @Test
    void shouldGetRegistrationInformation() {
        final RegistrationInformation registrationInformation = objectUnderTest.getRegistrationInformation();

        Assertions.assertThat(registrationInformation.getApiVersion()).isEqualTo("4");
        Assertions.assertThat(registrationInformation.getScope()).isEqualTo("PLATFORM");
        Assertions.assertThat(registrationInformation.getSoftwareVersion().getDescription()).isEqualTo("No Description");
        Assertions.assertThat(registrationInformation.getSoftwareVersion().getProductionDate()).isEqualTo("No date");
        Assertions.assertThat(registrationInformation.getSoftwareVersion().getProductName()).isEqualTo("eric-oss-pm-stats-calculator-backup-agent");
        Assertions.assertThat(registrationInformation.getSoftwareVersion().getProductNumber()).isEqualTo("APR201564");
        Assertions.assertThat(registrationInformation.getSoftwareVersion().getType()).isEqualTo("Not set");
        Assertions.assertThat(registrationInformation.getSoftwareVersion().getRevision()).isEqualTo("0.0.0");
    }

    @Test
    void shouldPrepareForBackup(@Mock final BackupPreparationActions backupPreparationActionsMock) {
        when(calculatorDatabaseBackupMock.prepareForBackup(backupPreparationActionsMock)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.prepareForBackup(backupPreparationActionsMock);

        verify(backupPreparationActionsMock).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }

    @Test
    void shouldExecuteBackup(@Mock final BackupExecutionActions backupExecutionActionsMock) {
        when(calculatorDatabaseBackupMock.executeBackup(backupExecutionActionsMock)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.executeBackup(backupExecutionActionsMock);

        verify(backupExecutionActionsMock).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }

    @Test
    void shouldPostBackup(@Mock final PostBackupActions postBackupActionsMock) {
        when(calculatorDatabaseBackupMock.postBackup(postBackupActionsMock)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.postBackup(postBackupActionsMock);

        verify(postBackupActionsMock).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }

    @Test
    void shouldPrepareForRestore(@Mock final RestorePreparationActions restorePreparationActionsMock) {
        when(calculatorDatabaseRestoreMock.prepareForRestore(restorePreparationActionsMock)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.prepareForRestore(restorePreparationActionsMock);

        verify(restorePreparationActionsMock).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }

    @Test
    void shouldExecuteRestore(@Mock final RestoreExecutionActions restoreExecutionActionsMock) {
        when(calculatorDatabaseRestoreMock.executeRestore(restoreExecutionActionsMock)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.executeRestore(restoreExecutionActionsMock);

        verify(restoreExecutionActionsMock).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }

    @Test
    void shouldPostRestore(@Mock final PostRestoreActions postRestoreActionsMock) {
        when(calculatorDatabaseRestoreMock.postRestore(postRestoreActionsMock)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.postRestore(postRestoreActionsMock);

        verify(postRestoreActionsMock).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }

    @Test
    void shouldCancelAction(@Mock final CancelActions cancelActions) {
        when(calculatorDatabaseBackupMock.cancel(cancelActions)).thenReturn(AGENT_RETURN_STATE);

        objectUnderTest.cancelAction(cancelActions);

        verify(cancelActions).sendStageComplete(AGENT_RETURN_STATE.isSuccessful(), AGENT_RETURN_STATE.getMessage());
    }
}