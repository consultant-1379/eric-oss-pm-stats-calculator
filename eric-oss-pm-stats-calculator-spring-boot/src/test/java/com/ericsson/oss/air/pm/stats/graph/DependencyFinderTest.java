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
import static com.ericsson.oss.air.pm.stats._helper.MotherObject.table;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionGraph;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import kpi.model.ScheduledComplex;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.complex.ComplexTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cases documented at <a href="https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/KPI+Execution+Group+graph+creating+test+scenarios">Confluence</a>.
 */
@ExtendWith(MockitoExtension.class)
class DependencyFinderTest {
    static final String KPI_A = "kpi_A";
    static final String KPI_B = "kpi_B";
    static final String KPI_C = "kpi_C";
    static final String KPI_D = "kpi_D";
    static final String KPI_E = "kpi_E";
    static final String GROUP_0 = "group_0";
    static final String GROUP_1 = "group_1";
    static final String GROUP_2 = "group_2";

    KpiDefinitionEntity complexKpiCDefinition;
    KpiDefinitionEntity complexKpiBDefinition;
    KpiDefinitionEntity complexKpiADefinition;
    KpiDefinitionEntity complexKpiDDefinition;
    KpiDefinitionEntity complexKpiEDefinition;
    KpiDefinitionEntity simpleKpiDefinition;

    private final KpiDefinitionVertex kpiANameWithGroup0 = KpiDefinitionVertex.builder().definitionName(KPI_A).executionGroup(GROUP_0)
            .build();
    private final KpiDefinitionVertex kpiBNameWithGroup0 = KpiDefinitionVertex.builder().definitionName(KPI_B).executionGroup(GROUP_0)
            .build();
    private final KpiDefinitionVertex kpiCNameWithGroup1 = KpiDefinitionVertex.builder().definitionName(KPI_C).executionGroup(GROUP_1)
            .build();
    private final KpiDefinitionVertex kpiDNameWithGroup2 = KpiDefinitionVertex.builder().definitionName(KPI_D).executionGroup(GROUP_2)
            .build();
    private final KpiDefinitionVertex kpiENameWithGroup2 = KpiDefinitionVertex.builder().definitionName(KPI_E).executionGroup(GROUP_2)
            .build();

    ComplexTable complexTable = table(60, "alias_value", List.of("table.column1", "table.column2"), false, 15, 7_200, false, List.of(
            kpiDefinition(
                    "kpi_A", "SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_0",
                    List.of("kpi_cell_sector_1440.kpi_B"), false, List.of("kpi_db://kpi_cell_sector_1440.TO_DATE(kpi_C)"), 15, 7_200, false
            ),
            kpiDefinition(
                    "kpi_B", "SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_0",
                    List.of("kpi_cell_sector_1440.kpi_A"), false, Collections.emptyList(), 15, 7_200, false
            ),
            kpiDefinition(
                    "kpi_C", "SUM(kpi_simple_60.random_kpi) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_1",
                    List.of("kpi_cell_sector_1440.kpi_not_in_payload"), false, List.of("kpi_cell_sector_1440.kpi_B"), 15, 7_200, false
            ),
            kpiDefinition(
                    "kpi_D", "SUM(kpi_simple_60.kpi_C) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_2",
                    List.of("kpi_cell_sector_1440.kpi_A"), false, Collections.emptyList(), 15, 7_200, false
            ),
            kpiDefinition(
                    "kpi_E", "SUM(kpi_simple_60.other_kpi) FROM kpi_db://kpi_simple_60", "INTEGER", AggregationType.SUM, "group_2",
                    List.of("kpi_cell_sector_1440.random_kpi"), false, Collections.emptyList(), 15, 7_200, false
            )));

    @Spy
    KpiDependencyHelper kpiDependencyHelper;
    @Mock
    KpiDefinitionService kpiDefinitionService;

    @InjectMocks
    DependencyFinder objectUnderTest;

    @BeforeEach
    public void setUp() {
        complexKpiADefinition = KpiDefinitionEntity.builder()
                .withName(KPI_A)
                .withExpression("SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60")
                .withAggregationElements(List.of("kpi_cell_sector_1440." + KPI_B))
                .withFilters(List.of("kpi_db://kpi_cell_sector_1440.TO_DATE(" + KPI_C + ")"))
                .withExecutionGroup(ExecutionGroup.builder().withId(1).withName(GROUP_0).build())
                .build();

        complexKpiBDefinition = KpiDefinitionEntity.builder()
                .withName(KPI_B)
                .withExpression("SUM(kpi_simple_60.simple_kpi) FROM kpi_db://kpi_simple_60")
                .withAggregationElements(List.of("kpi_cell_sector_1440." + KPI_A))
                .withExecutionGroup(ExecutionGroup.builder().withId(1).withName(GROUP_0).build())
                .build();

        complexKpiCDefinition = KpiDefinitionEntity.builder()
                .withName(KPI_C)
                .withExpression("SUM(kpi_simple_60.random_kpi) FROM kpi_db://kpi_simple_60")
                .withAggregationElements(List.of("kpi_cell_sector_1440.kpi_not_in_payload"))
                .withFilters(List.of("kpi_cell_sector_1440." + KPI_B))
                .withExecutionGroup(ExecutionGroup.builder().withId(2).withName(GROUP_1).build())
                .build();

        complexKpiDDefinition = KpiDefinitionEntity.builder()
                .withName(KPI_D)
                .withExpression("SUM(kpi_simple_60." + KPI_C + ") FROM kpi_db://kpi_simple_60")
                .withAggregationElements(List.of("kpi_cell_sector_1440." + KPI_A))
                .withExecutionGroup(ExecutionGroup.builder().withId(3).withName(GROUP_2).build())
                .build();

        complexKpiEDefinition = KpiDefinitionEntity.builder()
                .withName(KPI_E)
                .withExpression("SUM(kpi_simple_60.other_kpi) FROM kpi_db://kpi_simple_60")
                .withAggregationElements(List.of("kpi_cell_sector_1440.random_kpi"))
                .withExecutionGroup(ExecutionGroup.builder().withId(3).withName(GROUP_2).build())
                .build();

        simpleKpiDefinition = KpiDefinitionEntity.builder()
                .withName("simple_kpi")
                .withExpression("FIRST(fact_table_0.integerColumn0)")
                .withSchemaDataSpace("eric-data-message-bus-kf:9092")
                .withSchemaCategory("topic2")
                .withSchemaName("very_simple_kpi")
                .build();
    }

