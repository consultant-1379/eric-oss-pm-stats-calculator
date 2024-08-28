/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregationElementUtilsTest {
    @Mock DatabaseService databaseServiceMock;
    @Mock SchemaRegistryFacade schemaRegistryFacadeMock;
    @Mock ParameterService parameterServiceMock;
    @InjectMocks AggregationElementUtils objectUnderTest;


    @Test
    void whenCollectValidAggregationElementsIsCalledOnATable_thenValidAggregationElementsAreReturned() {
        final KpiDefinitionEntity simpleDefinition0 = KpiDefinitionEntity.builder()
                .withAggregationPeriod(60)
                .withAggregationElements(List.of("table.agg_column_0", "table.agg_column_1"))
                .withSchemaDataSpace("dataSpace")
                .withSchemaCategory("category")
                .withSchemaName("schema1")
                .withFilters(List.of())
                .build();

        final KpiDefinitionEntity simpleDefinition1 =  KpiDefinitionEntity.builder()
                .withAggregationPeriod(60)
                .withAggregationElements(List.of("table.agg_column_2", "table.agg_column_3"))
                .withSchemaDataSpace("dataSpace")
                .withSchemaCategory("category")
                .withSchemaName("schema1")
                .withFilters(List.of())
                .build();

        final KpiDefinitionEntity complexDefinition0 = KpiDefinitionEntity.builder()
                .withAggregationPeriod(60)
                .withAggregationElements(List.of("kpi_table.agg_column_4", "kpi_table.agg_column_5", "'${param.execution_id}' AS execution_id"))
                .withExpression("SUM(kpi_simple_60.simple_kpi1) FROM kpi_db://kpi_simple_60")
                .withExecutionGroup(ExecutionGroup.builder().withId(0).withName("executionGroup").build())
                .withFilters(List.of())
                .build();

        final KpiDefinitionEntity complexDefinition1 = KpiDefinitionEntity.builder()
                .withAggregationPeriod(60)
                .withAggregationElements(List.of("dim_table.agg_column_6", "dim_table.agg_column_7"))
                .withExpression("FIRST(dim_table_0.stringColumn0) FROM dim_ds_0://dim_table_0")
                .withFilters(List.of())
                .build();

        final Set<ParsedSchema> parsedSchemas = new HashSet<>();
        parsedSchemas.add(new AvroSchema(String.format("{\"type\":\"record\",\"name\":\"%s\",\"fields\":[{\"name\":\"agg_column_0\",\"type\":\"string\"},{\"name\":\"agg_column_1\",\"type\":\"int\"},{\"name\":\"agg_column_2\",\"type\":\"string\"}]}", "schema1")));

        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "60", List.of(simpleDefinition0, simpleDefinition1, complexDefinition0, complexDefinition1));

        when(schemaRegistryFacadeMock.getSchemasForDefinitions(any())).thenReturn(parsedSchemas);

        when(databaseServiceMock.findAggregationElementColumnType(any())).thenReturn(
                Map.of("agg_column_4", KpiDataType.POSTGRES_LONG,
                        "agg_column_5", KpiDataType.POSTGRES_FLOAT));

        final Map<String, KpiDataType> actual = objectUnderTest.collectValidAggregationElements(tableCreationInformation);

        verify(schemaRegistryFacadeMock).getSchemasForDefinitions(any());
        verify(databaseServiceMock).findAggregationElementColumnType(any());
        verify(parameterServiceMock).findAggregationElementTypeForTabularParameter(any());
        verifyNoMoreInteractions(schemaRegistryFacadeMock, databaseServiceMock, parameterServiceMock);

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(
                Map.of("agg_column_0", KpiDataType.POSTGRES_STRING,
                        "agg_column_1", KpiDataType.POSTGRES_INTEGER,
                        "agg_column_2", KpiDataType.POSTGRES_STRING,
                        "agg_column_4", KpiDataType.POSTGRES_LONG,
                        "agg_column_5", KpiDataType.POSTGRES_FLOAT,
                        "agg_column_6", KpiDataType.POSTGRES_LONG,
                        "agg_column_7", KpiDataType.POSTGRES_LONG,
                        "execution_id", KpiDataType.POSTGRES_STRING
                )
        );
    }

    @Test
    void whenGetValidComplexAndOnDemandAggregationElementsIsCalled_thenValidComplexAggregationElementIsReturned() {
        final KpiDefinitionEntity complexKpi0 = KpiDefinitionEntity.builder().build();
        final KpiDefinitionEntity complexKpi1 = KpiDefinitionEntity.builder().build();
        final KpiDefinitionEntity complexKpi2 = KpiDefinitionEntity.builder().build();

        complexKpi0.aggregationElements(List.of("kpi_simple_60.agg_column_0", "kpi_simple_60.agg_column_1"));
        complexKpi0.expression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        complexKpi0.filters(List.of());
        complexKpi0.aggregationPeriod(60);

        complexKpi1.aggregationElements(List.of("kpi_simple_60.fdn AS agg_column_2", "'${param.execution_id}' AS execution_id"));
        complexKpi1.expression("SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        complexKpi1.filters(List.of());
        complexKpi1.aggregationPeriod(60);

        complexKpi2.aggregationElements(List.of("dim_table.agg_column_3", "dim_table.agg_column_4"));
        complexKpi2.expression("SUM(kpi_cell_guid_simple_1440.sum_Integer_1440_simple) FROM kpi_db://kpi_cell_guid_simple_1440 INNER JOIN dim_ds_0://dim_table_2 ON kpi_simple_1440.agg_column_0 = dim_table_2.agg_column_0");
        complexKpi2.filters(List.of());
        complexKpi2.aggregationPeriod(60);

        when(databaseServiceMock.findAggregationElementColumnType(any())).thenReturn(
                Map.of("agg_column_0", KpiDataType.POSTGRES_LONG,
                        "agg_column_1", KpiDataType.POSTGRES_STRING,
                        "agg_column_2", KpiDataType.POSTGRES_INTEGER));

        final List<KpiDefinitionEntity> definitionList = List.of(complexKpi0, complexKpi1, complexKpi2);
        final Map<String, KpiDataType> actual = objectUnderTest.collectNotSimpleAggregationElementColumnNameDataType(definitionList, Set.of());

        verify(databaseServiceMock).findAggregationElementColumnType(any());

        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of("execution_id", KpiDataType.POSTGRES_STRING,
                                "agg_column_0", KpiDataType.POSTGRES_LONG,
                                "agg_column_1", KpiDataType.POSTGRES_STRING,
                                "agg_column_2", KpiDataType.POSTGRES_INTEGER,
                                "agg_column_3", KpiDataType.POSTGRES_LONG,
                                "agg_column_4", KpiDataType.POSTGRES_LONG));
    }

    @MethodSource("provideSimpleKpiData")
    @ParameterizedTest
    void whenGetValidSimpleAggregationElementsIsCalled_thenValidSimpleAggregationElementIsReturned (final Set<KpiDefinitionEntity> definitions,
                                                                                                    final Set<String> aggElements,
                                                                                                    final Set<ParsedSchema> parsedSchemas,
                                                                                                    final Map<String, KpiDataType> expected) {

        when(schemaRegistryFacadeMock.getSchemasForDefinitions(any())).thenReturn(parsedSchemas);

        Map<String, KpiDataType> actual = objectUnderTest.collectSimpleAggregationElementColumnNameDataType(definitions, aggElements);

        verify(schemaRegistryFacadeMock).getSchemasForDefinitions(any());

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    private static Stream<Arguments> provideSimpleKpiData() {
        final KpiDefinitionEntity simpleKpi0 = KpiDefinitionEntity.builder().build();
        final KpiDefinitionEntity simpleKpi1 = KpiDefinitionEntity.builder().build();
        final KpiDefinitionEntity simpleKpi2 = KpiDefinitionEntity.builder().build();
        final KpiDefinitionEntity simpleKpi3 = KpiDefinitionEntity.builder().build();

        final Set <String> collectedAggregationElements0 =Set.of("agg_column_0", "agg_column_1", "agg_column_2", "agg_column_3");
        final Set <String> collectedAggregationElements1 = Set.of("agg_column_2", "agg_column_3", "agg_column_4");

        final Set<ParsedSchema> parsedSchemas = Set.of(
                new AvroSchema(String.format("{\"type\":\"record\",\"name\":\"%s\",\"fields\":[{\"name\":\"agg_column_0\",\"type\":\"string\"},{\"name\":\"agg_column_1\",\"type\":\"int\"}]}", "schema0")),
                new AvroSchema(String.format("{\"type\":\"record\",\"name\":\"%s\",\"fields\":[{\"name\":\"agg_column_2\",\"type\":\"long\"},{\"name\":\"agg_column_3\",\"type\":\"float\"},{\"name\":\"agg_column_4\",\"type\":\"double\"}]}", "schema1")),
                new AvroSchema(String.format("{\"type\":\"record\",\"name\":\"%s\",\"fields\":[{\"name\":\"agg_column_5\",\"type\":\"string\"},{\"name\":\"agg_column_6\",\"type\":\"int\"}]}", "schema2"))
        );

        final Map<String, KpiDataType> result1 = Map.of(
                "agg_column_0", KpiDataType.POSTGRES_STRING,
                "agg_column_1", KpiDataType.POSTGRES_INTEGER,
                "agg_column_2", KpiDataType.POSTGRES_LONG,
                "agg_column_3", KpiDataType.POSTGRES_FLOAT);

        final Map<String, KpiDataType> result2 = Map.of(
                "agg_column_2", KpiDataType.POSTGRES_LONG,
                "agg_column_3", KpiDataType.POSTGRES_FLOAT,
                "agg_column_4", KpiDataType.POSTGRES_FLOAT);

        simpleKpi0.schemaDataSpace("dataSpace");
        simpleKpi0.schemaCategory("category");
        simpleKpi0.schemaName("schema0");
        simpleKpi0.aggregationElements(List.of("table.agg_column_0", "table.agg_column_1"));

        simpleKpi1.schemaDataSpace("dataSpace");
        simpleKpi1.schemaCategory("category");
        simpleKpi1.schemaName("schema1");
        simpleKpi1.aggregationElements(List.of("table.agg_column_2", "table.agg_column_3"));

        simpleKpi2.schemaDataSpace("dataSpace");
        simpleKpi2.schemaCategory("category");
        simpleKpi2.schemaName("schema1");
        simpleKpi2.aggregationElements(List.of("table.agg_column_2", "table.agg_column_4"));

        simpleKpi3.schemaDataSpace("dataSpace");
        simpleKpi3.schemaCategory("category");
        simpleKpi3.schemaName("schema2");
        simpleKpi3.aggregationElements(List.of("table.agg_column_1", "table.agg_column_2", "table.agg_column_3"));

        return Stream.of(
                Arguments.of(Set.of(simpleKpi0, simpleKpi1), collectedAggregationElements0, parsedSchemas, new HashMap<>(result1)),
                Arguments.of(Set.of(simpleKpi1, simpleKpi2), collectedAggregationElements1, parsedSchemas, new HashMap<>(result2)),
                Arguments.of(Set.of(simpleKpi3), collectedAggregationElements0, parsedSchemas, new HashMap<>()));
    }
}
