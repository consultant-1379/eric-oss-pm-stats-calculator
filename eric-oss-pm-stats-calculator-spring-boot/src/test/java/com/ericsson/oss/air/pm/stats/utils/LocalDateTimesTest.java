/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalDateTimesTest {
    @MethodSource("provideMinData")
    @ParameterizedTest(name = "[{index}] min(''{0}'', ''{1}'') ==> ''{2}''")
    void shouldReturnMin(final LocalDateTime left, final LocalDateTime right, final LocalDateTime expected) {
        final LocalDateTime actual = LocalDateTimes.min(left, right);
        assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideMaxData")
    @ParameterizedTest(name = "[{index}] max(''{0}'', ''{1}'') ==> ''{2}''")
    void maxTest(final LocalDateTime left, final LocalDateTime right, final LocalDateTime expected) {

        assertThat(LocalDateTimes.max(left, right)).isEqualTo(expected);

    }

    @MethodSource("provideCompareData")
    @ParameterizedTest(name = "[{index}] ''{0}'' is after or equal to: ''{1}'') ==> ''{2}''")
    void AfterOrEqualTest(final LocalDateTime left, final LocalDateTime right, final boolean expected) {

        assertThat(LocalDateTimes.isAfterOrEqual(left, right)).isEqualTo(expected);

    }

    static Stream<Arguments> provideMaxData() {
        return Stream.of(
                Arguments.of(testTime(12, 0), testTime(11, 59), testTime(12, 0)),
                Arguments.of(testTime(12, 0), testTime(12, 1), testTime(12, 1)),
                Arguments.of(testTime(12, 0), testTime(12, 0), testTime(12, 0))
        );
    }

    static Stream<Arguments> provideCompareData() {
        return Stream.of(
                Arguments.of(testTime(12, 0), testTime(11, 59), true),
                Arguments.of(testTime(12, 0), testTime(12, 1), false),
                Arguments.of(testTime(12, 0), testTime(12, 0), true)
        );
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 12, hour, minute);
    }

    static Stream<Arguments> provideMinData() {
        final LocalDateTime testTime = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);
        return Stream.of(
                Arguments.of(testTime, testTime, testTime),
                Arguments.of(testTime.minusMinutes(5), testTime, testTime.minusMinutes(5)),
                Arguments.of(testTime, testTime.minusMinutes(5), testTime.minusMinutes(5))
        );
    }
}