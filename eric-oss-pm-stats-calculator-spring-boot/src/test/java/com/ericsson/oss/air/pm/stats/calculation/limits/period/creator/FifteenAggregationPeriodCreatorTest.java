/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.creator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.AggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.ComplexAggregationPeriod;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FifteenAggregationPeriodCreatorTest {
    AggregationPeriodCreator objectUnderTest = new FifteenAggregationPeriodCreator();

    @Test
    void shouldCreateComplexAggregation() {
        final LocalDateTime lowerReadinessBound = LocalDateTime.of(2_023, Month.FEBRUARY, 26, 12, 30);
        final LocalDateTime upperReadinessBound = LocalDateTime.of(2_023, Month.FEBRUARY, 26, 12, 45);

        final ComplexAggregationPeriod actual = objectUnderTest.createComplexAggregation(lowerReadinessBound, upperReadinessBound);

        Assertions.assertThat(actual).isEqualTo(ComplexAggregationPeriod.of(
                lowerReadinessBound,
                upperReadinessBound
        ));
    }

    @Nested
    class SupportedAggregationPeriod {
        @Test
        void shouldVerifySupportedAggregationPeriod() {
            final Duration actual = objectUnderTest.getSupportedAggregationPeriod();

            Assertions.assertThat(actual).isEqualTo(Duration.ofMinutes(15));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class CreateAggregationPeriod {
        @MethodSource("provideCreateAggregationPeriodData")
        @ParameterizedTest(name = "[{index}] Lower readiness bound ''{0}'' aggregation period created [''{1}'' - ''{2}'')")
        void shouldCreateAggregationPeriod(final LocalDateTime lowerReadinessBound, final LocalDateTime periodLeft, final LocalDateTime periodRight) {
            final AggregationPeriod actual = objectUnderTest.create(lowerReadinessBound);

            Assertions.assertThat(actual.getLeftInclusive()).as("getLeftInclusive").isEqualTo(periodLeft);
            Assertions.assertThat(actual.getRightExclusive()).as("getRightExclusive").isEqualTo(periodRight);
        }

        Stream<Arguments> provideCreateAggregationPeriodData() {
            return Stream.of(
                    Arguments.of(lowerReadinessBound(12, 0), periodLeft(12, 0), periodRight(12, 15)),
                    Arguments.of(lowerReadinessBound(12, 1), periodLeft(12, 0), periodRight(12, 15)),
                    Arguments.of(lowerReadinessBound(12, 14), periodLeft(12, 0), periodRight(12, 15)),

                    Arguments.of(lowerReadinessBound(12, 15), periodLeft(12, 15), periodRight(12, 30)),
                    Arguments.of(lowerReadinessBound(12, 16), periodLeft(12, 15), periodRight(12, 30)),
                    Arguments.of(lowerReadinessBound(12, 29), periodLeft(12, 15), periodRight(12, 30)),

                    Arguments.of(lowerReadinessBound(12, 30), periodLeft(12, 30), periodRight(12, 45)),
                    Arguments.of(lowerReadinessBound(12, 31), periodLeft(12, 30), periodRight(12, 45)),
                    Arguments.of(lowerReadinessBound(12, 44), periodLeft(12, 30), periodRight(12, 45)),

                    Arguments.of(lowerReadinessBound(12, 45), periodLeft(12, 45), periodRight(13, 0)),
                    Arguments.of(lowerReadinessBound(12, 46), periodLeft(12, 45), periodRight(13, 0)),
                    Arguments.of(lowerReadinessBound(12, 59), periodLeft(12, 45), periodRight(13, 0)),

                    Arguments.of(lowerReadinessBound(13, 0), periodLeft(13, 0), periodRight(13, 15)),
                    Arguments.of(lowerReadinessBound(13, 1), periodLeft(13, 0), periodRight(13, 15)),
                    Arguments.of(lowerReadinessBound(13, 14), periodLeft(13, 0), periodRight(13, 15)),

                    Arguments.of(lowerReadinessBound(13, 15), periodLeft(13, 15), periodRight(13, 30)),
                    Arguments.of(lowerReadinessBound(13, 16), periodLeft(13, 15), periodRight(13, 30)),
                    Arguments.of(lowerReadinessBound(13, 29), periodLeft(13, 15), periodRight(13, 30))
            );
        }
    }

    @NonNull
    static LocalDateTime lowerReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    @NonNull
    static LocalDateTime periodLeft(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    @NonNull
    static LocalDateTime periodRight(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 10, hour, minute);
    }
}