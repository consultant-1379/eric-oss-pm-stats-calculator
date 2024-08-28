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

import static java.time.Duration.ofMillis;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;

import java.time.Duration;
import java.util.List;

import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(key = "org.slf4j.simpleLogger.defaultLogLevel", value = "DEBUG")
class BackupAndRestoreProcessHandlerTest {
    BackupAndRestoreProcessHandler objectUnderTest = new BackupAndRestoreProcessHandler();

    @Nested
    @EnabledOnOs(OS.LINUX)
    class EnabledOnLinux {
        @Test
        void shouldFail_whenCommandIsNotOk() {
            final List<String> command = List.of("unknown", "command");
            final String expected = "Cannot run program \"unknown\": error=2, No such file or directory";

            final ProcessResult actual = objectUnderTest.processHandler(command, emptyMap());
            assertThat(actual).isEqualTo(ProcessResult.ioException(expected));
        }

        @Test
        void shouldSimulate_whenProcessIsAlreadyRunning() {
            startDuplicatedProcessAfter(ofMillis(500));

            final ProcessResult actual = objectUnderTest.processHandler(List.of("sleep", "2"), emptyMap());
            assertThat(actual.getExitCode()).isZero();
        }

        @Test
        void shouldSimulate_whenProcessIsAlreadyRunning_andItIsCancelled() {
            cancelProcessAfter(ofMillis(500));

            final ProcessResult actual = objectUnderTest.processHandler(List.of("sleep", "2"), emptyMap());
            assertThat(actual).isEqualTo(ProcessResult.cancellationException(null));
        }
    }

    @Nested
    @EnabledOnOs(OS.WINDOWS)
    class EnabledOnWindows {
        @Test
        void shouldFail_whenCommandIsNotOk() {
            final List<String> command = List.of("unknown", "command");
            final String expected = "Cannot run program \"unknown\": CreateProcess error=2, The system cannot find the file specified";

            final ProcessResult actual = objectUnderTest.processHandler(command, emptyMap());
            assertThat(actual).isEqualTo(ProcessResult.ioException(expected));
        }

        @Test
        void shouldSimulate_whenProcessIsAlreadyRunning() {
            startDuplicatedProcessAfter(ofMillis(500));

            final ProcessResult actual = objectUnderTest.processHandler(List.of("powershell", "-command", "\"Start-Sleep -s 1\""), emptyMap());
            assertThat(actual.getExitCode()).isZero();
        }

        @Test
        void shouldSimulate_whenProcessIsAlreadyRunning_andItIsCancelled() {
            cancelProcessAfter(ofMillis(500));

            final ProcessResult actual = objectUnderTest.processHandler(List.of("powershell", "-command", "\"Start-Sleep -s 2\""), emptyMap());
            assertThat(actual).isEqualTo(ProcessResult.cancellationException(null));
        }
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_17, disabledReason = "Command available on Java [1.8 .. 17]")
    void shouldExecuteSimpleCommand() {
        final List<String> command = List.of("java", "-version");

        final ProcessResult actual = objectUnderTest.processHandler(command, emptyMap());

        assertThat(actual.getExitCode()).isZero();
        assertThat(actual.getResultLog()).isNotEmpty();
    }

    @Test
    void shouldDoNothing_whenCancel_andNoProcessIsRunning() {
        Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.cancelProcess());
    }

    @Test
    void shouldHandleExecutionException() {
        final ProcessResult processResult = objectUnderTest.processHandler(emptyList(), emptyMap());
        assertThat(processResult).isEqualTo(ProcessResult.executionException("java.lang.ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0"));
    }

    void startDuplicatedProcessAfter(final Duration sleepFor) {
        new Thread(() -> {
            sleepUninterruptibly(sleepFor);
            final ProcessResult processResult = objectUnderTest.processHandler(List.of("unknown", "command"), emptyMap());
            assertThat(processResult).isEqualTo(ProcessResult.duplicatedProcessCall());
        }).start();
    }

    void cancelProcessAfter(final Duration sleepFor) {
        new Thread(() -> {
            sleepUninterruptibly(sleepFor);
            objectUnderTest.cancelProcess();
        }).start();
    }
}