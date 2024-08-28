/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionParser;

/**
 * Handles Hierarchy of KPI definitions.
 */
//TODO Prepare unit tests
public final class KpiDefinitionHierarchy {

    private final Map<KpiDefinition, Set<KpiDefinition>> kpiPredecessors = new HashMap<>();
    private final Map<KpiDefinition, Set<KpiDefinition>> kpiSuccessors = new HashMap<>();

    public KpiDefinitionHierarchy(final Collection<KpiDefinition> kpiDefinitions) {
        populateKpiPredecessorsAndSuccessors(kpiDefinitions);
    }

    private void populateKpiPredecessorsAndSuccessors(final Collection<KpiDefinition> kpiDefinitions) {
        final Map<String, KpiDefinition> kpisByName = kpiDefinitions.stream()
                .collect(toMap(KpiDefinition::getName, kpiDefinition -> kpiDefinition));
        kpiDefinitions.forEach(kpi -> populateKpiPredecessors(kpi, kpisByName));
        populateKpiSuccessors();
    }

    private void populateKpiPredecessors(final KpiDefinition kpiDefinition, final Map<String, KpiDefinition> kpisByName) {
        if (!isSuccessorKpi(kpiDefinition)) {
            return;
        }
        final List<KpiDefinition> predecessorKpis = getPredecessorKpis(kpiDefinition, kpisByName);
        if (!predecessorKpis.isEmpty()) {
            for (final KpiDefinition predecessorKpi : predecessorKpis) {
                kpiPredecessors.computeIfAbsent(kpiDefinition, predecessor -> new LinkedHashSet<>()).add(predecessorKpi);
                populateKpiPredecessors(predecessorKpi, kpisByName);
                if (kpiPredecessors.get(predecessorKpi) != null) {
                    kpiPredecessors.get(kpiDefinition).addAll(kpiPredecessors.get(predecessorKpi));
                }
            }
        }
    }

    private static boolean isSuccessorKpi(final KpiDefinition kpiDefinition) {
        return KpiDefinitionParser.getSourceTables(kpiDefinition)
                .stream()
                .anyMatch(sourceTable -> sourceTable.getDatasource().isInMemory());
    }

    private static List<KpiDefinition> getPredecessorKpis(final KpiDefinition kpiDefinition, final Map<String, KpiDefinition> kpisByName) {
        final ArrayList<KpiDefinition> predecessorKpis = new ArrayList<>();
        final Set<SourceColumn> sourceColumns = new HashSet<>(KpiDefinitionParser.getSourceColumns(kpiDefinition));
        for (final SourceColumn sourceColumn : sourceColumns) {
            if (Datasource.isInMemory(sourceColumn.getDatasource()) && kpisByName.get(sourceColumn.getColumn()) != null) {
                predecessorKpis.add(kpisByName.get(sourceColumn.getColumn()));
            }
        }
        return predecessorKpis;
    }

    private void populateKpiSuccessors() {
        for (final Map.Entry<KpiDefinition, Set<KpiDefinition>> entry : kpiPredecessors.entrySet()) {
            for (final KpiDefinition kpiDefinition : entry.getValue()) {
                kpiSuccessors.computeIfAbsent(kpiDefinition, v -> new HashSet<>()).add(entry.getKey());
            }
        }
    }

    public Map<KpiDefinition, Set<KpiDefinition>> getKpiSuccessors() {
        return Collections.unmodifiableMap(this.kpiSuccessors);
    }
}
