/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

class CalculationLimitTest {

    @ParameterizedTest
    @MethodSource("provideDefaultValuesData")
    void shouldValidateDefaultValues(final CalculationLimit calculationLimit) {

        Assertions.assertThat(calculationLimit.calculationEndTime()).isEqualTo(LocalDateTime.MIN);
        Assertions.assertThat(calculationLimit.calculationStartTime()).isEqualTo(LocalDateTime.MIN);
    }

    static Stream<Arguments> provideDefaultValuesData() {
        return Stream.of(
                Arguments.of(CalculationLimit.DEFAULT),
                Arguments.of(CalculationLimit.builder().build())
        );
    }
}