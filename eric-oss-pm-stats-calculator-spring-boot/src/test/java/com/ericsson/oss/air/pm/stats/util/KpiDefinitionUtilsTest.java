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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.getAggregationElementsWithKpiSource;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.getDefaultElementsAndTypes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import org.apache.commons.text.StringSubstitutor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KpiDefinitionUtilsTest {

    private static final String CELL_GUID = "cell_guid";

    @Nested
    class IsDefaultAggregationPeriod {

        @Test
        void whenDefaultAggPeriod_thenReturnsTrue() {
            assertThat(KpiDefinitionUtils.isDefaultAggregationPeriod(DEFAULT_AGGREGATION_PERIOD)).isTrue();
        }

        @Test
        void whenNonDefaultAggPeriod_thenReturnsFalse() {
            assertThat(KpiDefinitionUtils.isDefaultAggregationPeriod(DAILY_AGGREGATION_PERIOD_IN_MINUTES)).isFalse();
        }

        @Test
        void whenNullAggPeriod_thenReturnsFalse() {
            assertThat(KpiDefinitionUtils.isDefaultAggregationPeriod(null)).isTrue();
        }

    }

    @Test
    void whenConvertingKpiDefinitionsToRawDefinitions_thenTheSameSizeCollectionIsReturned() {
        final KpiDefinition kpi = KpiDefinition.builder()
                .withName("brackets_kpi")
                .withAlias(CELL_GUID)
                .withExpression(
                        "100 * (counters_cell.counter1 + counters_cell.counter2 + counters_cell.counter3 + counters_cell.counter4 + counters_cell.counter5) FROM pm_stats://counters_cell")
                .withAggregationType("SUM")
                .withAggregationPeriod("1440")
                .withAggregationElements(Collections.singletonList("guid"))
                .build();

        final List<KpiDefinition> kpiDefinitionList = new ArrayList<>();
        kpiDefinitionList.add(kpi);

        assertThat(KpiDefinitionUtils.convertKpiDefinitionsToRawDefinitionSet(kpiDefinitionList)).hasSize(1);
    }

    @Test
    void whenConvertDefinitionsToKpiDefinitions_thenOutputMapHasCorrectDefinitionFormat1() {
        final Definition definition1 = new Definition();
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_NAME_KEY, "convert_definitions_to_kpi_definitions_kpi_1");
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_ALIAS_KEY, CELL_GUID);
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_EXPRESSION_KEY, "kpi_expression FROM dummy_source");
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_OBJECT_TYPE_KEY, "INTEGER");
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_AGGREGATION_TYPE_KEY, "SUM");
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_AGGREGATION_PERIOD_KEY, "1440");
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_AGGREGATION_ELEMENTS_KEY, new ArrayList<String>());
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_EXPORTABLE_KEY, true);
        definition1.setAttribute(KpiDefinitionUtils.DEFINITION_FILTER_KEY, new ArrayList<String>());

        final Set<Definition> kpiDefinitionSet1 = new HashSet<>();
        kpiDefinitionSet1.add(definition1);

        final Set<KpiDefinition> result1 = KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(kpiDefinitionSet1);

        assertThat(result1).hasSize(1)
                .first()
                .satisfies(kpiDefinition -> {
                    assertThat(kpiDefinition.getName()).isEqualTo("convert_definitions_to_kpi_definitions_kpi_1");
                    assertThat(kpiDefinition.getAlias()).isEqualTo(CELL_GUID);
                    assertThat(kpiDefinition.getExpression()).isEqualTo("kpi_expression FROM dummy_source");
                    assertThat(kpiDefinition.getObjectType()).isEqualTo("INTEGER");
                    assertThat(kpiDefinition.getAggregationType()).isEqualTo("SUM");
                    assertThat(kpiDefinition.getAggregationPeriod()).isEqualTo("1440");
                    assertThat(kpiDefinition.getAggregationElements()).isEmpty();
                    assertThat(kpiDefinition.getExportable()).isTrue();
                    assertThat(kpiDefinition.getFilter()).isEmpty();
                });
    }

    @Test
    void whenConvertDefinitionsToKpiDefinitions_thenOutputMapHasCorrectDefinitionFormat2() {
        final SchemaDetail schemaDetail = SchemaDetail.builder().withTopic("topic").build();
        final Definition definition2 = new Definition();
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_NAME_KEY, "convert_definitions_to_kpi_definitions_kpi_2");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_ALIAS_KEY, CELL_GUID);
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_EXPRESSION_KEY, "SUM(fact_table_2.integerArrayColumn0[1] + fact_table_2.integerArrayColumn0[3])");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_OBJECT_TYPE_KEY, "INTEGER");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_AGGREGATION_TYPE_KEY, "SUM");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_AGGREGATION_PERIOD_KEY, "1440");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_AGGREGATION_ELEMENTS_KEY, new ArrayList<String>());
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_EXPORTABLE_KEY, false);
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_FILTER_KEY, new ArrayList<String>());
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_SCHEMA_DATA_SPACE_KEY, "dataSpace");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_SCHEMA_CATEGORY_KEY, "category");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_SCHEMA_NAME_KEY, "schema");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_EXECUTION_GROUP_KEY, "group");
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_RELIABILITY_OFFSET_KEY, 1);
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_LOOKBACK_LIMIT_KEY, 2);
        definition2.setAttribute(KpiDefinitionUtils.DEFINITION_SCHEMA_DETAIL, schemaDetail);

        final Set<Definition> kpiDefinitionSet2 = new HashSet<>();
        kpiDefinitionSet2.add(definition2);

        final Set<KpiDefinition> result2 = KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(kpiDefinitionSet2);

        assertThat(result2).hasSize(1)
                .first()
                .satisfies(kpiDefinition -> {
                    assertThat(kpiDefinition.getName()).isEqualTo("convert_definitions_to_kpi_definitions_kpi_2");
                    assertThat(kpiDefinition.getAlias()).isEqualTo(CELL_GUID);
                    assertThat(kpiDefinition.getExpression()).isEqualTo("SUM(fact_table_2.integerArrayColumn0[1] + fact_table_2.integerArrayColumn0[3])");
                    assertThat(kpiDefinition.getObjectType()).isEqualTo("INTEGER");
                    assertThat(kpiDefinition.getAggregationType()).isEqualTo("SUM");
                    assertThat(kpiDefinition.getAggregationPeriod()).isEqualTo("1440");
                    assertThat(kpiDefinition.getAggregationElements()).isEmpty();
                    assertThat(kpiDefinition.getExportable()).isFalse();
                    assertThat(kpiDefinition.getFilter()).isEmpty();
                    assertThat(kpiDefinition.getInpDataIdentifier()).isEqualTo(DataIdentifier.of("dataSpace|category|schema"));
                    assertThat(kpiDefinition.getExecutionGroup()).isEqualTo("group");
                    assertThat(kpiDefinition.getDataReliabilityOffset()).isEqualTo(1);
                    assertThat(kpiDefinition.getDataLookbackLimit()).isEqualTo(2);
                    assertThat(kpiDefinition.getSchemaDetail()).isEqualTo(schemaDetail);
                });
    }

    @Test
    void whenGetAggregationTablesIsCalled_thenAggregationTablesAreReturned() {
        final Set<String> aggregationTables = KpiDefinitionUtils.getAggregationDbTables(List.of("kpi_table_60.agg_1", "kpi_table_1440.agg_2"));
        assertThat(aggregationTables).containsExactlyInAnyOrder("kpi_table_60", "kpi_table_1440");
    }

    @Test
    void whenGetAggregationElementsIsCalledWithSingleWordAggregationElement_thenSingleWordAggregationElementIsReturned() {
        final String aggregationElement = "guid";
        final Set<String> aggregationElements = KpiDefinitionUtils.getAggregationElements(Collections.singletonList(aggregationElement));
        assertThat(aggregationElements).contains(aggregationElement);
    }

    @Test
    void whenGetAggregationElementsIsCalledWithPeriodAggregationElement_thenValueAfterThePeriodIsReturned() {
        final String areaCode = "area_code";

        final Map<String, String> values = new HashMap<>(1);
        values.put("column", areaCode);

        final String aggregationElementTemplate = "cell_guid.${column}";
        final String aggregationElement = StringSubstitutor.replace(aggregationElementTemplate, values);

        final Set<String> aggregationElements = KpiDefinitionUtils.getAggregationElements(Collections.singletonList(aggregationElement));
        assertThat(aggregationElements).contains(areaCode);
    }

    @Test
    void whenGetAggregationElementsIsCalledWithAliasAggregationElement_thenAliasValueIsReturned() {
        final String guid = "guid";

        final Map<String, String> values = new HashMap<>(2);
        values.put("column", "id");
        values.put("alias", guid);

        final String aggregationElementTemplate = "cell_.${column} as ${alias}";
        final String aggregationElement = StringSubstitutor.replace(aggregationElementTemplate, values);

        final Set<String> aggregationElements = KpiDefinitionUtils.getAggregationElements(Collections.singletonList(aggregationElement));
        assertThat(aggregationElements).contains(guid);
    }

    @Test
    void shouldGetAggregationElementsWithKpiSource() {
        final Definition complexKpi0 = new Definition();
        final Definition complexKpi1 = new Definition();

        complexKpi0.setAttribute("aggregation_elements", List.of("kpi_table.agg_column_0", "table.agg_column_1"));
        complexKpi1.setAttribute("aggregation_elements", List.of("kpi_table.fdn AS agg_column_2", "'${param.execution_id}' AS execution_id"));

        final List<AggregationElement> actual = getAggregationElementsWithKpiSource(Set.of(complexKpi0, complexKpi1));
        final List<AggregationElement> expected =
                List.of(AggregationElement.builder()
                                .withExpression("kpi_table.agg_column_0")
                                .withSourceTable("kpi_table")
                                .withSourceColumn("agg_column_0")
                                .withTargetColumn("agg_column_0")
                                .withIsParametric(false)
                                .build(),
                        AggregationElement.builder()
                                .withExpression("kpi_table.fdn AS agg_column_2")
                                .withSourceTable("kpi_table")
                                .withSourceColumn("fdn")
                                .withTargetColumn("agg_column_2")
                                .withIsParametric(false)
                                .build());

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void shouldGetDefaultElementsAndTypes() {
        final Definition complexKpi0 = new Definition();
        final Definition complexKpi1 = new Definition();
        final Definition complexKpi2 = new Definition();

        complexKpi0.setAttribute("aggregation_elements", List.of("kpi_simple_60.agg_column_0", "kpi_simple_60.agg_column_1"));
        complexKpi0.setAttribute("expression", "SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        complexKpi1.setAttribute("aggregation_elements", List.of("kpi_simple_60.fdn AS agg_column_2", "'${param.execution_id}' AS execution_id"));
        complexKpi1.setAttribute("expression", "SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        complexKpi2.setAttribute("aggregation_elements", List.of("dim_table.agg_column_0", "dim_table.agg_column_1"));
        complexKpi2.setAttribute("expression", "SUM(kpi_cell_guid_simple_1440.sum_Integer_1440_simple) FROM kpi_db://kpi_cell_guid_simple_1440");

        final Map<String, KpiDataType> actual = getDefaultElementsAndTypes(Set.of(complexKpi0, complexKpi1, complexKpi2));
        final Map<String, KpiDataType> expected = Map.of(
                "execution_id", KpiDataType.POSTGRES_STRING);

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void whenGetAggregationElementsIsCalledWithNullObject_thenEmptyListIsReturned() {
        assertTrue(KpiDefinitionUtils.getAggregationElements(null).isEmpty());
    }

    @Test
    void whenGetAggregationTablesIsCalledWithNullObject_thenEmptyListIsReturned() {
        assertTrue(KpiDefinitionUtils.getAggregationDbTables(null).isEmpty());
    }

    @Test
    void shouldReplaceTableNameWhenKpiContainsTabularParameter() {
        final KpiDefinition kpiOne = KpiDefinition.builder()
                .withName("testKpi")
                .withExpression("cell_configuration_test.tabular_parameter_test_3 FROM tabular_parameters://cell_configuration_test")
                .withAggregationElements(List.of("cell_configuration_test.tabular_parameter_test_3 AS agg_column_0"))
                .withAggregationType("SUM")
                .withFilter(List.of(Filter.of("cell_configuration_test.nodeFDN > 10")))
                .build();

        final KpiDefinition kpiTwo = KpiDefinition.builder()
                .withName("testKpi2")
                .withExpression("testName.tabular_parameter_test_3 FROM tabular_parameters://testName")
                .withAggregationElements(List.of("testName.tabular_parameter_test_3 AS agg_column_0"))
                .withAggregationType("SUM")
                .build();

        final List<KpiDefinition> actual = KpiDefinitionUtils.changeTableNameIfTabularParameterIsPresent(List.of(kpiOne, kpiTwo),
                List.of("cell_configuration_test_b2531c89", "testName_b2531c89")
        );
        Assertions.assertThat(actual).satisfiesExactlyInAnyOrder(
                kpiDefinition -> {
                    Assertions.assertThat(kpiDefinition.getExpression()).isEqualTo("cell_configuration_test_b2531c89.tabular_parameter_test_3 FROM tabular_parameters://cell_configuration_test_b2531c89");
                    Assertions.assertThat(kpiDefinition.getAggregationElements()).containsExactly("cell_configuration_test_b2531c89.tabular_parameter_test_3 AS agg_column_0");
                    Assertions.assertThat(kpiDefinition.getFilter()).containsExactly(Filter.of("cell_configuration_test_b2531c89.nodeFDN > 10"));
                },
                kpiDefinition -> {
                    Assertions.assertThat(kpiDefinition.getExpression()).isEqualTo("testName_b2531c89.tabular_parameter_test_3 FROM tabular_parameters://testName_b2531c89");
                    Assertions.assertThat(kpiDefinition.getAggregationElements()).containsExactly("testName_b2531c89.tabular_parameter_test_3 AS agg_column_0");
                }
        );
    }

    @Test
    void shouldNotReplaceTableNameWhenNoTabularParameter() {
        final KpiDefinition kpiOne = KpiDefinition.builder()
                .withName("testKpi")
                .withExpression("kpi_cell_guid_simple_1440.sum_Integer_1440_simple FROM kpi_db://kpi_cell_guid_simple_1440")
                .withAggregationType("MAX")
                .build();

        final KpiDefinition kpiTwo = KpiDefinition.builder()
                .withName("testKpi2")
                .withExpression("rolling_aggregation.rolling_sum_integer_1440 / rolling_aggregation.rolling_max_integer_1440 FROM kpi_post_agg://rolling_aggregation")
                .withAggregationType("FIRST")
                .build();

        final List<KpiDefinition> actual = KpiDefinitionUtils.changeTableNameIfTabularParameterIsPresent(List.of(kpiOne, kpiTwo),
                List.of("cell_configuration_test_b2531c89", "cell_configuration_test_2_b2531c89"));

        Assertions.assertThat(actual).satisfiesExactlyInAnyOrder(
                kpiDefinition ->
                        Assertions.assertThat(kpiDefinition.getExpression()).isEqualTo("kpi_cell_guid_simple_1440.sum_Integer_1440_simple FROM kpi_db://kpi_cell_guid_simple_1440"),
                kpiDefinition ->
                        Assertions.assertThat(kpiDefinition.getExpression()).isEqualTo("rolling_aggregation.rolling_sum_integer_1440 / rolling_aggregation.rolling_max_integer_1440 FROM kpi_post_agg://rolling_aggregation")

        );
    }

    @Test
    void shouldThrowExceptionWhenTabularParameterTableNameCannotBeExtracted() {
        final KpiDefinition kpiDefinition = KpiDefinition.builder()
                .withName("testKpi")
                .withExpression("table_name.sum_Integer_1440_simple FROM tabular_parameters:// table_name")
                .withAggregationType("MAX")
                .build();

        Assertions.assertThatThrownBy(() -> KpiDefinitionUtils.changeTableNameIfTabularParameterIsPresent(List.of(kpiDefinition), List.of("table_name")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tabular parameter table name cannot be extracted");
    }

    @Test
    void givenExpressionWithAggregationType_sum_thenOutputExpressionHasCorrectFormat() {
        final String result = KpiDefinitionUtils.makeKpiNameAsSql("make_name_as_sql_kpi", "SUM");

        assertThat(result).isEqualTo("SUM(make_name_as_sql_kpi) AS make_name_as_sql_kpi");
    }
}