/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package org.jgrapht.graph;

import static org.assertj.core.util.Sets.newLinkedHashSet;

import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.graph.model.DependencyNetwork;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link DefaultEdge#getSource()} & {@link DefaultEdge#getTarget()} has <strong>protected</strong> access modifier
 * thus only available from the same package.
 */
class DefinitionNetworkTest {
    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";
    private static final String GROUP_3 = "group3";

    private static final KpiDefinitionVertex vertex1 = vertex(GROUP_1, "1");
    private static final KpiDefinitionVertex vertex2 = vertex(GROUP_2, "2");
    private static final KpiDefinitionVertex vertex3 = vertex(GROUP_3, "3");
    private static final KpiDefinitionVertex vertex4 = vertex(GROUP_3, "4");
    private static final KpiDefinitionVertex vertex5 = vertex(GROUP_3, "5");

    /**
     * <img src="./../../../../resources/graphs/dependency-network/network.png" alt="Network"/>
     * <p>
     * To edit the image:
     * <p>
     * <a href="http://graphonline.ru/en/?graph=VmQtCTusAZWfqzZFZZcst">http://graphonline.ru/en/?graph=VmQtCTusAZWfqzZFZZcst</a>
     */
    Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network;

    @BeforeEach
    void setUp() {
        network = Maps.newHashMapWithExpectedSize(5);
        network.put(vertex1, newLinkedHashSet(vertex2));
        network.put(vertex2, newLinkedHashSet(vertex3, vertex4));
        network.put(vertex3, newLinkedHashSet(vertex5));
        network.put(vertex4, newLinkedHashSet(vertex5));
        network.put(vertex5, newLinkedHashSet(vertex3));
    }

    @Test
    void shouldExtractExecutionGroupGraph_fromNetwork() {
        final Graph<String, DefaultEdge> actual = DependencyNetwork.executionGroupGraph(network);

        Assertions.assertThat(actual.vertexSet()).containsExactlyInAnyOrder(GROUP_1, GROUP_2, GROUP_3);
        Assertions.assertThat(actual.edgeSet()).extracting(DefaultEdge::getSource, DefaultEdge::getTarget).containsExactlyInAnyOrder(
                Tuple.tuple(GROUP_1, GROUP_2),
                Tuple.tuple(GROUP_2, GROUP_3)
        );
    }

    @Test
    void shouldExtractExecutionGroupGraph() {
        final DependencyNetwork dependencyNetwork = DependencyNetwork.of(network);

        final Graph<String, DefaultEdge> actual = dependencyNetwork.extractExecutionGroupGraph();

        Assertions.assertThat(actual.vertexSet()).containsExactlyInAnyOrder(GROUP_1, GROUP_2, GROUP_3);
        Assertions.assertThat(actual.edgeSet()).extracting(DefaultEdge::getSource, DefaultEdge::getTarget).containsExactlyInAnyOrder(
                Tuple.tuple(GROUP_1, GROUP_2),
                Tuple.tuple(GROUP_2, GROUP_3)
        );
    }

    @Test
    void shouldExtractKpiDependencyGraph() {
        final DependencyNetwork dependencyNetwork = DependencyNetwork.of(network);

        final Graph<String, DefaultEdge> actual = dependencyNetwork.extractKpiDependencyGraph();

        Assertions.assertThat(actual.vertexSet()).containsExactlyInAnyOrder("1", "2", "3", "4", "5");
        Assertions.assertThat(actual.edgeSet()).extracting(DefaultEdge::getSource, DefaultEdge::getTarget).containsExactlyInAnyOrder(
                Tuple.tuple("4", "5"),
                Tuple.tuple("3", "5"),
                Tuple.tuple("5", "3")
        );
    }

    static KpiDefinitionVertex vertex(final String group, final String name) {
        return KpiDefinitionVertex.builder().executionGroup(group).definitionName(name).build();
    }
}