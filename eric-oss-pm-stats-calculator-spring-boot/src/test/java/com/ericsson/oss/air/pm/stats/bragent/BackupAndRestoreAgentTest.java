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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.ericsson.adp.mgmt.bro.api.agent.Agent;
import com.ericsson.adp.mgmt.bro.api.agent.AgentFactory;
import com.ericsson.oss.air.pm.stats.bragent.agent.BackupAndRestoreAgentBehavior;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackupAndRestoreAgentTest {
    @Mock
    BackupAndRestoreAgentBehavior backupAndRestoreAgentBehaviorMock;

    @InjectMocks
    BackupAndRestoreAgent objectUnderTest;

    @Test
    @SetEnvironmentVariable(key = "BR_AGENT", value = "true")
    void shouldStartBrAgent(@Mock final Agent agentMock) {
        try (final MockedStatic<AgentFactory> agentFactoryMockedStatic = Mockito.mockStatic(AgentFactory.class)) {
            agentFactoryMockedStatic.when(() -> AgentFactory.createAgent(any(), eq(backupAndRestoreAgentBehaviorMock))).thenReturn(agentMock);

            objectUnderTest.createAgent();

            Assertions.assertThat(objectUnderTest.getAgent()).hasValue(agentMock);
        }
    }

    @Test
    @SetEnvironmentVariable(key = "BR_AGENT", value = "false")
    void shouldNotStartBrAgent() {
        objectUnderTest.createAgent();

        Assertions.assertThat(objectUnderTest.getAgent()).isNotPresent();
    }
}