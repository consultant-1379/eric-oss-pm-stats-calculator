/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.test_utils;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

import lombok.NonNull;

public class _KpiDefinitions {
    public static Set<KpiDefinition> getKpiDefinitionsForAggregationPeriod(
            final @NonNull Collection<? extends KpiDefinition> kpiDefinitions,
            final String aggregationPeriodInMinutes
    ) {
        return kpiDefinitions.stream()
                             .filter(kpi -> Objects.nonNull(kpi.getAggregationPeriod()))
                             .filter(kpi -> aggregationPeriodInMinutes.equals(kpi.getAggregationPeriod()))
                             .collect(toSet());
    }
}
