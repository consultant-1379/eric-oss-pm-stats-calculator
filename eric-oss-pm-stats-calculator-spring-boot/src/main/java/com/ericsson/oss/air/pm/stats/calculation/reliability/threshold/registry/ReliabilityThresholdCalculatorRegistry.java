/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry;

import java.util.Collection;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.exception.ReliabilityThresholdCalculatorNotFound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import com.google.common.base.Preconditions;
import org.apache.commons.collections4.IterableUtils;

@ApplicationScoped
public class ReliabilityThresholdCalculatorRegistry implements ReliabilityThresholdCalculatorRegistryFacade {
    @Inject
    @Any
    private Instance<ReliabilityThresholdCalculator> reliabilityThresholdCalculatorInstance;

    @Override
    public ReliabilityThresholdCalculator calculator(final Collection<KpiDefinitionEntity> definitions) {
        final Set<KpiType> kpiTypes = CollectionHelpers.collectDistinctBy(definitions, KpiDefinitionEntity::kpiType);

        Preconditions.checkArgument(kpiTypes.size() == 1, "Definitions must contain only one type of KPIs");

        final KpiType kpiType = IterableUtils.first(kpiTypes);

        return reliabilityThresholdCalculatorInstance
                .stream()
                .filter(reliabilityThresholdCalculator -> reliabilityThresholdCalculator.doesSupport(kpiType))
                .findFirst()
                .orElseThrow(() -> new ReliabilityThresholdCalculatorNotFound(String.format("KPI type '%s' is not supported", kpiType)));
    }

}
