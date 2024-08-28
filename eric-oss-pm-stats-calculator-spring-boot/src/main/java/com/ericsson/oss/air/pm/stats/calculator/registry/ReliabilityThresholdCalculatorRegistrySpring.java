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

import java.util.Collection;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.ReliabilityThresholdCalculatorRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.exception.ReliabilityThresholdCalculatorNotFound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReliabilityThresholdCalculatorRegistrySpring implements ReliabilityThresholdCalculatorRegistryFacade {
    private final PluginRegistry<ReliabilityThresholdCalculator, KpiType> reliabilityThresholdCalculatorPluginRegistry;

    @Override
    public ReliabilityThresholdCalculator calculator(final Collection<KpiDefinitionEntity> definitions) {
        final Set<KpiType> kpiTypes = CollectionHelpers.collectDistinctBy(definitions, KpiDefinitionEntity::kpiType);

        Preconditions.checkArgument(kpiTypes.size() == 1, "Definitions must contain only one type of KPIs");

        final KpiType kpiType = IterableUtils.first(kpiTypes);

        return reliabilityThresholdCalculatorPluginRegistry.getPluginFor(kpiType, () ->{
            final String message = String.format("KPI type '%s' is not supported", kpiType);
            return new ReliabilityThresholdCalculatorNotFound(message);
        });
    }


}