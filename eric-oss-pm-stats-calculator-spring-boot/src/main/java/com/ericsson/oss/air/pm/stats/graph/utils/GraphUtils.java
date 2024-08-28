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

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GraphUtils {

    public static <V, E> List<V> findAllParentsFromVertex(final Graph<V, E> graph, final V startVertex) {
        final List<V> parents = new ArrayList<>();
        final EdgeReversedGraph<V, E> reversedGraph = new EdgeReversedGraph<>(graph);
        final BreadthFirstIterator<V, E> breadthFirstIterator = new BreadthFirstIterator<>(reversedGraph, startVertex);
        while (breadthFirstIterator.hasNext()) {
            parents.add(breadthFirstIterator.next());
        }
        return parents;
    }

    public static <V> Graph<V, DefaultEdge> shallowCopy(final Graph<V, DefaultEdge> graph) {
        return shallowCopy((AbstractBaseGraph<V, DefaultEdge>) graph);
    }

    public static <V> Graph<V, DefaultEdge> shallowCopy(@NonNull final AbstractBaseGraph<V, DefaultEdge> abstractBaseGraph) {
        @SuppressWarnings("unchecked") final Graph<V, DefaultEdge> clone = (Graph<V, DefaultEdge>) abstractBaseGraph.clone();
        return clone;
    }

}
