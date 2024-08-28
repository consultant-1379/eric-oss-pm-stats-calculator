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

import static lombok.AccessLevel.PACKAGE;

import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.ericsson.adp.mgmt.bro.api.agent.Agent;
import com.ericsson.adp.mgmt.bro.api.agent.AgentFactory;
import com.ericsson.adp.mgmt.bro.api.agent.OrchestratorConnectionInformation;
import com.ericsson.oss.air.pm.stats.bragent.agent.BackupAndRestoreAgentBehavior;
import com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Startup
@Singleton
@NoArgsConstructor
@AllArgsConstructor
public class BackupAndRestoreAgent {
    private static final String BRO_HOST = "eric-ctrl-bro";
    private static final int BRO_PORT = 3_000;

    @Inject
    private BackupAndRestoreAgentBehavior calculatorAgentBehavior;

    @Getter(PACKAGE)
    private Optional<Agent> agent;

    @PostConstruct
    public void createAgent() {
        if ("true".equalsIgnoreCase(BackupAndRestoreConstants.getBrAgent())) {
            final OrchestratorConnectionInformation orchestratorConnectionInformation = new OrchestratorConnectionInformation(BRO_HOST, BRO_PORT);
            agent = Optional.of(AgentFactory.createAgent(orchestratorConnectionInformation, calculatorAgentBehavior));
            log.info("Backup and restore agent started");
        } else {
            agent = Optional.empty();
        }
    }
}
