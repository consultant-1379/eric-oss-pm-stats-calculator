/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

@Slf4j
@ToString
@EqualsAndHashCode
public final class DependencyNetwork {
    /**
     * {@link Map} representing the dependency network of a graph.
     * <pre>
     * Key:    Single Vertex.
     * Value:  {@link Set} of Vertexes on which the Key depends on.
     * </pre>
     */
    private final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = new HashMap<>();

    private DependencyNetwork(@NonNull final Map<? extends KpiDefinitionVertex, ? extends Set<KpiDefinitionVertex>> network) {
        Preconditions.checkArgument(!network.isEmpty(), "network must not be empty");
        this.network.putAll(network);
    }

    public static DependencyNetwork of(final Map<? extends KpiDefinitionVertex, ? extends Set<KpiDefinitionVertex>> dependencyNetwork) {
        return new DependencyNetwork(dependencyNetwork);
    }

    public static Graph<String, DefaultEdge> executionGroupGraph(final Map<? extends KpiDefinitionVertex, ? extends Set<KpiDefinitionVertex>> dependencyNetwork) {
        return new DependencyNetwork(dependencyNetwork).extractExecutionGroupGraph();
    }

    public Set<KpiDefinitionVertex> findDependencies(final KpiDefinitionVertex kpiDefinitionVertex) {
        return Collections.unmodifiableSet(network.get(kpiDefinitionVertex));
    }

    /**
     * Extract the <strong>execution group</strong> related graph from the dependency network.
     *
     * @return {@link Graph} containing <strong>execution group</strong> vertexes.
     */
    public Graph<String, DefaultEdge> extractExecutionGroupGraph() {
        final Graph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        network.forEach((vertex, dependencies) -> {
            directedGraph.addVertex(vertex.executionGroup());

            dependencies.forEach(dependency -> {
                if (vertex.hasSameExecutionGroup(dependency)) {
                    return;
                }

                directedGraph.addVertex(dependency.executionGroup());
                directedGraph.addEdge(vertex.executionGroup(), dependency.executionGroup());
            });
        });

        return directedGraph;
    }

    /**
     * Extract the <strong>KPI Definition</strong> related graph from the dependency network.
     * <p>
     * Dependencies only checked inside execution groups, so edges between execution groups are ignored.
     *
     * @return {@link Graph} containing <strong>KPI Definition</strong> vertexes.
     */
    public Graph<String, DefaultEdge> extractKpiDependencyGraph() {
        final Graph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        network.forEach((vertex, dependencies) -> {
            final String currentDefinition = vertex.definitionName();
            directedGraph.addVertex(currentDefinition);

            dependencies.forEach(dependency -> {
                if (vertex.hasDifferentExecutionGroup(dependency)) {
                    return;
                }

                final String otherDefinition = dependency.definitionName();
                directedGraph.addVertex(otherDefinition);
                directedGraph.addEdge(currentDefinition, otherDefinition);
            });
        });

        return directedGraph;
    }

}
