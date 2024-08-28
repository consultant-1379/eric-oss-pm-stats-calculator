/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.model;

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

class AggregationPeriodTest {
    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class Preconditions {
        @MethodSource("provideFailWhenLeftIsNotBeforeRightData")
        @ParameterizedTest(name = "[{index}] leftInclusive ''{0}'' is not before  rightExclusive ''{1}''")
        void shouldFail_whenLeftIsNotBeforeRight(final LocalDateTime leftInclusive, final LocalDateTime rightExclusive) {
            Assertions.assertThatThrownBy(() -> AggregationPeriod.of(leftInclusive, rightExclusive))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("left must be before right");
        }

        Stream<Arguments> provideFailWhenLeftIsNotBeforeRightData() {
            return Stream.of(
                    Arguments.of(leftInclusive(12, 0), rightExclusive(11, 59)),
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 0))
            );
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IsBefore {
        @MethodSource("provideIsBeforeData")
        @ParameterizedTest(name = "[{index}] Aggregation period [''{0}'' - ''{1}'') is before ''{2}'' ==> ''{3}''")
        void shouldVerifyIsBefore(final LocalDateTime leftInclusive, final LocalDateTime rightExclusive, final LocalDateTime target, final boolean expected) {
            final AggregationPeriod aggregationPeriod = AggregationPeriod.of(leftInclusive, rightExclusive);
            final boolean actual = aggregationPeriod.isBefore(target);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideIsBeforeData() {
            return Stream.of(
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 5), testTime(11, 59), false),
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 5), testTime(12, 0), false),
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 5), testTime(12, 1), false),
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 5), testTime(12, 4), false),
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 5), testTime(12, 5), true),
                    Arguments.of(leftInclusive(12, 0), rightExclusive(12, 5), testTime(12, 6), true)
            );
        }
    }

    static LocalDateTime leftInclusive(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime rightExclusive(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 12, hour, minute);
    }
}