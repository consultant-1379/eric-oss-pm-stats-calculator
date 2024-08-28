/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.constant;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.HOURLY_AGGREGATION_PERIOD_IN_MINUTES;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiCalculatorConstantsTest {
    @MethodSource("provideConstantsData")
    @ParameterizedTest(name = "[{index}] ''{0}'' has value: ''{2}''")
    void shouldVerifyConstants(final String name, final Object value, final Object expected) {
        Assertions.assertThat(value).as(name).isEqualTo(expected);
    }

    private static Stream<Arguments> provideConstantsData() {
        return Stream.of(Arguments.of("DAILY_AGGREGATION_PERIOD_IN_MINUTES", DAILY_AGGREGATION_PERIOD_IN_MINUTES, 1_440),
                Arguments.of("HOURLY_AGGREGATION_PERIOD_IN_MINUTES", HOURLY_AGGREGATION_PERIOD_IN_MINUTES, 60),
                Arguments.of("DEFAULT_AGGREGATION_PERIOD", DEFAULT_AGGREGATION_PERIOD, "-1"),
                Arguments.of("DEFAULT_AGGREGATION_PERIOD_INT", DEFAULT_AGGREGATION_PERIOD_INT, -1));
    }
}