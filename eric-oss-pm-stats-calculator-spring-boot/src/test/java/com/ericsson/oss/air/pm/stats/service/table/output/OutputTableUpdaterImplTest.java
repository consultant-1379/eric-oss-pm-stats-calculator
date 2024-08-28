/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.table.output;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutputTableUpdaterImplTest {
    @Mock
    DatabaseService databaseServiceMock;

    @InjectMocks
    OutputTableUpdaterImpl objectUnderTest;

    @Test
    void shouldUpdateDefaultOutputTable() {
        DriverManagerMock.prepare(connectionMock -> {
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of(
                    "tableName", "-1", List.of(entity(List.of("agg1", "agg2")))
            );
            objectUnderTest.updateOutputTable(tableCreationInformation, new HashMap<>());

            verify(databaseServiceMock).addColumnsToOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
            verify(databaseServiceMock).recreatePrimaryKeyConstraint(connectionMock,
                    Table.of("tableName"),
                    Arrays.asList(Column.of("agg1"), Column.of("agg2")));
            verify(databaseServiceMock).updateSchema(connectionMock, tableCreationInformation, new HashMap<>());
            verifyNoMoreInteractions(databaseServiceMock);
        });
    }

    @Test
    void shouldUpdateNonDefaultOutputTable() {
        DriverManagerMock.prepare(connectionMock -> {
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of(
                    "tableName", "60", List.of(entity(List.of("agg1", "agg2")))
            );

            objectUnderTest.updateOutputTable(tableCreationInformation, new HashMap<>());

            verify(databaseServiceMock).addColumnsToOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
            verify(databaseServiceMock).recreateUniqueIndex(connectionMock,
                    Table.of("tableName"),
                    Arrays.asList(Column.of("agg1"), Column.of("agg2")));
            verify(databaseServiceMock).updateSchema(connectionMock, tableCreationInformation, new HashMap<>());
            verifyNoMoreInteractions(databaseServiceMock);
        });
    }

    @Test
    void shouldThrowException_whenSomethingGoesWrong_onUpdateOutputTable() {
        DriverManagerMock.prepare(connectionMock -> {
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "60", List.of(entity()));

            doThrow(SQLException.class).when(databaseServiceMock).addColumnsToOutputTable(connectionMock, tableCreationInformation, new HashMap<>());

            Assertions.assertThatThrownBy(() -> objectUnderTest.updateOutputTable(tableCreationInformation, new HashMap<>()))
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Error updating KPI output table: '%s'", tableCreationInformation.getTableName());

            verify(databaseServiceMock).addColumnsToOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
        });
    }

    static KpiDefinitionEntity entity() {
        return KpiDefinitionEntity.builder().build();
    }

    static KpiDefinitionEntity entity(final List<String> aggregationElements) {
        return KpiDefinitionEntity.builder().withAggregationElements(aggregationElements).build();
    }
}