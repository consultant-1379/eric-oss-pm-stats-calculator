/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.model;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class BackupAndRestoreConstantsTest {

    @Test
    @SetEnvironmentVariable(key = "KPI_SERVICE_DB_PASSWORD", value = "pass1234")
    @SetEnvironmentVariable(key = "KPI_SERVICE_DB_USER", value = "kpi_service_user")
    @SetEnvironmentVariable(key = "BR_AGENT", value = "true")
    @SetEnvironmentVariable(key = "KPI_SERVICE_DB_JDBC_SERVICE_CONNECTION", value = "jdbc:postgresql://service-eric-pm-kpi-data:5432/kpi_service_db")
    void shouldReturnDatabaseHost() {
        Assertions.assertThat(BackupAndRestoreConstants.getPgHost()).isEqualTo("service-eric-pm-kpi-data");
        Assertions.assertThat(BackupAndRestoreConstants.getPgPort()).isEqualTo(5432);
        Assertions.assertThat(BackupAndRestoreConstants.getPgDatabase()).isEqualTo("kpi_service_db");
        Assertions.assertThat(BackupAndRestoreConstants.getPgClientUser()).isEqualTo("kpi_service_user");
        Assertions.assertThat(BackupAndRestoreConstants.getPgPass()).isEqualTo("pass1234");
        Assertions.assertThat(BackupAndRestoreConstants.getBrAgent()).isEqualTo("true");
    }

    @Test
    void shouldReturnDefaultWhenNotSet() {
        Assertions.assertThat(BackupAndRestoreConstants.getPgPass()).isEmpty();
    }

}