/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period;

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.AggregationPeriodCreatorRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.model.AggregationPeriod;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class AggregationPeriodSupporter {
    @Inject
    private AggregationPeriodCreatorRegistryFacade aggregationPeriodCreatorRegistry;

    public boolean areBoundsInDifferentAggregationPeriods(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound, final long period) {
        final AggregationPeriodCreator aggregationPeriodCreator = aggregationPeriodCreatorRegistry.aggregationPeriod(period);
        final AggregationPeriod aggregationPeriod = aggregationPeriodCreator.create(lowerReadinessBound);

        /* Aggregation period of lower readiness bound has to be before the upper readiness bound time */
        return aggregationPeriod.isBefore(upperReadinessBound);
    }

}
