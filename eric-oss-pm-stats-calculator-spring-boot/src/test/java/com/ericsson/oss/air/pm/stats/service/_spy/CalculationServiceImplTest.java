/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.service.CalculationServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {
    @Spy
    CalculationServiceImpl objectUnderTest;

    @Test
    void shouldVerifyIsAnyOnDemandCalculationRunning() {
        doReturn(true).when(objectUnderTest).isAnyCalculationRunning("ON_DEMAND");

        final boolean actual = objectUnderTest.isAnyOnDemandCalculationRunning();

        verify(objectUnderTest).isAnyCalculationRunning("ON_DEMAND");

        Assertions.assertThat(actual).isTrue();
    }
}