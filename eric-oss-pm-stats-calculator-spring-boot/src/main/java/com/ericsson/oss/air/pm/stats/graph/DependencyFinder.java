/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph;

import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionGraph;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import com.google.common.collect.Sets;
import kpi.model.ScheduledComplex;
import kpi.model.api.table.definition.KpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to gather complex scheduled KPI dependencies from given set of Definitions.
 */
@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class DependencyFinder {

    @Inject
    private KpiDependencyHelper kpiDependencyHelper;
    @Inject
    private KpiDefinitionService kpiDefinitionService;

    /**
     * This function checks the Complex Scheduled KPI definitions' dependencies, checks against the dependent Complex Scheduled KPI's execution group
     * and gathers them into a Map.
     *
     * @param definitions a Set of definitions, representing the KPI definitions
     * @return KpiDefinitionGraph that contains the dependencies map and the entry points of the graph
     */
    public KpiDefinitionGraph dependencyFinder(final Set<KpiDefinitionEntity> definitions) {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> mapOfExecutionGroupDependencies
                = kpiDependencyHelper.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitions);
        final Set<KpiDefinitionVertex> kpiExecutionGroupsEntryPoints = kpiDependencyHelper.createEntryPoints(mapOfExecutionGroupDependencies);

        return KpiDefinitionGraph.of(mapOfExecutionGroupDependencies, kpiExecutionGroupsEntryPoints);
    }

    /**
     * This function checks the Complex Scheduled KPI definitions dependencies, checks against the dependent Complex Scheduled KPI's execution group
     * and gathers them into a Map.
     *
     * @param complexDefinitions a Set of definitions, representing the complex KPI definitions
     * @return KpiDefinitionGraph that contains the dependencies map and the entry points of the graph
     */
    public KpiDefinitionGraph dependencyFinder(final ScheduledComplex complexDefinitions) {
        final Set<KpiDefinition> definitions = complexDefinitions.definitions();
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> mapOfExecutionGroupDependencies
                = kpiDependencyHelper.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitions);
        final Set<KpiDefinitionVertex> kpiExecutionGroupsEntryPoints = kpiDependencyHelper.createEntryPoints(mapOfExecutionGroupDependencies);

        return KpiDefinitionGraph.of(mapOfExecutionGroupDependencies, kpiExecutionGroupsEntryPoints);
    }

    /**
     * This function removes the provided KPIs from the graph, and collects the KPIs remaining in
     * the database, that are dependent on one of the provided KPIs.
     *
     * @param kpiDefinitionNames a List of definition names
     * @return Map that contains the KPIs and their dependencies
     */
    public Map<String, Set<String>> findInadequateDependencies(final Collection<String> kpiDefinitionNames, final UUID coolectionId) {
        final Set<String> kpiDefinitionNamesAsSet = new HashSet<>(kpiDefinitionNames);
        final Map<String, Set<String>> dependencyMap = new HashMap<>();
        final Map<String, Set<String>> mapOfDependencies
                = allMissingElements(kpiDependencyHelper.createMapOfDependencies(kpiDefinitionService.findAll(coolectionId)), kpiDefinitionNamesAsSet);
        mapOfDependencies.forEach((definition, dependencies) -> {
            final Set<String> intersection = Sets.intersection(dependencies, kpiDefinitionNamesAsSet);
            if (!intersection.isEmpty()) {
                dependencyMap.put(definition, intersection);
            }
        });

        return dependencyMap;
    }

    private Map<String, Set<String>> allMissingElements(
            final Map<String, Set<String>> base, final Set<String> from) {
        return base
                .entrySet()
                .stream()
                .filter(kpiDefinitionEntry -> !from.contains(kpiDefinitionEntry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
