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

import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.BACKUP_PATH;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.FILE_FORMAT;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.PG_ADMIN_USER;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgDatabase;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgHost;
import static com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants.getPgPort;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.bragent.model.BackupAndRestoreConstants;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class OptionsHandlerTest {

    @Test
    void shouldGetRestoreCommand() {
        final String expected = Path.of("/usr/bin").resolve("pg_restore").toString();
        final String actual = OptionsHandler.getRestoreCommand();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGetQueryCommand() {
        final String expected = Path.of("/usr/bin").resolve("psql").toString();
        final String actual = OptionsHandler.getQueryCommand();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGetRestoreOption() {
        try (MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("eric-pm-kpi-data-v2");


            final List<String> expected = List.of("--verbose", "--clean", String.format("--format=%s", FILE_FORMAT), "--jobs=10",
                    String.format("-d host=%s port=%s user=%s dbname=%s", getPgHost(), getPgPort(), PG_ADMIN_USER, getPgDatabase()));
            final List<String> actual = OptionsHandler.getRestoreCommandOptions("c");
            assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    @Test
    void shouldGetRestoreOptionAndFileFormat() {
        try (MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("eric-pm-kpi-data-v2");

            final List<String> expected = List.of("--verbose", "--clean", "--jobs=10",
                    String.format("-d host=%s port=%s user=%s dbname=%s", getPgHost(), getPgPort(), PG_ADMIN_USER, getPgDatabase()));
            final List<String> actual = OptionsHandler.getRestoreCommandOptions("");
            assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    @Test
    void shouldGetQueryOption() {
        try (MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("eric-pm-kpi-data-v2");

            final List<String> expected = List.of(
                    String.format("-d host=%s port=%s user=%s dbname=%s", getPgHost(), getPgPort(), PG_ADMIN_USER, getPgDatabase()));
            final List<String> actual = OptionsHandler.getQueryCommandOptions();
            assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    @Test
    void shouldGetBackupCommand() {
        String expected = Path.of("/usr/bin").resolve("pg_dump").toString();
        String actual = OptionsHandler.getBackupCommand();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGetCommandOptions() {
        try (MockedStatic<BackupAndRestoreConstants> backupAndRestoreConstantsMock = Mockito.mockStatic(BackupAndRestoreConstants.class)) {
            backupAndRestoreConstantsMock.when(BackupAndRestoreConstants::getPgHost).thenReturn("eric-pm-kpi-data-v2");
            final List<String> actual = OptionsHandler.getBackupCommandOptions(FILE_FORMAT, BACKUP_PATH.resolve("pgdump.tar.gz"));

            final List<String> expected = List.of(String.format("--format=%s", FILE_FORMAT), String.format("--file=%s", BACKUP_PATH.resolve("pgdump.tar.gz")),
                    String.format("-d host=%s port=%s user=%s dbname=%s", getPgHost(), getPgPort(), PG_ADMIN_USER, getPgDatabase()), "--compress=1");

            assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    @Test
    void shouldEmptyExtraEnv() {
        Map<String, String> actual = OptionsHandler.getExtraEnv();
        assertThat(actual).hasSize(1);
        assertThat(actual).containsKey("PGPASSWORD");
        assertThat(actual).doesNotContainValue(null);
    }
}