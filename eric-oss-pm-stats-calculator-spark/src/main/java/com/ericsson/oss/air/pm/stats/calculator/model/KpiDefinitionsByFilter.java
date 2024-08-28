/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
@NoArgsConstructor(staticName = "newInstance")
public class KpiDefinitionsByFilter {
    @Delegate(types = MapDelegate.class)
    private final Map<Set<Filter>, Set<KpiDefinition>> kpiDefinitionsByFilterMap = new HashMap<>();

    public void addKpiDefinitions(final Set<Filter> filters, final Set<KpiDefinition> kpiDefinitions) {
        kpiDefinitionsByFilterMap.computeIfAbsent(filters, kpis -> new HashSet<>()).addAll(kpiDefinitions);
    }

    public interface MapDelegate {
        Set<KpiDefinition> get(Set<Filter> key);
        Set<KpiDefinition> put(Set<Filter> key, Set<KpiDefinition> value);
        void forEach(BiConsumer<Set<Filter>, Set<KpiDefinition>> action);
        Set<Set<Filter>> keySet();

    }
}
