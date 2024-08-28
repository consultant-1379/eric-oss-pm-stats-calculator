/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.finalize;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.time.Duration;

import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HangingCalculationFinalizerTest {
    @Mock
    CalculationService calculationServiceMock;

    @InjectMocks
    HangingCalculationFinalizer objectUnderTest;

    @Nested
    class FinalizeHangingCalculationAfter {
        @Test
        void shouldFinalizeHangingCalculation() throws Exception {
            objectUnderTest.finalizeHangingCalculationsAfter(Duration.ZERO);

            verify(calculationServiceMock).finalizeHangingStartedCalculations(Duration.ZERO);
        }

        @Test
        void shouldDoNothing_whenFinalizationFails() throws Exception {
            doThrow(new SQLException("failed")).when(calculationServiceMock).finalizeHangingStartedCalculations(Duration.ZERO);

            objectUnderTest.finalizeHangingCalculationsAfter(Duration.ZERO);

            verify(calculationServiceMock).finalizeHangingStartedCalculations(Duration.ZERO);
        }
    }
}