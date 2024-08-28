/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.route;

import static com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertexRoute.differentExecutionGroupRoute;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertexRoute;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RouteHelperTest {
    final KpiDefinitionVertex vertex1_group1 = KpiDefinitionVertex.builder().executionGroup("group1").definitionName("1").build();
    final KpiDefinitionVertex vertex2_group2 = KpiDefinitionVertex.builder().executionGroup("group2").definitionName("2").build();
    final KpiDefinitionVertex vertex3_group3 = KpiDefinitionVertex.builder().executionGroup("group3").definitionName("3").build();
    final KpiDefinitionVertex vertex4_group3 = KpiDefinitionVertex.builder().executionGroup("group3").definitionName("4").build();
    final KpiDefinitionVertex vertex5_group1 = KpiDefinitionVertex.builder().executionGroup("group1").definitionName("5").build();


    RouteHelper objectUnderTest = new RouteHelper();

    @Nested
    class UnVisitedRoutes {
        @Test
        void shouldReturnTrue_whenRouteIsUnVisited() {
            final KpiDefinitionVertexRoute route1 = differentExecutionGroupRoute(vertex1_group1);
            final KpiDefinitionVertexRoute route2 = differentExecutionGroupRoute(vertex2_group2);

            final boolean actual = objectUnderTest.hasUnVisitedRoute(Arrays.asList(route1, route2));

            Assertions.assertThat(actual).isTrue();
        }

        @Test
        void shouldReturnFalse_whenRouteIsVisited() {
            final KpiDefinitionVertexRoute route1 = differentExecutionGroupRoute(vertex1_group1);
            final KpiDefinitionVertexRoute route2 = differentExecutionGroupRoute(vertex2_group2);

            route1.markVisited();
            route2.markVisited();

            final boolean actual = objectUnderTest.hasUnVisitedRoute(Arrays.asList(route1, route2));

            Assertions.assertThat(actual).isFalse();
        }

        @Test
        void shouldCollectUnvisitedRoutes() {
            final KpiDefinitionVertexRoute route1 = differentExecutionGroupRoute(vertex1_group1);
            final KpiDefinitionVertexRoute route2 = differentExecutionGroupRoute(vertex2_group2);
            final KpiDefinitionVertexRoute route3 = differentExecutionGroupRoute(vertex3_group3);

            route1.markVisited();

            final List<KpiDefinitionVertexRoute> actual = objectUnderTest.collectUnVisitedRoutes(Arrays.asList(route1, route2, route3));

            Assertions.assertThat(actual).containsExactly(
                    route2,
                    route3
            );
        }
    }

    @Nested
    class HandleLoops {
        @Test
        void shouldReturnRoutesWithLoop() {
            final KpiDefinitionVertexRoute route1 = differentExecutionGroupRoute(vertex1_group1);
            final KpiDefinitionVertexRoute route2 = differentExecutionGroupRoute(vertex2_group2);
            final KpiDefinitionVertexRoute route3 = differentExecutionGroupRoute(vertex3_group3);

            route1.addVertex(vertex2_group2);
            route1.addVertex(vertex5_group1);

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.filterRoutesWithLoop(Arrays.asList(route1, route2, route3));

            Assertions.assertThat(actual).satisfiesExactly(
                    route -> Arrays.asList(vertex1_group1, vertex2_group2, vertex5_group1)
            );
        }
    }

    @Nested
    class GenerateNewRoute {

        @Test
        void shouldGenerateNewRoutes() {
            final KpiDefinitionVertexRoute originalRoute = differentExecutionGroupRoute(vertex1_group1);
            originalRoute.addVertex(vertex2_group2);

            final List<KpiDefinitionVertexRoute> actual = objectUnderTest.generateNewRoutes(originalRoute, Arrays.asList(vertex5_group1, vertex4_group3));

            Assertions.assertThat(actual).satisfiesExactly(
                    kpiDefinitionVertexRoute -> {
                        Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group2, vertex5_group1);
                        Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isTrue();
                        Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isTrue();
                    },
                    kpiDefinitionVertexRoute -> {
                        Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group2, vertex4_group3);
                        Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
                        Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();
                    }
            );
        }
    }

}