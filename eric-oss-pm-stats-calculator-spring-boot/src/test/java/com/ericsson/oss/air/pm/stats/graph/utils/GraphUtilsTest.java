/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jgrapht.Graphs.addEdgeWithVertices;

import java.util.List;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GraphUtilsTest {

    @Nested
    class ShallowCopy {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        @BeforeEach
        void setUp() {
            addEdgeWithVertices(graph, "1", "2");
        }

        @Test
        void shouldVerifyEdgeNotRemovedBetweenOriginalAndReplica() {
            final Graph<String, DefaultEdge> replica = GraphUtils.shallowCopy(graph);

            assertThat(graph.containsEdge("1", "2")).isTrue();

            replica.removeEdge("1", "2");

            assertThat(graph.containsEdge("1", "2")).isTrue();
            assertThat(replica.containsEdge("1", "2")).isFalse();
        }

        @Test
        void shouldVerifyVertexNotRemovedBetweenOriginalAndReplica() {
            final Graph<String, DefaultEdge> replica = GraphUtils.shallowCopy(graph);

            assertThat(graph.vertexSet()).contains("1");

            replica.removeVertex("1");

            assertThat(graph.vertexSet()).contains("1");
            assertThat(replica.vertexSet()).doesNotContain("1");
        }
    }

    @Nested
    class FindAllParentsFromVertex {
        DefaultDirectedGraph<String, DefaultEdge> graph;

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class GeneralGraph {

            @BeforeEach
            void setUp() {
                graph = new DefaultDirectedGraph<>(DefaultEdge.class);

                addEdgeWithVertices(graph, "1", "2");
                addEdgeWithVertices(graph, "3", "4");
                addEdgeWithVertices(graph, "5", "6");
                addEdgeWithVertices(graph, "2", "5");
                addEdgeWithVertices(graph, "4", "5");
                addEdgeWithVertices(graph, "4", "7");
            }

            @MethodSource("provideFromVertexToPathToRoot")
            @ParameterizedTest(name = "[{index}] Start Vertex:  ''{0}'' expected path: ''{1}''")
            void findPathFromStartVertexTest(final String vertex, final List<String> expectedVertexes) {
                final List<String> actual = GraphUtils.findAllParentsFromVertex(graph, vertex);

                assertThat(actual).containsExactlyInAnyOrderElementsOf(expectedVertexes);
            }

            Stream<Arguments> provideFromVertexToPathToRoot() {
                return Stream.of(
                        Arguments.of(startVertex("1"), expectedVertexes("1")),
                        Arguments.of(startVertex("6"), expectedVertexes("6", "5", "4", "3", "2", "1")),
                        Arguments.of(startVertex("7"), expectedVertexes("7", "4", "3"))
                );
            }

            String startVertex(final String startVertex) {
                return startVertex;
            }

            List<String> expectedVertexes(final String... resultVertexes) {
                return List.of(resultVertexes);
            }
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class DiamondGraph {
            @BeforeEach
            void setUp() {
                graph = new DefaultDirectedGraph<>(DefaultEdge.class);

                addEdgeWithVertices(graph, "0", "1");
                addEdgeWithVertices(graph, "1", "2");
                addEdgeWithVertices(graph, "0", "3");
                addEdgeWithVertices(graph, "3", "2");
                addEdgeWithVertices(graph, "2", "4");
                addEdgeWithVertices(graph, "2", "5");
                addEdgeWithVertices(graph, "4", "6");
                addEdgeWithVertices(graph, "5", "6");
            }

            @Test
            void shouldNotRepeatVertexPath() {
                final List<String> actual = GraphUtils.findAllParentsFromVertex(graph, "6");

                assertThat(actual).containsExactlyInAnyOrder("0", "1", "2", "3", "4", "5", "6");
            }
        }
    }
}