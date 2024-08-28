/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.api;

import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;

public interface CalculatorDatabaseBackup {
    AgentReturnState prepareForBackup(BackupPreparationActions backupPreparationActions);

    AgentReturnState executeBackup(BackupExecutionActions backupExecutionActions);

    AgentReturnState postBackup(PostBackupActions postBackupActions);

    AgentReturnState cancel(CancelActions cancelActions);
}
