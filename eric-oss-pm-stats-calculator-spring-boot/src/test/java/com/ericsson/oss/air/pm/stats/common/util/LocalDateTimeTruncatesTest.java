/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.util;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalDateTimeTruncatesTest {

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class TruncateToAggregationPeriod {
        @Test
        void shouldFail_whenAggregationPeriodIsNotKnown() {
            final int aggregationPeriod = 75;
            Assertions.assertThatThrownBy(() -> LocalDateTimeTruncates.truncateToAggregationPeriod(testTime(12, 0), aggregationPeriod))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Aggregation period '%d' is not supported", aggregationPeriod);
        }

        @MethodSource("provideTruncateToAggregationPeriodData")
        @ParameterizedTest(name = "[{index}] LocalDateTime ''{0}'' with aggregation period ''{1}'' is truncated to ''{2}''")
        void shouldTruncateToAggregationPeriod(final LocalDateTime from, final int aggregationPeriod, final LocalDateTime expected) {
            final LocalDateTime actual = LocalDateTimeTruncates.truncateToAggregationPeriod(from, aggregationPeriod);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideTruncateToAggregationPeriodData() {
            return Stream.of(
                    Arguments.of(testTime(12, 13), 15, testTime(12, 0)),
                    Arguments.of(testTime(12, 13), 60, testTime(12, 0)),
                    Arguments.of(testTime(12, 13), 1_440, testTime(0, 0))
            );
        }
    }

    @MethodSource("provideTruncateToFifteenMinutesData")
    @ParameterizedTest(name = "[{index}] LocalDateTime ''{0}'' truncated to ''{1}''")
    void shouldTruncateToFifteenMinutes(final LocalDateTime from, final LocalDateTime expected) {
        final LocalDateTime actual = LocalDateTimeTruncates.truncateToFifteenMinutes(from);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideTruncateToFifteenMinutesData() {
        return Stream.of(
                Arguments.of(testTime(12, 0), testTime(12, 0)),
                Arguments.of(testTime(12, 1), testTime(12, 0)),
                Arguments.of(testTime(12, 14), testTime(12, 0)),

                Arguments.of(testTime(12, 15), testTime(12, 15)),
                Arguments.of(testTime(12, 16), testTime(12, 15)),
                Arguments.of(testTime(12, 29), testTime(12, 15)),

                Arguments.of(testTime(12, 30), testTime(12, 30)),
                Arguments.of(testTime(12, 31), testTime(12, 30)),
                Arguments.of(testTime(12, 44), testTime(12, 30)),

                Arguments.of(testTime(12, 45), testTime(12, 45)),
                Arguments.of(testTime(12, 46), testTime(12, 45)),
                Arguments.of(testTime(12, 59), testTime(12, 45)),

                Arguments.of(testTime(13, 0), testTime(13, 0)),
                Arguments.of(testTime(13, 1), testTime(13, 0)),
                Arguments.of(testTime(13, 14), testTime(13, 0)),

                Arguments.of(testTime(13, 15), testTime(13, 15)),
                Arguments.of(testTime(13, 16), testTime(13, 15)),
                Arguments.of(testTime(13, 29), testTime(13, 15))
        );
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_023, Month.FEBRUARY, 26, hour, minute);
    }

}