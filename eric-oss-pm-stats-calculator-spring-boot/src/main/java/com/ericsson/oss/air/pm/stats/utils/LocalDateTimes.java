/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDateTimes {
    public static LocalDateTime min(final LocalDateTime left, final LocalDateTime right) {
        return Comparators.min(left, right, LocalDateTime::compareTo);
    }

    public static LocalDateTime max(final LocalDateTime left, final LocalDateTime right) {
        return left.isAfter(right) ? left : right;
    }

    public static boolean isAfterOrEqual(final LocalDateTime left, final LocalDateTime right) {
        return !left.isBefore(right);
    }
}
