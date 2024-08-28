/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.MAX_SPARK_RETRY_ATTEMPTS;
import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.SPARK_RETRY_DURATION;
import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.SPARK_RETRY_DURATION_VALUE_MS;
import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.SPARK_RETRY_MULTIPLIER;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiCalculatorSparkLauncherConfigurationTest {
    @MethodSource("provideConstantsData")
    @ParameterizedTest(name = "[{index}] ''{0}'' has value: ''{2}''")
    void shouldVerifyConstants(final String name, final Object value, final Object expected) {
        Assertions.assertThat(value).as(name).isEqualTo(expected);
    }

    private static Stream<Arguments> provideConstantsData() {
        return Stream.of(Arguments.of("SPARK_RETRY_DURATION", SPARK_RETRY_DURATION, 1),
                         Arguments.of("MAX_SPARK_RETRY_ATTEMPTS", MAX_SPARK_RETRY_ATTEMPTS, 2),
                         Arguments.of("SPARK_RETRY_MULTIPLIER", SPARK_RETRY_MULTIPLIER, 1.5D),
                         Arguments.of("SPARK_RETRY_DURATION_VALUE_MS", SPARK_RETRY_DURATION_VALUE_MS, 60_000L));
    }
}