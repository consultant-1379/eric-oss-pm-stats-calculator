/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.registry;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.AggregationPeriodCreatorRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.exception.AggregationPeriodNotSupportedException;

import lombok.RequiredArgsConstructor;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AggregationPeriodCreatorRegistrySpring implements AggregationPeriodCreatorRegistryFacade {

    private final PluginRegistry<AggregationPeriodCreator, Long> aggregationPeriodCreatorPluginRegistry;

    @Override
    public AggregationPeriodCreator aggregationPeriod(final long aggregationPeriod) {
        return aggregationPeriodCreatorPluginRegistry.getPluginFor(aggregationPeriod, () -> {
            final String message = String.format("Aggregation period '%s' is not supported", aggregationPeriod);
            return new AggregationPeriodNotSupportedException(message);
        });
    }
}