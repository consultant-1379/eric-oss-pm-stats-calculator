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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.api.AggregationElementsAttribute;
import kpi.model.api.table.definition.api.FiltersAttribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

@Slf4j
@ApplicationScoped
public class KpiDependencyHelper {

    public Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(
            final Collection<KpiDefinitionEntity> complexKpiDefinitions) {

        if (!complexKpiDefinitions.stream().allMatch(KpiDefinitionEntity::isComplex)) {
            throw new UnsupportedOperationException("The list must contain only Complex KPI Definitions.");
        }

        final Map<String, KpiDefinitionVertex> complexKpiNamesCollection = collectExecGroups(complexKpiDefinitions);
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> mapOfExecutionGroupDependencies
                = fillKeys(new ArrayList<>(complexKpiNamesCollection.values()));
        final Pattern kpiNamesPattern = createPattern(complexKpiNamesCollection.keySet());

        for (final KpiDefinitionEntity complexKpi : complexKpiDefinitions) {
            final String kpiName = complexKpi.name();
            final List<String> collectedElements = collectElements(complexKpi);
            final List<String> dependencies = collectDependencies(collectedElements, kpiNamesPattern);

            final KpiDefinitionVertex keyToAdd = complexKpiNamesCollection.get(kpiName);
            final Set<KpiDefinitionVertex> vertexSet = collectDependencyVertex(complexKpiNamesCollection, dependencies);

            mapOfExecutionGroupDependencies.get(keyToAdd).addAll(vertexSet);
        }
        return mapOfExecutionGroupDependencies;
    }

    public Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(final Set<KpiDefinition> complexDefinitions) {

        if (!complexDefinitions.stream().allMatch(KpiDefinition::isScheduledComplex)) {
            throw new UnsupportedOperationException("The list must contain only Complex KPI Definitions.");
        }
        final Map<String, KpiDefinitionVertex> complexKpiNamesCollection = collectKpiGroup(complexDefinitions);
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> mapOfExecutionGroupDependencies
                = fillKeys(new ArrayList<>(complexKpiNamesCollection.values()));
        final Pattern kpiNamesPattern = createPattern(complexKpiNamesCollection.keySet());

        for (final KpiDefinition kpiDefinition : complexDefinitions) {
            final String kpiName = kpiDefinition.name().value();
            final List<String> collectedElements = collectElements(kpiDefinition);
            final List<String> dependencies = collectDependencies(collectedElements, kpiNamesPattern);

            final KpiDefinitionVertex keyToAdd = complexKpiNamesCollection.get(kpiName);
            final Set<KpiDefinitionVertex> vertexSet = collectDependencyVertex(complexKpiNamesCollection, dependencies);
            mapOfExecutionGroupDependencies.get(keyToAdd).addAll(vertexSet);
        }
        return mapOfExecutionGroupDependencies;
    }

    public Map<String, Set<String>> createMapOfDependencies(final Collection<KpiDefinitionEntity> kpiDefinitionEntities) {
        final Map<String, Set<String>> mapOfDependencies = kpiDefinitionEntities.stream()
                .collect(Collectors.toMap(KpiDefinitionEntity::name, kpiDefinitionEntity -> new HashSet<>()));
        final Pattern kpiNamesPattern = createPattern(mapOfDependencies.keySet());

        for (final KpiDefinitionEntity kpiDefinitionEntity : kpiDefinitionEntities) {
            final List<String> collectedElements = collectElements(kpiDefinitionEntity);
            mapOfDependencies.put(kpiDefinitionEntity.name(), new HashSet<>(collectDependencies(collectedElements, kpiNamesPattern)));
        }

        return mapOfDependencies;
    }

