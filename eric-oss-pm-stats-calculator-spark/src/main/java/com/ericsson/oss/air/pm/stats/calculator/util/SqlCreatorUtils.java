/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SqlCreatorUtils class contains operations common to various Sql Creators.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlCreatorUtils {

    /**
     * Filters kpi by name.
     *
     * @param kpiDefinitions
     *            kpis to be filtered
     * @param requiredKpiNames
     *            names to be used for filtering
     * @return {@link Set} of {@link KpiDefinition} has name present in @param requiredKpiNames
     */
    public static Set<KpiDefinition> filterKpiByName(final Collection<KpiDefinition> kpiDefinitions, final List<String> requiredKpiNames) {
        return kpiDefinitions.stream()
                .filter(kpi -> !requiredKpiNames.contains(kpi.getName()))
                .collect(toSet());
    }
}
