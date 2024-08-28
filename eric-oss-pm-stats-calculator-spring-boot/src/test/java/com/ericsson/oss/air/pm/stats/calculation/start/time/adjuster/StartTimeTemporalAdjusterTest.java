/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time.adjuster;

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

class StartTimeTemporalAdjusterTest {
    static final LocalDate TEST_DATE = LocalDate.of(2_022, Month.SEPTEMBER, 24);

    StartTimeTemporalAdjuster objectUnderTest = new StartTimeTemporalAdjuster();

    @MethodSource("provideCalculateData")
    @ParameterizedTest(name = "[{index}] Earliest collected data: ''{0}'' aggregation period: ''{1}'' start time ==> ''{2}''")
    void shouldCalculateReliabilityOffsetThreshold(final LocalDateTime earliestCollectedData,
                                                   final int aggregationPeriod,
                                                   final LocalDateTime expected) {
        final ReadinessLog readinessLog = ReadinessLog.builder().withEarliestCollectedData(earliestCollectedData).build();
        final KpiDefinitionEntity kpiDefinition = KpiDefinitionEntity.builder().withAggregationPeriod(aggregationPeriod).build();

        final LocalDateTime actual = objectUnderTest.calculate(readinessLog, kpiDefinition);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @NonNull
    static Stream<Arguments> provideCalculateData() {
        return Stream.of(
                Arguments.of(LocalDateTime.of(TEST_DATE, LocalTime.of(9, 13)), -1, LocalDateTime.of(TEST_DATE, LocalTime.of(9, 13))),
                Arguments.of(LocalDateTime.of(TEST_DATE, LocalTime.of(9, 13)), 15, LocalDateTime.of(TEST_DATE, LocalTime.of(9, 0))),
                Arguments.of(LocalDateTime.of(TEST_DATE, LocalTime.of(9, 13)), 60, LocalDateTime.of(TEST_DATE, LocalTime.of(9, 0))),
                Arguments.of(LocalDateTime.of(TEST_DATE, LocalTime.of(2, 20)), 1440, LocalDateTime.of(TEST_DATE, LocalTime.MIDNIGHT))
        );
    }
}
