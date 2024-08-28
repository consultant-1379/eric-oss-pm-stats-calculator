/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.exception.AggregationPeriodNotSupportedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AggregationPeriodCreatorRegistry implements AggregationPeriodCreatorRegistryFacade {
    @Inject
    @Any
    private Instance<AggregationPeriodCreator> aggregationPeriodCreators;

    @Override
    public AggregationPeriodCreator aggregationPeriod(final long aggregationPeriod) {
        return aggregationPeriodCreators
                .stream()
                .filter(aggregationPeriodCreator -> aggregationPeriodCreator.doesSupport(aggregationPeriod))
                .findFirst()
                .orElseThrow(() -> new AggregationPeriodNotSupportedException(
                        String.format("Aggregation period '%s' is not supported", aggregationPeriod)
                ));
    }
}
