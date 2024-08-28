/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.util;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.CELL_GUID;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.RELATION_GUID_SOURCE_GUID_TARGET_GUID;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;
import com.ericsson.oss.air.pm.stats.test.util.KpiDefinitionRetrievalTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KpiDefinitionParserTest {
    static final String SUM_INTEGER_ARRAYINDEX_1440 = "sum_integer_arrayindex_1440";
    static final String CELL_SECTOR = "cell_sector";
    static final String SECTOR = "sector";
    static final String ROLLING_AGGREGATION = "rolling_aggregation";
    static final String EXECUTION_ID = "execution_id";
    static final String FDN_AGG = "fdn_agg";

    static final List<KpiDefinition> KPI_DEFINITIONS = new ArrayList<>(
            KpiDefinitionRetrievalTestUtils.retrieveKpiDefinitions(new KpiDefinitionFileRetriever()));
    static final KpiDefinition KPI_DEFINITION_SUM_INTEGER_ARRAYINDEX_1440 =
            KpiDefinitionRetrievalTestUtils.initKpi(SUM_INTEGER_ARRAYINDEX_1440);

    final Collection<KpiDefinition> postAggregationKpiDefinitions = KpiDefinitionHandler
            .getPostAggregationKpiDefinitions(KPI_DEFINITIONS);

    @BeforeEach
    public void init() {
        KPI_DEFINITIONS.removeAll(postAggregationKpiDefinitions);
    }

    @Nested
    class SourceColumns {

        @Test
        void whenKpiDefinitionsAreLoaded_andAliasesAreUsedInExpression_sourcesParsedDoNotContainAlias() {
            final Set<SourceColumn> sourceColumns = KpiDefinitionParser.getSourceColumns(KPI_DEFINITION_SUM_INTEGER_ARRAYINDEX_1440);
            for (final SourceColumn source : sourceColumns) {
                assertThat(source.getTable().getName()).as("%s%n should have a source table other than \"alias\"", source).isNotEqualTo("alias");
            }
        }

        @Test
        void whenSimpleKpi_thenSourceColumnIsEmpty() {
            KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withInpDataIdentifier(DataIdentifier.of("4G|PM_COUNTERS|SampleCellFDD_1"))
                    .withExpression("table.column")
                    .build();

            final Set<SourceColumn> actual = KpiDefinitionParser.getSourceColumns(kpiDefinition);

            assertThat(actual).isEmpty();
        }

        @Test
        void whenComplexKpi_andWhereHasNoDatasource_andFromClauseHasDatasource_thenSourceColumnParsedCorrectly() {
            KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withExecutionGroup("test_execution_group")
                    .withExpression("table.column FROM kpi_db://table WHERE table.column = 1")
                    .build();

            final Set<SourceColumn> actual = KpiDefinitionParser.getSourceColumns(kpiDefinition);
            assertThat(actual).containsExactlyInAnyOrder(SourceColumn.from(reference(KPI_DB, table("table"), column("column"), null)));
        }

        @Test
        void whenOndemandKpi_andWhereHasNoDatasource_andFromClauseHasDatasource_andMultipleDatasource_thenSourceColumnParsedCorrectly() {
            KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withExpression("table1.column1 FROM kpi_db://table1 inner join kpi_db://table2 on table1.column1 = table2.column2 WHERE table1.column1 = 1")
                    .build();

            final Set<SourceColumn> actual = KpiDefinitionParser.getSourceColumns(kpiDefinition);
            assertThat(actual).containsExactlyInAnyOrder(
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("table1"), column("column1"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("table2"), column("column2"), null))
            );
        }

        @Test
        void whenExpressionHasJoinClause_andJoinClauseHasMatchingTableInFromClause_thenCorrectSourceColumnsAreReturned() {
            final KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withExpression(
                            "FIRST(least(fact_table_1.integerColumn0, fact_table_3.integerColumn0)) " +
                                    "FROM kpi_db://fact_table_1 " +
                                    "INNER JOIN kpi_db://fact_table_3 " +
                                    "ON fact_table_1.agg_column_0 = fact_table_3.agg_column_0")
                    .build();
            final Set<SourceColumn> actual = KpiDefinitionParser.getSourceColumns(kpiDefinition);

            assertThat(actual).containsExactlyInAnyOrder(
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_1"), column("agg_column_0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_3"), column("agg_column_0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_1"), column("integerColumn0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_3"), column("integerColumn0"), null))
            );
        }

        @Test
        void whenExpressionHasJoinClause_andJoinClauseHasNoMatchingTableInFromClause_thenCorrectSourceColumnsAreReturned() {
            final KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withExpression(
                            "FIRST(least(fact_table_1.integerColumn0, fact_table_3.integerColumn0)) " +
                                    "FROM kpi_db://fact_table_1 " +
                                    "INNER JOIN kpi_db://fact_table_4 " +
                                    "ON fact_table_1.agg_column_0 = fact_table_3.agg_column_0")
                    .build();
            final Set<SourceColumn> actual = KpiDefinitionParser.getSourceColumns(kpiDefinition);

            assertThat(actual).containsExactlyInAnyOrder(
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_1"), column("agg_column_0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_3"), column("agg_column_0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_1"), column("integerColumn0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_3"), column("integerColumn0"), null))
            );
        }

        @Test
        void whenExpressionHasJoinClause_andJoinClauseHasAlias_thenCorrectSourceColumnsAreReturned() {
            final KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withExpression(
                            "FIRST(least(fact_table_1.integerColumn0)) " +
                                    "FROM kpi_db://fact_table_1 " +
                                    "INNER JOIN kpi_db://fact_table_3 as alias " +
                                    "ON fact_table_1.agg_column_0 = alias.agg_column_0")
                    .build();
            final Set<SourceColumn> actual = KpiDefinitionParser.getSourceColumns(kpiDefinition);

            assertThat(actual).containsExactlyInAnyOrder(
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_1"), column("agg_column_0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_1"), column("integerColumn0"), null)),
                    SourceColumn.from(reference(References.datasource("kpi_db"), table("fact_table_3"), column("agg_column_0"), Alias.of("alias")))
            );
        }
    }

    @Test
    void whenKpiDefinitionsAreLoaded_thenKpisAreGroupedByAlias() {
        final Map<String, List<KpiDefinition>> kpisGroupedByAlias = KpiDefinitionParser.groupKpisByAlias(KPI_DEFINITIONS);

        assertSoftly(softly -> {
            softly.assertThat(kpisGroupedByAlias.keySet()).hasSize(9);
            softly.assertThat(kpisGroupedByAlias.get(CELL_GUID)).hasSize(19);
            softly.assertThat(kpisGroupedByAlias.get(RELATION_GUID_SOURCE_GUID_TARGET_GUID)).hasSize(4);
            softly.assertThat(kpisGroupedByAlias.get(CELL_SECTOR)).hasSize(2);
            softly.assertThat(kpisGroupedByAlias.get(SECTOR)).hasSize(3);
            softly.assertThat(kpisGroupedByAlias.get(ROLLING_AGGREGATION)).hasSize(2);
            softly.assertThat(kpisGroupedByAlias.get(EXECUTION_ID)).hasSize(1);
            softly.assertThat(kpisGroupedByAlias.get(FDN_AGG)).hasSize(1);
        });
    }

    @Test
    void whenParsingKpiDefinitions_thenSingleQuoteEscapeCharactersAreRemovedFromAggregationElements() {
        final List<String> aggregationElements = KPI_DEFINITIONS.stream().filter(kpi -> kpi.getName().equals("executionid_sum_integer_1440"))
                .map(KpiDefinition::getAggregationElements).collect(toList()).get(0);

        assertThat(aggregationElements.get(1)).isEqualTo("'${param.execution_id}' AS execution_id");
    }

    @Test
    void whenComplexKpi_shouldReturnSourceTable() {
        final KpiDefinition complexKpi = KpiDefinition.builder()
                .withExecutionGroup("test_execution_group")
                .withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_db://kpi_pme_cell_complex_60")
                .build();

        final Set<SourceTable> expected = Set.of(new SourceTable("kpi_db://kpi_pme_cell_complex_60"));
        assertThat(KpiDefinitionParser.getSourceTables(complexKpi)).isEqualTo(expected);
    }

    @Test
    void whenSimpleKpi_thenEmptySetIsReturned() {
        final KpiDefinition simpleKpi = KpiDefinition.builder()
                .withExpression("FIRST(least(fact_table_1.integerColumn0, fact_table_3.integerColumn0))")
                .withInpDataIdentifier(DataIdentifier.of("4G|PM_COUNTERS|SampleCellFDD_1"))
                .build();
        assertThat(KpiDefinitionParser.getSourceTables(simpleKpi)).isEqualTo(Collections.emptySet());
    }
}
