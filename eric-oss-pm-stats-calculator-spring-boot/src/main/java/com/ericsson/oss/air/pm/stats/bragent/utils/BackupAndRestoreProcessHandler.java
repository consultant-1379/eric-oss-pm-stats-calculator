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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.bragent.model.ProcessResult;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class BackupAndRestoreProcessHandler {
    private final ExecutorService executorService = brAgentExecutorService();

    private final Lock lock = new ReentrantLock();

    @Nullable
    private Future<ProcessResult> processResultFuture;

    public ProcessResult processHandler(final List<String> command, final Map<String, String> env) {
        if (lock.tryLock()) {
            try {
                processResultFuture = executorService.submit(new ProcessHandlerCallable(command, env));
                return computeProcessResult(processResultFuture);
            } finally {
                processResultFuture = null;
                lock.unlock();
            }
        }

        log.warn("Process is being handled at the moment, another one cannot be submitted until the running one finishes");
        return ProcessResult.duplicatedProcessCall();
    }

    private static ProcessResult computeProcessResult(@NonNull final Future<ProcessResult> future) {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            log.error("Execution exception occurred", e);
            return ProcessResult.executionException(e.getMessage());
        } catch (final CancellationException e) {
            log.error("Cancellation exception occurred", e);
            return ProcessResult.cancellationException(e.getMessage());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return ProcessResult.interruptedException(e.getMessage());
        }
    }

    public void cancelProcess() {
        if (processResultFuture == null) {
            log.warn("No process is running currently");
            return;
        }

        processResultFuture.cancel(true);
        log.info("Running process has been cancelled");
    }

    private static ExecutorService brAgentExecutorService() {
        final ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        threadFactoryBuilder.setNameFormat("br-agent-pool-%d");

        return newSingleThreadExecutor(threadFactoryBuilder.build());
    }

    private static final class ProcessHandlerCallable implements Callable<ProcessResult> {
        private final List<String> command;
        private final Map<String, String> environment;

        @Nullable
        private Process process;

        private ProcessHandlerCallable(final List<String> command, final Map<String, String> environment) {
            this.command = new ArrayList<>(command);
            this.environment = new HashMap<>(environment);
        }

        @Override
        public ProcessResult call() throws InterruptedException {
            if (log.isDebugEnabled()) {
                log.debug("Command required to be executed '{}'", String.join(" ", command));
            }

            try {
                final ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.environment().putAll(environment);
                processBuilder.redirectErrorStream(true);

                process = processBuilder.start();

                final StringBuilder result = new StringBuilder();
                try (final BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
                    stdInput.lines().forEach(processOutput -> {
                        log.info(processOutput);
                        result.append(processOutput).append(System.lineSeparator());
                    });
                }

                final int exitCode = process.waitFor();

                return ProcessResult.of(exitCode, result.toString());
            } catch (final IOException e) {
                log.error("I/O error occurred", e);
                return ProcessResult.ioException(e.getMessage());
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }

    }
}

