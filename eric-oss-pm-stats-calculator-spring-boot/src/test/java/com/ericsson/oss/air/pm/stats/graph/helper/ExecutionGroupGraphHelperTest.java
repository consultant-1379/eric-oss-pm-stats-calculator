/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.helper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.graph.KpiDependencyHelper;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.assertj.core.api.Assertions;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionGroupGraphHelperTest {

    @Mock
    KpiDependencyHelper kpiDependencyHelperMock;

    @InjectMocks
    ExecutionGroupGraphHelper objectUnderTest;

    List<KpiDefinitionEntity> testDefinitionEntity = List.of();

    @Test
    void shouldFetchComplexExecutionGroupGraph() {
        when(kpiDependencyHelperMock.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(testDefinitionEntity)).thenReturn(Map.of(
                vertex("1"), dependencies(vertex("2"), vertex("3")),
                vertex("2"), dependencies(vertex("4")),
                vertex("3"), dependencies(),
                vertex("4"), dependencies()
        ));

        final Graph<String, DefaultEdge> actual = objectUnderTest.fetchComplexExecutionGroupGraph(testDefinitionEntity);

        verify(kpiDependencyHelperMock).createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(testDefinitionEntity);

        Assertions.assertThat(actual.vertexSet()).containsExactlyInAnyOrder("1", "2", "3", "4");
        Assertions.assertThat(actual.edgeSet()).hasSize(3);
        Assertions.assertThat(actual.containsEdge("1", "2")).isTrue();
        Assertions.assertThat(actual.containsEdge("1", "3")).isTrue();
        Assertions.assertThat(actual.containsEdge("2", "4")).isTrue();
    }

    static Set<KpiDefinitionVertex> dependencies(final KpiDefinitionVertex... vertexes) {
        return Set.of(vertexes);
    }

    static KpiDefinitionVertex vertex(final String executionGroup) {
        return KpiDefinitionVertex.builder().executionGroup(executionGroup).definitionName("definitionName").build();
    }
}