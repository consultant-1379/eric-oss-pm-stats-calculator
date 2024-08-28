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

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionGraph;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertexRoute;
import com.ericsson.oss.air.pm.stats.graph.route.RouteHelper;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiGroupLoopDetector {
    @Inject
    private GraphCycleDetector graphCycleDetector;
    @Inject
    private RouteHelper routeHelper;

    /**
     * Detect loops on execution group level.
     *
     * @param kpiDefinitionGraph Unifying class that contains the dependencies and the entry points.
     * @return {@link List} of loops in the graph.
     */
    public List<Collection<KpiDefinitionVertex>> collectLoopsOnExecutionGroups(@NonNull final KpiDefinitionGraph kpiDefinitionGraph) {
        return collectLoops(kpiDefinitionGraph, KpiDefinitionGraph::extractExecutionGroupGraph, KpiDefinitionVertexRoute::differentExecutionGroupRoute);
    }

    /**
     * Detect loops on KPI Definition level.
     *
     * @param kpiDefinitionGraph Unifying class that contains the dependencies and the entry points.
     * @return {@link List} of loops in the graph.
     */
    public List<Collection<KpiDefinitionVertex>> collectLoopsOnKpiDefinitions(@NonNull final KpiDefinitionGraph kpiDefinitionGraph) {
        return collectLoops(kpiDefinitionGraph, KpiDefinitionGraph::extractKpiDependencyGraph, KpiDefinitionVertexRoute::sameExecutionGroupRoute);
    }

    private List<Collection<KpiDefinitionVertex>> collectLoops(
            final KpiDefinitionGraph kpiDefinitionGraph,
            final Function<KpiDefinitionGraph, Graph<String, DefaultEdge>> graphExtractor,
            final Function<KpiDefinitionVertex, KpiDefinitionVertexRoute> routeExtractor
    ) {
        final Graph<String, DefaultEdge> graph = graphExtractor.apply(kpiDefinitionGraph);
        if (graphCycleDetector.hasNoCycle(graph)) {
            log.info("The provided graph has no cycle");
            return emptyList();
        }

        final List<KpiDefinitionVertexRoute> routes = kpiDefinitionGraph.extractStartingRoutes(routeExtractor);

        while (routeHelper.hasUnVisitedRoute(routes)) {
            routeHelper.collectUnVisitedRoutes(routes).forEach(unvisitedRoute -> {
                final Set<KpiDefinitionVertex> dependencies = kpiDefinitionGraph.findDependencies(unvisitedRoute.getLastVertex());
                routes.addAll(calculateNewRoutes(unvisitedRoute, dependencies));
            });
        }

        return routeHelper.filterRoutesWithLoop(routes);
    }

    private List<KpiDefinitionVertexRoute> calculateNewRoutes(
            final KpiDefinitionVertexRoute route, final Collection<? extends KpiDefinitionVertex> dependencies
    ) {
        if (isEmpty(dependencies)) {
            route.markVisited();
            route.markCompleted();
            return emptyList();
        }

        final List<KpiDefinitionVertexRoute> newRoutes = routeHelper.generateNewRoutes(route, dependencies);

        route.markVisited();

        return newRoutes;
    }

}


