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

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

class EntryVertexesTest {
    @Test
    void shouldReturnsAsStartingRoutes() {
        final KpiDefinitionVertex vertex1 = vertex("group1", "1");
        final KpiDefinitionVertex vertex2 = vertex("group2", "2");

        final EntryVertexes entryVertexes = EntryVertexes.of(Sets.newLinkedHashSet(vertex1, vertex2));

        final List<KpiDefinitionVertexRoute> actual = entryVertexes.extractStartingRoutes(KpiDefinitionVertexRoute::differentExecutionGroupRoute);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                differentExecutionGroupRoute(vertex1),
                differentExecutionGroupRoute(vertex2)
        );
    }

    @Test
    void shouldFailInitialization_onEmptyRoots() {
        Assertions.assertThatThrownBy(() -> EntryVertexes.of(Collections.emptySet()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("entryVertexes must not be empty");
    }

    static KpiDefinitionVertex vertex(final String group, final String name) {
        return KpiDefinitionVertex.builder().executionGroup(group).definitionName(name).build();
    }
}