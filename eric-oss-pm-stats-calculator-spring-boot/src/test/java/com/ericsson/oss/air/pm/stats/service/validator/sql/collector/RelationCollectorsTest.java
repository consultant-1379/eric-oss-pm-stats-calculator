/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.collector;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_POST_AGGREGATION;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.datasource;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.RelationCollectors;

import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RelationCollectorsTest {
    final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());

    @ParameterizedTest
    @MethodSource("provideCollectRelationsData")
    void shouldCollectRelations(final ExpressionAttribute expressionAttribute, final Collection<Relation> expected) {
        final LogicalPlan logicalPlan = sqlParser.parsePlan(expressionAttribute);
        final Set<Relation> actual = RelationCollectors.collect(logicalPlan);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> provideCollectRelationsData() {
        return Stream.of(
                arguments(
                        expression(
                                "FIRST(rolling_aggregation.rolling_sum_integer_1440) / FIRST(rolling_aggregation.rolling_max_integer_1440) " +
                                        "FROM kpi_post_agg://rolling_aggregation"),
                        relations(relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null))
                ),
                arguments(
                        expression(
                                "FIRST(kpi_cell_guid_simple_1440.sum_Integer_1440_simple) " +
                                        "FROM kpi_db://kpi_cell_guid_simple_1440 " +
                                        "INNER JOIN dim_ds_0://dim_table_2 AS alias " +
                                        "   ON kpi_cell_guid_simple_1440.nodeFDN = alias.agg_column_0 " +
                                        "WHERE kpi_cell_guid_simple_1440.nodeFDN > 0"
                        ),
                        relations(
                                relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                relation(datasource("dim_ds_0"), table("dim_table_2"), alias("alias"))
                        )
                ),
                arguments(
                        expression(
                                "FIRST(kpi_cell_guid_simple_1440.sum_Integer_1440_simple) " +
                                        "FROM kpi_db://kpi_cell_guid_simple_1440 " +
                                        "INNER JOIN dim_ds_0://dim_table_2 " +
                                        "   ON kpi_cell_guid_simple_1440.nodeFDN = alias.agg_column_0 " +
                                        "WHERE kpi_cell_guid_simple_1440.nodeFDN > 0"
                        ),
                        relations(
                                relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                relation(datasource("dim_ds_0"), table("dim_table_2"), null)
                        )
                )
        );
    }

    static Set<Relation> relations(final Relation... relations) {
        return Set.of(relations);
    }

    static ExpressionAttribute expression(final String expression) {
        return OnDemandDefinitionExpression.of(expression);
    }
}