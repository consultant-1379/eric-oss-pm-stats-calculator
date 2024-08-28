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

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Time util class to calculate time slots. In one calculation process, kpi calculation will be done for 3 time slots. MAX_NUMBER_OF_TIME_SLOTS may be
 * defined as environment variable later.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {


    /**
     * Divide the time window between start timestamp and end time stamp to time slots.
     *
     * @param startTimestamp
     *            the start timestamp
     * @param endTimestamp
     *            the end timestamp
     * @return KPI calculation time slot list
     */
    public static List<KpiCalculatorTimeSlot> getKpiCalculationTimeSlots(final Timestamp startTimestamp, final Timestamp endTimestamp, final int aggregationPeriod) {
        final List<KpiCalculatorTimeSlot> timeSlots = new ArrayList<>();
        long startTime = startTimestamp.getTime();
        final long endTimeInMillis = endTimestamp.getTime();
        while ((new Timestamp(startTime).before(endTimestamp) || new Timestamp(startTime).equals(endTimestamp))) {
            final long endTime = Math.min(getEndTimestamp(new Timestamp(startTime), aggregationPeriod).getTime() - 10, endTimeInMillis);
            timeSlots.add(new KpiCalculatorTimeSlot(new Timestamp(startTime), new Timestamp(endTime)));
            startTime = endTime + 10;
        }
        return timeSlots;
    }

    private static Timestamp getEndTimestamp(final Timestamp startTimestamp, final int aggregationPeriod) {
        final ChronoUnit unit = aggregationPeriod == 1440 ? ChronoUnit.DAYS : ChronoUnit.HOURS;

        return Timestamp.valueOf(startTimestamp.toLocalDateTime()
                                               .truncatedTo(unit)
                                               .plus(1, unit));
    }
}
