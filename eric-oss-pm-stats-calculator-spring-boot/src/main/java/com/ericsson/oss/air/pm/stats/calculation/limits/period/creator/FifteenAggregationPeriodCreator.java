/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.creator;

import static com.ericsson.oss.air.pm.stats.common.util.LocalDateTimeTruncates.truncateToFifteenMinutes;

import java.time.Duration;
import java.time.LocalDateTime;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.AggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.ComplexAggregationPeriod;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class FifteenAggregationPeriodCreator implements AggregationPeriodCreator {
    private static final int SUPPORTED_AGGREGATION_PERIOD_IN_MINUTES = 15;

    @Override
    public Duration getSupportedAggregationPeriod() {
        return Duration.ofMinutes(SUPPORTED_AGGREGATION_PERIOD_IN_MINUTES);
    }

    @Override
    public ComplexAggregationPeriod createComplexAggregation(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound) {
        return ComplexAggregationPeriod.of(truncateToFifteenMinutes(lowerReadinessBound), truncateToFifteenMinutes(upperReadinessBound));
    }

    @Override
    public AggregationPeriod create(@NonNull final LocalDateTime lowerReadinessBound) {
        final LocalDateTime periodStart = truncateToFifteenMinutes(lowerReadinessBound);
        final LocalDateTime periodEnd = periodStart.plus(getSupportedAggregationPeriod());

        return AggregationPeriod.of(periodStart, periodEnd);
    }
}
