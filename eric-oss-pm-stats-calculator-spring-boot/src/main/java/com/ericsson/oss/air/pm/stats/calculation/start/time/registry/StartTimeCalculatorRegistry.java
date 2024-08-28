/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time.registry;

import java.util.Collection;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.start.time.api.StartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.registry.exception.StartTimeCalculatorNotFound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import com.google.common.base.Preconditions;
import org.apache.commons.collections4.IterableUtils;

@ApplicationScoped
public class StartTimeCalculatorRegistry implements StartTimeCalculatorRegistryFacade {

    @Inject
    @Any
    private Instance<StartTimeCalculator> startTimeCalculatorInstance;

    @Override
    public StartTimeCalculator calculator(final Collection<KpiDefinitionEntity> definitions) {
        final Set<KpiType> kpiTypes = CollectionHelpers.collectDistinctBy(definitions, KpiDefinitionEntity::kpiType);

        Preconditions.checkArgument(kpiTypes.size() == 1, "Definitions must contain only one type of KPIs");

        final KpiType kpiType = IterableUtils.first(kpiTypes);

        return startTimeCalculatorInstance
                .stream()
                .filter(startTimeCalculator -> startTimeCalculator.doesSupport(kpiType))
                .findFirst()
                .orElseThrow(() -> new StartTimeCalculatorNotFound(String.format("KPI type '%s' is not supported", kpiType)));
    }
}
