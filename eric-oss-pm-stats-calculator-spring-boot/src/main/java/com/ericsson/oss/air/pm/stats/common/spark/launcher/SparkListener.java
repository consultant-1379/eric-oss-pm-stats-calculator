/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.launcher;

import static com.ericsson.oss.air.pm.stats.common.spark.launcher.AbstractSparkLauncher.SPARK_JOB_FAILED_STATES;

import java.util.Objects;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;

/**
 * A {@link SparkAppHandle.Listener} used to track the various state changes during a Spark job execution.
 */
@Slf4j
public class SparkListener implements SparkAppHandle.Listener {

    private final AtomicReference<SparkAppHandle.State> currentSparkState;

    private final Phaser phaser;

    SparkListener(final AtomicReference<SparkAppHandle.State> currentSparkState, final Phaser phaser) {
        this.currentSparkState = currentSparkState;
        this.phaser = phaser;
    }

    /**
     * In this scenario we are only interested in the following state changes FINISHED, FAILED, KILLED, and LOST these are considered final states. If
     * a job returns one of these states then the Spark job has finished running. This method is only triggered when there is an update to the
     * handle's state.
     *
     * @param sparkAppHandle
     *            The updated handle
     */
    @Override
    public final void stateChanged(final SparkAppHandle sparkAppHandle) {
        log.debug("CM parsing Spark job state changed to: '{}'", sparkAppHandle.getState());
        if (Objects.isNull(sparkAppHandle.getAppId())) {
            return;
        }

        if (!sparkAppHandle.getState().isFinal()) {
            log.debug("Ignoring Spark application ID '{}' with state '{}'", sparkAppHandle.getAppId(), sparkAppHandle.getState());
            return;
        }

        if ((SPARK_JOB_FAILED_STATES.contains(sparkAppHandle.getState()) || SparkAppHandle.State.FINISHED == sparkAppHandle.getState())) {
            currentSparkState.set(sparkAppHandle.getState());
            phaser.arriveAndDeregister();
        }
    }

    @Override
    public void infoChanged(final SparkAppHandle sparkAppHandle) {
        // Not interested in this event
    }
}
