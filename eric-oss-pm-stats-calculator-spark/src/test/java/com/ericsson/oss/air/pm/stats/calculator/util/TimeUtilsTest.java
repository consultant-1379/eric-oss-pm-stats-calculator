/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimeUtils}.
 */
class TimeUtilsTest {

    @Test
    void whenIntervalBetweenStartTimestampAndEndTimestampIsMoreThanHourlyTimeSlotLength_thenMoreThanOneTimeSlotsCreated() {
        final Timestamp startTimestamp = Timestamp.valueOf("2019-05-09 04:45:00.00");
        final Timestamp endTimestamp = Timestamp.valueOf("2019-05-09 11:45:00");
        final List<KpiCalculatorTimeSlot> timeSlots = TimeUtils.getKpiCalculationTimeSlots(startTimestamp, endTimestamp, 60);

        assertEquals(8, timeSlots.size());
    }

    @Test
    void whenIntervalBetweenStartTimestampAndEndTimestampIsMoreThanDailyTimeSlotLength_thenMoreThanOneTimeSlotsCreated() {
        final Timestamp startTimestamp = Timestamp.valueOf("2019-05-09 04:45:00.00");
        final Timestamp endTimestamp = Timestamp.valueOf("2019-05-10 11:45:00");
        final List<KpiCalculatorTimeSlot> timeSlots = TimeUtils.getKpiCalculationTimeSlots(startTimestamp, endTimestamp, 1440);

        assertEquals(2, timeSlots.size());
    }

    @Test
    void whenIntervalBetweenStartTimestampAndEndTimestampIsMoreThanHourlyTimeSlotLength_thenTimeSlotsCreatedOnHourBasis() {
        final Timestamp startTimestamp = Timestamp.valueOf("2019-05-09 11:45:00.01");
        final Timestamp endTimestamp = Timestamp.valueOf("2019-05-09 13:15:00");
        final List<KpiCalculatorTimeSlot> timeSlots = TimeUtils.getKpiCalculationTimeSlots(startTimestamp, endTimestamp, 60);

        assertEquals(3, timeSlots.size());

        assertEquals(Timestamp.valueOf("2019-05-09 11:45:00.01"), timeSlots.get(0).getStartTimestamp());
        assertEquals(Timestamp.valueOf("2019-05-09 11:59:59.99"), timeSlots.get(0).getEndTimestamp());

        assertEquals(Timestamp.valueOf("2019-05-09 12:00:00"), timeSlots.get(1).getStartTimestamp());
        assertEquals(Timestamp.valueOf("2019-05-09 12:59:59.99"), timeSlots.get(1).getEndTimestamp());

        assertEquals(Timestamp.valueOf("2019-05-09 13:00:00"), timeSlots.get(2).getStartTimestamp());
        assertEquals(Timestamp.valueOf("2019-05-09 13:15:00.0"), timeSlots.get(2).getEndTimestamp());
    }

    @Test
    void whenIntervalBetweenStartTimestampAndEndTimestampIsMoreThanDailyTimeSlotLength_thenTimeSlotsCreatedOnDailyBasis() {
        final Timestamp startTimestamp = Timestamp.valueOf("2019-05-09 11:45:00.01");
        final Timestamp endTimestamp = Timestamp.valueOf("2019-05-10 13:15:00");
        final List<KpiCalculatorTimeSlot> timeSlots = TimeUtils.getKpiCalculationTimeSlots(startTimestamp, endTimestamp, 1440);

        Assertions.assertThat(timeSlots).hasSize(2).satisfiesExactly(
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 11:45:00.01"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-09 23:59:59.99"));
                },
                kpiCalculatorTimeSlot  -> {
                    Assertions.assertThat(kpiCalculatorTimeSlot.getStartTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-10 00:00:00"));
                    Assertions.assertThat(kpiCalculatorTimeSlot.getEndTimestamp()).isEqualTo(Timestamp.valueOf("2019-05-10 13:15:00.0"));
                }
        );
    }

    @Test
    void whenIntervalBetweenStartTimestampAndEndTimestampIsMoreThanTimeSlotLengthButLessThanTwoTimeSlotsLength_thenTwoTimeSlotsCreated() {
        final Timestamp startTimestamp = Timestamp.valueOf("2019-06-07 12:00:01.01");
        final Timestamp endTimestamp = Timestamp.valueOf("2019-06-07 13:15:00.0");
        final List<KpiCalculatorTimeSlot> timeSlots = TimeUtils.getKpiCalculationTimeSlots(startTimestamp, endTimestamp, 60);

        assertEquals(2, timeSlots.size());

        Timestamp startTimestampForTimeSlot = timeSlots.get(0).getStartTimestamp();
        Timestamp endTimestampForTimeSlot = timeSlots.get(0).getEndTimestamp();
        assertEquals(startTimestampForTimeSlot, Timestamp.valueOf("2019-06-07 12:00:01.01"));
        assertEquals(endTimestampForTimeSlot, Timestamp.valueOf("2019-06-07 12:59:59.99"));

        startTimestampForTimeSlot = timeSlots.get(1).getStartTimestamp();
        endTimestampForTimeSlot = timeSlots.get(1).getEndTimestamp();
        assertEquals(startTimestampForTimeSlot, Timestamp.valueOf("2019-06-07 13:00:00"));
        assertEquals(endTimestampForTimeSlot, Timestamp.valueOf("2019-06-07 13:15:00.0"));
    }

    @Test
    void whenIntervalBetweenStartTimestampAndEndTimestampIsLessThanTimeSlotLength_thenOneTimeSlotCreated() {
        final Timestamp startTimestamp = Timestamp.valueOf("2019-05-09 11:30:00.00");
        final Timestamp endTimestamp = Timestamp.valueOf("2019-05-09 11:45:00");
        final List<KpiCalculatorTimeSlot> timeSlots = TimeUtils.getKpiCalculationTimeSlots(startTimestamp, endTimestamp, 60);

        assertEquals(1, timeSlots.size());

        final Timestamp startTimestampForTimeSlot = timeSlots.get(0).getStartTimestamp();
        final Timestamp endTimestampForTimeSlot = timeSlots.get(0).getEndTimestamp();
        assertEquals(startTimestampForTimeSlot, Timestamp.valueOf("2019-05-09 11:30:00.00"));
        assertEquals(endTimestampForTimeSlot, Timestamp.valueOf("2019-05-09 11:45:00.0"));
    }

}
