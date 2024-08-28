/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.utils;

import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.BACKUP_COMMAND;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.COMMAND_PATH;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.PG_ADMIN_USER;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.QUERY_COMMAND;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.RESTORE_COMMAND;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgDatabase;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgHost;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgPass;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgPort;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptionsHandler {

    private static final int RESTORE_JOBS = 10;
    private static final int COMPRESSION = 1;

    public static String getBackupCommand() {
        return Path.of(COMMAND_PATH).resolve(BACKUP_COMMAND).toString();
    }

    public static String getRestoreCommand() {
        return Path.of(COMMAND_PATH).resolve(RESTORE_COMMAND).toString();
    }

    public static String getQueryCommand() {
        return Path.of(COMMAND_PATH).resolve(QUERY_COMMAND).toString();
    }

    public static List<String> getBackupCommandOptions(final String fileFormat, final Path backupFilePath) {
        final List<String> options = new ArrayList<>();

        options.add(authenticationOptions());
        options.add(String.format("--format=%s", fileFormat));
        options.add(String.format("--file=%s", backupFilePath.toString()));
        options.add(String.format("--compress=%s", COMPRESSION));
        return options;
    }

    public static List<String> getRestoreCommandOptions(final String fileFormat) {
        final List<String> options = new ArrayList<>();
        options.add("--verbose");
        options.add("--clean");
        options.add(authenticationOptions());

        if (fileFormat != null && !fileFormat.isBlank()) {
            options.add(String.format("--format=%s", fileFormat));
        }
        options.add(String.format("--jobs=%s", RESTORE_JOBS));

        return options;
    }

    public static List<String> getQueryCommandOptions() {
        final List<String> options = new ArrayList<>();
        options.add(authenticationOptions());
        return options;
    }

    public static Map<String, String> getExtraEnv() {
        final Map<String, String> env = new HashMap<>();
        env.put("PGPASSWORD", getPgPass());
        return env;
    }

    private static String authenticationOptions() {
        return String.format("-d host=%s port=%s user=%s dbname=%s", getPgHost(), getPgPort(), PG_ADMIN_USER, getPgDatabase());
    }
}
