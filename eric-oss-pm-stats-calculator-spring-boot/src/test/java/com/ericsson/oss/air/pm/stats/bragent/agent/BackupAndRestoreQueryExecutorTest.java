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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.bragent._utils.BackupAndRestoreConstantPrepares;
import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.OptionsHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackupAndRestoreQueryExecutorTest {
    @Mock
    BackupAndRestoreProcessHandler backupAndRestoreProcessHandlerMock;

    @InjectMocks
    BackupAndRestoreQueryExecutor objectUnderTest;

    @Test
    void shouldRevokeAccess() {
        BackupAndRestoreConstantPrepares.prepareConstants(null, () -> {
            when(backupAndRestoreProcessHandlerMock.processHandler(
                    assertArg(createdCommand -> assertThat(createdCommand).containsExactly(
                            OptionsHandler.getQueryCommand(),
                            "-d host=eric-pm-kpi-data-v2 port=5432 user=postgres dbname=calculator-database",
                            "-c REVOKE connect ON DATABASE kpi_service_db from \"kpi_service_user\";",
                            "-c REVOKE connect ON DATABASE kpi_service_db from group PUBLIC;"
                    )),
                    assertArg(env -> assertThat(env).containsEntry("PGPASSWORD", ""))
            )).thenReturn(ProcessResult.success("success"));

            final ProcessResult actual = objectUnderTest.revokeAccess("kpi_service_db", "kpi_service_user");

            assertThat(actual).isEqualTo(ProcessResult.success("success"));
        });
    }

    @Test
    void shouldTerminateBackend() {
        BackupAndRestoreConstantPrepares.prepareConstants(true, () -> {
            when(backupAndRestoreProcessHandlerMock.processHandler(
                    assertArg(createdCommand -> assertThat(createdCommand).containsExactly(
                            OptionsHandler.getQueryCommand(),
                            "-d host=eric-pm-kpi-data-v2 port=5432 user=postgres dbname=calculator-database",
                            "-c SELECT pg_terminate_backend(pg_stat_activity.pid) " +
                                    "FROM pg_stat_activity " +
                                    "WHERE pg_stat_activity.datname ='kpi_service_db' " +
                                    "AND pid <> pg_backend_pid();"
                    )),
                    assertArg(env -> assertThat(env).containsEntry("PGPASSWORD", "true"))
            )).thenReturn(ProcessResult.success("success"));

            final ProcessResult actual = objectUnderTest.terminateBackend("kpi_service_db");

            assertThat(actual).isEqualTo(ProcessResult.success("success"));
        });
    }

    @Test
    void shouldGrantAccess() {
        BackupAndRestoreConstantPrepares.prepareConstants(true, () -> {
            when(backupAndRestoreProcessHandlerMock.processHandler(
                    assertArg(createdCommand -> assertThat(createdCommand).containsExactly(
                            OptionsHandler.getQueryCommand(),
                            "-d host=eric-pm-kpi-data-v2 port=5432 user=postgres dbname=calculator-database",
                            "-c GRANT connect ON DATABASE kpi_service_db to \"kpi_service_user\";",
                            "-c GRANT connect ON DATABASE kpi_service_db to group PUBLIC;"
                    )),
                    assertArg(env -> assertThat(env).containsEntry("PGPASSWORD", "true"))
            )).thenReturn(ProcessResult.success("success"));

            final ProcessResult actual = objectUnderTest.grantAccess("kpi_service_db", "kpi_service_user");

            assertThat(actual).isEqualTo(ProcessResult.success("success"));
        });
    }
}