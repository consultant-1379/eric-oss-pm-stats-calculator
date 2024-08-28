/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.priority;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationPriorityRankerTest {
    @Mock CalculatorProperties calculatorPropertiesMock;
    @Mock CalculationService calculationServiceMock;

    @InjectMocks CalculationPriorityRanker objectUnderTest;

    @Nested
    class IsOnDemandPrioritized {

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class WhenBlockScheduledWhenHandlingOnDemand {
            @MethodSource("provideIsOnDemandPrioritizedData")
            @ParameterizedTest(name = "[{index}] blockScheduledWhenHandlingOnDemand: ''{0}'' and isAnyOnDemandCalculationRunning: ''{1}'' expected ==> ''{2}''")
            void shouldVerifyIsOnDemandPrioritized(final boolean blockScheduledWhenHandlingOnDemand, final boolean isAnyOnDemandCalculationRunning, final boolean expected) {
                when(calculatorPropertiesMock.getBlockScheduledWhenHandlingOnDemand()).thenReturn(blockScheduledWhenHandlingOnDemand);
                when(calculationServiceMock.isAnyOnDemandCalculationRunning()).thenReturn(isAnyOnDemandCalculationRunning);

                final boolean actual = objectUnderTest.isOnDemandPrioritized();

                verify(calculatorPropertiesMock).getBlockScheduledWhenHandlingOnDemand();
                verify(calculationServiceMock).isAnyOnDemandCalculationRunning();

                Assertions.assertThat(actual).isEqualTo(expected);
            }

            Stream<Arguments> provideIsOnDemandPrioritizedData() {
                return Stream.of(
                        Arguments.of(true, true, true),
                        Arguments.of(true, false, false)
                );
            }
        }

        @Nested
        class WhenNoBlockScheduledWhenHandlingOnDemand {
            @Test
            void shouldVerifyIsOnDemandPrioritized() {
                when(calculatorPropertiesMock.getBlockScheduledWhenHandlingOnDemand()).thenReturn(false);

                final boolean actual = objectUnderTest.isOnDemandPrioritized();

                verify(calculatorPropertiesMock).getBlockScheduledWhenHandlingOnDemand();
                verifyNoInteractions(calculationServiceMock);

                Assertions.assertThat(actual).isFalse();
            }
        }
    }
}