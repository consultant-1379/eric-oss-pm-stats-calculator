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

import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_BEGIN_TIME;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_END_TIME;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_IN_MEMORY;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_POST_AGGREGATION;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.datasource;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias.virtualAlias;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable.virtualTable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter.ParameterBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter.TabularParameterBuilder;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.exception.DatasourceNotFoundException;
import com.ericsson.oss.air.pm.stats.service.DatabaseServiceImpl;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabase;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.definition.ComplexKpiDefinitions;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.complex.table.definition.optional.ComplexDefinitionAggregationElements;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandParameter.OnDemandParameterBuilder;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.OnDemandTabularParameter;
import kpi.model.ondemand.OnDemandTabularParameter.OnDemandTabularParameterBuilder;
import kpi.model.ondemand.ParameterType;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class VirtualDatabaseServiceTest {
    final DatabaseService databaseServiceMock = mock(DatabaseServiceImpl.class);
    final SqlRelationExtractor sqlRelationExtractorMock = mock(SqlRelationExtractor.class);
    final ParameterRepository parameterRepositoryMock = mock(ParameterRepository.class);

    final VirtualDatabaseService objectUnderTest = objectUnderTest(
            databaseServiceMock,
            parameterRepositoryMock,
            sqlRelationExtractorMock
    );

    @Test
    void shouldMergeWithCurrentState() {
        final KpiDefinitionRequest kpiDefinitionRequestMock = mock(KpiDefinitionRequest.class, RETURNS_DEEP_STUBS);

        final ComplexTable tableMock1 = mock(ComplexTable.class, RETURNS_DEEP_STUBS);
        final OnDemandTable tableMock2 = mock(OnDemandTable.class, RETURNS_DEEP_STUBS);

        final ComplexKpiDefinition kpiDefinitionMock1 = mock(ComplexKpiDefinition.class, RETURNS_DEEP_STUBS);
        final ComplexKpiDefinition kpiDefinitionMock2 = mock(ComplexKpiDefinition.class, RETURNS_DEEP_STUBS);

        final OnDemandKpiDefinition kpiDefinitionMock3 = mock(OnDemandKpiDefinition.class, RETURNS_DEEP_STUBS);
        final OnDemandKpiDefinition kpiDefinitionMock4 = mock(OnDemandKpiDefinition.class, RETURNS_DEEP_STUBS);

        when(databaseServiceMock.findAllOutputTablesWithoutPartition()).thenReturn(List.of("kpi_cell_guid_60", "kpi_cell_guid_1440"));
        when(databaseServiceMock.findColumnNamesForTable("kpi_cell_guid_60")).thenReturn(List.of(
                "column_1", "column_2", AGGREGATION_BEGIN_TIME.getName(), AGGREGATION_END_TIME.getName()
        ));
        when(databaseServiceMock.findColumnNamesForTable("kpi_cell_guid_1440")).thenReturn(List.of(
                "column_3", "column_4", AGGREGATION_BEGIN_TIME.getName(), AGGREGATION_END_TIME.getName()
        ));

        when(kpiDefinitionRequestMock.tables()).thenReturn(List.of(tableMock1, tableMock2));
        when(kpiDefinitionRequestMock.scheduledComplex().kpiOutputTables()).thenReturn(List.of(tableMock1));
        when(kpiDefinitionRequestMock.onDemand().kpiOutputTables()).thenReturn(List.of(tableMock2));
        when(kpiDefinitionRequestMock.onDemand().tabularParameters()).thenReturn(List.of(onDemandTabularParameter(
                "tabular_parameter_1",
                onDemandParameter("b_column_1", ParameterType.INTEGER), onDemandParameter("b_column_2", ParameterType.STRING)
        )));

        when(kpiDefinitionMock1.expression().value()).thenReturn("select * from db");
        when(kpiDefinitionMock2.expression().value()).thenReturn("select * from db");

        final ComplexKpiDefinitions complexKpiDefinitions = complexDefinitions(kpiDefinitionMock1, kpiDefinitionMock2);
        when(tableMock1.kpiDefinitions()).thenReturn(complexKpiDefinitions);
        when(tableMock1.tableName()).thenReturn("kpi_complex_1440");

        when(kpiDefinitionMock3.expression().value()).thenReturn("select * from db");
        when(kpiDefinitionMock4.expression().value()).thenReturn("select * from db");

        final OnDemandKpiDefinitions onDemandKpiDefinitions = onDemandDefinitions(kpiDefinitionMock3, kpiDefinitionMock4);
        when(tableMock2.kpiDefinitions()).thenReturn(onDemandKpiDefinitions);
        when(tableMock2.tableName()).thenReturn("kpi_on_demand_60");

        when(kpiDefinitionMock1.name().value()).thenReturn("complex_1");
        when(kpiDefinitionMock1.aggregationElements()).thenReturn(complexElements(
                "complex_table.complex_2"
        ));

        when(kpiDefinitionMock2.name().value()).thenReturn("complex_2");
        when(kpiDefinitionMock2.aggregationElements()).thenReturn(complexElements(
                "complex_table.complex_2 AS complex_3"
        ));

        when(kpiDefinitionMock3.name().value()).thenReturn("on_demand_1");
        when(kpiDefinitionMock3.aggregationElements()).thenReturn(onDemandElements(
                "on_demand_table.on_demand_0 AS on_demand_1",
                "'${param.execution_id}' AS on_demand_2"
        ));

        when(kpiDefinitionMock4.name().value()).thenReturn("on_demand_2");
        when(kpiDefinitionMock4.aggregationElements()).thenReturn(onDemandElements(
                "on_demand_table.on_demand_0",
                "on_demand_table.on_demand_0 AS on_demand_1",
                "'${param.execution_id}' AS on_demand_2"
        ));

        when(sqlRelationExtractorMock.extractColumns(kpiDefinitionMock1)).thenReturn(Set.of(relation(KPI_DB, table("table_1"), null)));
        when(sqlRelationExtractorMock.extractColumns(kpiDefinitionMock2)).thenReturn(Set.of(relation(KPI_DB, table("table_2"), null)));
        when(sqlRelationExtractorMock.extractColumns(kpiDefinitionMock3)).thenReturn(Set.of(relation(KPI_DB, table("table_3"), null)));
        when(sqlRelationExtractorMock.extractColumns(kpiDefinitionMock4)).thenReturn(Set.of(relation(KPI_DB, table("table_3"), null)));

        when(parameterRepositoryMock.findAllParameters()).thenReturn(List.of(
                parameter("b_column_3", tabularParameter("tabular_parameter_1")),
                parameter("b_column_4", tabularParameter("tabular_parameter_2"))
        ));

        final VirtualDatabases actual = objectUnderTest.virtualize(kpiDefinitionRequestMock);

        final VirtualDatabase kpiDatabase = VirtualDatabase.empty(KPI_DB);
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_60"), column("column_1"));
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_60"), column("column_2"));
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_60"), AGGREGATION_BEGIN_TIME);
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_60"), AGGREGATION_END_TIME);

        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_1440"), column("column_3"));
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_1440"), column("column_4"));
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_1440"), AGGREGATION_BEGIN_TIME);
        kpiDatabase.addColumn(virtualTable("kpi_cell_guid_1440"), AGGREGATION_END_TIME);

        kpiDatabase.addColumn(virtualTable("kpi_on_demand_60"), column("on_demand_0"));
        kpiDatabase.addColumn(virtualTable("kpi_on_demand_60"), column("on_demand_1"));
        kpiDatabase.addColumn(virtualTable("kpi_on_demand_60"), column("on_demand_2"));
        kpiDatabase.addColumn(virtualTable("kpi_on_demand_60"), AGGREGATION_BEGIN_TIME);
        kpiDatabase.addColumn(virtualTable("kpi_on_demand_60"), AGGREGATION_END_TIME);

        kpiDatabase.addColumn(virtualTable("kpi_complex_1440"), column("complex_1"));
        kpiDatabase.addColumn(virtualTable("kpi_complex_1440"), column("complex_2"));
        kpiDatabase.addColumn(virtualTable("kpi_complex_1440"), column("complex_3"));
        kpiDatabase.addColumn(virtualTable("kpi_complex_1440"), AGGREGATION_BEGIN_TIME);
        kpiDatabase.addColumn(virtualTable("kpi_complex_1440"), AGGREGATION_END_TIME);

        final VirtualDatabase tabularDatabase = VirtualDatabase.empty(TABULAR_PARAMETERS);
        tabularDatabase.addColumn(virtualAlias("tabular_parameter_1"), column("b_column_1"));
        tabularDatabase.addColumn(virtualAlias("tabular_parameter_1"), column("b_column_2"));
        tabularDatabase.addColumn(virtualAlias("tabular_parameter_1"), column("b_column_3"));
        tabularDatabase.addColumn(virtualAlias("tabular_parameter_2"), column("b_column_4"));

        final VirtualDatabases expected = VirtualDatabases.empty();
        expected.registerDatabase(kpiDatabase);
        expected.registerDatabase(VirtualDatabase.toInMemory(KPI_IN_MEMORY, kpiDatabase));
        expected.registerDatabase(VirtualDatabase.toInMemory(KPI_POST_AGGREGATION, kpiDatabase));
        expected.registerDatabase(tabularDatabase);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldFailOnUnknownDatasource() {
        final KpiDefinitionRequest kpiDefinitionRequestMock = mock(KpiDefinitionRequest.class, RETURNS_DEEP_STUBS);

        final ComplexTable tableMock1 = mock(ComplexTable.class, RETURNS_DEEP_STUBS);

        final ComplexKpiDefinition kpiDefinitionMock1 = mock(ComplexKpiDefinition.class, RETURNS_DEEP_STUBS);

        when(databaseServiceMock.findAllOutputTablesWithoutPartition()).thenReturn(Collections.emptyList());

        when(kpiDefinitionMock1.expression().value()).thenReturn("select * from db");

        final ComplexKpiDefinitions complexKpiDefinitions = complexDefinitions(kpiDefinitionMock1);
        when(tableMock1.kpiDefinitions()).thenReturn(complexKpiDefinitions);
        when(tableMock1.tableName()).thenReturn("kpi_complex_1440");
        when(kpiDefinitionRequestMock.onDemand().kpiOutputTables()).thenReturn(Collections.emptyList());

        when(kpiDefinitionRequestMock.tables()).thenReturn(List.of(tableMock1));
        when(kpiDefinitionRequestMock.scheduledComplex().kpiOutputTables()).thenReturn(List.of(tableMock1));

        when(sqlRelationExtractorMock.extractColumns(kpiDefinitionMock1)).thenReturn(Set.of(relation(datasource("external"), table("table_1"), null)));

        assertThatThrownBy(() -> objectUnderTest.virtualize(kpiDefinitionRequestMock))
                .isInstanceOf(DatasourceNotFoundException.class)
                .hasMessage("Datasource 'external' is unknown. Use sources '[kpi_db, kpi_inmemory, kpi_post_agg, tabular_parameters]'");
    }

    static TabularParameter tabularParameter(final String name) {
        final TabularParameterBuilder builder = TabularParameter.builder();
        builder.withName(name);
        return builder.build();
    }

    static Parameter parameter(final String name, final TabularParameter tabularParameter) {
        final ParameterBuilder builder = Parameter.builder();
        builder.withName(name);
        builder.withTabularParameter(tabularParameter);
        return builder.build();
    }

    static OnDemandTabularParameter onDemandTabularParameter(final String name, final OnDemandParameter... onDemandParameters) {
        final OnDemandTabularParameterBuilder builder = OnDemandTabularParameter.builder();
        builder.name(name);
        builder.columns(List.of(onDemandParameters));
        return builder.build();
    }

    static OnDemandParameter onDemandParameter(final String name, final ParameterType type) {
        final OnDemandParameterBuilder builder = OnDemandParameter.builder();
        builder.name(name);
        builder.type(type);
        return builder.build();
    }

    static OnDemandDefinitionAggregationElements onDemandElements(final String... aggregationElements) {
        return OnDemandDefinitionAggregationElements.of(
                Arrays.stream(aggregationElements)
                        .map(OnDemandAggregationElement::of)
                        .collect(Collectors.toList())
        );
    }

    static ComplexDefinitionAggregationElements complexElements(final String... aggregationElements) {
        return ComplexDefinitionAggregationElements.of(
                Arrays.stream(aggregationElements)
                        .map(ComplexAggregationElement::of)
                        .collect(Collectors.toList())
        );
    }

    static OnDemandKpiDefinitions onDemandDefinitions(final OnDemandKpiDefinition... kpiDefinitions) {
        return OnDemandKpiDefinitions.of(List.of(kpiDefinitions));
    }

    static ComplexKpiDefinitions complexDefinitions(final ComplexKpiDefinition... kpiDefinitions) {
        return ComplexKpiDefinitions.of(List.of(kpiDefinitions));
    }

    static VirtualDatabaseService objectUnderTest(
            final DatabaseService databaseServiceMock,
            final ParameterRepository parameterRepositoryMock,
            final SqlRelationExtractor relationExtractorMock
    ) {
        final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());
        final SqlProcessorService sqlProcessorService = new SqlProcessorService(sqlParser, new ExpressionCollector());
        return new VirtualDatabaseService(
                relationExtractorMock,
                parameterRepositoryMock,
                sqlProcessorService,
                databaseServiceMock
        );
    }

}