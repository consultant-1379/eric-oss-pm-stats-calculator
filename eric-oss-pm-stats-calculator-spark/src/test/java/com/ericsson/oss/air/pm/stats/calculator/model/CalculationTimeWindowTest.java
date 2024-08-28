/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model;

import java.sql.Timestamp;
import java.util.Queue;

import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CalculationTimeWindowTest {
    @Test
    void shouldCalculateTimeSlots_AndReturnEmpty_whenStartIsAfterEnd() {
        final Timestamp start = Timestamp.valueOf("2019-05-09 13:15:00");
        final Timestamp end = Timestamp.valueOf("2019-05-09 11:45:00.01");

        final Queue<KpiCalculatorTimeSlot> actual = CalculationTimeWindow.of(start, end).calculateTimeSlots(60);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldCalculateTimeSlotsWithHourlyPeriod() {
        final Timestamp start = Timestamp.valueOf("2019-05-09 11:45:00.01");
        final Timestamp end = Timestamp.valueOf("2019-05-09 13:15:00");

        final Queue<KpiCalculatorTimeSlot> actual = CalculationTimeWindow.of(start, end).calculateTimeSlots(60);

        Assertions.assertThat(actual).satisfiesExactly(
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 11:45:00.01"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 11:59:59.99"));
                },
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 12:00:00"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 12:59:59.99"));
                },
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 13:00:00"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 13:15:00.0"));
                }
        );
    }

    @Test
    void shouldCalculateTimeSlotsWithDailyPeriod() {
        final Timestamp start = Timestamp.valueOf("2019-05-09 11:45:00.01");
        final Timestamp end = Timestamp.valueOf("2019-05-10 13:15:00");

        final Queue<KpiCalculatorTimeSlot> actual = CalculationTimeWindow.of(start, end).calculateTimeSlots(1440);

        Assertions.assertThat(actual).satisfiesExactly(
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 11:45:00.01"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 23:59:59.99"));
                },
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-10 00:00:00.00"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-10 13:15:00.0"));
                }
        );
    }
}