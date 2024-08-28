/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RangeUtils {

    /**
     * Merges two ranges together, to get the widest range.
     *
     * @param left  the first range
     * @param right the second range
     * @return a {@link Range} between the lowest and the highest time from the two range
     */
    public static Range<LocalDateTime> mergeRanges(final Range<LocalDateTime> left, final Range<LocalDateTime> right) {
        return makeRange(min(left.getMinimum(), right.getMinimum()), max(left.getMaximum(), right.getMaximum()));
    }

    /**
     * Makes a range between the two input.
     *
     * @param left  the first {@link LocalDateTime}
     * @param right the second {@link LocalDateTime}
     * @return a {@link Range} of {@link LocalDateTime} between the two time given
     */
    public static Range<LocalDateTime> makeRange(final LocalDateTime left, final LocalDateTime right) {
        return Range.between(left, right, LocalDateTime::compareTo);
    }

    /**
     * Makes a range between the two input.
     *
     * @param left  the first {@link Timestamp}
     * @param right the second {@link Timestamp}
     * @return a {@link Range} of {@link LocalDateTime} between the two time given
     */
    public static Range<LocalDateTime> makeRange(final Timestamp left, final Timestamp right) {
        return Range.between(left.toLocalDateTime(), right.toLocalDateTime(), LocalDateTime::compareTo);
    }

    private static LocalDateTime min(final LocalDateTime left, final LocalDateTime right) {
        return left.isBefore(right) ? left : right;
    }

    private static LocalDateTime max(final LocalDateTime left, final LocalDateTime right) {
        return left.isBefore(right) ? right : left;
    }
}
