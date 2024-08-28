/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {
    public static final LocalDateTime EPOCH_DAY = LocalDateTime.of(LocalDate.ofEpochDay(0), LocalTime.MIDNIGHT);

    public static final LocalDateTime TODAY = daysAgo(0);
    public static final LocalDateTime A_DAY_AGO = daysAgo(1);
    public static final LocalDateTime TWO_DAYS_AGO = daysAgo(2);

    public static LocalDateTime daysAgo(final int days) {
        return LocalDateTime.of(LocalDate.now().minusDays(days), LocalTime.MIDNIGHT);
    }

    public static LocalDateTime daysAgo(final int days, final int hour) {
        return daysAgo(days, hour, 0);
    }

    public static LocalDateTime daysAgo(final int days, final int hour, final int minutes) {
        return LocalDateTime.of(LocalDate.now().minusDays(days), LocalTime.of(hour, minutes));
    }

}
