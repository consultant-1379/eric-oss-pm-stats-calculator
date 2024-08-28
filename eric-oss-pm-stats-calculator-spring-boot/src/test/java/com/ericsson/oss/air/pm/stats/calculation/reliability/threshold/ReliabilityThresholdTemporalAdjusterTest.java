/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.reliability.threshold;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReliabilityThresholdTemporalAdjusterTest {
    static final LocalDate TEST_DATE = LocalDate.of(2_022, Month.SEPTEMBER, 10);

    ReliabilityThresholdTemporalAdjuster objectUnderTest = new ReliabilityThresholdTemporalAdjuster();

    @MethodSource("provideCalculateData")
    @ParameterizedTest(name = "[{index}] Latest collected data: ''{0}'' reliability offset: ''{1}'' aggregation period: ''{2}'' reliability threshold ==> ''{3}''")
    void shouldCalculateReliabilityOffsetThreshold(final LocalDateTime latestCollectedData,
                                                   final int reliabilityOffset,
                                                   final int aggregationPeriod,
                                                   final LocalDateTime expected) {
        final ReadinessLog readinessLog = ReadinessLog.builder().withLatestCollectedData(latestCollectedData).build();
        final KpiDefinitionEntity kpiDefinition = KpiDefinitionEntity.builder().withDataReliabilityOffset(reliabilityOffset).withAggregationPeriod(aggregationPeriod).build();

        final LocalDateTime actual = objectUnderTest.calculate(readinessLog, kpiDefinition);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @NonNull
    static Stream<Arguments> provideCalculateData() {
        return Stream.of(
                Arguments.of(hourly(9, 13), 15, -1, hourly(8, 58)),
                Arguments.of(hourly(9, 13), 15, 15, hourly(8, 45)),
                Arguments.of(hourly(9, 13), 15, 60, hourly(8, 0)),
                Arguments.of(hourly(9, 15), 15, 60, hourly(9, 0)),
                Arguments.of(hourly(9, 17), 15, 60, hourly(9, 0)),
                Arguments.of(hourly(8, 55), -10, 60, hourly(9, 0)),
                Arguments.of(hourly(0, 20), -10, 1440, daily(TEST_DATE)),
                Arguments.of(hourly(0, 5), 10, 1440, daily(TEST_DATE.minusDays(1))),
                Arguments.of(hourly(23, 55), -10, 1440, daily(TEST_DATE.plusDays(1)))
        );
    }

    @NonNull
    static LocalDateTime daily(final LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }

    @NonNull
    static LocalDateTime hourly(final int hour, final int minute) {
        return LocalDateTime.of(TEST_DATE, LocalTime.of(hour, minute));
    }
}