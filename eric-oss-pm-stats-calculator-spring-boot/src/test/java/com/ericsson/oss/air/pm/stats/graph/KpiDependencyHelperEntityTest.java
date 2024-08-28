/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.junit.jupiter.api.Test;

class KpiDependencyHelperEntityTest {
    private final KpiDependencyHelper objectUnderTest = new KpiDependencyHelper();

    static final String KPI_A_NAME = "kpi_A";
    static final String KPI_B_NAME = "kpi_B";
    static final String KPI_D_NAME = "kpi_D";
    static final String SIMPLE_1_NAME = "simple_kpi_1";
    static final String SIMPLE_2_NAME = "simple_kpi_2";
    static final String KPI_ABC_EXPRESSION = "SUM(kpi_simple_60.simple_kpi_1) FROM kpi_db://kpi_simple_60";
    static final String KPI_D_EXPRESSION = "SUM(kpi_simple_60.simple_kpi kpi_simple_60.simple_kpi_2) FROM kpi_db://kpi_simple_60";
    static final String SIMPLE_KPI_EXPRESSION = "FIRST(fact_table_0.integerColumn0)";
    static final List<String> KPI_A_AGG_ELEMENTS = Collections.singletonList("kpi_cell_sector_1440.kpi_B");
    static final List<String> KPI_B_AGG_ELEMENTS = Collections.singletonList("kpi_cell_sector_1440.kpi_A");
    static final List<String> KPI_D_AGG_ELEMENTS = List.of("kpi_cell_sector_1440.kpi_A");
    static final List<String> KPI_A_FILTER = Collections.singletonList("kpi_db://kpi_cell_sector_1440.TO_DATE(kpi_C)");
    static final List<String> SIMPLE_KPI_AGG_ELEMENTS = Collections.singletonList("fact_table_0.agg_column_0");
    static final String GROUP_0 = "group_0";
    static final String GROUP_2 = "group_2";
    static final String SIMPLE_GROUP = "simple_group";
    static final String SIMPLE_SCHEMA_NAME = "testSchemaName";

    KpiDefinitionEntity simpleDefinitionEntity1 = KpiDefinitionEntity.builder()
            .withName(SIMPLE_1_NAME)
            .withExpression(SIMPLE_KPI_EXPRESSION)
            .withExecutionGroup(ExecutionGroup.builder().withName(SIMPLE_GROUP).build())
            .withAggregationElements(SIMPLE_KPI_AGG_ELEMENTS)
            .withSchemaName(SIMPLE_SCHEMA_NAME).build();

    KpiDefinitionEntity simpleDefinitionEntity2 = KpiDefinitionEntity.builder()
            .withName(SIMPLE_2_NAME)
            .withExpression(SIMPLE_KPI_EXPRESSION)
            .withExecutionGroup(ExecutionGroup.builder().withName(SIMPLE_GROUP).build())
            .withAggregationElements(SIMPLE_KPI_AGG_ELEMENTS)
            .withSchemaName(SIMPLE_SCHEMA_NAME).build();

    KpiDefinitionEntity complexDefinitionEntityA = KpiDefinitionEntity.builder()
            .withName(KPI_A_NAME)
            .withExpression(KPI_ABC_EXPRESSION)
            .withObjectType("INTEGER")
            .withAggregationType("SUM")
            .withExecutionGroup(ExecutionGroup.builder().withName(GROUP_0).build())
            .withAggregationElements(KPI_A_AGG_ELEMENTS)
            .withExportable(true)
            .withFilters(KPI_A_FILTER)
            .withDataReliabilityOffset(15)
            .withDataLookbackLimit(7_200)
            .withReexportLateData(false).build();

    KpiDefinitionEntity complexDefinitionEntityB = KpiDefinitionEntity.builder()
            .withName(KPI_B_NAME)
            .withExpression(KPI_ABC_EXPRESSION)
            .withObjectType("INTEGER")
            .withAggregationType("SUM")
            .withExecutionGroup(ExecutionGroup.builder().withName(GROUP_0).build())
            .withAggregationElements(KPI_B_AGG_ELEMENTS)
            .withExportable(true)
            .withFilters(Collections.emptyList())
            .withDataReliabilityOffset(15)
            .withDataLookbackLimit(7_200)
            .withReexportLateData(false).build();