    @Test
    void givenKpiDefinition_whenContainsTable_thenOnlyComplexKpiDefinitionsExecutionGroupsAreAddedToMap() {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> expectedMap = new HashMap<>();
        expectedMap.put(kpiANameWithGroup0, new HashSet<>(Arrays.asList(kpiCNameWithGroup1, kpiBNameWithGroup0)));
        expectedMap.put(kpiBNameWithGroup0, Collections.singleton(kpiANameWithGroup0));
        expectedMap.put(kpiCNameWithGroup1, Collections.singleton(kpiBNameWithGroup0));
        expectedMap.put(kpiDNameWithGroup2, new HashSet<>(Arrays.asList(kpiCNameWithGroup1, kpiANameWithGroup0)));
        expectedMap.put(kpiENameWithGroup2, Collections.emptySet());

        final Set<KpiDefinitionVertex> expectedEntryPoints = new HashSet<>(Arrays.asList(kpiDNameWithGroup2, kpiENameWithGroup2));
        final KpiDefinitionGraph expectedGraph = KpiDefinitionGraph.of(expectedMap, expectedEntryPoints);
        final ScheduledComplex scheduledComplex = ScheduledComplex.builder().kpiOutputTables(List.of(complexTable)).build();
        final KpiDefinitionGraph resultDependencyMap = objectUnderTest.dependencyFinder(scheduledComplex);

        assertThat(resultDependencyMap).isEqualTo(expectedGraph);

    }

    @Test
    void givenSetOfDefinition_whenContainsComplexKpiDependenciesInDifferentGroups_thenOnlyComplexKpisExecutionGroupsAreAddedToMap() {
        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> expectedMap = new HashMap<>();
        expectedMap.put(kpiANameWithGroup0, new HashSet<>(Arrays.asList(kpiCNameWithGroup1, kpiBNameWithGroup0)));
        expectedMap.put(kpiBNameWithGroup0, Collections.singleton(kpiANameWithGroup0));
        expectedMap.put(kpiCNameWithGroup1, Collections.singleton(kpiBNameWithGroup0));
        expectedMap.put(kpiDNameWithGroup2, new HashSet<>(Arrays.asList(kpiCNameWithGroup1, kpiANameWithGroup0)));
        expectedMap.put(kpiENameWithGroup2, Collections.emptySet());

        final Set<KpiDefinitionVertex> expectedEntryPoints = new HashSet<>(Arrays.asList(kpiDNameWithGroup2, kpiENameWithGroup2));
        final KpiDefinitionGraph expectedGraph = KpiDefinitionGraph.of(expectedMap, expectedEntryPoints);

        final Set<KpiDefinitionEntity> definitionSet = new HashSet<>(6);
        definitionSet.add(complexKpiADefinition);
        definitionSet.add(complexKpiBDefinition);
        definitionSet.add(complexKpiCDefinition);
        definitionSet.add(complexKpiDDefinition);
        definitionSet.add(complexKpiEDefinition);

        final KpiDefinitionGraph resultDependencyMap = objectUnderTest.dependencyFinder(definitionSet);

        assertThat(resultDependencyMap).isEqualTo(expectedGraph);
    }

    @Test
    void givenKpiSetInDb_whenListOfDefinitionNamesPassed_thenCorrectMapShouldBeReturned() {
        when(kpiDefinitionService.findAll(DEFAULT_COLLECTION_ID)).thenReturn(List.of(
                complexKpiADefinition,
                complexKpiBDefinition,
                complexKpiCDefinition,
                complexKpiDDefinition,
                complexKpiEDefinition,
                simpleKpiDefinition
        ));

        final Map<String, Set<String>> expectedMap = Map.of(
                KPI_D, Set.of(KPI_C),
                KPI_A, Set.of(KPI_C)
        );

        final Map<String, Set<String>> actualMap = objectUnderTest.findInadequateDependencies(List.of(KPI_C), DEFAULT_COLLECTION_ID);

        assertThat(actualMap)
                .containsAllEntriesOf(expectedMap);
    }
}
