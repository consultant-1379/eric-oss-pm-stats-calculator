/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;

import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SqlProcessorServiceTest {
    final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());
    final SqlProcessorService objectUnderTest = new SqlProcessorService(sqlParser, new ExpressionCollector());

    @Test
    void shouldRaiseAnalysisException_onUnknownFunction() {
        final OnDemandDefinitionExpression expression = OnDemandDefinitionExpression.of(
                "FARST(TRANSFORM(kpi_simple_60.integer_array_simple , x -> x * kpi_simple_60.integer_simple)) " +
                        "FROM kpi_db://kpi_simple_60"
        );

        Assertions.assertThatThrownBy(() -> objectUnderTest.parsePlan(expression))
                .isExactlyInstanceOf(AnalysisException.class)
                .hasMessage(
                        "Undefined function: FARST. This function is neither a built-in/temporary function, " +
                                "nor a persistent function that is qualified as spark_catalog.default.farst.; line 1 pos 7"
                );
    }

    @MethodSource("provideExtractAggregationElementsData")
    @ParameterizedTest(name = "[{index}] Aggregation element ''{0}'' is parsed to ''{1}''")
    void shouldExtractAggregationElements(final AggregationElement aggregationElement, final Reference expected) {
        final Set<Reference> actual = objectUnderTest.extractAggregationElementTableColumns(List.of(aggregationElement));
        Assertions.assertThat(actual).containsExactlyInAnyOrder(expected);
    }

    @MethodSource("provideExtractFiltersData")
    @ParameterizedTest(name = "[{index}] Filter ''{0}'' is parsed to ''{1}''")
    void shouldExtractFilters(final FilterElement filter, final Collection<Reference> expected) {
        final Set<Reference> actual = objectUnderTest.extractFilterTableColumns(List.of(filter));
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> provideExtractFiltersData() {
        return Stream.of(
                arguments(
                        filter("agg_column_0 > 550"),
                        references(reference(null, null, column("agg_column_0"), null))
                ),
                arguments(
                        filter("kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'"),
                        references(reference(KPI_DB, table("kpi_cell_guid_1440"), column("aggregation_begin_time"), null))
                ),
                arguments(
                        filter("TO_DATE(kpi_db://kpi_cell_guid_1440.COALESCE(aggregation_begin_time, NULL), 'yyyy-MM-dd') = nodeFDN"),
                        references(
                                reference(KPI_DB, table("kpi_cell_guid_1440"), column("aggregation_begin_time"), null),
                                reference(null, null, column("nodeFDN"), null)
                        )
                ),
                arguments(
                        filter("kpi_db://kpi_simple_60.aggregation_begin_time BETWEEN (date_trunc('hour', TO_TIMESTAMP('${param.start_date_time}')) - interval 1 day) and date_trunc('hour', TO_TIMESTAMP('${param.end_date_time}'))"),
                        references(
                                reference(KPI_DB, table("kpi_simple_60"), column("aggregation_begin_time"), null)
                        )
                )
        );
    }

    static Stream<Arguments> provideExtractAggregationElementsData() {
        return Stream.of(
                arguments(
                        aggregationElement("kpi_simple_60.agg_column_0"),
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null)
                ),
                arguments(
                        aggregationElement("kpi_simple_60.agg_column_0 AS agg_column_1"),
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), alias("agg_column_1"))
                ),
                arguments(
                        aggregationElement("'${param.execution_id}' AS execution_id"),
                        reference(null, null, null, alias("execution_id"))
                ),
                arguments(
                        aggregationElement("FDN_PARSE(kpi_simple_60.nodeFDN, \"SubNetwork\") AS subnet"),
                        reference(null, table("kpi_simple_60"), column("nodeFDN"), alias("subnet"), "FDN_PARSE(kpi_simple_60.nodeFDN, 'SubNetwork')")
                )
        );
    }

    static List<Reference> references(final Reference... references) {
        return List.of(references);
    }

    static FilterElement filter(final String filter) {
        return OnDemandFilterElement.of(filter);
    }

    static AggregationElement aggregationElement(final String aggregationElement) {
        return OnDemandAggregationElement.of(aggregationElement);
    }
}