    KpiDefinitionEntity complexDefinitionEntityD = KpiDefinitionEntity.builder()
            .withName(KPI_D_NAME)
            .withExpression(KPI_D_EXPRESSION)
            .withObjectType("INTEGER")
            .withAggregationType("SUM")
            .withExecutionGroup(ExecutionGroup.builder().withName(GROUP_2).build())
            .withAggregationElements(KPI_D_AGG_ELEMENTS)
            .withExportable(false)
            .withFilters(Collections.emptyList())
            .withDataReliabilityOffset(15)
            .withDataLookbackLimit(7_200)
            .withReexportLateData(true).build();

    private final KpiDefinitionVertex kpiANameWithGroup0 = KpiDefinitionVertex.builder().definitionName(KPI_A_NAME).executionGroup(GROUP_0).build();
    private final KpiDefinitionVertex kpiBNameWithGroup0 = KpiDefinitionVertex.builder().definitionName(KPI_B_NAME).executionGroup(GROUP_0).build();
    private final KpiDefinitionVertex kpiDNameWithGroup2 = KpiDefinitionVertex.builder().definitionName(KPI_D_NAME).executionGroup(GROUP_2).build();

    @Test
    void shouldCreateMapOfDependenciesFromDefEntities() {
        final Set<KpiDefinitionEntity> inputDefinitions = new HashSet<>(3);
        inputDefinitions.add(complexDefinitionEntityA);
        inputDefinitions.add(complexDefinitionEntityB);
        inputDefinitions.add(complexDefinitionEntityD);

        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> expectedMap = new HashMap<>();
        expectedMap.put(kpiANameWithGroup0, Collections.singleton(kpiBNameWithGroup0));
        expectedMap.put(kpiBNameWithGroup0, Collections.singleton(kpiANameWithGroup0));
        expectedMap.put(kpiDNameWithGroup2, Collections.singleton(kpiANameWithGroup0));

        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> resultDependencyMap = objectUnderTest.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(inputDefinitions);

        assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(expectedMap);
    }

    @Test
    void shouldFindSimpleDependencyMap() {

        final List<KpiDefinitionEntity> inputDefinitions = List.of(complexDefinitionEntityA, complexDefinitionEntityD, simpleDefinitionEntity1, simpleDefinitionEntity2);

        final Map<String, Set<KpiDefinitionEntity>> expected = new HashMap<>();
        expected.put(KPI_A_NAME, Set.of(simpleDefinitionEntity1));
        expected.put(KPI_D_NAME, Set.of(simpleDefinitionEntity1, simpleDefinitionEntity2));

        final Map<String, Set<KpiDefinitionEntity>> resultDependencyMap = objectUnderTest.findSimpleDependencyMap(inputDefinitions);

        assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void shouldFindSimpleDependencyMapForEmptyCollection() {
        final Map<String, Set<KpiDefinitionEntity>> resultDependencyMap = objectUnderTest.findSimpleDependencyMap(Collections.emptyList());

        assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(Collections.emptyMap());
    }

    @Test
    void shouldCollectComplexes() {
        final Set<KpiDefinitionEntity> inputDefinitions = new HashSet<>();
        inputDefinitions.add(complexDefinitionEntityA);
        inputDefinitions.add(complexDefinitionEntityB);
        inputDefinitions.add(complexDefinitionEntityD);
        inputDefinitions.add(simpleDefinitionEntity1);

        final Set<KpiDefinitionEntity> expected = new HashSet<>();
        expected.add(complexDefinitionEntityA);
        expected.add(complexDefinitionEntityB);
        expected.add(complexDefinitionEntityD);

        assertThat(objectUnderTest.collectComplexKpisNewModel(inputDefinitions)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void shouldCollectElements() {
        List<String> expected = new ArrayList<>();
        expected.add(KPI_ABC_EXPRESSION);
        expected.addAll(KPI_A_FILTER);
        expected.addAll(KPI_A_AGG_ELEMENTS);
        assertThat(objectUnderTest.collectElements(complexDefinitionEntityA)).containsExactlyInAnyOrderElementsOf(expected);
    }
}
