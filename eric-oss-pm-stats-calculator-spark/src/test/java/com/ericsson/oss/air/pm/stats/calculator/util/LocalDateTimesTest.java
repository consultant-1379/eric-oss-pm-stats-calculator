/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalDateTimesTest {

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class Min {
        @MethodSource("provideMinData")
        @ParameterizedTest(name = "[{index}] min(''{0}'', ''{1}'') ==> ''{2}''")
        void shouldVerifyMin(final LocalDateTime left, final LocalDateTime right, final LocalDateTime expected) {
            final LocalDateTime actual = LocalDateTimes.min(left, right);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideMinData() {
            return Stream.of(
                    Arguments.of(testTime(12, 0), testTime(12, 0), testTime(12, 0)),
                    Arguments.of(testTime(12, 0), testTime(12, 1), testTime(12, 0)),
                    Arguments.of(testTime(12, 1), testTime(12, 0), testTime(12, 0))
            );
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class Max {
        @MethodSource("provideMaxData")
        @ParameterizedTest(name = "[{index}] max(''{0}'', ''{1}'') ==> ''{2}''")
        void shouldVerifyMax(final LocalDateTime left, final LocalDateTime right, final LocalDateTime expected) {
            final LocalDateTime actual = LocalDateTimes.max(left, right);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideMaxData() {
            return Stream.of(
                    Arguments.of(testTime(12, 0), testTime(12, 0), testTime(12, 0)),
                    Arguments.of(testTime(12, 0), testTime(12, 1), testTime(12, 1)),
                    Arguments.of(testTime(12, 1), testTime(12, 0), testTime(12, 1))
            );
        }
    }

    static LocalDateTime testTime(final int hour, final int min) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 29, hour, min);
    }
}