    public Map<String, Set<KpiDefinitionEntity>> findSimpleDependencyMap(final List<KpiDefinitionEntity> definitions) {
        final Map<String, Set<KpiDefinitionEntity>> result = new HashMap<>();

        final Set<KpiDefinitionEntity> complexKpis = collectComplexKpisNewModel(definitions);
        if (complexKpis.isEmpty()) {
            return Collections.emptyMap();
        }

        complexKpis.forEach(complexKpi -> result.put(complexKpi.name(), new HashSet<>()));

        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> dependencyTree =
                createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(complexKpis);
        final Graph<KpiDefinitionVertex, DefaultEdge> dependencyGraph = fillGraphFromMap(dependencyTree);

        final Set<KpiDefinitionEntity> simpleKpis = collectSimpleKpisNewModel(definitions);
        final Pattern kpiNamesPattern = createPattern(simpleKpis.stream().map(KpiDefinitionEntity::name).collect(Collectors.toSet()));

        final LinkedList<KpiDefinitionVertex> queue = dependencyTree.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final KpiDefinitionVertex node = queue.poll();

            result.get(node.definitionName()).addAll(
                    Graphs.successorListOf(dependencyGraph, node).stream()
                            .flatMap(successor -> result.get(successor.definitionName()).stream())
                            .collect(Collectors.toSet())
            );

            final List<String> collectedElements = collectElements(
                    Objects.requireNonNull(definitions.stream()
                            .filter(definition -> node.definitionName().equals(definition.name()))
                            .findFirst().orElse(null))
            );
            final List<String> dependencies = collectDependencies(collectedElements, kpiNamesPattern);

            result.get(node.definitionName()).addAll(
                    definitions.stream()
                            .filter(definition -> dependencies.contains(definition.name()))
                            .collect(Collectors.toSet()));

            queue.addAll(Graphs.predecessorListOf(dependencyGraph, node));
        }

