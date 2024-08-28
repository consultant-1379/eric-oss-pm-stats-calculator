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
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.sparkproject.guava.util.concurrent.Uninterruptibles;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.ericsson.oss.air.pm.stats.common.spark.launcher.SparkLauncherConfiguration.*;

/**
 * An Abstract class which is used to launch a Spark job, monitor it's state and retry on a failure condition.
 */
@Slf4j
public abstract class AbstractSparkLauncher implements Callable<SparkAppHandle.State> {

    public static final Set<SparkAppHandle.State> SPARK_JOB_FAILED_STATES = Collections
            .unmodifiableSet(EnumSet.of(SparkAppHandle.State.FAILED, SparkAppHandle.State.KILLED,
                    SparkAppHandle.State.LOST));

    private static final int PHASER_INITIAL_SIZE = 1;

    final AtomicReference<SparkAppHandle.State> currentSparkState = new AtomicReference<>(SparkAppHandle.State.UNKNOWN);

    private String applicationHomeDirectory;

    /**
     * Sets the application's home directory i.e. the location where the spark dependency jars are located.
     *
     * @param applicationHomeDirectory The location to use
     */
    public void setApplicationHomeDirectory(final String applicationHomeDirectory) {
        this.applicationHomeDirectory = applicationHomeDirectory;
    }

    /**
     * Launches a Spark job with the given {@code applicationParameters} supplied.
     *
     * @param applicationId The application ID used in the log
     * @return the {@link SparkAppHandle.State} of the completed Spark job
     */
    public SparkAppHandle.State startSparkLauncher(final String applicationId) {
        log.info("Started Spark job '{}'", applicationId);
        final Retry retry = getRetry();
        retry.getEventPublisher().onRetry(e -> log.info("Retrying spark job '{}'", applicationId));
        final Callable<SparkAppHandle.State> sparkStateCallable = Retry.decorateCallable(retry, this);
        final SparkAppHandle.State sparkAppHandleState = Try.ofCallable(sparkStateCallable).get();
        log.info("Finished Spark job '{}'", applicationId);
        return sparkAppHandleState;
    }

    /**
     * Launches the Spark job and waits for it to finish or fail.
     *
     * @return the {@link SparkAppHandle.State} of the Spark job
     * @throws Exception if there is an issue launching the Spark job
     */
    @Override
    public SparkAppHandle.State call() throws Exception {
        currentSparkState.set(SparkAppHandle.State.UNKNOWN);

        final Phaser phaser = new Phaser(PHASER_INITIAL_SIZE);
        phaser.register();

        startSparkLauncherWithListener(new SparkListener(currentSparkState, phaser));

        phaser.arriveAndAwaitAdvance();

        /*
         * From the spark javadoc: "A state can be 'final', in which case it will not change after it's reached, and means the application is not
         * running anymore." What happens in reality is described in the state transitions. The spark state transitions for a valid flow is: UNKNOWN->
         * CONNECTED -> SUBMITTED -> RUNNING -> FINISHED. When a spark job fails the state transition is: UNKNOWN -> CONNECTED -> SUBMITTED -> RUNNING
         * -> FINISHED -> FAILED
         */
        if (SparkAppHandle.State.FINISHED == currentSparkState.get()) {
            // If the job has failed the stateChanged method will have been called and the CURRENT_SPARK_STATE will
            // get updated.
            log.info("Waiting {} {} to ensure Spark job has not failed", POST_SPARK_EXECUTION_WAIT_TIME, POST_SPARK_EXECUTION_WAIT_TIME_UNIT);
            Uninterruptibles.sleepUninterruptibly(POST_SPARK_EXECUTION_WAIT_VALUE_MS, TimeUnit.MILLISECONDS);
        }

        log.info("SparkAppHandle is in final state '{}' and is no longer running", currentSparkState.get());
        return currentSparkState.get();
    }

    /**
     * Returns the {@code SparkLauncher} class specific implementation.
     *
     * @return a {@code SparkLauncher} implementation
     */
    protected abstract SparkLauncher getSparkLauncher();

    /**
     * Returns the {@code Retry} class specific implementation.
     *
     * @return a {@code Retry} implementation
     */
    protected abstract Retry getRetry();

    /**
     * List all the jars needed by Spark under a directory
     *
     * @return Spark dependencies jars names
     */
    List<String> getJarsAbsolutePaths() {
        if (Objects.isNull(applicationHomeDirectory)) {
            return Collections.emptyList();
        }

        final File directory = new File(applicationHomeDirectory);
        final File[] fileNames = directory.listFiles((dir, name) -> name.toLowerCase(Locale.UK).endsWith(".jar"));
        if (Objects.isNull(fileNames)) {
            return Collections.emptyList();
        }
        return Arrays.stream(fileNames)
                .filter(Objects::nonNull)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    void startSparkLauncherWithListener(final SparkAppHandle.Listener listener) throws IOException {
        final SparkLauncher sparkLauncher = getSparkLauncher();
        getJarsAbsolutePaths().forEach(sparkLauncher::addJar);
        sparkLauncher.startApplication(listener);
    }
}