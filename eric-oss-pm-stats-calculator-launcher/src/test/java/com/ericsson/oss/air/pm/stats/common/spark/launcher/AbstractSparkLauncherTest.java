/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.launcher;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sparkproject.guava.util.concurrent.Uninterruptibles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ericsson.oss.air.pm.stats.common.spark.launcher.SparkLauncherConfiguration.POST_SPARK_EXECUTION_WAIT_VALUE_MS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AbstractSparkLauncher}.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class AbstractSparkLauncherTest {
    static final String APPLICATION_ID = "test application";
    static final List<String> TEST_JARS = Arrays.asList("ecson-api.jar", "ecson-impl.jar");

    @TempDir
    Path tempDir;

    @Mock
    SparkLauncher sparkLauncher;

    AbstractSparkLauncher abstractSparkLauncher;

    @BeforeEach
    void beforeTest() {
        abstractSparkLauncher = getAbstractSparkLauncher();
    }

    @Test
    void whenApplicationHomeDirectoryIsNotSet_thenNoJarsWillBeIncludedInSparkClasspath() {
        assertTrue(abstractSparkLauncher.getJarsAbsolutePaths().isEmpty());
    }

    @Test
    void whenApplicationHomeDirectoryIsSet_thenJarsWillBeIncludedInSparkClasspath() {
        assertTrue(abstractSparkLauncher.getJarsAbsolutePaths().isEmpty());

        createTestJarFiles();
        abstractSparkLauncher.setApplicationHomeDirectory(tempDir.toFile().getAbsolutePath());

        // Using TreeSets to keep order consistent
        final Set<String> jarsAbsolutePaths = new TreeSet<>(abstractSparkLauncher.getJarsAbsolutePaths());
        final Set<String> expectedJarsAbsolutePaths = TEST_JARS.stream()
                .map(jar -> tempDir.toFile().getAbsolutePath() + File.separator + jar)
                .collect(Collectors.toSet());

        assertThat(jarsAbsolutePaths)
                .isEqualTo(expectedJarsAbsolutePaths);
    }

    @Test
    void whenApplicationHomeDirectoryIsSetButDirectoryContainNoJars_thenNoJarsWillBeIncludedInSparkClasspath() {
        assertTrue(abstractSparkLauncher.getJarsAbsolutePaths().isEmpty());

        abstractSparkLauncher.setApplicationHomeDirectory(tempDir.toFile().getAbsolutePath() + "_does_not_exist");

        assertTrue(abstractSparkLauncher.getJarsAbsolutePaths().isEmpty());
    }

    @Test
    void whenStartSparkLauncherWithListenerIsCalledWithListener_thenItRequestsTheSparkLauncherToStart() throws IOException {
        createTestJarFiles();
        final SparkAppHandle.Listener listener = mock(SparkAppHandle.Listener.class);

        abstractSparkLauncher.setApplicationHomeDirectory(tempDir.toFile().getAbsolutePath());
        abstractSparkLauncher.startSparkLauncherWithListener(listener);

        verify(sparkLauncher, times(2)).addJar(any(String.class));
        verify(sparkLauncher).startApplication(listener);
    }

    @Test
    void whenStartSparkLauncherIsCalledAndItRunsSuccessfully_thenFinishedStateIsReturned() throws Exception {
        try (MockedStatic<Uninterruptibles> uninterruptiblesMockedStatic = mockStatic(Uninterruptibles.class)) {
            when(sparkLauncher.startApplication(any(SparkAppHandle.Listener.class)))
                    .thenAnswer(answer -> getSparkAppHandle(answer, SparkAppHandle.State.FINISHED));

            final SparkAppHandle.State completedState = abstractSparkLauncher.startSparkLauncher(APPLICATION_ID);
            assertEquals(SparkAppHandle.State.FINISHED, completedState);
        }
    }

    @Test
    void whenStartSparkLauncherIsCalledAndItFailsItRetriesTwice_thenFailedStateIsReturned() throws Exception {
        try (MockedStatic<Uninterruptibles> uninterruptiblesMockedStatic = mockStatic(Uninterruptibles.class)) {
            uninterruptiblesMockedStatic.when(() -> Uninterruptibles.sleepUninterruptibly(POST_SPARK_EXECUTION_WAIT_VALUE_MS, TimeUnit.MILLISECONDS)).thenAnswer(invocation -> {
                Thread.sleep(300);
                return null;
            });
            when(sparkLauncher.startApplication(any(SparkAppHandle.Listener.class)))
                    .thenAnswer(answer -> getSparkAppHandle(answer, SparkAppHandle.State.FAILED))
                    .thenAnswer(answer -> getSparkAppHandle(answer, SparkAppHandle.State.FAILED));

            final SparkAppHandle.State completedState = abstractSparkLauncher.startSparkLauncher(APPLICATION_ID);
            uninterruptiblesMockedStatic.verify(() -> Uninterruptibles.sleepUninterruptibly(POST_SPARK_EXECUTION_WAIT_VALUE_MS, TimeUnit.MILLISECONDS), times(2));
            assertEquals(SparkAppHandle.State.FAILED, completedState);
        }
    }

    AbstractSparkLauncher getAbstractSparkLauncher() {
        return new AbstractSparkLauncher() {
            @Override
            protected SparkLauncher getSparkLauncher() {
                return sparkLauncher;
            }

            @Override
            protected Retry getRetry() {
                return Retry.of("testRetry",
                        RetryConfig.<SparkAppHandle.State>custom().retryOnResult(result -> result == SparkAppHandle.State.FAILED).maxAttempts(2)
                                .ignoreExceptions(Throwable.class).build());
            }
        };
    }

    void createTestJarFiles() {
        TEST_JARS.forEach(jarFileName -> {
            try {
                Files.createFile(tempDir.resolve(jarFileName));
            } catch (final IOException e) {
                log.warn("Failed to create jar file '{}'", jarFileName, e);
            }
        });
    }

    SparkAppHandle getSparkAppHandle(final InvocationOnMock invocationOnMock, final SparkAppHandle.State response) {
        final SparkListener sparkListener = invocationOnMock.getArgument(0);
        assertEquals(SparkAppHandle.State.UNKNOWN, abstractSparkLauncher.currentSparkState.get());

        final SparkAppHandle sparkAppHandle = mock(SparkAppHandle.class);
        when(sparkAppHandle.getAppId()).thenReturn("test");

        new Thread(() -> {
            when(sparkAppHandle.getState()).thenReturn(SparkAppHandle.State.FINISHED);
            sparkListener.stateChanged(sparkAppHandle);

            await().atMost(2, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(abstractSparkLauncher.currentSparkState.get()).isEqualTo(SparkAppHandle.State.FINISHED));

            if (SparkAppHandle.State.FINISHED == response) {
                return;
            }

            when(sparkAppHandle.getState()).thenReturn(response);
            sparkListener.stateChanged(sparkAppHandle);

            await().atMost(2, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(abstractSparkLauncher.currentSparkState.get()).isEqualTo(response));
        }).start();
        return sparkAppHandle;
    }

}