        return result;
    }

    public Set<KpiDefinitionVertex> createEntryPoints(final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> mapOfExecutionGroupDependencies) {
        final Set<KpiDefinitionVertex> kpiExecutionGroupsEntryPoints = new HashSet<>(mapOfExecutionGroupDependencies.keySet());
        mapOfExecutionGroupDependencies.values().forEach(kpiExecutionGroupsEntryPoints::removeAll);

        final Graph<KpiDefinitionVertex, DefaultEdge> directedGraph = fillGraphFromMap(mapOfExecutionGroupDependencies);
        if (isCycleDetected(directedGraph)) {
            kpiExecutionGroupsEntryPoints.addAll(collectHiddenEntryPoints(directedGraph, kpiExecutionGroupsEntryPoints));
        }

        return kpiExecutionGroupsEntryPoints;
    }

    public Set<KpiDefinitionEntity> collectComplexKpisNewModel(final Collection<KpiDefinitionEntity> definitions) {
        return definitions.stream().filter(KpiDefinitionEntity::isComplex).collect(Collectors.toSet());
    }

    public Set<KpiDefinitionEntity> collectSimpleKpisNewModel(final Collection<KpiDefinitionEntity> definitions) {
        return definitions.stream().filter(KpiDefinitionEntity::isSimple).collect(Collectors.toSet());
    }

    public Map<String, KpiDefinitionVertex> collectExecGroups(final Collection<KpiDefinitionEntity> kpis) {
        return kpis.stream()
                .map(definition -> KpiDefinitionVertex.of(definition.executionGroup().name(), definition.name()))
                .collect(Collectors.toMap(KpiDefinitionVertex::definitionName, Function.identity()));
    }

    private Map<String, KpiDefinitionVertex> collectKpiGroup(final Set<KpiDefinition> complexDefinitions) {
        return complexDefinitions.stream()
                .map(definition -> KpiDefinitionVertex.of(definition.executionGroup().value(), definition.name().value()))
                .collect(Collectors.toMap(KpiDefinitionVertex::definitionName, Function.identity()));
    }

    public Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> fillKeys(final List<KpiDefinitionVertex> complexKpiNamesList) {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> resultMap = new HashMap<>();
        complexKpiNamesList.forEach(vertex -> resultMap.put(vertex, new HashSet<>()));
        return resultMap;
    }

    public List<String> collectElements(final KpiDefinitionEntity complexKpi) {
        final List<String> result = new ArrayList<>();

        result.add(complexKpi.expression());

        final List<String> filter = complexKpi.filters();
        if (filter != null) {
            result.addAll(filter);
        }

        final List<String> aggElements = complexKpi.aggregationElements();
        if (aggElements != null) {
            result.addAll(aggElements);
        }
        return result;
    }

    public List<String> collectElements(final KpiDefinition complexKpi) {
        final List<String> result = new ArrayList<>();
        result.add(complexKpi.expression().value());
        final FiltersAttribute<FilterElement> filter = complexKpi.filters();
        final AggregationElementsAttribute<AggregationElement> aggElements = complexKpi.aggregationElements();
        if (filter != null) {
            filter.value().stream().map(FilterElement::value).forEach(result::add);
        }
        if (aggElements != null) {
            aggElements.value().stream().map(AggregationElement::value).forEach(result::add);
        }
        return result;
    }

    public Pattern createPattern(final Set<String> kpiNamesList) {
        return Pattern.compile(kpiNamesList.stream().collect(Collectors.joining("\\b|\\b", "\\b", "\\b")));
    }

    public List<String> collectDependencies(final List<String> collectedElements, final Pattern kpiNamesPattern) {
        return collectedElements.stream().flatMap(element -> kpiNamesPattern.matcher(element).results())
                .map(MatchResult::group).collect(Collectors.toList());
    }

    public Set<KpiDefinitionVertex> collectDependencyVertex(final Map<String, KpiDefinitionVertex> kpiNamesCollection,
                                                            final List<String> dependencies) {
        final Set<KpiDefinitionVertex> valueList = new HashSet<>();
        for (final String dependency : dependencies) {
            final KpiDefinitionVertex dependencyToAddToValue = kpiNamesCollection.get(dependency);
            valueList.add(dependencyToAddToValue);
        }
        return valueList;
    }

    public Set<KpiDefinitionVertex> collectHiddenEntryPoints(final Graph<KpiDefinitionVertex, DefaultEdge> directedGraph,
                                                             final Set<KpiDefinitionVertex> kpiExecutionGroupsEntryPoints) {

        ConnectivityInspector<KpiDefinitionVertex, DefaultEdge> inspector = new ConnectivityInspector<>(directedGraph);
        HawickJamesSimpleCycles<KpiDefinitionVertex, DefaultEdge> simpleCycles = new HawickJamesSimpleCycles<>(directedGraph);

        final List<List<KpiDefinitionVertex>> cycles = simpleCycles.findSimpleCycles();

        collectLoopsFirstElements(cycles).filter(loopElement -> hasNoPathFromEntry(kpiExecutionGroupsEntryPoints, loopElement, inspector))
                .forEach(kpiExecutionGroupsEntryPoints::add);

        return kpiExecutionGroupsEntryPoints;
    }

    public Graph<KpiDefinitionVertex, DefaultEdge> fillGraphFromMap(final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>>
                                                                            mapOfExecutionGroupDependencies) {
        final Graph<KpiDefinitionVertex, DefaultEdge> directedGraph = createGraph();
        mapOfExecutionGroupDependencies.forEach((vertex, dependencies) -> {
            directedGraph.addVertex(vertex);
            dependencies.forEach(dependency -> {
                directedGraph.addVertex(dependency);
                directedGraph.addEdge(vertex, dependency);
            });
        });
        return directedGraph;
    }

    public boolean isCycleDetected(final Graph<KpiDefinitionVertex, DefaultEdge> directedGraph) {
        return new CycleDetector<>(directedGraph).detectCycles();
    }

    private Graph<KpiDefinitionVertex, DefaultEdge> createGraph() {
        return new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    private boolean hasNoPathFromEntry(final Set<KpiDefinitionVertex> kpiExecutionGroupsEntryPoints, final KpiDefinitionVertex loopElement,
                                       final ConnectivityInspector<KpiDefinitionVertex, DefaultEdge> inspector) {
        return kpiExecutionGroupsEntryPoints.stream().noneMatch(entry -> inspector.pathExists(entry, loopElement));
    }

    private Stream<KpiDefinitionVertex> collectLoopsFirstElements(final List<List<KpiDefinitionVertex>> cycles) {
        return cycles.stream().map(KpiDependencyHelper::getFirstElementOfCircle);
    }

    private static KpiDefinitionVertex getFirstElementOfCircle(final List<KpiDefinitionVertex> loop) {
        return IterableUtils.first(loop);
    }
}
