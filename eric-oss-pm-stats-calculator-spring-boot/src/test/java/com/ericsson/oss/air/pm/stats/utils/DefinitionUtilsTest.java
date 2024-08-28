/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import static com.ericsson.oss.air.pm.stats.utils.DefinitionUtils.getAggregationElementsWithKpiSource;
import static com.ericsson.oss.air.pm.stats.utils.DefinitionUtils.getAggregationElementsWithTabularParameterDataSource;
import static com.ericsson.oss.air.pm.stats.utils.DefinitionUtils.getDefaultElementsAndTypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefinitionUtilsTest {

    @Test
    void shouldGetDefaultElementsAndTypes() {
        final KpiDefinitionEntityBuilder complexKpiBuilder0 = KpiDefinitionEntity.builder();
        final KpiDefinitionEntityBuilder complexKpiBuilder1 = KpiDefinitionEntity.builder();
        final KpiDefinitionEntityBuilder complexKpiBuilder2 = KpiDefinitionEntity.builder();

        complexKpiBuilder0.withAggregationElements(List.of("kpi_simple_60.agg_column_0", "kpi_simple_60.agg_column_1"));
        complexKpiBuilder0.withExpression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        complexKpiBuilder1.withAggregationElements(List.of("kpi_simple_60.fdn AS agg_column_2", "'${param.execution_id}' AS execution_id"));
        complexKpiBuilder1.withExpression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        complexKpiBuilder2.withAggregationElements(List.of("dim_table.agg_column_0", "dim_table.agg_column_1"));
        complexKpiBuilder2.withExpression("SUM(kpi_cell_guid_simple_1440.sum_Integer_1440_simple) FROM kpi_db://kpi_cell_guid_simple_1440");

        final KpiDefinitionEntity complexKpi0 = complexKpiBuilder0.build();
        final KpiDefinitionEntity complexKpi1 = complexKpiBuilder1.build();
        final KpiDefinitionEntity complexKpi2 = complexKpiBuilder2.build();
        final Map<String, KpiDataType> actual = getDefaultElementsAndTypes(Set.of(complexKpi0, complexKpi1, complexKpi2));
        final Map<String, KpiDataType> expected = Map.of(
                "execution_id", KpiDataType.POSTGRES_STRING
        );

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void shouldGetAggregationElementsWithKpiSource() {
        final KpiDefinitionEntityBuilder complexKpiBuilder0 = KpiDefinitionEntity.builder().withAggregationPeriod(60).withAlias("table");
        final KpiDefinitionEntityBuilder complexKpiBuilder1 = KpiDefinitionEntity.builder().withAggregationPeriod(60).withAlias("table");
        final KpiDefinitionEntityBuilder complexKpiBuilder2 = KpiDefinitionEntity.builder().withAggregationPeriod(60).withAlias("table");

        complexKpiBuilder0.withAggregationElements(List.of("kpi_table.agg_column_0", "table.agg_column_1"));
        complexKpiBuilder1.withAggregationElements(List.of("kpi_table.fdn AS agg_column_2", "'${param.execution_id}' AS execution_id"));
        complexKpiBuilder2.withAggregationElements(List.of("kpi_table_60.fdn"));

        final KpiDefinitionEntity complexKpi0 = complexKpiBuilder0.build();
        final KpiDefinitionEntity complexKpi1 = complexKpiBuilder1.build();
        final KpiDefinitionEntity sameTable = complexKpiBuilder2.build();
        final List<AggregationElement> actual = getAggregationElementsWithKpiSource(Set.of(complexKpi0, complexKpi1, sameTable), Set.of("fdn"));
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
    void shouldGetAggregationElementsWithTabularParameterSource() {
        final KpiDefinitionEntityBuilder complexKpiBuilder0 = KpiDefinitionEntity.builder();
        final KpiDefinitionEntityBuilder complexKpiBuilder1 = KpiDefinitionEntity.builder();

        final KpiDefinitionEntityBuilder onDemandBuilder1 = KpiDefinitionEntity.builder();
        final KpiDefinitionEntityBuilder onDemandBuilder2 = KpiDefinitionEntity.builder();

        complexKpiBuilder0.withAggregationElements(List.of("kpi_simple_60.agg_column_0", "kpi_simple_60.agg_column_1"));
        complexKpiBuilder0.withExpression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");

        complexKpiBuilder1.withAggregationElements(List.of("kpi_simple_60.fdn AS agg_column_2", "'${param.execution_id}' AS execution_id"));
        complexKpiBuilder1.withExpression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");

        onDemandBuilder1.withAggregationElements(List.of("cell_configuration.agg_column_3"));
        onDemandBuilder1.withExpression("cell_configuration_test.tabular_parameter_test_3) FROM tabular_parameters://cell_configuration_test");

        onDemandBuilder2.withAggregationElements(List.of("cell_configuration_2.agg_column_4"));
        onDemandBuilder2.withExpression("cell_configuration_2.tabular_parameter_test_3) FROM tabular_parameters://cell_configuration_2");

        final KpiDefinitionEntity complex0 = complexKpiBuilder0.build();
        final KpiDefinitionEntity complex1 = complexKpiBuilder1.build();
        final KpiDefinitionEntity onDemand1 = onDemandBuilder1.build();
        final KpiDefinitionEntity onDemand2 = onDemandBuilder2.build();

        final List<AggregationElement> actual = getAggregationElementsWithTabularParameterDataSource(Set.of(complex0, complex1, onDemand1, onDemand2));

        final List<AggregationElement> expected =
                List.of(AggregationElement.builder()
                                .withExpression("cell_configuration.agg_column_3")
                                .withSourceTable("cell_configuration")
                                .withSourceColumn("agg_column_3")
                                .withTargetColumn("agg_column_3")
                                .withIsParametric(false)
                                .build(),
                        AggregationElement.builder()
                                .withExpression("cell_configuration_2.agg_column_4")
                                .withSourceTable("cell_configuration_2")
                                .withSourceColumn("agg_column_4")
                                .withTargetColumn("agg_column_4")
                                .withIsParametric(false)
                                .build());

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void shouldGetDefaultTypeForSparkUdf() {
        final KpiDefinitionEntityBuilder complexKpiBuilder = KpiDefinitionEntity.builder();

        complexKpiBuilder.withAggregationElements(List.of("FDN_PARSE(kpi_simple_60.agg_column_0, \"MeContext\") AS fdn"));
        complexKpiBuilder.withExpression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");

        final KpiDefinitionEntity complexKpi = complexKpiBuilder.build();
        final Map<String, KpiDataType> actual = getDefaultElementsAndTypes(Set.of(complexKpi));
        final Map<String, KpiDataType> expected = Map.of(
                "fdn", KpiDataType.POSTGRES_UNLIMITED_STRING
        );

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

}