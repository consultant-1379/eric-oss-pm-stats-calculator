/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiCalculationStateTest {
    @Test
    void shouldVerifySuccessfulDoneKpiCalculationStates() {
        final List<KpiCalculationState> actual = KpiCalculationState.getSuccessfulDoneKpiCalculationStates();
        Assertions.assertThat(actual)
                .isUnmodifiable()
                .containsExactly(KpiCalculationState.FINISHED, KpiCalculationState.FINALIZING);
    }

    @Test
    void shouldVerifyRunningCalculationStates() {
        final List<KpiCalculationState> actual = KpiCalculationState.getRunningKpiCalculationStates();
        Assertions.assertThat(actual)
                .isUnmodifiable()
                .containsExactly(KpiCalculationState.STARTED, KpiCalculationState.IN_PROGRESS);
    }

    @MethodSource("provideEnumToStrings")
    @ParameterizedTest(name = "[{index}] kpiCalculationState: {0} has toString of {1}")
    void shouldTestToString(final KpiCalculationState kpiCalculationState, final String expectedToString) {
        Assertions.assertThat(kpiCalculationState)
                .as("To String representation is used to filter records")
                .hasToString(expectedToString);
    }

    @Test
    void shouldValidateValues() {
        Assertions.assertThat(KpiCalculationState.values())
                .as("Enum is persisted to the database. Deleting values might violate consistency.")
                .containsExactlyInAnyOrder(KpiCalculationState.STARTED,
                        KpiCalculationState.IN_PROGRESS,
                        KpiCalculationState.FINALIZING,
                        KpiCalculationState.FINISHED,
                        KpiCalculationState.FAILED,
                        KpiCalculationState.LOST,
                        KpiCalculationState.NOTHING_CALCULATED);
    }

    private static Stream<Arguments> provideEnumToStrings() {
        return Stream.of(Arguments.of(KpiCalculationState.STARTED, "STARTED"),
                Arguments.of(KpiCalculationState.IN_PROGRESS, "IN_PROGRESS"),
                Arguments.of(KpiCalculationState.FINALIZING, "FINALIZING"),
                Arguments.of(KpiCalculationState.FINISHED, "FINISHED"),
                Arguments.of(KpiCalculationState.FAILED, "FAILED"),
                Arguments.of(KpiCalculationState.LOST, "LOST"),
                Arguments.of(KpiCalculationState.NOTHING_CALCULATED, "NOTHING_CALCULATED"));
    }
}