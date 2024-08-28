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

import org.apache.spark.launcher.SparkAppHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SparkListenerTest {
    final SparkAppHandle sparkAppHandle = mock(SparkAppHandle.class);

    final Phaser phaser = mock(Phaser.class);

    final AtomicReference<SparkAppHandle.State> currentState = new AtomicReference<>(SparkAppHandle.State.UNKNOWN);

    static final UUID CALCULATION_ID = UUID.fromString("812f4d8a-6c8f-4dcd-b278-e4dfadbacc7f");

    SparkListener cmDynamicTransformerSparkListener;

    @BeforeEach
    void before() {
        currentState.set(SparkAppHandle.State.UNKNOWN);
    }

    @Test
    void whenStateChangedIsCalledAndSparkAppIdIsNull_thenStateRemainsUnchanged() {
        when(sparkAppHandle.getAppId()).thenReturn(null);

        cmDynamicTransformerSparkListener = new SparkListener(currentState, phaser);
        cmDynamicTransformerSparkListener.stateChanged(sparkAppHandle);

        assertEquals(SparkAppHandle.State.UNKNOWN, currentState.get());
    }

    @Test
    void whenStateChangedIsCalledAndSparkSparkStateIsNotFinal_thenStateRemainsUnchanged() {
        when(sparkAppHandle.getAppId()).thenReturn(CALCULATION_ID.toString());
        when(sparkAppHandle.getState()).thenReturn(SparkAppHandle.State.SUBMITTED);

        cmDynamicTransformerSparkListener = new SparkListener(currentState, phaser);
        cmDynamicTransformerSparkListener.stateChanged(sparkAppHandle);

        assertEquals(SparkAppHandle.State.UNKNOWN, currentState.get());
    }

    @Test
    void whenStateChangedIsCalledAndSparkStateIsFinished_thenStateIsSetToFinishedAndThePhaserIsTriggered() {
        when(sparkAppHandle.getAppId()).thenReturn(CALCULATION_ID.toString());
        when(sparkAppHandle.getState()).thenReturn(SparkAppHandle.State.FINISHED);

        cmDynamicTransformerSparkListener = new SparkListener(currentState, phaser);
        cmDynamicTransformerSparkListener.stateChanged(sparkAppHandle);

        assertEquals(SparkAppHandle.State.FINISHED, currentState.get());
        verify(phaser, times(1)).arriveAndDeregister();
    }

    @Test
    void whenStateChangedIsCalledAndSparkStateIsFailed_thenStateIsSetToFailedAndThePhaserIsTriggered() {
        when(sparkAppHandle.getAppId()).thenReturn(CALCULATION_ID.toString());
        when(sparkAppHandle.getState()).thenReturn(SparkAppHandle.State.FAILED);

        cmDynamicTransformerSparkListener = new SparkListener(currentState, phaser);
        cmDynamicTransformerSparkListener.stateChanged(sparkAppHandle);

        assertEquals(SparkAppHandle.State.FAILED, currentState.get());
        verify(phaser, times(1)).arriveAndDeregister();
    }

    @Test
    void whenInfoChangedIsCalled_thenNoInteractionTakesPlace() {
        cmDynamicTransformerSparkListener = new SparkListener(currentState, phaser);
        cmDynamicTransformerSparkListener.infoChanged(sparkAppHandle);
        verify(sparkAppHandle, never()).getState();
        verify(sparkAppHandle, never()).getAppId();
    }

}