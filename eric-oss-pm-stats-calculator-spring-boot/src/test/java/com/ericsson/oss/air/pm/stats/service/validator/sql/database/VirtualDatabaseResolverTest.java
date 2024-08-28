/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_POST_AGGREGATION;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias.virtualAlias;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable.virtualTable;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabase;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition.OnDemandKpiDefinitionBuilder;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VirtualDatabaseResolverTest {
    final VirtualDatabaseResolver objectUnderTest = objectUnderTest();

    @ParameterizedTest
    @MethodSource("provideResolveReferencesData")
    void shouldResolveReferences(final int aggregationPeriod, final ResolutionResult expected, final VirtualDatabases virtualDatabases) {
        final ResolutionResult actual = objectUnderTest.resolveReferences(virtualDatabases, expected.kpiDefinition(), aggregationPeriod);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideResolveReferencesData() {
        final Arguments argument1 = arguments(
                1_440,
                resolutionResult(
                        onDemand(
                                "rolling_sum_integer_1440",
                                "SUM(kpi_cell_guid_simple_1440.sum_Integer_1440_simple) FROM kpi_db://kpi_cell_guid_simple_1440",
                                List.of(onDemandElement("kpi_cell_guid_simple_1440.nodeFDN")),
                                List.of()
                        ),
                        resolvedResolutions(
                                resolution(
                                        relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                        reference(null, table("kpi_cell_guid_simple_1440"), column("nodeFDN"), null)),
                                resolution(
                                        relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                        reference(null, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null))
                        ),
                        unResolvedResolutions()
                ),
                virtualDatabases(virtualDatabase(KPI_DB, tableColumns(
                        virtualTable("kpi_cell_guid_simple_1440"),
                        column("sum_Integer_1440_simple"), column("agg_column_0"), column("nodeFDN"))
                ))
        );

        final Arguments argument2 = arguments(
                1_440,
                resolutionResult(
                        onDemand(
                                "first_float_operator_1440_post_aggregation",
                                "FIRST(rolling_aggregation.rolling_sum_integer_1440) / FIRST(rolling_aggregation.rolling_max_integer_1440) " +
                                        "FROM kpi_post_agg://rolling_aggregation",
                                List.of(onDemandElement("rolling_aggregation.agg_column_0")),
                                List.of()
                        ), resolvedResolutions(
                                resolution(
                                        relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                        reference(null, table("rolling_aggregation"), column("rolling_sum_integer_1440"), null)
                                ),
                                resolution(
                                        relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                        reference(null, table("rolling_aggregation"), column("agg_column_0"), null)
                                )
                        ),
                        unResolvedResolutions(
                                resolution(
                                        null,
                                        reference(null, table("rolling_aggregation"), column("rolling_max_integer_1440"), null))
                        )
                ),
                virtualDatabases(virtualDatabase(KPI_POST_AGGREGATION, tableColumns(
                        virtualAlias("rolling_aggregation", 1_440),
                        column("agg_column_0"), column("rolling_sum_integer_1440"))
                ))
        );

        final Arguments argument3 = arguments(
                1_440,
                resolutionResult(
                        onDemand("executionid_sum_integer_1440",
                                "SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60",
                                List.of(
                                        onDemandElement("kpi_simple_60.agg_column_0"),
                                        onDemandElement("'${param.execution_id}' AS execution_id")
                                ),
                                List.of()
                        ),
                        resolvedResolutions(
                                resolution(
                                        relation(KPI_DB, table("kpi_simple_60"), null),
                                        reference(null, null, null, alias("execution_id"))
                                ),
                                resolution(
                                        relation(KPI_DB, table("kpi_simple_60"), null),
                                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null)
                                )
                        ),
                        unResolvedResolutions(
                                resolution(
                                        null,
                                        reference(null, table("kpi_simple_60"), column("integer_simple"), null)
                                )
                        )
                ),
                virtualDatabases(virtualDatabase(KPI_DB, tableColumns(
                        virtualTable("kpi_simple_60"),
                        column("executionid_sum_integer_1440"), column("random_integer_simple"), column("execution_id"), column("agg_column_0"))
                ))
        );

        return Stream.of(argument1, argument2, argument3);
    }

    static List<RelationReferenceResolution> resolvedResolutions(final RelationReferenceResolution... resolutions) {
        return List.of(resolutions);
    }

    static List<RelationReferenceResolution> unResolvedResolutions(final RelationReferenceResolution... resolutions) {
        return List.of(resolutions);
    }

    static RelationReferenceResolution resolution(final Relation relation, final Reference reference) {
        return new RelationReferenceResolution(relation, reference);
    }

    static ResolutionResult resolutionResult(
            final KpiDefinition kpiDefinition,
            final List<? extends RelationReferenceResolution> resolved,
            final List<? extends RelationReferenceResolution> unresolved
    ) {
        final ResolutionResult resolutionResult = new ResolutionResult(kpiDefinition);

        resolved.forEach(resolution -> {
            resolutionResult.addResolvedResolution(resolution.relation().orElse(null), resolution.reference());
        });

        unresolved.forEach(resolution -> {
            resolutionResult.addUnresolvedResolution(resolution.relation().orElse(null), resolution.reference());
        });

        return resolutionResult;
    }

    static OnDemandAggregationElement onDemandElement(final String element) {
        return OnDemandAggregationElement.of(element);
    }

    static OnDemandFilterElement onDemandFilter(final String filter) {
        return OnDemandFilterElement.of(filter);
    }

    static OnDemandKpiDefinition onDemand(
            final String name, final String expression, final List<OnDemandAggregationElement> elements, final List<OnDemandFilterElement> filters
    ) {
        final OnDemandKpiDefinitionBuilder builder = OnDemandKpiDefinition.builder();
        builder.name(OnDemandDefinitionName.of(name));
        builder.expression(OnDemandDefinitionExpression.of(expression));
        builder.filters(OnDemandDefinitionFilters.of(filters));
        builder.aggregationElements(OnDemandDefinitionAggregationElements.of(elements));
        return builder.build();
    }

    static VirtualReferenceColumns tableColumns(final VirtualTableReference tableReference, final Column... columns) {
        return new VirtualReferenceColumns(tableReference, Set.of(columns));
    }

    static VirtualDatabase virtualDatabase(final Datasource datasource, final VirtualReferenceColumns... virtualReferenceColumns) {
        final VirtualDatabase virtualDatabase = VirtualDatabase.empty(datasource);

        for (final VirtualReferenceColumns tableColumn : virtualReferenceColumns) {
            for (final Column column : tableColumn.columns()) {
                virtualDatabase.addColumn(tableColumn.tableReference(), column);
            }
        }

        return virtualDatabase;
    }

    static VirtualDatabases virtualDatabases(final VirtualDatabase... virtualDatabases) {
        final VirtualDatabases result = VirtualDatabases.empty();
        for (final VirtualDatabase database : virtualDatabases) {
            result.registerDatabase(database);
        }
        return result;
    }

    static VirtualDatabaseResolver objectUnderTest() {
        final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());

        final SqlRelationExtractor sqlRelationExtractor = new SqlRelationExtractor(sqlParser);
        final ExpressionCollector expressionCollector = new ExpressionCollector(new LeafCollector());
        final SqlProcessorService sqlProcessorService = new SqlProcessorService(sqlParser, expressionCollector);
        final SqlExtractorService sqlExtractorService = new SqlExtractorService(sqlProcessorService);
        return new VirtualDatabaseResolver(sqlExtractorService, sqlRelationExtractor);
    }

    @Data
    @Accessors(fluent = true)
    static final class VirtualReferenceColumns {
        final VirtualTableReference tableReference;
        final Set<Column> columns;
    }

}