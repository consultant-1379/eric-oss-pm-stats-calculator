/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

class DailyAggregationPeriodCreatorTest {
    AggregationPeriodCreator objectUnderTest = new DailyAggregationPeriodCreator();

    @Nested
    class SupportedAggregationPeriod {
        @Test
        void shouldVerifySupportedAggregationPeriod() {
            final Duration actual = objectUnderTest.getSupportedAggregationPeriod();

            Assertions.assertThat(actual).isEqualTo(Duration.ofDays(1));
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

        @MethodSource("provideCreateComplexAggregationPeriodData")
        @ParameterizedTest(name = "[{index}] Lower readiness bound ''{0}'' complex aggregation period created [''{2}'' - ''{3}'')")
        void shouldCreateComplexAggregationPeriod(final LocalDateTime lowerReadinessBound,
                                                  final LocalDateTime upperReadinessBound,
                                                  final LocalDateTime calculationStart,
                                                  final LocalDateTime reliabilityThreshold) {
            final ComplexAggregationPeriod actual = objectUnderTest.createComplexAggregation(lowerReadinessBound, upperReadinessBound);

            Assertions.assertThat(actual.getCalculationStartTime()).as("getCalculationStartTime").isEqualTo(calculationStart);
            Assertions.assertThat(actual.getReliabilityThreshold()).as("getReliabilityThreshold").isEqualTo(reliabilityThreshold);
        }

        Stream<Arguments> provideCreateAggregationPeriodData() {
            return Stream.of(
                    Arguments.of(lowerReadinessBound(8, 23, 59), periodLeft(8), periodRight(9)),
                    Arguments.of(lowerReadinessBound(9, 0, 0), periodLeft(9), periodRight(10)),
                    Arguments.of(lowerReadinessBound(9, 0, 1), periodLeft(9), periodRight(10)),
                    Arguments.of(lowerReadinessBound(9, 23, 59), periodLeft(9), periodRight(10)),
                    Arguments.of(lowerReadinessBound(10, 0, 0), periodLeft(10), periodRight(11))
            );
        }

        Stream<Arguments> provideCreateComplexAggregationPeriodData() {
            return Stream.of(
                    Arguments.of(lowerReadinessBound(8, 23, 59), upperReadinessBound(11, 23, 59),
                            calculationStart(8), reliabilityThreshold(11)),
                    Arguments.of(lowerReadinessBound(8, 20, 59), upperReadinessBound(8, 23, 59),
                            calculationStart(8), reliabilityThreshold(8))
            );
        }

    }

    @NonNull
    static LocalDateTime lowerReadinessBound(final int day, final int hour, final int minute) {
        return testTime(day, hour, minute);
    }

    @NonNull
    static LocalDateTime upperReadinessBound(final int day, final int hour, final int minute) {
        return testTime(day, hour, minute);
    }

    @NonNull
    static LocalDateTime periodLeft(final int day) {
        return testTime(day, 0, 0);
    }

    @NonNull
    static LocalDateTime periodRight(final int day) {
        return testTime(day, 0, 0);
    }

    @NonNull
    static LocalDateTime calculationStart(final int day) {
        return testTime(day, 0, 0);
    }

    @NonNull
    static LocalDateTime reliabilityThreshold(final int day) {
        return testTime(day, 0, 0);
    }

    static LocalDateTime testTime(final int day, final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, day, hour, minute);
    }
}