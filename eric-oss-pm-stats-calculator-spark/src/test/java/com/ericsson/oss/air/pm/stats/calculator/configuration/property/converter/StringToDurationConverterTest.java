/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration.property.converter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringToDurationConverterTest {
    StringToDurationConverter objectUnderTest = new StringToDurationConverter();

    @MethodSource("provideConvertStringToDurationData")
    @ParameterizedTest(name = "[{index}] Source ''{0}'' converted to => ''{1}''")
    void shouldConvertStringToDuration(final String source, final Duration expected) {
        final Duration actual = objectUnderTest.convert(source);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideConvertStringToDurationData() {
        return Stream.of(
                Arguments.of("ALL", ChronoUnit.FOREVER.getDuration()),
                Arguments.of("10ms", Duration.ofMillis(10)),
                Arguments.of("10s", Duration.ofSeconds(10)),
                Arguments.of("10h", Duration.ofHours(10)),
                Arguments.of("10d", Duration.ofDays(10))
        );
    }
}