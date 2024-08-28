/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.graph.KpiDependencyHelper;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleKpiDependencyCacheTest {

    @Mock
    KpiDefinitionRepository kpiDefinitionRepositoryMock;
    @Mock
    KpiDependencyHelper kpiDependencyHelperMock;

    @InjectMocks
    SimpleKpiDependencyCache objectUnderTest;

    @Test
    void whenLoadDependenciesForNoDefinitions_thenEmptyMapGetsReturned() {
        final Map<String, Set<KpiDefinitionEntity>> actual = objectUnderTest.loadDependenciesForMultipleDefinitions(emptyList());

        assertThat(actual).isEmpty();

        verify(kpiDefinitionRepositoryMock, never()).findAll();
    }

    @Nested
    class AvailableDefinitions {
        final KpiDefinitionEntity simpleDefinitionMock1 = mock(KpiDefinitionEntity.class);
        final KpiDefinitionEntity simpleDefinitionMock2 = mock(KpiDefinitionEntity.class);
        final Map<String, Set<KpiDefinitionEntity>> testDependencyMap = new HashMap<>();

        @BeforeEach
        void setUp() {
            final List<KpiDefinitionEntity> retrievedDefinitionEntities = List.of();

            testDependencyMap.put("complex_kpi_definition_1", Set.of(simpleDefinitionMock1, simpleDefinitionMock2));
            testDependencyMap.put("complex_kpi_definition_2", Set.of(simpleDefinitionMock1));

            when(kpiDefinitionRepositoryMock.findAll()).thenReturn(retrievedDefinitionEntities);
            when(kpiDependencyHelperMock.findSimpleDependencyMap(retrievedDefinitionEntities)).thenReturn(testDependencyMap);

            objectUnderTest.populateCache();
        }

        @Test
        void populateCache() {
            verify(kpiDefinitionRepositoryMock).findAll();
            verify(kpiDependencyHelperMock).findSimpleDependencyMap(any());
        }

        @Test
        void whenLoadDependenciesForInvalidDefinition_thenDependencyMapWithNullGetsReturned() {
            final String INVALID_NAME = "invalid_name";

            Assertions.assertThatThrownBy(() -> objectUnderTest.loadDependenciesForMultipleDefinitions(List.of(INVALID_NAME)))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage(String.format("The KPI with the name '%s' does not exist", INVALID_NAME));
        }

        @Test
        void whenLoadDependenciesForValidDefinition_thenSimpleDependencyMapGetsReturned() {
            final Map<String, Set<KpiDefinitionEntity>> actual = objectUnderTest.loadDependenciesForMultipleDefinitions(List.of(
                    "complex_kpi_definition_1",
                    "complex_kpi_definition_2"
            ));

            assertThat(actual).containsAllEntriesOf(Map.of(
                    "complex_kpi_definition_1", Set.of(simpleDefinitionMock1, simpleDefinitionMock2),
                    "complex_kpi_definition_2", Set.of(simpleDefinitionMock1)
            ));
        }
    }
}