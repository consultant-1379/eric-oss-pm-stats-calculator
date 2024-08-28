/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.calculator.api;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;

public interface CalculatorHandlerRegistryFacade {
    KpiCalculator defaultCalculator(Collection<KpiDefinition> kpiDefinitions);

    KpiCalculator customCalculator(Collection<KpiDefinition> kpiDefinitions);

    CalculatorHandler calculator(FilterType filterType);
}
