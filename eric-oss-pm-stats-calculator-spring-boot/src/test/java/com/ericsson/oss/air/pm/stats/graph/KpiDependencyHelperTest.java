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

import static com.ericsson.oss.air.pm.stats._helper.MotherObject.kpiDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KpiDependencyHelperTest {
    private final KpiDependencyHelper objectUnderTest = new KpiDependencyHelper();

    static final String KPI_A = "kpi_A";
    static final String KPI_B = "kpi_B";
    static final String KPI_C = "kpi_C";
    static final String KPI_D = "kpi_D";
    static final String SIMPLE_KPI = "simple_kpi";
    static final String SIMPLE_KPI_2 = "simple_kpi_2";
    static final String ON_DEMAND_KPI = "on_demand_kpi";
    static final String KPI_ABC_EXPRESSION = "SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60";
    static final String KPI_D_EXPRESSION = "SUM(kpi_simple_60.simple_kpi kpi_simple_60.simple_kpi_2) FROM kpi_db://kpi_simple_60";
    static final String SIMPLE_KPI_EXPRESSION = "FIRST(fact_table_0.integerColumn0)";
    static final String ON_DEMAND_KPI_EXPRESSION = "SUM(kpi_cell_sector_1440.kpi_A kpi_cell_sector_1440.kpi_D) FROM kpi_db://kpi_cell_sector_1440";
    static final List<String> KPI_A_AGG_ELEMENTS = List.of("kpi_cell_sector_1440.kpi_B");
    static final List<String> KPI_B_AGG_ELEMENTS = List.of("kpi_cell_sector_1440.kpi_A");
    static final List<String> KPI_D_AGG_ELEMENTS = List.of("kpi_cell_sector_1440.kpi_A");
    static final List<String> KPI_ON_DEMAND_AGG_ELEMENTS = List.of("kpi_cell_sector_1440.kpi_C");
    static final List<String> KPI_A_FILTER = List.of("kpi_db://kpi_cell_sector_1440.TO_DATE(kpi_C)");
    static final List<String> SIMPLE_KPI_AGG_ELEMENTS = List.of("fact_table_0.agg_column_0");
    static final String WORD_BOUNDARY_REGEX = "\\b";
    static final String OR_REGEX = WORD_BOUNDARY_REGEX + '|' + WORD_BOUNDARY_REGEX;
    static final String GROUP_0 = "group_0";
    static final String GROUP_2 = "group_2";
    static final String SIMPLE_GROUP = "simple_group";
    static final List<String> KPI_A_FILTERS = List.of("kpi_db://kpi_cell_sector_1440.TO_DATE(kpi_C)");

    ComplexKpiDefinition complexKpiDefinitionA = kpiDefinition(
            "kpi_A", "SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_0",
            List.of("kpi_cell_sector_1440.kpi_B"), true, List.of("kpi_db://kpi_cell_sector_1440.TO_DATE(kpi_C)"), 15, 7_200, false
    );

    ComplexKpiDefinition complexKpiDefinitionB = kpiDefinition(
            "kpi_B", "SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_0",
            List.of("kpi_cell_sector_1440.kpi_A"), true, List.of(), 15, 7_200, false
    );
    ComplexKpiDefinition complexKpiDefinitionC = kpiDefinition(
            "kpi_D", "SUM(kpi_simple_60.simple_kpi kpi_simple_60.simple_kpi_2) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_2",
            List.of("kpi_cell_sector_1440.kpi_A"), false, List.of(), 15, 7_200, true
    );

    SimpleKpiDefinition simpleKpiDefinition3 = kpiDefinition(
            "kpi_simple", "SUM(fact_table.pmCounters.integerColumn0)", "INTEGER", AggregationType.SUM,
            List.of("kpi_cell_sector_1440.kpi_A"), false, List.of(), 15, 7_200, true, "dataSpace|category|fact_table"
    );

    KpiDefinitionEntity complexKpiADefinition;
    KpiDefinitionEntity complexKpiBDefinition;
    KpiDefinitionEntity complexKpiDDefinition;
    KpiDefinitionEntity simpleKpiDefinition;
    KpiDefinitionEntity simpleKpiDefinition2;
    KpiDefinitionEntity onDemandDefinition;

    private final KpiDefinitionVertex kpiANameWithGroup0 = KpiDefinitionVertex.of(GROUP_0, KPI_A);
    private final KpiDefinitionVertex kpiBNameWithGroup0 = KpiDefinitionVertex.of(GROUP_0, KPI_B);
    private final KpiDefinitionVertex kpiCNameWithGroup2 = KpiDefinitionVertex.of(GROUP_2, KPI_C);
    private final KpiDefinitionVertex kpiDNameWithGroup2 = KpiDefinitionVertex.of(GROUP_2, KPI_D);

    @BeforeEach
    public void setUp() {
        final ExecutionGroup group0 = ExecutionGroup.builder().withName(GROUP_0).build();
        final ExecutionGroup group2 = ExecutionGroup.builder().withName(GROUP_2).build();
        final ExecutionGroup simpleGroup = ExecutionGroup.builder().withName(SIMPLE_GROUP).build();
        complexKpiADefinition = entity(KPI_A, KPI_ABC_EXPRESSION, KPI_A_AGG_ELEMENTS, KPI_A_FILTERS, group0);
        complexKpiBDefinition = entity(KPI_B, KPI_ABC_EXPRESSION, KPI_B_AGG_ELEMENTS, List.of(), group0);
        complexKpiDDefinition = entity(KPI_D, KPI_D_EXPRESSION, KPI_D_AGG_ELEMENTS, List.of(), group2);

        simpleKpiDefinition = entity(SIMPLE_KPI, SIMPLE_KPI_EXPRESSION, SIMPLE_KPI_AGG_ELEMENTS, simpleGroup, "dataSpace", "schemaCategory", "schemaName");
        simpleKpiDefinition2 = entity(SIMPLE_KPI_2, SIMPLE_KPI_EXPRESSION, SIMPLE_KPI_AGG_ELEMENTS, simpleGroup, "dataSpace", "schemaCategory", "schemaName");

        onDemandDefinition = entity(ON_DEMAND_KPI, ON_DEMAND_KPI_EXPRESSION, KPI_ON_DEMAND_AGG_ELEMENTS, List.of());
    }

    @Test
    void collectElementsTest() {
        final List<String> expected = new ArrayList<>();
        expected.add(KPI_ABC_EXPRESSION);
        expected.addAll(KPI_A_FILTERS);
        expected.addAll(KPI_A_AGG_ELEMENTS);
        assertThat(objectUnderTest.collectElements(complexKpiADefinition)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void collectElementsTestNew() {
        final List<String> expected = new ArrayList<>();
        expected.add(KPI_ABC_EXPRESSION);
        expected.addAll(KPI_A_FILTER);
        expected.addAll(KPI_A_AGG_ELEMENTS);
        assertThat(objectUnderTest.collectElements(complexKpiDefinitionA)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void concatenateElementsWithoutFilterTest() {
        final List<String> expected = new ArrayList<>();
        expected.add(KPI_ABC_EXPRESSION);
        expected.addAll(KPI_B_AGG_ELEMENTS);
        assertThat(objectUnderTest.collectElements(complexKpiBDefinition)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void createPatternTest() {
        final Set<String> input = new LinkedHashSet();
        input.add(KPI_A);
        input.add(KPI_B);
        input.add(KPI_C);

        final String expected = WORD_BOUNDARY_REGEX + KPI_A + OR_REGEX + KPI_B + OR_REGEX + KPI_C + WORD_BOUNDARY_REGEX;

        assertThat(objectUnderTest.createPattern(input).pattern()).isEqualTo(expected);
    }

    @Test
    void collectDependenciesTest() {
        final List<String> collectedElements = new ArrayList<>();
        collectedElements.add(KPI_ABC_EXPRESSION);
        collectedElements.addAll(KPI_A_FILTERS);
        collectedElements.addAll(KPI_A_AGG_ELEMENTS);
        final Pattern kpiNamesPattern = Pattern.compile(WORD_BOUNDARY_REGEX + KPI_A + OR_REGEX + KPI_B + OR_REGEX + KPI_C + WORD_BOUNDARY_REGEX);

        final List<String> expected = Arrays.asList(KPI_C, KPI_B);

        assertThat(objectUnderTest.collectDependencies(collectedElements, kpiNamesPattern)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void collectDependencyVertexTest() {
        final Map<String, KpiDefinitionVertex> complexKpiNamesCollection = new HashMap<>();
        complexKpiNamesCollection.put(KPI_A, kpiANameWithGroup0);
        complexKpiNamesCollection.put(KPI_B, kpiBNameWithGroup0);
        complexKpiNamesCollection.put(KPI_D, kpiDNameWithGroup2);
        final List<String> dependencies = Arrays.asList(KPI_A, KPI_B);

        final Set<KpiDefinitionVertex> expected = new HashSet<>();
        expected.add(kpiANameWithGroup0);
        expected.add(kpiBNameWithGroup0);

        assertThat(objectUnderTest.collectDependencyVertex(complexKpiNamesCollection, dependencies)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void collectComplexKpisTest() {
        final Set<KpiDefinitionEntity> definitionSet = new HashSet<>();
        definitionSet.add(complexKpiADefinition);
        definitionSet.add(complexKpiBDefinition);
        definitionSet.add(complexKpiDDefinition);
        definitionSet.add(simpleKpiDefinition);

        assertThat(objectUnderTest.collectComplexKpisNewModel(definitionSet)).containsExactlyInAnyOrder(
                complexKpiADefinition,
                complexKpiBDefinition,
                complexKpiDDefinition
        );
    }

    @Test
    void fillKeysTest() {
        final List<KpiDefinitionVertex> complexKpiNamesList = Arrays.asList(kpiANameWithGroup0, kpiBNameWithGroup0, kpiDNameWithGroup2);
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> expected = new HashMap<>();
        expected.put(kpiANameWithGroup0, Set.of());
        expected.put(kpiBNameWithGroup0, Set.of());
        expected.put(kpiDNameWithGroup2, Set.of());

        assertThat(objectUnderTest.fillKeys(complexKpiNamesList)).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void createGraphFromMapTest() {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> inputMap = new HashMap<>();
        inputMap.put(kpiANameWithGroup0, Set.of(kpiBNameWithGroup0, kpiCNameWithGroup2));

        final Set<KpiDefinitionVertex> actual = objectUnderTest.fillGraphFromMap(inputMap).vertexSet();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(List.of(kpiANameWithGroup0, kpiBNameWithGroup0, kpiCNameWithGroup2));
    }

    @Test
    void collectHiddenEntryPointsTest() {
        final Graph<KpiDefinitionVertex, DefaultEdge> inputGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        inputGraph.addVertex(kpiANameWithGroup0);
        inputGraph.addVertex(kpiBNameWithGroup0);
        inputGraph.addVertex(kpiCNameWithGroup2);
        inputGraph.addVertex(kpiDNameWithGroup2);
        inputGraph.addEdge(kpiANameWithGroup0, kpiBNameWithGroup0);
        inputGraph.addEdge(kpiANameWithGroup0, kpiCNameWithGroup2);
        inputGraph.addEdge(kpiBNameWithGroup0, kpiANameWithGroup0);

        final Set<KpiDefinitionVertex> kpiExecutionGroupsEntryPoints = new HashSet<>(List.of(kpiDNameWithGroup2));
        Set<KpiDefinitionVertex> expected = new HashSet<>(Arrays.asList(kpiANameWithGroup0, kpiDNameWithGroup2));

        final Set<KpiDefinitionVertex> actual = objectUnderTest.collectHiddenEntryPoints(inputGraph, kpiExecutionGroupsEntryPoints);

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Nested
    class CreateExecutionGroupDependencies {

        @Test
        void whenComplexDefinitionEntityIsPassed_nothingShouldBeThrown() {
            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> expectedMap = new HashMap<>();
            expectedMap.put(kpiANameWithGroup0, Set.of(kpiBNameWithGroup0));
            expectedMap.put(kpiBNameWithGroup0, Set.of(kpiANameWithGroup0));
            expectedMap.put(kpiDNameWithGroup2, Set.of(kpiANameWithGroup0));

            final Set<KpiDefinitionEntity> definitionSet = new HashSet<>(4);
            definitionSet.add(complexKpiADefinition);
            definitionSet.add(complexKpiBDefinition);
            definitionSet.add(complexKpiDDefinition);
            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> resultDependencyMap = objectUnderTest.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitionSet);

            assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(expectedMap);
        }

        @Test
        void whenComplexDefinitionIsPassed_nothingShouldBeThrown() {
            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> expectedMap = new HashMap<>();
            expectedMap.put(kpiANameWithGroup0, Set.of(kpiBNameWithGroup0));
            expectedMap.put(kpiBNameWithGroup0, Set.of(kpiANameWithGroup0));
            expectedMap.put(kpiDNameWithGroup2, Set.of(kpiANameWithGroup0));

            final Set<KpiDefinition> definitionSet = new HashSet<>(3);
            definitionSet.add(complexKpiDefinitionA);
            definitionSet.add(complexKpiDefinitionB);
            definitionSet.add(complexKpiDefinitionC);
            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> resultDependencyMap = objectUnderTest.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitionSet);

            assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(expectedMap);
        }

        @Test
        void whenComplexAndSimpleDefinitionEntityIsPassed_shouldThrowUnsupportedException() {
            final Set<KpiDefinitionEntity> definitionSet = new HashSet<>(4);
            definitionSet.add(complexKpiADefinition);
            definitionSet.add(complexKpiBDefinition);
            definitionSet.add(complexKpiDDefinition);
            definitionSet.add(simpleKpiDefinition);

            assertThatThrownBy(() -> objectUnderTest.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitionSet))
                    .isInstanceOf(UnsupportedOperationException.class);

        }

        @Test
        void whenComplexAndSimpleDefinitionIsPassed_shouldThrowUnsupportedException() {
            final Set<KpiDefinition> definitionSet = new HashSet<>(4);
            definitionSet.add(complexKpiDefinitionA);
            definitionSet.add(complexKpiDefinitionB);
            definitionSet.add(complexKpiDefinitionC);
            definitionSet.add(simpleKpiDefinition3);

            assertThatThrownBy(() -> objectUnderTest.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitionSet))
                    .isInstanceOf(UnsupportedOperationException.class);

        }
    }

    @Test
    void createMapOfDependenciesTest() {
        final Map<String, Set<String>> expectedMap = new HashMap<>();
        expectedMap.put(KPI_A, Set.of(KPI_B, SIMPLE_KPI));
        expectedMap.put(KPI_B, Set.of(KPI_A, SIMPLE_KPI));
        expectedMap.put(KPI_D, Set.of(KPI_A, SIMPLE_KPI, SIMPLE_KPI_2));
        expectedMap.put(SIMPLE_KPI, Collections.emptySet());
        expectedMap.put(SIMPLE_KPI_2, Collections.emptySet());
        expectedMap.put(ON_DEMAND_KPI, Set.of(KPI_A, KPI_D));

        final Set<KpiDefinitionEntity> definitionSet = Set.of(
                complexKpiADefinition,
                complexKpiBDefinition,
                complexKpiDDefinition,
                simpleKpiDefinition,
                simpleKpiDefinition2,
                onDemandDefinition
        );
        final Map<String, Set<String>> resultDependencyMap = objectUnderTest.createMapOfDependencies(definitionSet);

        assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(expectedMap);
    }

    @Test
    void findSimpleDependencyMap1() {
        final List<KpiDefinitionEntity> definitionSet = List.of(complexKpiADefinition, complexKpiDDefinition, simpleKpiDefinition, simpleKpiDefinition2);
        final Map<String, Set<KpiDefinitionEntity>> expected = new HashMap<>();
        expected.put(KPI_A, Set.of(simpleKpiDefinition));
        expected.put(KPI_D, Set.of(simpleKpiDefinition, simpleKpiDefinition2));

        final Map<String, Set<KpiDefinitionEntity>> resultDependencyMap = objectUnderTest.findSimpleDependencyMap(definitionSet);

        assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void findSimpleDependencyMap2() {
        final Map<String, Set<KpiDefinitionEntity>> resultDependencyMap = objectUnderTest.findSimpleDependencyMap(List.of());

        assertThat(resultDependencyMap).containsExactlyInAnyOrderEntriesOf(Map.of());
    }

    @Test
    void createEntryPointsWithoutLoopsTest() {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> inputMap = new HashMap<>();
        inputMap.put(kpiANameWithGroup0, Set.of(kpiCNameWithGroup2));
        inputMap.put(kpiCNameWithGroup2, Set.of());

        final Set<KpiDefinitionVertex> expectedEntryPoints = Set.of(kpiANameWithGroup0);

        final Set<KpiDefinitionVertex> resultEntryPoints = objectUnderTest.createEntryPoints(inputMap);

        assertThat(resultEntryPoints).containsExactlyInAnyOrderElementsOf(expectedEntryPoints);
    }

    @Test
    void createEntryPointsWithLoopsTest() {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> inputMap = new HashMap<>();
        inputMap.put(kpiANameWithGroup0, new HashSet<>(Arrays.asList(kpiBNameWithGroup0, kpiCNameWithGroup2)));
        inputMap.put(kpiBNameWithGroup0, Set.of(kpiANameWithGroup0));
        inputMap.put(kpiCNameWithGroup2, Set.of(kpiBNameWithGroup0));

        final Set<KpiDefinitionVertex> expectedEntryPoints = new HashSet<>(List.of(kpiANameWithGroup0));

        final Set<KpiDefinitionVertex> resultEntryPoints = objectUnderTest.createEntryPoints(inputMap);

        assertThat(resultEntryPoints).containsExactlyInAnyOrderElementsOf(expectedEntryPoints);
    }

    static KpiDefinitionEntity entity(
            final String name, final String expression, final List<String> elements, final ExecutionGroup executionGroup,
            final String dataSpace, final String schemaCategory, final String schemaName
    ) {
        final KpiDefinitionEntity entity = entity(name, expression, elements, List.of(), executionGroup);
        entity.schemaDataSpace(dataSpace);
        entity.schemaCategory(schemaCategory);
        entity.schemaName(schemaName);

        return entity;
    }

    static KpiDefinitionEntity entity(
            final String name, final String expression, final List<String> elements, final List<String> filters, final ExecutionGroup executionGroup
    ) {
        final KpiDefinitionEntity entity = entity(name, expression, elements, filters);
        entity.executionGroup(executionGroup);
        return entity;
    }

    static KpiDefinitionEntity entity(final String name, final String expression, final List<String> elements, final List<String> filters) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withExpression(expression);
        builder.withAggregationElements(elements);
        builder.withFilters(filters);
        return builder.build();
    }
}
