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

import java.util.LinkedList;
import java.util.Queue;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.graph.utils.GraphUtils;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.model.ComplexExecutionOrder;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

@Slf4j
@ApplicationScoped
public class ComplexExecutionOrderDeterminer {

    public ComplexExecutionOrder computeExecutionOrder(final Graph<String, DefaultEdge> graph) {
        final Graph<String, DefaultEdge> replicaGraph = GraphUtils.shallowCopy(graph);
        final LinkedList<String> nonSorted = new LinkedList<>(replicaGraph.vertexSet());
        final Queue<String> sorted = new LinkedList<>();

        while (!nonSorted.isEmpty()) {
            final String executionGroup = nonSorted.poll();

            if (replicaGraph.outgoingEdgesOf(executionGroup).isEmpty()) {
                sorted.add(executionGroup);
                replicaGraph.removeVertex(executionGroup);
            } else {
                nonSorted.addLast(executionGroup);
            }
        }

        return ComplexExecutionOrder.of(graph, sorted);
    }
}
