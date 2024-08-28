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

import static com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertexRoute.differentExecutionGroupRoute;
import static com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertexRoute.sameExecutionGroupRoute;

import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KpiDefinitionVertexRouteTest {

    @Test
    void shouldGetRoute() {
        final KpiDefinitionVertex vertex = vertex("1", "1");
        final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex);

        final Collection<KpiDefinitionVertex> actual = kpiDefinitionVertexRoute.getRoute();

        Assertions.assertThat(actual).isUnmodifiable().containsExactlyInAnyOrder(vertex);
    }

    @Test
    void shouldGetLastVertex() {
        final KpiDefinitionVertex vertex = vertex("1", "1");
        final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex);

        final KpiDefinitionVertex actual = kpiDefinitionVertexRoute.getLastVertex();

        Assertions.assertThat(actual).isEqualTo(vertex);
        Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).as("should not delete last node").containsExactly(vertex);
    }

    @Nested
    class SameExecutionGroup {
        final KpiDefinitionVertex vertex1_group1 = vertex("group1", "1");
        final KpiDefinitionVertex vertex2_group1 = vertex("group1", "2");
        final KpiDefinitionVertex vertex3_group1 = vertex("group1", "3");
        final KpiDefinitionVertex vertex4_group1 = vertex("group1", "4");
        final KpiDefinitionVertex vertex5_group1 = vertex("group1", "5");

        @Test
        void shouldNotAddVertex_whenTheRouteIsAlreadyVisited() {
            final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);
            kpiDefinitionVertexRoute.markVisited();
            kpiDefinitionVertexRoute.addVertex(vertex2_group1);

            Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1);
        }

        @Test
        void shouldNotAddVertex_whenItContainsLoop() {
            final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);
            kpiDefinitionVertexRoute.addVertex(vertex2_group1);
            kpiDefinitionVertexRoute.addVertex(vertex3_group1);
            kpiDefinitionVertexRoute.addVertex(vertex1_group1);

            Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isTrue();

            kpiDefinitionVertexRoute.addVertex(vertex5_group1);

            Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group1, vertex3_group1, vertex1_group1);
        }

        @Nested
        class LoopHandling {

            @Test
            void shouldNotMarkRoute_whenNewVertexIsNotInCurrentGroup() {
                final KpiDefinitionVertex otherVertex = vertex("group2", "1");
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);

                kpiDefinitionVertexRoute.addVertex(otherVertex);

                Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, otherVertex);
            }

            @Test
            void shouldNotMarkRoute_whenNewVertexIsInCurrentGroup_butTheGroupIsNotAlreadyVisited() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);

                kpiDefinitionVertexRoute.addVertex(vertex2_group1);
                kpiDefinitionVertexRoute.addVertex(vertex3_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group1, vertex3_group1);
            }

            @Test
            void shouldMarkRoute_whenLoopIsDetected() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);

                kpiDefinitionVertexRoute.addVertex(vertex2_group1);
                kpiDefinitionVertexRoute.addVertex(vertex3_group1);
                kpiDefinitionVertexRoute.addVertex(vertex4_group1);
                kpiDefinitionVertexRoute.addVertex(vertex5_group1);
                kpiDefinitionVertexRoute.addVertex(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isTrue();
                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isTrue();

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(
                        vertex1_group1,
                        vertex2_group1,
                        vertex3_group1,
                        vertex4_group1,
                        vertex5_group1,
                        vertex1_group1
                );
            }
        }

        @Nested
        class Mark {
            @Test
            void shouldMarkVisited() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();

                kpiDefinitionVertexRoute.markVisited();

                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isTrue();
            }

            @Test
            void shouldMarkCompleted() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.isCompleted()).isFalse();

                kpiDefinitionVertexRoute.markCompleted();

                Assertions.assertThat(kpiDefinitionVertexRoute.isCompleted()).isTrue();
            }
        }

        @Nested
        class Initialization {
            @Test
            void shouldInitialize() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1);
                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();
                Assertions.assertThat(kpiDefinitionVertexRoute.isCompleted()).isFalse();
                Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
            }

            @Test
            void shouldInitializeWithCopyConstructor() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = sameExecutionGroupRoute(vertex1_group1);
                final KpiDefinitionVertexRoute kpiDefinitionVertexRouteCopy = KpiDefinitionVertexRoute.copyOf(kpiDefinitionVertexRoute);

                Assertions.assertThat(kpiDefinitionVertexRouteCopy.getRoute()).containsExactlyElementsOf(kpiDefinitionVertexRoute.getRoute());
                Assertions.assertThat(kpiDefinitionVertexRouteCopy.isVisited()).isEqualTo(kpiDefinitionVertexRoute.isVisited());
                Assertions.assertThat(kpiDefinitionVertexRouteCopy.isCompleted()).isEqualTo(kpiDefinitionVertexRoute.isCompleted());
                Assertions.assertThat(kpiDefinitionVertexRouteCopy.doesContainLoop()).isEqualTo(kpiDefinitionVertexRoute.doesContainLoop());
            }
        }
    }

    @Nested
    class DifferentExecutionGroup {
        final KpiDefinitionVertex vertex1_group1 = vertex("group1", "1");
        final KpiDefinitionVertex vertex2_group2 = vertex("group2", "2");
        final KpiDefinitionVertex vertex3_group3 = vertex("group3", "3");
        final KpiDefinitionVertex vertex4_group3 = vertex("group3", "4");
        final KpiDefinitionVertex vertex5_group1 = vertex("group1", "5");
        final KpiDefinitionVertex vertex6_group3 = vertex("group3", "6");

        @Nested
        class AddVertex {
            @Test
            void shouldNotAddVertex_whenTheRouteIsAlreadyVisited() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);
                kpiDefinitionVertexRoute.markVisited();
                kpiDefinitionVertexRoute.addVertex(vertex2_group2);

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1);
            }

            @Test
            void shouldNotAddVertex_whenItContainsLoop() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);
                kpiDefinitionVertexRoute.addVertex(vertex2_group2);
                kpiDefinitionVertexRoute.addVertex(vertex3_group3);
                kpiDefinitionVertexRoute.addVertex(vertex5_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isTrue();

                kpiDefinitionVertexRoute.addVertex(vertex4_group3);

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group2, vertex3_group3, vertex5_group1);
            }

            @Nested
            class LoopHandling {

                @Test
                void shouldNotMarkRoute_whenNewVertexIsNotInCurrentGroup() {
                    final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);

                    kpiDefinitionVertexRoute.addVertex(vertex2_group2);

                    Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
                    Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();

                    Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group2);
                }

                @Test
                void shouldNotMarkRoute_whenNewVertexIsInCurrentGroup_butTheGroupIsNotAlreadyVisited() {
                    final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);

                    kpiDefinitionVertexRoute.addVertex(vertex2_group2);
                    kpiDefinitionVertexRoute.addVertex(vertex3_group3);

                    Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
                    Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();

                    Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1, vertex2_group2, vertex3_group3);
                }

                @Test
                void shouldMarkRoute_whenLoopIsDetected() {
                    final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);

                    kpiDefinitionVertexRoute.addVertex(vertex2_group2);
                    kpiDefinitionVertexRoute.addVertex(vertex3_group3);
                    kpiDefinitionVertexRoute.addVertex(vertex6_group3);
                    kpiDefinitionVertexRoute.addVertex(vertex5_group1);

                    Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isTrue();
                    Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isTrue();

                    Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(
                            vertex1_group1,
                            vertex2_group2,
                            vertex3_group3,
                            vertex6_group3,
                            vertex5_group1
                    );
                }
            }
        }

        @Nested
        class Mark {
            @Test
            void shouldMarkVisited() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();

                kpiDefinitionVertexRoute.markVisited();

                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isTrue();
            }

            @Test
            void shouldMarkCompleted() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.isCompleted()).isFalse();

                kpiDefinitionVertexRoute.markCompleted();

                Assertions.assertThat(kpiDefinitionVertexRoute.isCompleted()).isTrue();
            }
        }

        @Nested
        class Initialization {
            @Test
            void shouldInitialize() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);

                Assertions.assertThat(kpiDefinitionVertexRoute.getRoute()).containsExactly(vertex1_group1);
                Assertions.assertThat(kpiDefinitionVertexRoute.isVisited()).isFalse();
                Assertions.assertThat(kpiDefinitionVertexRoute.isCompleted()).isFalse();
                Assertions.assertThat(kpiDefinitionVertexRoute.doesContainLoop()).isFalse();
            }

            @Test
            void shouldInitializeWithCopyConstructor() {
                final KpiDefinitionVertexRoute kpiDefinitionVertexRoute = differentExecutionGroupRoute(vertex1_group1);
                final KpiDefinitionVertexRoute kpiDefinitionVertexRouteCopy = KpiDefinitionVertexRoute.copyOf(kpiDefinitionVertexRoute);

                Assertions.assertThat(kpiDefinitionVertexRouteCopy.getRoute()).containsExactlyElementsOf(kpiDefinitionVertexRoute.getRoute());
                Assertions.assertThat(kpiDefinitionVertexRouteCopy.isVisited()).isEqualTo(kpiDefinitionVertexRoute.isVisited());
                Assertions.assertThat(kpiDefinitionVertexRouteCopy.isCompleted()).isEqualTo(kpiDefinitionVertexRoute.isCompleted());
                Assertions.assertThat(kpiDefinitionVertexRouteCopy.doesContainLoop()).isEqualTo(kpiDefinitionVertexRoute.doesContainLoop());
            }
        }
    }

    static KpiDefinitionVertex vertex(final String group, final String name) {
        return KpiDefinitionVertex.builder().executionGroup(group).definitionName(name).build();
    }
}