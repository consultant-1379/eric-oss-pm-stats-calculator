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

import static com.ericsson.oss.air.pm.stats.common.env.Environment.getEnvironmentValue;

import java.net.URI;
import java.nio.file.Path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackupAndRestoreConstants {

    public static final String BACKUP_COMMAND = "pg_dump";
    public static final String RESTORE_COMMAND = "pg_restore";
    public static final String QUERY_COMMAND = "psql";
    public static final String FILE_FORMAT = "c";
    public static final String COMMAND_PATH = Path.of("/usr").resolve("bin").toString();
    public static final Path BACKUP_PATH = Path.of("/tmp").resolve("bragent").resolve("backup");
    public static final String BACKUP_PGDUMP_FILE = "pgdump.tar.gz";
    public static final String SOFTWARE_VERSION_DESCRIPTION_PROPERTY = "No Description";
    public static final String SOFTWARE_VERSION_PRODUCTION_DATE_PROPERTY = "No date";
    public static final String SOFTWARE_VERSION_TYPE_PROPERTY = "Not set";
    public static final String SOFTWARE_VERSION_PRODUCT_NAME = "eric-oss-pm-stats-calculator-backup-agent";
    public static final String SOFTWARE_VERSION_PRODUCT_NUMBER = "APR201564";
    public static final String SOFTWARE_VERSION_REVISION_PROPERTY = "0.0.0";
    public static final String API_VERSION_PROPERTY = "4";
    public static final String SCOPE_PROPERTY = "PLATFORM";
    public static final int FRAGMENT_NUMBER = 1;
    public static final String FRAGMENT_VERSION = "0.0.0";
    public static final String PG_ADMIN_USER = "postgres";

    public static Path getBackupPath() {
        return BACKUP_PATH;
    }

    public static String getPgHost() {
        return getDatabaseUrl().getHost();
    }

    public static int getPgPort() {
        return getDatabaseUrl().getPort();
    }

    public static String getPgDatabase() {
        return getDatabaseUrl().getPath().substring(1);
    }

    public static String getAgentIdProperty() { return getEnvironmentValue("BACKUP_REGISTRATION_NAME");}

    public static String getPgClientUser() {
        return getEnvironmentValue("KPI_SERVICE_DB_USER");
    }

    public static String getPgPass() {
        return getEnvironmentValue("KPI_SERVICE_DB_PASSWORD", "");
    }

    public static String getBrAgent() {
        return getEnvironmentValue("BR_AGENT");
    }

    private static URI getDatabaseUrl() {
        final String jdbcUrl = getEnvironmentValue("KPI_SERVICE_DB_JDBC_SERVICE_CONNECTION");
        return URI.create(jdbcUrl.substring(5));
    }
}
