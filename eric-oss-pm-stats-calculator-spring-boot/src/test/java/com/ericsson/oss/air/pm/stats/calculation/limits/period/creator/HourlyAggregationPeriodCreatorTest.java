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

class HourlyAggregationPeriodCreatorTest {
    AggregationPeriodCreator objectUnderTest = new HourlyAggregationPeriodCreator();

    @Nested
    class SupportedAggregationPeriod {
        @Test
        void shouldVerifySupportedAggregationPeriod() {
            final Duration actual = objectUnderTest.getSupportedAggregationPeriod();

            Assertions.assertThat(actual).isEqualTo(Duration.ofHours(1));
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

        @MethodSource("provideComplexCreateAggregationPeriodData")
        @ParameterizedTest(name = "[{index}] Lower readiness bound ''{0}'' complex aggregation period created [''{2}'' - ''{3}'')")
        void shouldCreateComplexAggregationPeriod(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound,
                                                  final LocalDateTime calculationStart, final LocalDateTime reliabilityThreshold) {
            final ComplexAggregationPeriod actual = objectUnderTest.createComplexAggregation(lowerReadinessBound, upperReadinessBound);

            Assertions.assertThat(actual.getCalculationStartTime()).as("getReliabilityThreshold").isEqualTo(calculationStart);
            Assertions.assertThat(actual.getReliabilityThreshold()).as("getReliabilityThreshold").isEqualTo(reliabilityThreshold);
        }

        Stream<Arguments> provideCreateAggregationPeriodData() {
            return Stream.of(
                    Arguments.of(lowerReadinessBound(11, 59), periodLeft(11), periodRight(12)),
                    Arguments.of(lowerReadinessBound(12, 0), periodLeft(12), periodRight(13)),
                    Arguments.of(lowerReadinessBound(12, 1), periodLeft(12), periodRight(13)),
                    Arguments.of(lowerReadinessBound(12, 59), periodLeft(12), periodRight(13)),
                    Arguments.of(lowerReadinessBound(13, 0), periodLeft(13), periodRight(14)),
                    Arguments.of(lowerReadinessBound(13, 1), periodLeft(13), periodRight(14))
            );
        }

        Stream<Arguments> provideComplexCreateAggregationPeriodData() {
            return Stream.of(
                    Arguments.of(lowerReadinessBound(11, 59), upperReadinessBound(12, 59),
                            calculationStart(11), reliabilityThreshold(12)),
                    Arguments.of(lowerReadinessBound(11, 59), upperReadinessBound(14, 59),
                            calculationStart(11), reliabilityThreshold(14)),
                    Arguments.of(lowerReadinessBound(11, 50), upperReadinessBound(11, 55),
                            calculationStart(11), reliabilityThreshold(11))
            );
        }
    }

    @NonNull
    static LocalDateTime lowerReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    @NonNull
    static LocalDateTime upperReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    @NonNull
    static LocalDateTime periodLeft(final int hour) {
        return testTime(hour, 0);
    }

    @NonNull
    static LocalDateTime periodRight(final int hour) {
        return testTime(hour, 0);
    }

    @NonNull
    static LocalDateTime calculationStart(final int hour) {
        return testTime(hour, 0);
    }

    @NonNull
    static LocalDateTime reliabilityThreshold(final int hour) {
        return testTime(hour, 0);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 10, hour, minute);
    }
}