/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph;

import javax.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;

@Slf4j
@ApplicationScoped
public class GraphCycleDetector {

    public boolean hasNoCycle(final Graph<String, DefaultEdge> graph) {
        return !isCycleDetected(graph);
    }

    private boolean isCycleDetected(final Graph<String, DefaultEdge> directedGraph) {
        return new CycleDetector<>(directedGraph).detectCycles();
    }
}
