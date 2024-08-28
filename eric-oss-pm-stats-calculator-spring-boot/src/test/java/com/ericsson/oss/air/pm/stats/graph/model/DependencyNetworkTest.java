/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.model;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DependencyNetworkTest {
    final KpiDefinitionVertex vertex1 = KpiDefinitionVertex.builder().executionGroup("group1").definitionName("1").build();
    final KpiDefinitionVertex vertex2 = KpiDefinitionVertex.builder().executionGroup("group2").definitionName("2").build();
    final KpiDefinitionVertex vertex3 = KpiDefinitionVertex.builder().executionGroup("group3").definitionName("3").build();
    final KpiDefinitionVertex vertex4 = KpiDefinitionVertex.builder().executionGroup("group3").definitionName("4").build();

    Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network;

    @BeforeEach
    void setUp() {
        network = Maps.newHashMapWithExpectedSize(4);
        network.put(vertex1, singleton(vertex2));
        network.put(vertex2, Sets.newLinkedHashSet(vertex3, vertex4));
        network.put(vertex3, emptySet());
        network.put(vertex4, emptySet());
    }

    @Test
    void shouldFindDependencies() {
        final DependencyNetwork dependencyNetwork = DependencyNetwork.of(network);

        final Set<KpiDefinitionVertex> actual = dependencyNetwork.findDependencies(vertex2);

        Assertions.assertThat(actual).isUnmodifiable().containsExactlyInAnyOrder(vertex3, vertex4);
    }

    @Test
    void shouldFailInitialization_onEmptyNetwork() {
        Assertions.assertThatThrownBy(() -> DependencyNetwork.of(Collections.emptyMap()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("network must not be empty");
    }
}