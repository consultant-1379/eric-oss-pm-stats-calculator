/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jgrapht.Graphs.addEdgeWithVertices;

import com.ericsson.oss.air.pm.stats.scheduler.execution.order.model.ComplexExecutionOrder;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComplexExecutionOrderDeterminerTest {

    DefaultDirectedGraph<String, DefaultEdge> graph;

    @InjectMocks
    ComplexExecutionOrderDeterminer objectUnderTest;

    @BeforeEach
    void setUp() {
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        addEdgeWithVertices(graph, "0", "1");
        addEdgeWithVertices(graph, "0", "3");
        addEdgeWithVertices(graph, "1", "2");
        addEdgeWithVertices(graph, "3", "2");
        addEdgeWithVertices(graph, "2", "4");
        addEdgeWithVertices(graph, "2", "5");
        addEdgeWithVertices(graph, "4", "6");
        addEdgeWithVertices(graph, "5", "6");
    }

    @Test
    void sortedListTest() {
        final ComplexExecutionOrder actual = objectUnderTest.computeExecutionOrder(graph);

        assertThat(actual.getGraph()).isEqualTo(graph);
        assertThat(actual.getOrder()).containsExactly("6", "4", "5", "2", "1", "3", "0");
    }

}