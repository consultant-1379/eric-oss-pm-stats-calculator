/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.kpiDefinitionHelper;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.HOURLY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.RELATION_GUID_SOURCE_GUID_TARGET_GUID;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTableFilters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.model.KpiDefinitionsByFilter;
import com.ericsson.oss.air.pm.stats.calculator.test_utils.KpiDefinitionFileRetriever;
import com.ericsson.oss.air.pm.stats.calculator.test_utils.KpiDefinitionRetrievalTestUtils;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectEnumerableAssert;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KpiDefinitionHelperImplTest {
    KpiDefinitionHierarchy kpiDefinitionHierarchyMock = mock(KpiDefinitionHierarchy.class);

    KpiDefinitionHelperImpl objectUnderTest = kpiDefinitionHelper(kpiDefinitionHierarchyMock);

    static final String CELL_GUID = "cell_guid";
    static final String CELL_SECTOR = "cell_sector";
    static final Set<KpiDefinition> KPI_DEFINITIONS_SET = KpiDefinitionRetrievalTestUtils.retrieveKpiDefinitions(new KpiDefinitionFileRetriever());
    static final int INVALID_AGGREGATION_PERIOD = 10;

    @Nested
    @DisplayName("Testing Data Identifier grouping")
    class DataIdentifierTest {
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of(null)).build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier_1")).build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier_1")).build();
        final KpiDefinition kpiDefinition4 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier_2")).build();
        final KpiDefinition kpiDefinition5 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier_2")).build();

        final List<KpiDefinition> kpiDefinitions = asList(kpiDefinition1, kpiDefinition2, kpiDefinition3, kpiDefinition4, kpiDefinition5);

        @Test
        void shouldGroupByDataIdentifier() {
            final Map<DataIdentifier, List<KpiDefinition>> actual = objectUnderTest.groupByDataIdentifier(kpiDefinitions);

            final Map<DataIdentifier, List<KpiDefinition>> expected = Maps.newHashMapWithExpectedSize(2);
            expected.put(DataIdentifier.of("identifier_1"), asList(kpiDefinition2, kpiDefinition3));
            expected.put(DataIdentifier.of("identifier_2"), asList(kpiDefinition4, kpiDefinition5));

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Testing Filter grouping")
    class FilterGroupingTest {
        @Test
        void shouldGroupByFilter() {
            final Filter filter1 = Filter.of("kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'");
            final Filter filter2 = Filter.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2023-08-13'");
            final Filter filter3 = Filter.of(
                    "kpi_db://kpi_simple_60.aggregation_begin_time BETWEEN (" +
                    "date_trunc('hour', TO_TIMESTAMP('${param.start_date_time}')) - interval 1 day) " +
                    "and " +
                    "date_trunc('hour', TO_TIMESTAMP('${param.end_date_time}')" +
                    ')'
            );

            final KpiDefinition kpiDefinition1 = kpiDefinition(filter1, filter2);
            final KpiDefinition kpiDefinition2 = kpiDefinition(filter1, filter2);
            final KpiDefinition kpiDefinition3 = kpiDefinition(filter2, filter1);
            final KpiDefinition kpiDefinition4 = kpiDefinition(filter2, filter1, filter3);
            final KpiDefinition kpiDefinition5 = kpiDefinition(filter3);

            final KpiDefinitionsByFilter actual = objectUnderTest.groupByFilter(List.of(
                kpiDefinition1, kpiDefinition2, kpiDefinition3, kpiDefinition4, kpiDefinition5
            ));

            final Map<Set<Filter>, Set<KpiDefinition>> expected = Map.of(
                newHashSet(filter1, filter2), newHashSet(kpiDefinition1, kpiDefinition2, kpiDefinition3),
                newHashSet(filter1, filter2, filter3), newHashSet(kpiDefinition4),
                newHashSet(filter3), newHashSet(kpiDefinition5));

            actual.forEach((filter, kpi) -> {
                assertThat(expected).containsKey(filter);
                assertThat(expected).containsValue(kpi);
            });
        }

        KpiDefinition kpiDefinition(final Filter... filters) {
            return KpiDefinition.builder().withFilter(asList(filters)).build();
        }

    }

    @Nested
    @DisplayName("Testing custom filter KPI Definition extraction and grouping")
    class CustomFilterKPIDefinitions {

        @Test
        void whenProvidedKpiDefinitionsEmpty_shouldRaiseException() {
            Assertions.assertThatThrownBy(() -> objectUnderTest.areCustomFilterDefinitions(Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("kpiDefinitions is empty");
        }

        @Test
        void whenFilteredKpi_shouldReturnTrue() {
            final List<Filter> filters = List.of(
                    new Filter("kpi_db://kpi_sector_60.TO_DATE(local_timestamp) = '${param.date_for_filter}'"));

            final KpiDefinition kpi2 = KpiDefinition.builder()
                    .withName("get_kpi_definitions_with_filter_attribute")
                    .withAlias("sector")
                    .withFilter(filters)
                    .withExpression("MAX(kpi_sector_60.sum_integer_60_join_kpidb) FROM kpi_db://kpi_sector_60")
                    .withAggregationType("MAX")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("kpi_sector_60.agg_column_0"))
                    .build();

            final List<KpiDefinition> kpiDefinitionList = List.of(kpi2);

            final boolean actual = objectUnderTest.areCustomFilterDefinitions(kpiDefinitionList);

            assertThat(actual).isTrue();
        }

        @Test
        void whenNonFilteredKpi_shouldReturnFalse() {
            final KpiDefinition kpi2 = KpiDefinition.builder()
                    .withName("get_kpi_definitions_without_filter_attribute")
                    .withAlias("sector")
                    .withExpression("MAX(kpi_sector_60.sum_integer_60_join_kpidb) FROM kpi_db://kpi_sector_60")
                    .withAggregationType("MAX")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("kpi_sector_60.agg_column_0"))
                    .build();

            final List<KpiDefinition> kpiDefinitionList = List.of(kpi2);

            final boolean actual = objectUnderTest.areCustomFilterDefinitions(kpiDefinitionList);

            assertThat(actual).isFalse();
        }

        @Test
        void whenGettingKpiDefinitionsWithCustomFilter_thenProperKpisAreReturned() {

            final List<String> actual = objectUnderTest.getFilterAssociatedKpiDefinitions(KPI_DEFINITIONS_SET).stream()
                    .map(KpiDefinition::getName)
                    .collect(Collectors.toList());

            assertThat(actual).hasSize(4);
            assertThat(actual).containsExactlyInAnyOrder("max_integer_1440_kpidb",
                    "first_integer_1440_join_kpidb_filter",
                    "sum_integer_60_join_kpidb",
                    "sum_operator_integer_kpidb_filter");
        }

        @Test
        void whenFilterIsNull_thenNullFilterKpisNotReturned() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("get_kpi_definitions_with_filter_attribute")
                    .withAlias(CELL_GUID)
                    .withExpression("kpi_expression FROM dummy_source")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final List<KpiDefinition> kpiDefinitionList = new ArrayList<>();
            kpiDefinitionList.add(kpi);

            final Set<KpiDefinition> result = objectUnderTest.getFilterAssociatedKpiDefinitions(kpiDefinitionList);
            assertThat(result).isEmpty();
        }

        @Test
        void whenFilterIsEmptyList_thenEmptyFiltersListKpisNotReturned() {
            final List<Filter> filters = new ArrayList<>();
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("get_kpi_definitions_with_filter_attribute")
                    .withAlias(CELL_GUID)
                    .withFilter(filters)
                    .withExpression("kpi_expression FROM dummy_source")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final List<KpiDefinition> kpiDefinitionList = new ArrayList<>();

            kpiDefinitionList.add(kpi);

            final Set<KpiDefinition> result = objectUnderTest.getFilterAssociatedKpiDefinitions(kpiDefinitionList);
            assertThat(result).isEmpty();
        }

        @Test
        void whenFilterIsNonEmptyList_thenKpisWithFiltersReturned() {
            final List<Filter> filters = new ArrayList<>();
            filters.add(new Filter("test_filter"));

            final KpiDefinition kpi1 = KpiDefinition.builder()
                    .withName("get_kpi_definitions_with_filter_attribute")
                    .withAlias(CELL_GUID)
                    .withExpression("kpi_expression FROM dummy_source")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final KpiDefinition kpi2 = KpiDefinition.builder()
                    .withName("get_kpi_definitions_with_filter_attribute")
                    .withAlias(CELL_GUID)
                    .withFilter(filters)
                    .withExpression("kpi_expression FROM dummy_source")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final List<KpiDefinition> kpiDefinitionList = new ArrayList<>();
            kpiDefinitionList.add(kpi1);
            kpiDefinitionList.add(kpi2);

            final Set<KpiDefinition> result = objectUnderTest.getFilterAssociatedKpiDefinitions(kpiDefinitionList);
            assertThat(result).isNotEmpty();
        }

        @Test
        void whenGroupFilterAssociatedKpisByFilters_ThenOneKpiGroupIsReturned() {
            final KpiDefinitionsByFilter actual = objectUnderTest.groupFilterAssociatedKpisByFilters(KPI_DEFINITIONS_SET,
                    new KpiDefinitionHierarchy(KPI_DEFINITIONS_SET));

            final Set<Filter> key = newHashSet(
                    new Filter("kpi_db://kpi_cell_sector_60.TO_DATE(local_timestamp) = '${param.date_for_filter}'"),
                    new Filter("kpi_db://kpi_sector_60.TO_DATE(local_timestamp) = '${param.date_for_filter}'"),
                    new Filter("kpi_db://kpi_cell_guid_1440.TO_DATE(local_timestamp) = '${param.date_for_filter}'"));

            final Set<Set<Filter>> expected = Set.of(key);

            assertThat(actual.keySet()).isEqualTo(expected);
            assertThat(actual.get(key)).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Testing extracting aliases")
    class Alias {
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withAlias("alias1").build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withAlias("alias2").build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withAlias("alias1").build();
        final KpiDefinition kpiDefinition4 = KpiDefinition.builder().withAlias("alias1").build();
        final KpiDefinition kpiDefinition5 = KpiDefinition.builder().withAlias("alias1").build();

        final List<KpiDefinition> kpiDefinitions = asList(kpiDefinition1, kpiDefinition2, kpiDefinition3, kpiDefinition4, kpiDefinition5);

        @Test
        void shouldExtractAlias() {
            final Set<String> actual = objectUnderTest.extractAliases(kpiDefinitions);
            assertThat(actual).containsExactlyInAnyOrder("alias1", "alias2");
        }
    }

    @Nested
    @DisplayName("Testing parsing aggregation elements and columns")
    class AggregationElements {

        List<KpiDefinition> testDefinitions = List.of(
                definition("alias1", "fact_table_0.agg_column_0"),
                definition("alias1", "fact_table_0.agg_column_0"),
                definition("alias1", "fact_table_0.agg_column_1"),
                definition("alias1", "fact_table_0.agg_column_2 AS agg_column_3"),
                definition("alias1", "'${param.execution_id}' AS agg_column_4"),
                definition("alias2", "fact_table_0.agg_column_5"));

        @Test
        void shouldExtractAggregationElementColumns() {
            final List<Column> actual = objectUnderTest.extractAggregationElementColumns("alias1", testDefinitions);

            assertThat(actual).containsExactlyInAnyOrder(
                    Column.of("agg_column_0"),
                    Column.of("agg_column_0"),
                    Column.of("agg_column_1"),
                    Column.of("agg_column_3"),
                    Column.of("agg_column_4")
            );
        }

        @Test
        void shouldExtractAggregationElementsForAnAlias() {
            final List<String> aggregationElements = objectUnderTest.getKpiAggregationElementsForAlias(testDefinitions, "alias1");

            assertThat(aggregationElements).containsExactlyInAnyOrder(
                    "fact_table_0.agg_column_0",
                    "fact_table_0.agg_column_1",
                    "fact_table_0.agg_column_2 AS agg_column_3",
                    "'${param.execution_id}' AS agg_column_4");
        }

        @Test
        void shouldExtractAggregationElements() {

            final KpiDefinition testKpi = definition("alias", List.of(
                        "kpi_simple_60.agg_column_0",
                        "kpi_simple_60.agg_column_1 as alias",
                        "agg_column_0",
                        "kpi_simple_60.agg_column_2"));

            final Set<String> actual = objectUnderTest.extractAggregationElementColumns(testKpi);
            assertThat(actual).containsExactlyInAnyOrder("alias", "agg_column_0", "agg_column_2");
        }

        KpiDefinition definition(final String alias, final String aggregationElement) {
            return KpiDefinition.builder().withAlias(alias).withAggregationElements(singletonList(aggregationElement)).build();
        }

        KpiDefinition definition(final String alias, final List<String> aggregationElements) {
            return KpiDefinition.builder().withAlias(alias).withAggregationElements(aggregationElements).build();
        }
    }

    @Nested
    @DisplayName("Testing KPI Definition name columns")
    class KpiDefinitionNameColumns {
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withName("kpi_a").withAggregationPeriod("60").withAlias("alias1").build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withName("kpi_b").withAggregationPeriod("60").withAlias("alias2").build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withName("kpi_c").withAggregationPeriod("1_440").withAlias("alias1").build();
        final KpiDefinition kpiDefinition4 = KpiDefinition.builder().withName("kpi_d").withAggregationPeriod("60").withAlias("alias2").build();
        final KpiDefinition kpiDefinition5 = KpiDefinition.builder().withName("kpi_e").withAggregationPeriod("60").withAlias("alias1").build();

        final List<KpiDefinition> kpiDefinitions = asList(kpiDefinition1, kpiDefinition2, kpiDefinition3, kpiDefinition4, kpiDefinition5);

        @Test
        void shouldExtractKpiDefinitionNameColumns() {
            final Set<Column> actual = objectUnderTest.extractKpiDefinitionNameColumns(kpiDefinitions, "alias1", 60);

            assertThat(actual).containsExactlyInAnyOrder(
                    Column.of("kpi_a"),
                    Column.of("kpi_e")
            );
        }
    }

    @Nested
    @DisplayName("Testing source tables")
    class SourceTables {
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_inmemory://in_memory").build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_post_agg://in_memory").build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_db://non_in_memory").build();
        final KpiDefinition kpiDefinition4 = KpiDefinition.builder().withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM external://non_in_memory").build();
        final KpiDefinition kpiDefinition5 = KpiDefinition.builder().withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM external://non_in_memory").build();
        final KpiDefinition simpleKpiDefinition = KpiDefinition.builder()
                .withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim))")
                .withInpDataIdentifier(DataIdentifier.of("4G|PM_COUNTERS|SampleCellFDD_1"))
                .build();

        final List<KpiDefinition> kpiDefinitions = List.of(kpiDefinition1, kpiDefinition2, kpiDefinition3, kpiDefinition4, kpiDefinition5, simpleKpiDefinition);

        @Test
        void whenComplexKpi_shouldReturnProperSourceTable() {
            final KpiDefinition complexKpi = KpiDefinition.builder()
                    .withExecutionGroup("test_execution_group")
                    .withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_db://kpi_pme_cell_complex_60")
                    .build();

            final Set<SourceTable> expected = Set.of(new SourceTable("kpi_db://kpi_pme_cell_complex_60"));
            assertThat(objectUnderTest.getSourceTables(complexKpi)).isEqualTo(expected);
        }

        @Test
        void whenSimpleKpi_shouldReturnEmptySet() {
            final KpiDefinition simpleKpi = KpiDefinition.builder()
                    .withInpDataIdentifier(DataIdentifier.of("4G|PM_COUNTERS|SampleCellFDD_1"))
                    .withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim))")
                    .build();

            assertThat(objectUnderTest.getSourceTables(simpleKpi)).isEqualTo(Collections.emptySet());
        }

        @Test
        void shouldExtractNonInMemoryDataSources() {
            final Set<Datasource> actual = objectUnderTest.extractNonInMemoryDataSources(kpiDefinitions);

            assertThat(actual).containsExactlyInAnyOrder(KPI_DB, Datasource.of("external"));
        }

        @Test
        void shouldExtractNonInMemoryDatasourceTables() {
            final DatasourceTables actual = objectUnderTest.extractNonInMemoryDatasourceTables(kpiDefinitions);

            assertThat(actual.entrySet()).satisfiesExactlyInAnyOrder(kpiDatabase -> {
                assertThat(kpiDatabase.getKey()).isEqualTo(KPI_DB);
                assertThat(kpiDatabase.getValue()).containsExactlyInAnyOrder(Table.of("non_in_memory"));
            }, external -> {
                assertThat(external.getKey()).isEqualTo(Datasource.of("external"));
                assertThat(external.getValue()).containsExactlyInAnyOrder(Table.of("non_in_memory"));
            });
        }

        @Test
        void shouldExtractNonInMemoryTablesFromStagedKpis() {
            final Map<Integer, List<KpiDefinition>> stagedKpiDefinitions = new HashMap<>();
            stagedKpiDefinitions.put(1, List.of(kpiDefinition1, kpiDefinition2, kpiDefinition3));
            stagedKpiDefinitions.put(2, List.of(kpiDefinition4, kpiDefinition5));

            final Set<Table> actual = objectUnderTest.getTablesFromStagedKpis(stagedKpiDefinitions);

            final Set<Table> expected = Set.of(Table.of("non_in_memory"));

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Testing source columns")
    class SourceColumns {

        @Test
        void whenKpiDefinitionsAreParsed_andDefinitionHasWhereClause_columnsInWhereClauseAreInSourceColumns() {
            final KpiDefinition kpiDefinition = definition(
                    "estimated_cells.coverage_balance_distance " +
                    "FROM kpi_db://kpi_cell_sector_1440 " +
                    "LEFT JOIN kpi_db://kpi_cell_sector_1440 AS estimated_cells on estimated_cells " +
                    "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid"
            );

            final Set<SourceColumn> actual = objectUnderTest.getSourceColumns(kpiDefinition);
            assertThat(actual).contains(SourceColumn.from(reference(KPI_DB, table("kpi_cell_sector_1440"), column("ref_cell"), null)));
        }

        @Test
        void whenKpiDefinitionsAreParsed_andDefinitionUsesAliases_aliasDoesNotAppearInSourceColumns() {
            final KpiDefinition kpiDefinition = definition(
                    "estimated_cells.coverage_balance_distance " +
                    "FROM kpi_db://kpi_cell_sector_1440 " +
                    "left join kpi_db://kpi_cell_sector_1440 AS estimated_cells " +
                    "on kpi_cell_sector_1440.ref_cell = estimated_cells.guid " +
                    "where kpi_cell_sector_1440.ref_cell = estimated_cells.guid"
            );

            final Set<SourceColumn> sourceColumns = objectUnderTest.getSourceColumns(kpiDefinition);
            for (final SourceColumn source : sourceColumns) {
                assertThat(source.getTable()).as("%s%n should have a source table other than \"estimated_cells\"", source)
                        .isNotEqualTo(Table.of("estimated_cells"));
                assertThat(source.getDatasource()).isNotNull();
            }
        }

        @Test
        void whenKpiDefinitionsAreParsed_andDefinitionUsesAliasesWithNoDatasource_aliasDoesNotAppearInSourceColumns() {
            final KpiDefinition kpiDefinition = definition(
                    "estimated_cells.coverage_balance_distance " +
                    "FROM kpi_db://kpi_cell_sector_1440 " +
                    "left join kpi_db://kpi_cell_sector_1440 AS estimated_cells on estimated_cells " +
                    "where kpi_cell_sector_1440.ref_cell = estimated_cells.guid"
            );

            final Set<SourceColumn> sourceColumns = objectUnderTest.getSourceColumns(kpiDefinition);
            for (final SourceColumn source : sourceColumns) {
                assertThat(source.getTable()).as("%s%n should have a source table other than \"estimated_cells\"", source)
                        .isNotEqualTo(Table.of("estimated_cells"));
            }
        }

        @Test
        void whenExpressionSelectAndWhereHasNoDatasourceAndFromClauseHasDatasource_thenKpiExpressionIsParsedCorrectly() {
            final KpiDefinition kpiDefinition = definition("table.column FROM kpi_db://table WHERE table.column = 1");

            final Set<SourceColumn> actual = objectUnderTest.getSourceColumns(kpiDefinition);

            assertThat(actual).containsExactlyInAnyOrder(SourceColumn.from(reference(KPI_DB, table("table"), column("column"), null)));
        }

        @Test
        void whenExpressionSelectAndWhereHasNoDatasourceAndFromClauseHasDatasource_andMultipleDatasource_thenKpiExpressionIsParsedCorrectly() {
            final KpiDefinition kpiDefinition = definition(
                    "table1.column1 FROM db1://table1 inner join db2://table2 on table1.column1 = table2.column2 WHERE table1.column1 = 1"
            );

            final Set<SourceColumn> actual = objectUnderTest.getSourceColumns(kpiDefinition);

            assertThat(actual).containsExactlyInAnyOrder(
                    SourceColumn.from(reference(References.datasource("db1"), table("table1"), column("column1"), null)),
                    SourceColumn.from(reference(References.datasource("db2"), table("table2"), column("column2"), null))
            );
        }

        KpiDefinition definition(final String expression) {
            return KpiDefinition.builder().withExpression(expression).build();
        }
    }

    @Nested
    @DisplayName("Testing filter processing")
    class FilterProcessing {

        @Test
        void shouldExtractColumnsFromFilters() {
            final Set<Column> actual = objectUnderTest.extractColumns(List.of(
                    Filter.of("kpi_db://kpi_cell_guid.aggregation_begin_time = '${param.date_for_filter}'"),
                    Filter.of("kpi_db://kpi_cell_guid_1440.TO_DATE(local_timestamp) = '${param.date_for_filter}'")
            ));

            assertThat(actual).containsExactlyInAnyOrder(
                    Column.of("aggregation_begin_time"),
                    Column.of("local_timestamp")
            );
        }

        @Test
        void shouldProcessFilters() {
            final DatasourceTableFilters actual = objectUnderTest.groupFilters(Sets.newLinkedHashSet(
                    Filter.of("kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'"),
                    Filter.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2023-08-13'")
            ));

            final DatasourceTableFilters expected = DatasourceTableFilters.newInstance();
            expected.put(KPI_DB, Map.of(
                Table.of("kpi_cell_guid_1440"), singletonList(Filter.of("TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'")),
                Table.of("kpi_sector_60"), singletonList(Filter.of("TO_DATE(aggregation_begin_time) = '2023-08-13'"))
            ));

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Testing aggregation period")
    class AggregationPeriod {
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withAggregationPeriod("-1").build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withAggregationPeriod("-1").build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withAggregationPeriod("60").build();
        final KpiDefinition kpiDefinition4 = KpiDefinition.builder().withAggregationPeriod("60").build();
        final KpiDefinition kpiDefinition5 = KpiDefinition.builder().withAggregationPeriod("1440").build();

        final List<KpiDefinition> kpiDefinitions = asList(
                kpiDefinition1,
                kpiDefinition2,
                kpiDefinition3,
                kpiDefinition4,
                kpiDefinition5
        );

        @Test
        void shouldExtractAggregationPeriods() {
            final Set<Integer> actual = objectUnderTest.extractAggregationPeriods(kpiDefinitions);

            assertThat(actual).containsExactlyInAnyOrder(-1, 60, 1_440);
        }

        @Test
        void shouldExtractAggregationPeriod() {
            final Integer actual = objectUnderTest.extractAggregationPeriod(asList(kpiDefinition3, kpiDefinition4));

            assertThat(actual).isEqualTo(60);
        }

        @Test
        void shouldRaiseException_whenProvidedDefinitionsContainMultipleAggregationPeriods() {
            Assertions.assertThatThrownBy(() -> objectUnderTest.extractAggregationPeriod(kpiDefinitions))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("KPI Definitions should contain one aggregation period only");
        }

        @Test
        void shouldFilterByAggregationPeriod() {
            final List<KpiDefinition> actual = objectUnderTest.filterByAggregationPeriod(60, kpiDefinitions);

            assertThat(actual).containsExactlyInAnyOrder(kpiDefinition3, kpiDefinition4);
        }

        @Test
        void shouldGroupByAggregationPeriod() {
            final Map<Integer, List<KpiDefinition>> actual = objectUnderTest.groupByAggregationPeriod(kpiDefinitions);

            assertThat(actual).containsOnlyKeys(-1, 60, 1_440);

            extractByKey(-1, actual).containsExactlyInAnyOrder(kpiDefinition1, kpiDefinition2);
            extractByKey(60, actual).containsExactlyInAnyOrder(kpiDefinition3, kpiDefinition4);
            extractByKey(1_440, actual).containsExactlyInAnyOrder(kpiDefinition5);
        }

        ObjectEnumerableAssert<ListAssert<KpiDefinition>, KpiDefinition> extractByKey(
                final int key,
                final Map<? super Integer, List<KpiDefinition>> actual) {

            return assertThat(actual).extractingByKey(
                    key,
                    InstanceOfAssertFactories.list(KpiDefinition.class)
            );
        }
    }

    @Nested
    @DisplayName("Testing extracting data identifiers")
    class DataIdentifierExtraction {
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier1")).build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of(null)).build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier2")).build();
        final KpiDefinition kpiDefinition4 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier1")).build();
        final KpiDefinition kpiDefinition5 = KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier3")).build();

        final List<KpiDefinition> kpiDefinitions = asList(kpiDefinition1, kpiDefinition2, kpiDefinition3, kpiDefinition4, kpiDefinition5);

        @Test
        void shouldExtractDataIdentifiers() {
            final Set<DataIdentifier> actual = objectUnderTest.extractDataIdentifiers(kpiDefinitions);
            assertThat(actual).containsExactlyInAnyOrder(
                    DataIdentifier.of("identifier1"), DataIdentifier.of("identifier2"), DataIdentifier.of("identifier3")
            );
        }
    }

    @Nested
    @DisplayName("Testing extracting execution group")
    class ExecutionGroup {
        final String executionGroup = "executionGroup";

        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withExecutionGroup(executionGroup).build();

        @Test
        void shouldExtractExecutionGroup() {
            final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withExecutionGroup(executionGroup).build();
            final String actual = objectUnderTest.extractExecutionGroup(asList(kpiDefinition1, kpiDefinition2));
            assertThat(actual).isEqualTo(executionGroup);
        }

        @Test
        void shouldRaiseException_whenProvidedDefinitionsContainMultipleExecutionGroups() {
            final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withExecutionGroup("another").build();
            final List<KpiDefinition> kpiDefinitions = asList(kpiDefinition1, kpiDefinition2);

            Assertions.assertThatThrownBy(() -> objectUnderTest.extractExecutionGroup(kpiDefinitions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("KPI Definitions should only contain one execution group");
        }
    }

    @Nested
    @DisplayName("Testing extracting data_lookback_limit")
    class DataLookbackLimit {

        @Test
        void shouldExtractLargestDataLookbackLimit() {
            final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withDataLookbackLimit(10).build();
            final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withDataLookbackLimit(15).build();
            final Integer actual = objectUnderTest.extractDataLookbackLimit(asList(kpiDefinition1, kpiDefinition2));
            assertThat(actual).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Testing extracting schema detail")
    class SchemaDetailTest {

        final SchemaDetail schemaDetail1 = SchemaDetail.builder().withTopic("topic1").withNamespace("namespace1").build();
        final SchemaDetail schemaDetail2 = SchemaDetail.builder().withTopic("topic2").withNamespace("namespace2").build();
        final DataIdentifier dataIdentifier = DataIdentifier.of("identifier1");
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withSchemaDetail(schemaDetail1).withInpDataIdentifier(dataIdentifier).build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withSchemaDetail(schemaDetail2).withInpDataIdentifier(dataIdentifier).build();

        @Test
        void shouldExtractSchemaDetails() {
            final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withSchemaDetail(schemaDetail2).build();
            final Set<SchemaDetail> actual = objectUnderTest.extractSchemaDetails(asList(kpiDefinition1, kpiDefinition2, kpiDefinition3));
            assertThat(actual).containsExactlyInAnyOrder(schemaDetail1, schemaDetail2);
        }

        @Test
        void shouldGetSchemaDetail() {
            final SchemaDetail actual = objectUnderTest.getSchemaDetailBy(singleton(kpiDefinition1), dataIdentifier);

            assertThat(actual).isEqualTo(schemaDetail1);
        }

        @Test
        void shouldRaiseException_whenKpiSetIsEmpty() {
            Assertions.assertThatThrownBy(() -> objectUnderTest.extractSchemaDetails(Collections.emptySet()))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("kpis is empty");
        }

        @Test
        void shouldRaiseException_whenThereIsMoreResultForDataIdentifier() {
            Assertions.assertThatThrownBy(() -> objectUnderTest.getSchemaDetailBy(asList(kpiDefinition1, kpiDefinition2), dataIdentifier))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("KPI definitions should only have a single Schema Detail for the same data identifier");
        }
    }

    @Nested
    @DisplayName("Testing stage grouping")
    class GroupByStage {

        @Test
        void whenGroupingKpiDefinitionsByStage_thenCorrectResultReturned() {
            final Map<Integer, List<KpiDefinition>> kpisGroupedByStage = objectUnderTest.groupKpisByStage(KPI_DEFINITIONS_SET);

            assertThat(kpisGroupedByStage.keySet()).hasSize(4);

            assertThat(kpisGroupedByStage.get(1)).hasSize(32);
            assertThat(kpisGroupedByStage.get(2)).hasSize(4);
            assertThat(kpisGroupedByStage.get(3)).hasSize(3);
            assertThat(kpisGroupedByStage.get(4)).hasSize(1);
        }

        @Test
        void whenGroupingKpiDefinitionsByStage_thenCorrectResultReturned2() {

            final KpiDefinition stage1Kpi1 = KpiDefinition.builder()
                    .withName("integer_simple")
                    .withExpression("FIRST(a_new_very_simple_kpi.pmCounters.integerColumn0)")
                    .withInpDataIdentifier(DataIdentifier.of("dataSpace|category|a_new_very_simple_kpi"))
                    .build();

            final KpiDefinition stage1Kpi2 = KpiDefinition.builder()
                    .withName("float_simple")
                    .withExpression("FIRST(a_new_very_simple_kpi.pmCounters.floatColumn0)")
                    .withInpDataIdentifier(DataIdentifier.of("dataSpace|category|a_new_very_simple_kpi"))
                    .build();

            final KpiDefinition stage2kpi1 = KpiDefinition.builder()
                    .withName("first_integer_operator_60_stage2")
                    .withExpression("FIRST(kpi_simple_60.integer_simple) / FIRST(kpi_simple_60.float_simple) FROM kpi_db://kpi_simple_60")
                    .withAggregationElements(List.of("kpi_simple_60.agg_column_0"))
                    .build();

            final KpiDefinition stage3kpi3 = KpiDefinition.builder()
                    .withName("first_integer_operator_60_stage3")
                    .withExpression("FIRST(cell_guid.first_integer_operator_60_stage2) / 10 FROM kpi_inmemory://cell_guid")
                    .withAggregationElements(List.of("cell_guid.agg_column_0"))
                    .build();

            final KpiDefinition stage2kpi2 = KpiDefinition.builder()
                    .withName("sum_integer_60_join_kpidb")
                    .withExpression("SUM(kpi_simple_60.integer_simple) " +
                            "FROM kpi_db://kpi_simple_60 " +
                            "INNER JOIN kpi_db://fact_table_3 " +
                            "ON kpi_simple_60.agg_column_0 = fact_table_3.agg_column_0")
                    .withAggregationElements(List.of("kpi_simple_60.agg_column_0"))
                    .build();

            final KpiDefinition stage2kpi3 = KpiDefinition.builder()
                    .withName("sum_integer_60_join_kpidb_alias")
                    .withExpression("SUM(kpi_simple_60.integer_simple) " +
                            "FROM kpi_db://kpi_simple_60 " +
                            "INNER JOIN kpi_db://fact_table_3 as alias " +
                            "ON kpi_simple_60.agg_column_0 = alias.agg_column_0")
                    .withAggregationElements(List.of("kpi_simple_60.agg_column_0"))
                    .build();

            final KpiDefinition stage2kpi4 = KpiDefinition.builder()
                    .withName("sum_integer_60_join_kpidb_nondefined_join")
                    .withExpression("SUM(kpi_simple_60.integer_simple) " +
                            "FROM kpi_db://kpi_simple_60 " +
                            "INNER JOIN kpi_db://fact_table_4 " +
                            "ON kpi_simple_60.agg_column_0 = fact_table_3.agg_column_0")
                    .withAggregationElements(List.of("kpi_simple_60.agg_column_0"))
                    .build();

            final KpiDefinition stage3kpi1 = KpiDefinition.builder()
                    .withName("max_integer_1440_kpidb")
                    .withExpression("MAX(kpi_sector_60.sum_integer_60_join_kpidb) FROM kpi_db://kpi_sector_60")
                    .withAggregationElements(List.of("kpi_simple_60.agg_column_0", "kpi_simple_60.agg_column_1 as alias"))
                    .build();

            List<KpiDefinition> allKpis = List.of(stage1Kpi1, stage1Kpi2, stage2kpi1, stage2kpi2, stage2kpi3, stage2kpi4, stage3kpi1, stage3kpi3);
            final Map<Integer, List<KpiDefinition>> actual = objectUnderTest.groupKpisByStage(allKpis);

            assertThat(actual.get(1)).isEqualTo(List.of(stage1Kpi1, stage1Kpi2));
            assertThat(actual.get(2)).isEqualTo(List.of(stage2kpi1, stage2kpi2, stage2kpi3, stage2kpi4));
            assertThat(actual.get(3)).isEqualTo(List.of(stage3kpi1, stage3kpi3));
        }

        @Test
        void whenGetKpiByAggregationPeriod_givenAnInvalidAggregationPeriod_thenNoKpisAreReturned() {
            final Map<Integer, List<KpiDefinition>> result = objectUnderTest.getStagedKpisForAggregationPeriod(KPI_DEFINITIONS_SET,
                    INVALID_AGGREGATION_PERIOD);
            assertThat(result).isEmpty();
        }

        @Test
        void whenGetKpisForAlias_givenAllDailyKpisAndCellSectorAlias_thenOnlyCellSectorKpisReturned() {
            final Set<KpiDefinition> kpis = KpiDefinitionHandler
                    .getKpisForAGivenAliasFromStagedKpis(
                            objectUnderTest.getStagedKpisForAggregationPeriod(KPI_DEFINITIONS_SET, DAILY_AGGREGATION_PERIOD_IN_MINUTES),
                            CELL_SECTOR);

            assertThat(kpis).hasSize(1);
            final Set<String> kpiNames = kpis.stream().map(KpiDefinition::getName).collect(toSet());
            assertThat(kpiNames).containsExactlyInAnyOrder("first_integer_1440_join_kpidb_filter");
        }

        @Test
        void whenGetKpisForAlias_givenAllDailyKpisAndRelationAlias_thenOnlyRelationKpisReturned() {
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(
                    objectUnderTest.getStagedKpisForAggregationPeriod(KPI_DEFINITIONS_SET, DAILY_AGGREGATION_PERIOD_IN_MINUTES),
                    RELATION_GUID_SOURCE_GUID_TARGET_GUID);
            assertThat(kpis.size()).isEqualTo(3);
            final Set<String> kpiNames = kpis.stream().map(KpiDefinition::getName).collect(toSet());
            assertThat(kpiNames).containsExactlyInAnyOrder( "sum_integer_operator_1440", "first_boolean_1440",
                    "sum_real_1440");
        }

        @Test
        void whenGetKpisForStageAndAlias_givenAllDailyKpisAndStageTwoAndCellAlias_thenOnlySecondLevelKpisForCellSectorAliasReturned() {
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenStageAndAliasFromStagedKpis(
                    objectUnderTest.getStagedKpisForAggregationPeriod(KPI_DEFINITIONS_SET, DAILY_AGGREGATION_PERIOD_IN_MINUTES), 2, CELL_SECTOR);

            final Set<String> kpiNames = kpis.stream().map(KpiDefinition::getName).collect(toSet());

            assertThat(kpiNames).containsExactlyInAnyOrder("first_integer_1440_join_kpidb_filter");
        }

        @Test
        void whenGetKpisForStageAndAlias_givenAllHourlyKpisAndStageOneAndRelationAlias_thenOnlyFirstLevelKpisForRelationAliasReturned() {
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenStageAndAliasFromStagedKpis(
                    objectUnderTest.getStagedKpisForAggregationPeriod(KPI_DEFINITIONS_SET, HOURLY_AGGREGATION_PERIOD_IN_MINUTES), 1,
                    RELATION_GUID_SOURCE_GUID_TARGET_GUID);
            assertThat(kpis.size()).isEqualTo(1);
            final Set<String> kpiNames = kpis.stream().map(KpiDefinition::getName).collect(toSet());

            assertThat(kpiNames).containsExactlyInAnyOrder("first_float_divideby0_60");
        }

        @Test
        void whenGetKpisByAggregationPeriod_thenOnlyKpisWithThatAggregationPeriodAreReturned() {
            Collection<KpiDefinition> postAggregationKpiDefinitions = KpiDefinitionHandler
                    .getPostAggregationKpiDefinitions(KPI_DEFINITIONS_SET);
            KPI_DEFINITIONS_SET.removeAll(postAggregationKpiDefinitions);
            final Map<Integer, List<KpiDefinition>> result = objectUnderTest.getStagedKpisForAggregationPeriod(KPI_DEFINITIONS_SET,
                    DAILY_AGGREGATION_PERIOD_IN_MINUTES);

            final List<KpiDefinition> allKpis = result.values().stream().flatMap(Collection::stream).collect(toList());
            assertThat(allKpis.size()).isEqualTo(26);
            assertThat(result).containsKeys(1, 2, 3);

            KPI_DEFINITIONS_SET.addAll(postAggregationKpiDefinitions);
        }
    }
}