/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.util;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.FIFTEEN_MINUTES_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.HOURLY_AGGREGATION_PERIOD_IN_MINUTES;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDateTimeTruncates {

    public static LocalDateTime truncateToAggregationPeriod(@NonNull final LocalDateTime from, final int aggregationPeriod) {
        switch (aggregationPeriod) {
            case FIFTEEN_MINUTES_AGGREGATION_PERIOD:
                return truncateToFifteenMinutes(from);
            case HOURLY_AGGREGATION_PERIOD_IN_MINUTES:
                return from.truncatedTo(ChronoUnit.HOURS);
            case DAILY_AGGREGATION_PERIOD_IN_MINUTES:
                return from.truncatedTo(ChronoUnit.DAYS);
            default:
                throw new IllegalArgumentException(String.format("Aggregation period '%d' is not supported", aggregationPeriod));
        }
    }

    public static LocalDateTime truncateToFifteenMinutes(@NonNull final LocalDateTime from) {
        final int period = FIFTEEN_MINUTES_AGGREGATION_PERIOD;
        final int floor = period * (from.getMinute() / period);

        return from.truncatedTo(ChronoUnit.HOURS).plusMinutes(floor);
    }
}
