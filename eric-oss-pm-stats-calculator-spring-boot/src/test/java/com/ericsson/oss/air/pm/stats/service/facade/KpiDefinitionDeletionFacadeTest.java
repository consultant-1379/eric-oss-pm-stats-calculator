/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.OperationRunner;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class KpiDefinitionDeletionFacadeTest {
    static final Integer AGGREGATION_PERIOD = 60;

    static final String ALIAS_1 = "alias1";
    static final String ALIAS_2 = "alias2";
    static final String ALIAS_3 = "alias3";

    static final String TABLE_NAME_1 = "kpi_alias1_60";
    static final String TABLE_NAME_2 = "kpi_alias2_60";
    static final String TABLE_NAME_3 = "kpi_alias3_60";

    static final Table TABLE_1 = Table.of(TABLE_NAME_1);
    static final Table TABLE_2 = Table.of(TABLE_NAME_2);
    static final Table TABLE_3 = Table.of(TABLE_NAME_3);

    static final String KPI_NAME_IN_TABLE_1 = "kpi_in_table_1";
    static final String KPI_1_NAME_IN_TABLE_2 = "kpi_1_in_table_2";
    static final String KPI_2_NAME_IN_TABLE_2 = "kpi_2_in_table_2";
    static final String KPI_1_NAME_IN_TABLE_3 = "kpi_1_in_table_3";
    static final String KPI_2_NAME_IN_TABLE_3 = "kpi_2_in_table_3";

    static final KpiDefinitionEntity KPI_IN_TABLE_1 = entity(KPI_NAME_IN_TABLE_1, ALIAS_1);
    static final KpiDefinitionEntity KPI_1_IN_TABLE_2 = entity(KPI_1_NAME_IN_TABLE_2, ALIAS_2);
    static final KpiDefinitionEntity KPI_2_IN_TABLE_2 = entity(KPI_2_NAME_IN_TABLE_2, ALIAS_2);
    static final KpiDefinitionEntity KPI_1_IN_TABLE_3 = entity(KPI_1_NAME_IN_TABLE_3, ALIAS_3);
    static final KpiDefinitionEntity KPI_2_IN_TABLE_3 = entity(KPI_2_NAME_IN_TABLE_3, ALIAS_3);

    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    DatabaseService databaseServiceMock;
    @Mock
    KpiExposureService kpiExposureServiceMock;
    @Mock
    RetentionPeriodService retentionPeriodService;

    @InjectMocks
    KpiDefinitionDeletionFacade objectUnderTest;

    @Captor
    ArgumentCaptor<OperationRunner> operationRunnerArgumentCaptor;

    @Test
    @SneakyThrows
    void shouldHardDeleteNecessaryKpiDefinitions() {
        DriverManagerMock.prepare(connectionMock -> {
            final List<KpiDefinitionEntity> allKpisToDelete = List.of(KPI_IN_TABLE_1, KPI_1_IN_TABLE_2, KPI_2_IN_TABLE_2, KPI_1_IN_TABLE_3, KPI_2_IN_TABLE_3);
            final List<String> allKpiNamesToDelete = List.of(KPI_NAME_IN_TABLE_1, KPI_1_NAME_IN_TABLE_2, KPI_2_NAME_IN_TABLE_2, KPI_1_NAME_IN_TABLE_3, KPI_2_NAME_IN_TABLE_3);
            final Map<Table, Boolean> tableWithKpiInformationMap = Map.of(TABLE_1, false, TABLE_2, true, TABLE_3, false);

            tableWithKpiInformationMap.forEach((table, hasKpisLeft) ->
                    when(kpiDefinitionServiceMock.doesTableHaveAnyKpisLeft(connectionMock, table)).thenReturn(hasKpisLeft));

            objectUnderTest.hardDeleteNecessaryKpiDefinitions(allKpisToDelete);

            verify(kpiDefinitionServiceMock).deleteKpiDefinitionsByName(connectionMock, allKpiNamesToDelete);
            tableWithKpiInformationMap.keySet().forEach(table -> verify(kpiDefinitionServiceMock).doesTableHaveAnyKpisLeft(connectionMock, table));
            verify(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_2, List.of(KPI_1_NAME_IN_TABLE_2, KPI_2_NAME_IN_TABLE_2));
            verify(databaseServiceMock).deleteTables(connectionMock, List.of(TABLE_3.getName()));
            verify(retentionPeriodService).deleteFromTableLevelRetention(connectionMock, TABLE_3.getName());
            verify(kpiExposureServiceMock).updateExposure();
        });
    }

    @Test
    @SneakyThrows
    void whenDeleteColumnsThrowsException_shouldOtherTasksBeHandledCorrectly() {
        DriverManagerMock.prepare(connectionMock -> {
            List<KpiDefinitionEntity> kpisToDelete = List.of(KPI_1_IN_TABLE_2, KPI_1_IN_TABLE_3);
            when(kpiDefinitionServiceMock.doesTableHaveAnyKpisLeft(connectionMock, TABLE_2)).thenReturn(true);
            when(kpiDefinitionServiceMock.doesTableHaveAnyKpisLeft(connectionMock, TABLE_3)).thenReturn(false);
            doThrow(SQLException.class).when(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_2, List.of(KPI_1_NAME_IN_TABLE_2));
            doNothing().when(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_3, List.of(KPI_1_NAME_IN_TABLE_3));

            objectUnderTest.hardDeleteNecessaryKpiDefinitions(kpisToDelete);

            verify(kpiDefinitionServiceMock).doesTableHaveAnyKpisLeft(connectionMock, TABLE_2);
            verify(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_2, List.of(KPI_1_NAME_IN_TABLE_2));
            verify(kpiExposureServiceMock).updateExposure();
        });
    }

    @Test
    @SneakyThrows
    void whenDeleteOutPutTablesThrowsException_shouldOtherTasksBeHandledCorrectly() {
        DriverManagerMock.prepare(connectionMock -> {
            List<KpiDefinitionEntity> kpisToDelete = List.of(KPI_2_IN_TABLE_2, KPI_1_IN_TABLE_3);
            when(kpiDefinitionServiceMock.doesTableHaveAnyKpisLeft(connectionMock, TABLE_2)).thenReturn(false);
            doThrow(SQLException.class).when(databaseServiceMock).deleteTables(connectionMock, List.of(TABLE_NAME_2));

            objectUnderTest.hardDeleteNecessaryKpiDefinitions(kpisToDelete);

            verify(kpiDefinitionServiceMock).doesTableHaveAnyKpisLeft(connectionMock, TABLE_2);
            verify(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_3, List.of(KPI_1_NAME_IN_TABLE_3));
            verify(kpiExposureServiceMock).updateExposure();
        });
    }

    @Test
    @SneakyThrows
    void whenNothingDeletedDueToExceptions_shouldNotNeedExposureUpdate() {
        DriverManagerMock.prepare(connectionMock -> {
            List<KpiDefinitionEntity> kpisToDelete = List.of(KPI_2_IN_TABLE_2, KPI_1_IN_TABLE_3);
            when(kpiDefinitionServiceMock.doesTableHaveAnyKpisLeft(connectionMock, TABLE_2)).thenReturn(false);
            when(kpiDefinitionServiceMock.doesTableHaveAnyKpisLeft(connectionMock, TABLE_3)).thenReturn(true);
            doThrow(SQLException.class).when(databaseServiceMock).deleteTables(connectionMock, List.of(TABLE_NAME_2));
            doThrow(SQLException.class).when(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_3, List.of(KPI_1_NAME_IN_TABLE_3));
            doThrow(SQLException.class).when(databaseServiceMock).deleteColumnsForTable(connectionMock, TABLE_NAME_2, List.of(KPI_2_NAME_IN_TABLE_2));
            objectUnderTest.hardDeleteNecessaryKpiDefinitions(kpisToDelete);

            verify(kpiExposureServiceMock, never()).updateExposure();
        });
    }


    @Test
    @SneakyThrows
    void whenTransactionExecutorThrowsException_shouldHardDeleteNotThrow() {
        try (final MockedStatic<TransactionExecutor> transactionExecutorMockedStatic = mockStatic(TransactionExecutor.class)) {
            final MockedStatic.Verification transactionVerification = () -> TransactionExecutor.executeInTransaction(operationRunnerArgumentCaptor.capture());
            transactionExecutorMockedStatic.when(transactionVerification).thenThrow(SQLException.class);

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.hardDeleteNecessaryKpiDefinitions(List.of()));
        }
    }

    private static KpiDefinitionEntity entity(final String name, final String alias) {
        return KpiDefinitionEntity
                .builder()
                .withName(name)
                .withAlias(alias)
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .build();
    }
}
