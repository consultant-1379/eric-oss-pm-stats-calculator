/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertexRoute;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RouteHelper {

    public boolean hasUnVisitedRoute(@NonNull final List<KpiDefinitionVertexRoute> routes) {
        return routes.stream().anyMatch(KpiDefinitionVertexRoute::isNotVisited);
    }

    public List<KpiDefinitionVertexRoute> collectUnVisitedRoutes(@NonNull final List<KpiDefinitionVertexRoute> routes) {
        return routes.stream().filter(KpiDefinitionVertexRoute::isNotVisited).collect(Collectors.toList());
    }

    public List<Collection<KpiDefinitionVertex>> filterRoutesWithLoop(@NonNull final List<KpiDefinitionVertexRoute> routes) {
        return routes.stream()
                .filter(KpiDefinitionVertexRoute::doesContainLoop)
                .map(KpiDefinitionVertexRoute::getRoute)
                .collect(Collectors.toList());
    }

    public List<KpiDefinitionVertexRoute> generateNewRoutes(final KpiDefinitionVertexRoute originalRoute,
                                                            @NonNull final Collection<? extends KpiDefinitionVertex> dependencies) {

        final List<KpiDefinitionVertexRoute> newRoutes = new ArrayList<>(dependencies.size());

        for (final KpiDefinitionVertex dependency : dependencies) {
            final KpiDefinitionVertexRoute copyOfOriginalRoute = KpiDefinitionVertexRoute.copyOf(originalRoute);
            copyOfOriginalRoute.addVertex(dependency);
            newRoutes.add(copyOfOriginalRoute);
        }

        return newRoutes;
    }
}
