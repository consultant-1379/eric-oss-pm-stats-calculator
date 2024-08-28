/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.sql;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.sqlProcessorDelegator;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class SqlProcessorDelegatorTest {
    SqlProcessorDelegator objectUnderTest = sqlProcessorDelegator(new SparkSqlParser());

    @Test
    void shouldObtainFilters() {
        final Set<Reference> actual = objectUnderTest.filters(List.of(
                Filter.of("agg_column_0 > 550"),
                Filter.of("kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'"),
                Filter.of("TO_DATE(kpi_db://kpi_cell_guid_1440.COALESCE(aggregation_begin_time, NULL), 'yyyy-MM-dd') = nodeFDN")
        ));

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                reference(null, null, column("agg_column_0"), null),
                reference(KPI_DB, table("kpi_cell_guid_1440"), column("aggregation_begin_time"), null),
                reference(null, null, column("nodeFDN"), null)
        );
    }

    @Test
    void shouldObtainAggregationElements() {
        final Set<Reference> actual = objectUnderTest.aggregationElements(List.of(
                "fact_table_0.agg_column_0",
                "fact_table_0.agg_column_1",
                "fact_table_0.agg_column_2 AS agg_column_3",
                "kpi_cell_guid_simple_1440.nodeFDN AS agg_column_4",
                "rolling_aggregation.agg_column_0",
                "'${param.execution_id}' AS execution_id"
        ));

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                reference(null, table("fact_table_0"), column("agg_column_0"), null),
                reference(null, table("fact_table_0"), column("agg_column_1"), null),
                reference(null, table("fact_table_0"), column("agg_column_2"), alias("agg_column_3")),
                reference(null, table("rolling_aggregation"), column("agg_column_0"), null),
                reference(null, table("kpi_cell_guid_simple_1440"), column("nodeFDN"), alias("agg_column_4")),
                reference(null, null, null, alias("execution_id"))
        );
    }

    @TestFactory
    Stream<DynamicTest> shouldVerifyJsonPaths() {
        final String sql1 = "SUM(fact_table.pmCounters.integerColumn0)";
        final String sql2 = "SUM(fact_table.pmCounters.integerArrayColumn0[1] + fact_table.pmCounters.integerArrayColumn0[3])";
        final String sql3 = "SUM(z_new_fact_table_1.pmCounters.floatColumn0.counterValue)";
        final String sql4 = "ARRAY_INDEX_SUM(TRANSFORM(limited_agg.pmCounters.integerArrayColumn, x -> x * limited_agg.pmCounters.integerColumn1))";

        return Stream.of(
                dynamicTest(sql1, () -> {
                    final Set<JsonPath> actual = objectUnderTest.jsonPaths(sql1);
                    Assertions.assertThat(actual).first().isEqualTo(
                            JsonPath.of(List.of("fact_table", "pmCounters", "integerColumn0"))
                    );
                }),
                dynamicTest(sql2, () -> {
                    final Set<JsonPath> actual = objectUnderTest.jsonPaths(sql2);
                    Assertions.assertThat(actual).first().isEqualTo(
                            JsonPath.of(List.of("fact_table", "pmCounters", "integerArrayColumn0"))
                    );
                }),
                dynamicTest(sql3, () -> {
                    final Set<JsonPath> actual = objectUnderTest.jsonPaths(sql3);
                    Assertions.assertThat(actual).first().isEqualTo(
                            JsonPath.of(List.of("z_new_fact_table_1", "pmCounters", "floatColumn0", "counterValue"))
                    );
                }),
                dynamicTest(sql4, () -> {
                    final Set<JsonPath> actual = objectUnderTest.jsonPaths(sql4);
                    Assertions.assertThat(actual).containsExactlyInAnyOrder(
                            JsonPath.of(List.of("limited_agg", "pmCounters", "integerArrayColumn")),
                            JsonPath.of(List.of("limited_agg", "pmCounters", "integerColumn1"))
                    );
                })
        );
    }
}