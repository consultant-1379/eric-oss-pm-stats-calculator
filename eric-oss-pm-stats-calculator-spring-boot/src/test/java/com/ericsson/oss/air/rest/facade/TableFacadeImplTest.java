/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.facade;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableDefinitions;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;
import com.ericsson.oss.air.pm.stats.service.table.output.api.OutputTableCreator;
import com.ericsson.oss.air.pm.stats.service.table.output.api.OutputTableUpdater;
import com.ericsson.oss.air.rest.facade.grouper.api.DefinitionGrouper;
import com.ericsson.oss.air.rest.util.AggregationElementUtils;

import kpi.model.KpiDefinitionRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableFacadeImplTest {
    @Mock DatabaseService databaseServiceMock;
    @Mock OutputTableCreator outputTableCreatorMock;
    @Mock OutputTableUpdater outputTableUpdaterMock;
    @Mock DefinitionGrouper definitionGrouperMock;
    @Mock SchemaRegistryFacade schemaRegistryFacadeMock;
    @Mock ParameterService parameterServiceMock;
    @Mock AggregationElementUtils aggregationElementUtilsMock;
    @InjectMocks TableFacadeImpl objectUnderTest;
    @Captor ArgumentCaptor<TableCreationInformation> tableCreationInformationCaptor;

    @Test
    void shouldUpdateSimple_onCreateOrUpdateOutputTable(@Mock final KpiDefinitionRequest kpiDefinitionMock) {
        final KpiDefinitionEntity simpleDefinition = KpiDefinitionEntity.builder()
                .withAggregationPeriod(-1)
                .withAlias("alias1")
                .withSchemaDataSpace("dataSpace")
                .withSchemaCategory("category")
                .withSchemaName("schema1")
                .withAggregationElements(List.of("agg_column_0"))
                .withFilters(List.of())
                .withExpression("expression")
                .build();

        final LinkedList<TableDefinitions> tableDefinitions = new LinkedList<>(List.of(TableDefinitions.of(Table.of("kpi_alias1"), Set.of(simpleDefinition))));

        when(definitionGrouperMock.groupByOutputTable(kpiDefinitionMock)).thenReturn(tableDefinitions);
        when(databaseServiceMock.doesTableExistByName("kpi_alias1")).thenReturn(true);
        when(aggregationElementUtilsMock.collectValidAggregationElements(tableCreationInformationCaptor.capture())).thenReturn(Map.of("agg_column_0", KpiDataType.POSTGRES_LONG));

        objectUnderTest.createOrUpdateOutputTable(kpiDefinitionMock, Optional.empty());

        verify(definitionGrouperMock).groupByOutputTable(kpiDefinitionMock);
        verify(databaseServiceMock).doesTableExistByName("kpi_alias1");
        verify(outputTableUpdaterMock).updateOutputTable(tableCreationInformationCaptor.capture(), eq(Map.of("agg_column_0", KpiDataType.POSTGRES_LONG)));
        verify(aggregationElementUtilsMock).collectValidAggregationElements(tableCreationInformationCaptor.capture());
        verifyNoMoreInteractions(outputTableUpdaterMock, definitionGrouperMock, databaseServiceMock, aggregationElementUtilsMock);
        verifyNoInteractions(parameterServiceMock, schemaRegistryFacadeMock, outputTableCreatorMock);

        Assertions.assertThat(tableCreationInformationCaptor.getValue()).satisfies(tableCreationInformation -> {
            Assertions.assertThat(tableCreationInformation.getTableName()).isEqualTo("kpi_alias1");
            Assertions.assertThat(tableCreationInformation.getAggregationPeriod()).isEqualTo("-1");
            Assertions.assertThat(tableCreationInformation.getDefinitions()).containsExactly(simpleDefinition);
        });
    }

    @Test
    void shouldUpdateComplex_onCreateOrUpdateOutputTable(@Mock final KpiDefinitionRequest kpiDefinitionMock) {
        final KpiDefinitionEntity complexDefinition = KpiDefinitionEntity.builder()
                .withAggregationPeriod(-1)
                .withAlias("alias1")
                .withExpression("SUM(kpi_simple_60.integer_simple) FROM dim_db://kpi_simple_60")
                .withAggregationElements(List.of("dim_table.agg_column_0"))
                .withFilters(List.of())
                .build();

        final LinkedList<TableDefinitions> tableDefinitions = new LinkedList<>(List.of(TableDefinitions.of(Table.of("kpi_alias1"), Set.of(complexDefinition))));

        when(definitionGrouperMock.groupByOutputTable(kpiDefinitionMock)).thenReturn(tableDefinitions);
        when(databaseServiceMock.doesTableExistByName("kpi_alias1")).thenReturn(true);
        when(aggregationElementUtilsMock.collectValidAggregationElements(tableCreationInformationCaptor.capture())).thenReturn(Map.of("agg_column_0", KpiDataType.POSTGRES_LONG));

        objectUnderTest.createOrUpdateOutputTable(kpiDefinitionMock, Optional.empty());

        verify(definitionGrouperMock).groupByOutputTable(kpiDefinitionMock);
        verify(databaseServiceMock).doesTableExistByName("kpi_alias1");
        verify(outputTableUpdaterMock).updateOutputTable(tableCreationInformationCaptor.capture(), eq(Map.of("agg_column_0", KpiDataType.POSTGRES_LONG)));
        verify(aggregationElementUtilsMock).collectValidAggregationElements(tableCreationInformationCaptor.capture());
        verifyNoMoreInteractions(outputTableUpdaterMock, definitionGrouperMock, databaseServiceMock, aggregationElementUtilsMock);
        verifyNoInteractions(parameterServiceMock, schemaRegistryFacadeMock, outputTableCreatorMock);

        Assertions.assertThat(tableCreationInformationCaptor.getValue()).satisfies(tableCreationInformation -> {
            Assertions.assertThat(tableCreationInformation.getTableName()).isEqualTo("kpi_alias1");
            Assertions.assertThat(tableCreationInformation.getAggregationPeriod()).isEqualTo("-1");
            Assertions.assertThat(tableCreationInformation.getDefinitions()).containsExactly(complexDefinition);
        });
    }

    @Test
    void shouldCreateSimple_onCreateOrUpdateOutputTable(@Mock final KpiDefinitionRequest kpiDefinitionMock) {
        final KpiDefinitionEntity simpleDefinition = KpiDefinitionEntity.builder()
                .withAggregationPeriod(-1)
                .withAlias("alias1")
                .withSchemaDataSpace("dataSpace")
                .withSchemaCategory("category")
                .withSchemaName("schema1")
                .withFilters(List.of())
                .withAggregationElements(List.of("agg_column_0"))
                .build();

        final LinkedList<TableDefinitions> tableDefinitions = new LinkedList<>(List.of(TableDefinitions.of(Table.of("kpi_alias1"), Set.of(simpleDefinition))));

        when(definitionGrouperMock.groupByOutputTable(kpiDefinitionMock)).thenReturn(tableDefinitions);
        when(databaseServiceMock.doesTableExistByName("kpi_alias1")).thenReturn(false);
        when(aggregationElementUtilsMock.collectValidAggregationElements(tableCreationInformationCaptor.capture())).thenReturn(Map.of("agg_column_0", KpiDataType.POSTGRES_STRING));

        objectUnderTest.createOrUpdateOutputTable(kpiDefinitionMock, Optional.empty());

        verify(definitionGrouperMock).groupByOutputTable(kpiDefinitionMock);
        verify(databaseServiceMock).doesTableExistByName("kpi_alias1");
        verify(outputTableCreatorMock).createOutputTable(tableCreationInformationCaptor.capture(), eq(Map.of("agg_column_0", KpiDataType.POSTGRES_STRING)));
        verify(aggregationElementUtilsMock).collectValidAggregationElements(tableCreationInformationCaptor.capture());
        verifyNoMoreInteractions(outputTableCreatorMock, definitionGrouperMock, databaseServiceMock, aggregationElementUtilsMock);
        verifyNoInteractions(parameterServiceMock, schemaRegistryFacadeMock, outputTableUpdaterMock);

        Assertions.assertThat(tableCreationInformationCaptor.getValue()).satisfies(tableCreationInformation -> {
            Assertions.assertThat(tableCreationInformation.getTableName()).isEqualTo("kpi_alias1");
            Assertions.assertThat(tableCreationInformation.getAggregationPeriod()).isEqualTo("-1");
            Assertions.assertThat(tableCreationInformation.getDefinitions()).containsExactly(simpleDefinition);
        });
    }

    @Test
    void shouldCreateComplex_onCreateOrUpdateOutputTable(@Mock final KpiDefinitionRequest kpiDefinitionMock) {
        final KpiDefinitionEntity complexDefinition = KpiDefinitionEntity.builder()
                .withAggregationPeriod(-1)
                .withAlias("alias1")
                .withExpression("SUM(kpi_simple_60.integer_simple) FROM dim_db://kpi_simple_60")
                .withAggregationElements(List.of("dim_table.agg_column_0"))
                .withFilters(List.of())
                .build();
        final LinkedList<TableDefinitions> tableDefinitions = new LinkedList<>(List.of(TableDefinitions.of(Table.of("kpi_alias1"), Set.of(complexDefinition))));

        when(definitionGrouperMock.groupByOutputTable(kpiDefinitionMock)).thenReturn(tableDefinitions);
        when(databaseServiceMock.doesTableExistByName("kpi_alias1")).thenReturn(false);
        when(aggregationElementUtilsMock.collectValidAggregationElements(tableCreationInformationCaptor.capture())).thenReturn(Map.of("agg_column_0", KpiDataType.POSTGRES_LONG));

        objectUnderTest.createOrUpdateOutputTable(kpiDefinitionMock, Optional.empty());

        verify(definitionGrouperMock).groupByOutputTable(kpiDefinitionMock);
        verify(databaseServiceMock).doesTableExistByName("kpi_alias1");
        verify(outputTableCreatorMock).createOutputTable(tableCreationInformationCaptor.capture(), eq(Map.of("agg_column_0", KpiDataType.POSTGRES_LONG)));
        verify(aggregationElementUtilsMock).collectValidAggregationElements(tableCreationInformationCaptor.capture());
        verifyNoMoreInteractions(outputTableCreatorMock, definitionGrouperMock, databaseServiceMock, aggregationElementUtilsMock);
        verifyNoInteractions(parameterServiceMock, schemaRegistryFacadeMock, outputTableUpdaterMock);

        Assertions.assertThat(tableCreationInformationCaptor.getValue()).satisfies(tableCreationInformation -> {
            Assertions.assertThat(tableCreationInformation.getTableName()).isEqualTo("kpi_alias1");
            Assertions.assertThat(tableCreationInformation.getAggregationPeriod()).isEqualTo("-1");
            Assertions.assertThat(tableCreationInformation.getDefinitions()).containsExactly(complexDefinition);
        });
    }
}
