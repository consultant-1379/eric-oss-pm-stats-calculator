/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class KpiCalculationMediatorImplTest {
    @Mock
    KpiCalculationExecutionController kpiCalculationExecutionControllerMock;

    @InjectMocks
    KpiCalculationMediatorImpl objectUnderTest;

    @Test
    void removeRunningCalculationShouldBeCalled() {
        final UUID uuid = UUID.fromString("42ae9673-32ba-41a1-a87c-6adf8175b1dd");

        Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.removeRunningCalculation(uuid));

        verify(kpiCalculationExecutionControllerMock).removeRunningCalculationAndStartNext(uuid);
    }
}