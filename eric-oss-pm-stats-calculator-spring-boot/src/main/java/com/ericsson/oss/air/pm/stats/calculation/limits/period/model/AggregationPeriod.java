/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.model;

import java.time.LocalDateTime;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;

/**
 * An aggregation period is represented with left inclusive and right exclusive bounds.
 *
 * <pre>{@code
 *              10:00     10:59       12:00
 * ====[===AP1===)[===AP2===)[===AP3===)[====>
 *   09:00     09:59       11:00     11:59
 * }</pre>
 * <p>
 * The <strong>AP1</strong> is inclusively between 09:00 - 09:59. <br>
 * The <strong>AP2</strong> is inclusively between 10:00 - 10:59. <br>
 * The <strong>AP3</strong> is inclusively between 11:00 - 11:59.
 */
@Data
public class AggregationPeriod {
    private final LocalDateTime leftInclusive;
    private final LocalDateTime rightExclusive;

    private AggregationPeriod(final @NonNull LocalDateTime leftInclusive, final @NonNull LocalDateTime rightExclusive) {
        Preconditions.checkArgument(leftInclusive.isBefore(rightExclusive), "left must be before right");

        this.leftInclusive = leftInclusive;
        this.rightExclusive = rightExclusive;
    }

    public static AggregationPeriod of(final LocalDateTime leftInclusive, final LocalDateTime rightExclusive) {
        return new AggregationPeriod(leftInclusive, rightExclusive);
    }

    public boolean isBefore(final LocalDateTime other) {
        return rightExclusive.isBefore(other) || rightExclusive.equals(other);
    }

}
