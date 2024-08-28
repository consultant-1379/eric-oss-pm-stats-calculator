/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPathReference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.api.table.definition.api.NameAttribute;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class SqlExtractorServiceTest {
    private final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());
    private final SqlProcessorService sqlProcessorService = new SqlProcessorService(sqlParser, new ExpressionCollector(new LeafCollector()));

    SqlExtractorService objectUnderTest = new SqlExtractorService(sqlProcessorService);

    @TestFactory
    Stream<DynamicTest> verifyColumnsExtracted() {
        final KpiDefinitionRequest kpiDefinition = loadDefinition("NewRequiredKpis.json");

        final Set<kpi.model.api.table.definition.KpiDefinition> definitions = kpiDefinition.definitions();
        assertThat(definitions).isNotEmpty();

        final Map<String, Set<Reference>> expectedDefinitionTableColumns = ofEntries(
                entry("rolling_sum_integer_1440", Set.of(
                        reference(null, table("kpi_cell_guid_simple_1440"), column("nodeFDN"), References.alias("agg_column_0")),
                        reference(null, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null)
                )),
                entry("rolling_max_integer_1440", Set.of(
                        reference(null, table("kpi_cell_guid_simple_1440"), column("nodeFDN"), References.alias("agg_column_0")),
                        reference(null, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null)
                )),
                entry("first_float_operator_1440_post_aggregation", Set.of(
                        reference(null, table("rolling_aggregation"), column("agg_column_0"), null),
                        reference(null, table("rolling_aggregation"), column("rolling_sum_integer_1440"), null),
                        reference(null, table("rolling_aggregation"), column("rolling_max_integer_1440"), null)
                )),
                entry("executionid_sum_integer_1440", Set.of(
                        reference(null, null, null, References.alias("execution_id")),
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null),
                        reference(null, table("kpi_simple_60"), column("integer_simple"), null)
                )),
                entry("first_integer_aggregate_slice_1440", Set.of(
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null),
                        reference(null, table("kpi_simple_60"), column("integer_array_simple"), null)
                )),
                entry("first_integer_operator_60_stage2", Set.of(
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null),
                        reference(null, table("kpi_simple_60"), column("integer_simple"), null),
                        reference(null, table("kpi_simple_60"), column("float_simple"), null)
                )),
                entry("first_integer_operator_60_stage3", Set.of(
                        reference(null, table("cell_guid"), column("agg_column_0"), null),
                        reference(null, table("cell_guid"), column("first_integer_operator_60_stage2"), null)
                )),
                entry("first_integer_operator_60_stage4", Set.of(
                        reference(null, table("cell_guid"), column("agg_column_0"), null),
                        reference(null, table("cell_guid"), column("first_integer_operator_60_stage2"), null),
                        reference(null, table("cell_guid"), column("first_integer_operator_60_stage3"), null)
                )),
                entry("first_float_divideby0_60", Set.of(
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null),
                        reference(null, table("kpi_simple_60"), column("agg_column_1"), null),
                        reference(null, table("kpi_simple_60"), column("integer_simple"), null)
                )),
                entry("first_integer_dim_enrich_1440", Set.of(
                        reference(null, table("kpi_cell_guid_simple_1440"), column("nodeFDN"), null),
                        reference(null, table("cell_configuration_test"), column("nodeFDN"), null),
                        reference(null, table("cell_configuration_test"), column("moFdn"), null),
                        reference(null, table("cell_configuration_test"), column("execution_id"), null),
                        reference(null, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null),
                        reference(KPI_DB, table("kpi_cell_guid_simple_1440"), column("aggregation_begin_time"), null),
                        reference(TABULAR_PARAMETERS, table("cell_configuration_test"), column("execution_id"), null)
                )),
                entry("first_float_dim_enrich_1440", Set.of(
                        reference(null, table("kpi_cell_guid_simple_1440"), column("nodeFDN"), null),
                        reference(null, table("cell_configuration_test_2"), column("nodeFDN"), null),
                        reference(null, table("cell_configuration_test_2"), column("moFdn"), null),
                        reference(null, table("cell_configuration_test_2"), column("execution_id"), null),
                        reference(null, table("kpi_cell_guid_simple_1440"), column("sum_integer_arrayindex_1440_simple"), null),
                        reference(null, table("cell_configuration_test_2"), column("integer_property"), null),
                        reference(KPI_DB, table("kpi_cell_guid_simple_1440"), column("aggregation_begin_time"), null),
                        reference(TABULAR_PARAMETERS, table("cell_configuration_test_2"), column("execution_id"), null)
                )),
                entry("max_integer_1440_kpidb", Set.of(
                        reference(null, table("kpi_sector_60"), column("agg_column_0"), null),
                        reference(KPI_DB, table("kpi_sector_60"), column("aggregation_begin_time"), null),
                        reference(null, table("kpi_sector_60"), column("sum_integer_60_join_kpidb"), null)
                )),
                entry("sum_integer_60_join_kpidb", Set.of(
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null),
                        reference(null, table("kpi_simple_60"), column("integer_simple"), null),
                        reference(KPI_DB, table("kpi_simple_60"), column("aggregation_begin_time"), null)
                )),
                entry("integer_simple", Set.of(
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_0"), null),
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_1"), null)
                )),
                entry("float_array_simple", Set.of(
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_0"), null),
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_1"), null)
                )),
                entry("integer_array_simple", Set.of(
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_0"), null),
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_1"), null)
                )),
                entry("float_simple", Set.of(
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_0"), null),
                        reference(null, table("a_new_very_simple_kpi"), column("agg_column_1"), null)
                )),
                entry("sum_Integer_1440_simple", Set.of(
                        reference(null, table("fact_table_0"), column("agg_column_0"), null)
                )),
                entry("sum_integer_arrayindex_1440_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("first_calculate_percentile_value_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("first_calculate_percentile_bin_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("first_calculate_bin_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("first_update_null_time_advanced_kpis_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("first_calculate_after_float_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("first_add_integer_to_array_with_limit_simple", Set.of(
                        reference(null, table("fact_table_2"), column("nodeFDN"), null)
                )),
                entry("sum_float_1440_simple", Set.of(
                        reference(null, table("fact_table_1"), column("agg_column_0"), null),
                        reference(null, table("fact_table_1"), column("agg_column_1"), null),
                        reference(null, table("fact_table_1"), column("agg_column_2"), null)
                ))
        );

        final Map<String, List<JsonPath>> jsonPaths = ofEntries(
                entry("integer_simple", List.of(
                        jsonPath("a_new_very_simple_kpi", "pmCounters", "integerColumn0")
                )),
                entry("float_array_simple", List.of(
                        jsonPath("a_new_very_simple_kpi", "pmCounters", "floatArrayColumn0")
                )),
                entry("integer_array_simple", List.of(
                        jsonPath("a_new_very_simple_kpi", "pmCounters", "integerArrayColumn0")
                )),
                entry("float_simple", List.of(
                        jsonPath("a_new_very_simple_kpi", "pmCounters", "floatColumn0")
                )),
                entry("sum_Integer_1440_simple", List.of(
                        jsonPath("fact_table_0", "integerColumn0")
                )),
                entry("sum_integer_arrayindex_1440_simple", List.of(
                        jsonPath("fact_table_2", "integerArrayColumn0")
                )),
                entry("first_calculate_percentile_value_simple", List.of(
                        jsonPath("fact_table_2", "floatArrayColumn0")
                )),
                entry("first_calculate_percentile_bin_simple", List.of(
                        jsonPath("fact_table_2", "integerArrayColumn0")
                )),
                entry("first_calculate_bin_simple", List.of(
                        jsonPath("fact_table_2", "integerColumn0")
                )),
                entry("first_degradation_threshold_simple", List.of(
                        jsonPath("fact_table_2", "floatArrayColumn0")
                )),
                entry("first_update_null_time_advanced_kpis_simple", List.of(
                        jsonPath("fact_table_2", "integerArrayColumn0")
                )),
                entry("first_calculate_after_float_simple", List.of(
                        jsonPath("fact_table_2", "floatColumn0")
                )),
                entry("first_add_integer_to_array_with_limit_simple", List.of(
                        jsonPath("fact_table_2", "integerArrayColumn0")
                )),
                entry("sum_float_1440_simple", List.of(
                        jsonPath("fact_table_1", "floatColumn0")
                ))
        );

        return definitions.stream().map(definition -> {
            final NameAttribute name = definition.name();
            final Set<Reference> expectedReferences = expectedDefinitionTableColumns.get(name.value());

            final String displayName = String.format(
                    "For definition '%s' expected table columns '%s'", name.value(), expectedReferences
            );
            return dynamicTest(displayName, () -> {
                if (definition instanceof SimpleKpiDefinition) {
                    final SimpleKpiDefinition simpleKpiDefinition = (SimpleKpiDefinition) definition;
                    final JsonPathReference jsonPathReference = objectUnderTest.extractColumns(simpleKpiDefinition);

                    final List<JsonPath> jsonPath = jsonPaths.get(name.value());
                    assertThat(jsonPath).isNotEmpty();

                    assertThat(jsonPathReference.jsonPaths()).containsExactlyInAnyOrderElementsOf(jsonPath);
                    assertThat(jsonPathReference.references()).containsExactlyInAnyOrderElementsOf(expectedReferences);
                } else if (definition instanceof OnDemandKpiDefinition) {
                    final OnDemandKpiDefinition onDemandKpiDefinition = (OnDemandKpiDefinition) definition;
                    final Set<Reference> actual = objectUnderTest.extractColumns(onDemandKpiDefinition);
                    assertThat(actual).containsExactlyInAnyOrderElementsOf(expectedReferences);
                } else {
                    final ComplexKpiDefinition complexKpiDefinition = (ComplexKpiDefinition) definition;
                    final Set<Reference> actual = objectUnderTest.extractColumns(complexKpiDefinition);
                    assertThat(actual).containsExactlyInAnyOrderElementsOf(expectedReferences);
                }
            });
        });
    }

    @Nested
    class ExtractFromEntity {
        final KpiDefinitionAdapter kpiDefinitionAdapter = new KpiDefinitionAdapter(new SchemaDetailCache());

        @Test
        void shouldExtractSimple() {
            final KpiDefinitionEntity entity = byEntityName("first_add_integer_to_array_with_limit_simple");
            final JsonPathReference actual = objectUnderTest.extractColumnsFromSimple(entity);

            assertThat(actual.jsonPaths()).containsExactlyInAnyOrder(jsonPath("fact_table_2", "integerArrayColumn0"));

        }

        @Test
        void shouldExtractOnDemand() {
            final KpiDefinitionEntity entity = byEntityName("dim_enrich_1");
            final Set<Reference> actual = objectUnderTest.extractColumnsFromOnDemand(entity);

            assertThat(actual).containsExactlyInAnyOrder(
                    reference(null, table("cell_configuration_test"), column("tabular_parameter_dimension"), null),
                    reference(null, table("cell_configuration_test"), column("tabular_parameter_agg"), alias("agg_column_0"))
            );
        }

        @Test
        void shouldExtractComplex() {
            final KpiDefinitionEntity entity = byEntityName("sum_integer_60_complex");
            final Set<Reference> actual = objectUnderTest.extractColumnsFromComplex(entity);

            assertThat(actual).containsExactlyInAnyOrder(
                    reference(null, table("kpi_simple_60"), column("agg_column_1"), null),
                    reference(null, table("kpi_simple_60"), column("agg_column_0"), null),
                    reference(null, table("kpi_simple_60"), column("integer_simple"), null)
            );
        }

        private KpiDefinitionEntity byEntityName(final String definitionName) {
            return kpiDefinitionAdapter.toListOfEntities(loadDefinition("json/single_element_request.json")).stream()
                    .filter(entity -> definitionName.equals(entity.name()))
                    .collect(onlyElement());
        }
    }

    static JsonPath jsonPath(final String... paths) {
        return JsonPath.of(List.of(paths));
    }

    static KpiDefinitionRequest loadDefinition(final String resourceName) {
        final String jsonContent = JsonLoaders.load(resourceName);
        return Serialization.deserialize(jsonContent, KpiDefinitionRequest.class);
    }
}