/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;
import com.ericsson.oss.air.pm.stats.repository.api.DatabaseRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.api.PartitionRepository;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.service.validator.PostgresDataTypeChangeValidator;

import kpi.model.ondemand.ParameterType;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceImplTest {
    static final Table TABLE = Table.of("table");
    static final Column COLUMN_1 = Column.of("column1");
    static final Column COLUMN_2 = Column.of("column2");
    static final Column COLUMN_3 = Column.of("column3");
    static final UUID CALCULATION_ID = UUID.fromString("b2531c89-8513-4a88-aa1a-b99484321628");

    @Mock
    DatabaseRepository databaseRepositoryMock;
    @Mock
    PartitionRepository partitionRepositoryMock;
    @Mock
    PostgresDataTypeChangeValidator postgresDataTypeChangeValidatorMock;
    @Mock
    ParameterRepository parameterRepositoryMock;

    @InjectMocks
    DatabaseServiceImpl objectUnderTest;

    @Test
    void shouldVerifyIsAvailable() {
        when(databaseRepositoryMock.isAvailable()).thenReturn(true);

        final boolean actual = objectUnderTest.isAvailable();

        verify(databaseRepositoryMock).isAvailable();

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldVerifyDoesTableExistByName() {
        final String tableName = "tableName";
        when(databaseRepositoryMock.doesTableExistByName(tableName)).thenReturn(true);

        final boolean actual = objectUnderTest.doesTableExistByName(tableName);

        verify(databaseRepositoryMock).doesTableExistByName(tableName);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldVerifyDoesTablesExistByName() {
        when(databaseRepositoryMock.doesTableExistByName(any())).thenReturn(true);

        final Set<String> tableNames = Set.of("tableName1", "tableName2");
        final boolean actual = objectUnderTest.doesTablesExistByName(tableNames);

        verify(databaseRepositoryMock, times(2)).doesTableExistByName(any());
        Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldFindAllCalculationOutputTables() {
        when(databaseRepositoryMock.findAllCalculationOutputTables()).thenReturn(Collections.emptyList());

        final List<String> actual = objectUnderTest.findAllCalculationOutputTables();

        verify(databaseRepositoryMock).findAllCalculationOutputTables();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllColumnNamesForTable() {
        when(databaseRepositoryMock.findAllColumnDefinitions(TABLE)).thenReturn(List.of(
                ColumnDefinition.of(COLUMN_1, KpiDataType.POSTGRES_STRING),
                ColumnDefinition.of(COLUMN_2, KpiDataType.POSTGRES_STRING),
                ColumnDefinition.of(COLUMN_3, KpiDataType.POSTGRES_STRING)));

        final List<String> actual = objectUnderTest.findColumnNamesForTable(TABLE.getName());

        verify(databaseRepositoryMock).findAllColumnDefinitions(TABLE);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(COLUMN_1.getName(), COLUMN_2.getName(), COLUMN_3.getName());
    }

    @Test
    void shouldCreateSchema(){
        UUID collectionId = UUID.fromString("0f71a481-fdf1-487c-9ea6-480183149d97");
        objectUnderTest.createSchemaByCollectionId(collectionId);

        verify(databaseRepositoryMock).createSchemaByCollectionId(collectionId);
    }

    @Test
    @SneakyThrows
    void shouldFindTypeForColumn() {
        final AggregationElement aggregationElement1 = AggregationElement.builder()
                .withSourceTable("table")
                .withSourceColumn("column1")
                .withTargetColumn("column")
                .withIsParametric(false)
                .build();
        final AggregationElement aggregationElement2 = AggregationElement.builder()
                .withSourceTable("table")
                .withSourceColumn("column2")
                .withTargetColumn("column2")
                .withIsParametric(false)
                .build();
        final List<ColumnDefinition> columnDefinitionList = List.of(
                ColumnDefinition.of(COLUMN_1, KpiDataType.POSTGRES_STRING),
                ColumnDefinition.of(COLUMN_2, KpiDataType.POSTGRES_INTEGER),
                ColumnDefinition.of(COLUMN_3, KpiDataType.POSTGRES_STRING));

        final Map<String, KpiDataType> expected = Map.of("column", KpiDataType.POSTGRES_STRING,
                "column2", KpiDataType.POSTGRES_INTEGER);

        when(databaseRepositoryMock.findAllColumnDefinitions(TABLE)).thenReturn(columnDefinitionList);

        final Map<String, KpiDataType> actual = objectUnderTest.findAggregationElementColumnType(List.of(aggregationElement1, aggregationElement2));

        verify(databaseRepositoryMock).findAllColumnDefinitions(TABLE);

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void shouldThrowExceptionIfTypeForColumnNotFound() {
        List<AggregationElement> aggregationElementList = List.of(AggregationElement.builder()
                .withExpression("table.column0 AS column")
                .withSourceTable("table")
                .withTargetColumn("column")
                .build());

        when(databaseRepositoryMock.findAllColumnDefinitions(TABLE)).thenReturn(List.of(ColumnDefinition.of(COLUMN_1, KpiDataType.POSTGRES_STRING)));

        Assertions.assertThatThrownBy(() -> objectUnderTest.findAggregationElementColumnType(aggregationElementList))
                .isInstanceOf(KpiPersistenceException.class).hasMessage("Invalid aggregation element in the given definitions: 'table.column0 AS column' is invalid.");
    }

    @Test
    void shouldCreateOutputTable() throws Exception {
        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "60", List.of(entity()));
        final Connection connectionMock = mock(Connection.class);

        objectUnderTest.createOutputTable(connectionMock, tableCreationInformation, new HashMap<>());

        verify(databaseRepositoryMock).createOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
    }

    @Test
    void shouldAddColumnsToOutputTable() throws Exception {
        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "60", List.of(entity()));
        final Connection connectionMock = mock(Connection.class);

        objectUnderTest.addColumnsToOutputTable(connectionMock, tableCreationInformation, new HashMap<>());

        verify(databaseRepositoryMock).addColumnsToOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
    }

    @Nested
    @DisplayName("recreatePrimaryKeyConstraint")
    class RecreatePrimaryKeyConstraint {
        @Test
        void not_WhenProvidedPrimaryKeysAreEmpty() throws Exception {
            final Connection connectionMock = mock(Connection.class);

            objectUnderTest.recreatePrimaryKeyConstraint(connectionMock, TABLE, Collections.emptyList());

            verify(databaseRepositoryMock, never()).findAllPrimaryKeys(TABLE);
            verify(databaseRepositoryMock, never()).dropPrimaryKeyConstraint(connectionMock, TABLE);
            verify(databaseRepositoryMock, never()).addPrimaryKeyConstraint(eq(connectionMock), eq(TABLE), anyList());
        }

        @Test
        void not_WhenNewPrimaryKeysAreEmpty() throws Exception {
            final Connection connectionMock = mock(Connection.class);

            when(databaseRepositoryMock.findAllPrimaryKeys(TABLE)).thenReturn(Arrays.asList(COLUMN_1, COLUMN_2));

            objectUnderTest.recreatePrimaryKeyConstraint(connectionMock, TABLE, Arrays.asList(COLUMN_1, COLUMN_2));

            verify(databaseRepositoryMock).findAllPrimaryKeys(TABLE);
            verify(databaseRepositoryMock, never()).dropPrimaryKeyConstraint(connectionMock, TABLE);
            verify(databaseRepositoryMock, never()).addPrimaryKeyConstraint(eq(connectionMock), eq(TABLE), anyList());
        }

        @Test
        void whenPrimaryKeyRecreationIsPossible() throws Exception {
            final Connection connectionMock = mock(Connection.class);

            when(databaseRepositoryMock.findAllPrimaryKeys(TABLE)).thenReturn(Collections.singletonList(COLUMN_1));

            objectUnderTest.recreatePrimaryKeyConstraint(connectionMock, TABLE, Arrays.asList(COLUMN_2, COLUMN_1));

            verify(databaseRepositoryMock).findAllPrimaryKeys(TABLE);
            verify(databaseRepositoryMock).dropPrimaryKeyConstraint(connectionMock, TABLE);
            verify(databaseRepositoryMock).addPrimaryKeyConstraint(connectionMock, TABLE, Arrays.asList(COLUMN_1, COLUMN_2));
        }
    }

    @Nested
    @DisplayName("recreateUniqueIndex")
    class RecreateUniqueIndex {
        private static final String PARTITION_NAME = "partitionName";
        private static final String UNIQUE_INDEX_NAME = "uniqueIndexName";

        @Test
        void not_WhenProvidedIndexesAreEmpty() throws Exception {
            final Connection connectionMock = mock(Connection.class);

            objectUnderTest.recreateUniqueIndex(connectionMock, TABLE, Collections.emptyList());

            verify(partitionRepositoryMock, never()).findAllPartitionUniqueIndexes(TABLE);
            verify(partitionRepositoryMock, never()).dropUniqueIndex(eq(connectionMock), any(PartitionUniqueIndex.class));
            verify(partitionRepositoryMock, never()).createUniqueIndex(eq(connectionMock), any(PartitionUniqueIndex.class));
        }

        @Test
        void not_WhenIndexRecreationIsNotNeeded() throws Exception {
            final Connection connectionMock = mock(Connection.class);

            final List<PartitionUniqueIndex> oldPartitionUniqueIndexes = Collections.singletonList(partitionUniqueIndex(COLUMN_1, COLUMN_2));
            when(partitionRepositoryMock.findAllPartitionUniqueIndexes(TABLE)).thenReturn(oldPartitionUniqueIndexes);

            objectUnderTest.recreateUniqueIndex(connectionMock, TABLE, Arrays.asList(COLUMN_1, COLUMN_2));

            verify(partitionRepositoryMock).findAllPartitionUniqueIndexes(TABLE);
            verify(partitionRepositoryMock, never()).dropUniqueIndex(eq(connectionMock), any(PartitionUniqueIndex.class));
            verify(partitionRepositoryMock, never()).createUniqueIndex(eq(connectionMock), any(PartitionUniqueIndex.class));
        }

        @Test
        void whenIndexRecreationIsNeeded() throws Exception {
            final Connection connectionMock = mock(Connection.class);

            final List<PartitionUniqueIndex> oldPartitionUniqueIndexes = Collections.singletonList(partitionUniqueIndex(COLUMN_1));
            when(partitionRepositoryMock.findAllPartitionUniqueIndexes(TABLE)).thenReturn(oldPartitionUniqueIndexes);

            objectUnderTest.recreateUniqueIndex(connectionMock, TABLE, Arrays.asList(COLUMN_2, COLUMN_3));

            final ArgumentCaptor<PartitionUniqueIndex> partitionUniqueIndexArgumentCaptor = ArgumentCaptor.forClass(PartitionUniqueIndex.class);

            verify(partitionRepositoryMock).findAllPartitionUniqueIndexes(TABLE);
            verify(partitionRepositoryMock).dropUniqueIndex(eq(connectionMock), partitionUniqueIndexArgumentCaptor.capture());

            Assertions.assertThat(partitionUniqueIndexArgumentCaptor.getValue()).satisfies(partitionUniqueIndex -> {
                Assertions.assertThat(partitionUniqueIndex.getPartitionName()).isEqualTo(PARTITION_NAME);
                Assertions.assertThat(partitionUniqueIndex.getUniqueIndexName()).isEqualTo(UNIQUE_INDEX_NAME);
                Assertions.assertThat(partitionUniqueIndex.indexColumns()).containsExactlyInAnyOrder(Column.of("column1"));
            });

            verify(partitionRepositoryMock).createUniqueIndex(eq(connectionMock), partitionUniqueIndexArgumentCaptor.capture());

            Assertions.assertThat(partitionUniqueIndexArgumentCaptor.getValue()).satisfies(partitionUniqueIndex -> {
                Assertions.assertThat(partitionUniqueIndex.getPartitionName()).isEqualTo(PARTITION_NAME);
                Assertions.assertThat(partitionUniqueIndex.getUniqueIndexName()).isEqualTo(UNIQUE_INDEX_NAME);
                Assertions.assertThat(partitionUniqueIndex.indexColumns()).containsExactlyInAnyOrder(
                        Column.of("column1"),
                        Column.of("column2"),
                        Column.of("column3")
                );
            });
        }

        PartitionUniqueIndex partitionUniqueIndex(final Column... column) {
            return new PartitionUniqueIndex(
                    PARTITION_NAME,
                    UNIQUE_INDEX_NAME,
                    Arrays.asList(column)
            );
        }
    }

    @Test
    @SneakyThrows
    void shouldSaveTabularParametersWithCsvValue(@Mock final Connection connectionMock) {
        final TabularParameters tabularParameter = TabularParameters.builder().name("name").value("value").format(Format.CSV).build();

        when(databaseRepositoryMock.saveTabularParameter(eq(connectionMock), eq(Table.of("name_b2531c89")), eq(null), any(Reader.class))).thenReturn(1L);

        final Long actual = objectUnderTest.saveTabularParameter(connectionMock, tabularParameter, CALCULATION_ID);

        verify(databaseRepositoryMock).saveTabularParameter(eq(connectionMock), eq(Table.of("name_b2531c89")), eq(null), any(Reader.class));

        Assertions.assertThat(actual).isOne();
    }

    @Test
    @SneakyThrows
    void shouldSaveTabularParametersWithJsonValue(@Mock final Connection connectionMock) {
        final String json = "{\"cell_configuration_test_1\": [{\"field1\": 11}]}";
        final TabularParameters tabularParameter = TabularParameters.builder().name("name").value(json).format(Format.JSON).build();

        when(databaseRepositoryMock.saveTabularParameter(eq(connectionMock), eq(Table.of("name_b2531c89")), eq("field1"), any(Reader.class))).thenReturn(1L);

        final Long actual = objectUnderTest.saveTabularParameter(connectionMock, tabularParameter, CALCULATION_ID);

        verify(databaseRepositoryMock).saveTabularParameter(eq(connectionMock), eq(Table.of("name_b2531c89")), eq("field1"), any(Reader.class));

        Assertions.assertThat(actual).isOne();
    }

    @Test
    @SneakyThrows
    void shouldThrowTabularParameterValidationException_onSaveTabularParameters(@Mock final Connection connectionMock) {
        final TabularParameters tabularParameter = TabularParameters.builder().name("name").value("value").build();

        verifyNoInteractions(databaseRepositoryMock);

        Assertions.assertThatThrownBy(() -> objectUnderTest.saveTabularParameter(connectionMock, tabularParameter, CALCULATION_ID))
                .isInstanceOf(TabularParameterValidationException.class);

    }

    @Nested
    @DisplayName("updateSchema")
    class UpdateSchema {
        final ColumnDefinition columnDefinitionFdn = ColumnDefinition.of(Column.of("fdn"), KpiDataType.POSTGRES_UNLIMITED_STRING);
        final ColumnDefinition columnDefinitionGuid = ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_FLOAT);

        @Test
        void shouldUpdate_onDefinitionChange() throws Exception {
            final Connection connectionMock = mock(Connection.class);
            final KpiDefinitionEntity definition1 = entity("fdn", "alias", "TEXT");
            final KpiDefinitionEntity definition2 = entity("guid", "alias", "LONG");
            final KpiDefinitionEntity definition3 = entity("newColumn", "alias", "LONG");

            final TableCreationInformation tableCreationInformation = TableCreationInformation.of(
                    TABLE.getName(), "60", List.of(definition1, definition2, definition3)
            );

            when(databaseRepositoryMock.findAllColumnDefinitions(TABLE)).thenReturn(Arrays.asList(columnDefinitionFdn, columnDefinitionGuid));
            when(postgresDataTypeChangeValidatorMock.isInvalidChange(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_LONG)).thenReturn(false);
            objectUnderTest.updateSchema(connectionMock, tableCreationInformation, new HashMap<>());

            verify(databaseRepositoryMock, times(2)).findAllColumnDefinitions(TABLE);
            verify(databaseRepositoryMock).changeColumnType(connectionMock, TABLE, ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG));
            verify(postgresDataTypeChangeValidatorMock).isInvalidChange(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_LONG);
        }

        @Test
        void shouldUpdate_onAggregationElementsChange() throws Exception {
            final Connection connectionMock = mock(Connection.class);
            final KpiDefinitionEntity definition1 = KpiDefinitionEntity.builder()
                    .withAggregationElements(Collections.singletonList("fdn"))
                    .withAlias("alias")
                    .withName("newColumn3")
                    .withObjectType("TEXT")
                    .build();
            final KpiDefinitionEntity definition2 = KpiDefinitionEntity.builder()
                    .withAggregationElements(Collections.singletonList("guid"))
                    .withAlias("alias")
                    .withName("newColumn2")
                    .withObjectType("LONG")
                    .build();
            final KpiDefinitionEntity definition3 = KpiDefinitionEntity.builder()
                    .withAggregationElements(Collections.singletonList("newAggregation"))
                    .withAlias("alias")
                    .withName("newColumn1")
                    .withObjectType("TEXT")
                    .build();

            final TableCreationInformation tableCreationInformation = TableCreationInformation.of(
                    TABLE.getName(), "60", List.of(definition1, definition2, definition3)
            );

            final ColumnDefinition dummyColumnDefinitionGuid = ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_INTEGER);
            when(databaseRepositoryMock.findAllColumnDefinitions(TABLE)).thenReturn(Arrays.asList(columnDefinitionFdn, dummyColumnDefinitionGuid));
            when(postgresDataTypeChangeValidatorMock.isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG)).thenReturn(false);

            objectUnderTest.updateSchema(connectionMock, tableCreationInformation, Map.of("fdn", KpiDataType.POSTGRES_UNLIMITED_STRING, "guid", KpiDataType.POSTGRES_LONG));

            verify(databaseRepositoryMock, times(2)).findAllColumnDefinitions(TABLE);
            verify(databaseRepositoryMock).changeColumnType(connectionMock, TABLE, ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG));
            verify(postgresDataTypeChangeValidatorMock).isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG);
        }

        @Test
        void shouldThrowException_whenDataTypeChangeIsInvalid() {
            final KpiDefinitionEntity definition1 = entity("fdn", "alias", "TEXT");
            final KpiDefinitionEntity definition2 = entity("guid", "alias", "BOOLEAN");
            final KpiDefinitionEntity definition3 = entity("newColumn", "alias", "BOOLEAN");

            final TableCreationInformation tableCreationInformation = TableCreationInformation.of(
                    TABLE.getName(), "60", List.of(definition1, definition2, definition3)
            );

            final ColumnDefinition dummyColumnDefinitionGuid = ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_INTEGER);

            when(databaseRepositoryMock.findAllColumnDefinitions(TABLE)).thenReturn(Arrays.asList(columnDefinitionFdn, dummyColumnDefinitionGuid));
            when(postgresDataTypeChangeValidatorMock.isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_BOOLEAN)).thenReturn(true);

            final Connection connectionMock = mock(Connection.class);

            Assertions.assertThatThrownBy(() -> objectUnderTest.updateSchema(connectionMock, tableCreationInformation, new HashMap<>()))
                    .isInstanceOf(SQLException.class)
                    .hasMessage("Column data type change from 'int4' to 'bool' for table: 'table' is invalid");

            verify(databaseRepositoryMock, times(2)).findAllColumnDefinitions(TABLE);
            verify(postgresDataTypeChangeValidatorMock).isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_BOOLEAN);
        }
    }

    @Test
    @SneakyThrows
    void shouldChangeColumnType(@Mock Connection connectionMock) {
        final KpiDefinitionEntity kpiDefinitionEntity = entity("guid", "alias1", "LONG");
        final ColumnDefinition dummyColumnDefinition = ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_INTEGER);
        final Table table = Table.of("kpi_alias1_");

        when(databaseRepositoryMock.findColumnDefinition(table, "guid")).thenReturn(dummyColumnDefinition);
        when(postgresDataTypeChangeValidatorMock.isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG)).thenReturn(false);

        objectUnderTest.changeColumnType(connectionMock, kpiDefinitionEntity);

        verify(databaseRepositoryMock).findColumnDefinition(table, "guid");
        verify(postgresDataTypeChangeValidatorMock).isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG);
        verify(databaseRepositoryMock).changeColumnType(connectionMock, table, ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG));
    }

    @Test
    void shouldNotChangeColumnType_whenDataTypeChangeIsInvalid(@Mock Connection connectionMock) {
        final KpiDefinitionEntity kpiDefinitionEntity = entity("guid", "alias1", "INTEGER");
        final ColumnDefinition dummyColumnDefinition = ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG);
        final Table table = Table.of("kpi_alias1_");

        when(databaseRepositoryMock.findColumnDefinition(table, "guid")).thenReturn(dummyColumnDefinition);
        when(postgresDataTypeChangeValidatorMock.isInvalidChange(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_INTEGER)).thenReturn(true);

        Assertions.assertThatThrownBy(() -> objectUnderTest.changeColumnType(connectionMock, kpiDefinitionEntity))
                .isInstanceOf(KpiPersistenceException.class)
                .hasMessage("Column data type change from 'POSTGRES_LONG' to 'POSTGRES_INTEGER' for table: 'kpi_alias1_' is invalid");

        verify(databaseRepositoryMock).findColumnDefinition(table, "guid");
        verify(postgresDataTypeChangeValidatorMock).isInvalidChange(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_INTEGER);
        verifyNoMoreInteractions(databaseRepositoryMock);
    }

    @Test
    @SneakyThrows
    void shouldThrowException_whenSomethingGoesWrong_onChangeColumnType(@Mock Connection connectionMock) {
        final KpiDefinitionEntity kpiDefinitionEntity = entity("guid", "alias1", "LONG");
        final ColumnDefinition dummyColumnDefinition = ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_INTEGER);
        final Table table = Table.of("kpi_alias1_");

        when(databaseRepositoryMock.findColumnDefinition(table, "guid")).thenReturn(dummyColumnDefinition);
        when(postgresDataTypeChangeValidatorMock.isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG)).thenReturn(false);

        doThrow(SQLException.class).when(databaseRepositoryMock).changeColumnType(connectionMock, table,
                ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG));

        Assertions.assertThatThrownBy(() -> objectUnderTest.changeColumnType(connectionMock, kpiDefinitionEntity))
                .isInstanceOf(KpiPersistenceException.class)
                .hasMessage("Error changing column type, KPI output table: 'kpi_alias1_', column: 'guid', type: 'int8'");

        verify(databaseRepositoryMock).findColumnDefinition(table, "guid");
        verify(postgresDataTypeChangeValidatorMock).isInvalidChange(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG);
        verify(databaseRepositoryMock).changeColumnType(connectionMock, table, ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG));
    }

    @Test
    void shouldCreateTabularParametersTables(@Mock Connection connectionMock) throws SQLException {
        final List<String> tabularParameterNames = List.of("tabularParameterName1");
        final Parameter parameter1 = parameter("cell_config", ParameterType.STRING);
        final Parameter parameter2 = parameter("cell_config2", ParameterType.INTEGER);
        final List<Parameter> tabularParameters = List.of(parameter1, parameter2);

        when(parameterRepositoryMock.findParametersForTabularParameter("tabularParameterName1")).thenReturn(tabularParameters);

        objectUnderTest.createTabularParameterTables(connectionMock, tabularParameterNames, CALCULATION_ID);

        verify(parameterRepositoryMock).findParametersForTabularParameter("tabularParameterName1");
        verify(databaseRepositoryMock).createTabularParameterTable(connectionMock, tabularParameters, "tabularParameterName1_b2531c89");

    }

    @Test
    @SneakyThrows
    void shouldDeleteColumns(@Mock Connection connectionMock) {
        final String table = "table";
        final List<String> columns = List.of("column1", "column2");

        objectUnderTest.deleteColumnsForTable(connectionMock, table, columns);

        verify(databaseRepositoryMock).deleteColumnsForTable(connectionMock, table, columns);
    }

    @Test
    @SneakyThrows
    void shouldDeleteTables(@Mock Connection connectionMock) {
        final List<String> tables = List.of("table1", "table2");

        objectUnderTest.deleteTables(connectionMock, tables);

        verify(databaseRepositoryMock).deleteTables(connectionMock, tables);
    }

    static KpiDefinitionEntity entity(final String name, final String alias, final String objectType) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withAlias(alias);
        builder.withObjectType(objectType);
        builder.withAggregationPeriod(-1);
        return builder.build();
    }

    static KpiDefinitionEntity entity() {
        return KpiDefinitionEntity.builder().build();
    }

    static Parameter parameter(final String name, final ParameterType type) {
        return Parameter.builder().withName(name).withType(type).build();
    }
}