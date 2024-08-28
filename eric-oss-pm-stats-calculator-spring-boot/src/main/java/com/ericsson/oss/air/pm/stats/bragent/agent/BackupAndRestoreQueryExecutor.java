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

import static lombok.AccessLevel.PUBLIC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.OptionsHandler;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class BackupAndRestoreQueryExecutor {
    @Inject
    private BackupAndRestoreProcessHandler backupAndRestoreProcessHandler;

    public ProcessResult revokeAccess(final String database, final String user) {
        return doExecuteQueries(
                String.format("REVOKE connect ON DATABASE %s from \"%s\";", database, user),
                String.format("REVOKE connect ON DATABASE %s from group PUBLIC;", database)
        );
    }

    public ProcessResult grantAccess(final String database, final String user) {
        return doExecuteQueries(
                String.format("GRANT connect ON DATABASE %s to \"%s\";", database, user),
                String.format("GRANT connect ON DATABASE %s to group PUBLIC;", database)
        );
    }

    public ProcessResult terminateBackend(final String database) {
        return doExecuteQueries(String.format(
                "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
                        "FROM pg_stat_activity " +
                        "WHERE pg_stat_activity.datname ='%s' " +
                        "AND pid <> pg_backend_pid();", database
        ));
    }

    private ProcessResult doExecuteQueries(final String... queries) {
        final String command = OptionsHandler.getQueryCommand();

        final List<String> options = OptionsHandler.getQueryCommandOptions();
        for (final String query : queries) {
            options.add("-c " + query);
        }

        final List<String> createdCommand = new ArrayList<>(List.of(command));
        createdCommand.addAll(options);

        final Map<String, String> env = OptionsHandler.getExtraEnv();
        return backupAndRestoreProcessHandler.processHandler(createdCommand, env);
    }
}
