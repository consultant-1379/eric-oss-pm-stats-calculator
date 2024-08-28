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

import com.ericsson.adp.mgmt.bro.api.agent.PostRestoreActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;
import com.ericsson.oss.air.pm.stats.bragent.model.AgentReturnState;

public interface CalculatorDatabaseRestore {
    AgentReturnState prepareForRestore(RestorePreparationActions restorePreparationActions);

    AgentReturnState executeRestore(RestoreExecutionActions restoreExecutionActions);

    AgentReturnState postRestore(PostRestoreActions postRestoreActions);
}
