/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api;

import java.time.Duration;
import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.AggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.ComplexAggregationPeriod;
import com.ericsson.oss.air.pm.stats.registry.Registry;

import lombok.NonNull;
import org.springframework.plugin.core.Plugin;

public interface AggregationPeriodCreator extends Registry<Long>, Plugin<Long> {
    Duration getSupportedAggregationPeriod();

    AggregationPeriod create(LocalDateTime lowerReadinessBound);

    ComplexAggregationPeriod createComplexAggregation(LocalDateTime lowerReadinessBound, LocalDateTime upperReadinessBound);

    @Override
    default boolean doesSupport(@NonNull final Long aggregationPeriodInMinutes) {
        return aggregationPeriodInMinutes.equals(getSupportedAggregationPeriod().toMinutes());
    }

    @Override
    default boolean supports(@NonNull final Long aggregationPeriodInMinutes) {
        return aggregationPeriodInMinutes.equals(getSupportedAggregationPeriod().toMinutes());
    }
}
