/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.OperationRunner;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodManager;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodMemoizer;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartitionRetentionFacadeTest {
    static final UUID COLLECTION_ID = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");

    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    KpiDefinitionDeletionFacade definitionDeletionFacadeMock;
    @Mock
    RetentionPeriodManager retentionPeriodManagerMock;
    @Mock
    PartitionRetentionManager partitionRetentionManagerMock;

    @InjectMocks
    PartitionRetentionFacade objectUnderTest;

    @Captor
    ArgumentCaptor<OperationRunner> operationRunnerArgumentCaptor;

    List<KpiDefinitionEntity> kpiDefinitionEntities = new ArrayList<>();

    KpiDefinitionEntity entityFirstOfAlias1;
    KpiDefinitionEntity entitySecondOfAlias1;
    KpiDefinitionEntity entityFirstOfAlias2;
    KpiDefinitionEntity entitySecondOfAlias2;

    @BeforeEach
    void init() {
        entityFirstOfAlias1 = createDefinitionForAttributes("alias_1", 1_440, new String[]{"agg_1", "agg_2"});
        entitySecondOfAlias1 = createDefinitionForAttributes("alias_1", 1_440, new String[]{"agg_1", "agg_2"});
        entityFirstOfAlias2 = createDefinitionForAttributes("alias_2", 60, new String[]{"agg_1"}).timeDeleted(LocalDateTime.of(2019, 5, 3, 12, 0));
        entitySecondOfAlias2 = createDefinitionForAttributes("alias_2", 60, new String[]{"agg_1"}).timeDeleted(LocalDateTime.of(2019, 5, 1, 12, 0));

        kpiDefinitionEntities.add(entityFirstOfAlias1);
        kpiDefinitionEntities.add(entitySecondOfAlias1);
        kpiDefinitionEntities.add(entityFirstOfAlias2);
        kpiDefinitionEntities.add(entitySecondOfAlias2);
        kpiDefinitionEntities.add(createDefinitionForAttributes("alias_4", -1, new String[]{"agg_1", "agg_2"}));
        kpiDefinitionEntities.add(createDefinitionForAttributes("alias_4", -1, new String[]{"agg_1", "agg_2"}));
    }

    @Test
    @SneakyThrows
    void whenNoDefinitionsReturnedAreEmpty_thenNoRetentionExecuted() {
        doReturn(new ArrayList<>()).when(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();

        objectUnderTest.runRetention();

        verify(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();
        verify(definitionDeletionFacadeMock).hardDeleteNecessaryKpiDefinitions(any(List.class));
    }


    @Test
    void whenDefinitionsRetentionIsOver_thenDefinitionsDeleted(@Mock Connection connectionMock) {
        try (MockedStatic<TransactionExecutor> transactionExecutorMockedStatic = mockStatic(TransactionExecutor.class)) {
            final MockedStatic.Verification transactionVerification = () -> TransactionExecutor.executeInTransaction(operationRunnerArgumentCaptor.capture());
            transactionExecutorMockedStatic.when(transactionVerification).thenAnswer(invocation -> null);

            final RetentionPeriodMemoizer retentionPeriodMemoizerMock = mock(RetentionPeriodMemoizer.class);

            doReturn(kpiDefinitionEntities).when(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();

            when(retentionPeriodManagerMock.retentionPeriodMemoizer()).thenReturn(retentionPeriodMemoizerMock);
            when(retentionPeriodMemoizerMock.computeRetentionPeriod(any(KpiDefinitionEntity.class))).thenReturn(2);
            when(partitionRetentionManagerMock.getRetentionDate(2)).thenReturn(LocalDateTime.parse("2019-05-03T00:00:00"));

            objectUnderTest.runRetention();
            runAllOperationRunner(connectionMock);

            verify(definitionDeletionFacadeMock).hardDeleteNecessaryKpiDefinitions(anyList());
        }
    }

    @Test
    @SneakyThrows
    void whenRetentionIsTriggered_andDoRetentionActivityOnAllTables(@Mock Connection connectionMock) {
        try (MockedStatic<TransactionExecutor> transactionExecutorMockedStatic = mockStatic(TransactionExecutor.class)) {
            final MockedStatic.Verification transactionVerification = () -> TransactionExecutor.executeInTransaction(operationRunnerArgumentCaptor.capture());
            transactionExecutorMockedStatic.when(transactionVerification).thenAnswer(invocation -> null);

            final RetentionPeriodMemoizer retentionPeriodMemoizerMock = mock(RetentionPeriodMemoizer.class);
            when(retentionPeriodManagerMock.retentionPeriodMemoizer()).thenReturn(retentionPeriodMemoizerMock);

            doReturn(List.of(entityFirstOfAlias1, entityFirstOfAlias2)).when(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();

            when(retentionPeriodMemoizerMock.computeRetentionPeriod(entityFirstOfAlias1)).thenReturn(2);
            when(retentionPeriodMemoizerMock.computeRetentionPeriod(entityFirstOfAlias2)).thenReturn(10);
            when(partitionRetentionManagerMock.getRetentionDate(2)).thenReturn(LocalDateTime.parse("2019-05-03T00:00:00"));
            when(partitionRetentionManagerMock.getRetentionDate(10)).thenReturn(LocalDateTime.parse("2019-04-25T00:00:00"));

            objectUnderTest.runRetention();
            runAllOperationRunner(connectionMock);


            transactionExecutorMockedStatic.verify(transactionVerification, times(2));
            verify(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();
            verify(partitionRetentionManagerMock).runCleanUpForPartitionedTable(connectionMock, entityFirstOfAlias1.tableName(), LocalDateTime.parse("2019-05-03T00:00:00"));
            verify(partitionRetentionManagerMock).createNewPartitionForTable(connectionMock, entityFirstOfAlias1.tableName(), Set.of("aggregation_begin_time", "agg_1", "agg_2"), LocalDateTime.parse("2019-05-03T00:00:00"));
            verify(partitionRetentionManagerMock).runCleanUpForPartitionedTable(connectionMock, entityFirstOfAlias2.tableName(), LocalDateTime.parse("2019-04-25T00:00:00"));
            verify(partitionRetentionManagerMock).createNewPartitionForTable(connectionMock, entityFirstOfAlias2.tableName(), Set.of("aggregation_begin_time", "agg_1"), LocalDateTime.parse("2019-04-25T00:00:00"));
        }
    }

    @Test
    @SneakyThrows
    void whenRetentionIsTriggered_andDoRetentionActivityOnAllTablesOnce(@Mock Connection connectionMock) {
        try (MockedStatic<TransactionExecutor> transactionExecutorMockedStatic = mockStatic(TransactionExecutor.class)) {
            final MockedStatic.Verification transactionVerification = () -> TransactionExecutor.executeInTransaction(operationRunnerArgumentCaptor.capture());
            transactionExecutorMockedStatic.when(transactionVerification).thenAnswer(invocation -> null);

            final RetentionPeriodMemoizer retentionPeriodMemoizerMock = mock(RetentionPeriodMemoizer.class);
            when(retentionPeriodManagerMock.retentionPeriodMemoizer()).thenReturn(retentionPeriodMemoizerMock);

            doReturn(List.of(entityFirstOfAlias1, entitySecondOfAlias1, entityFirstOfAlias2)).when(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();

            when(retentionPeriodMemoizerMock.computeRetentionPeriod(entityFirstOfAlias1)).thenReturn(2);
            when(retentionPeriodMemoizerMock.computeRetentionPeriod(entitySecondOfAlias1)).thenReturn(2);
            when(retentionPeriodMemoizerMock.computeRetentionPeriod(entityFirstOfAlias2)).thenReturn(10);

            when(partitionRetentionManagerMock.getRetentionDate(2)).thenReturn(LocalDateTime.parse("2019-05-02T00:00:00"));
            when(partitionRetentionManagerMock.getRetentionDate(10)).thenReturn(LocalDateTime.parse("2019-04-25T00:00:00"));

            objectUnderTest.runRetention();
            runAllOperationRunner(connectionMock);


            transactionExecutorMockedStatic.verify(transactionVerification, times(2));
            verify(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();
            verify(partitionRetentionManagerMock).runCleanUpForPartitionedTable(connectionMock, entityFirstOfAlias1.tableName(), LocalDateTime.parse("2019-05-02T00:00:00"));
            verify(partitionRetentionManagerMock).createNewPartitionForTable(connectionMock, entityFirstOfAlias1.tableName(), Set.of("aggregation_begin_time", "agg_1", "agg_2"), LocalDateTime.parse("2019-05-02T00:00:00"));
            verify(partitionRetentionManagerMock).runCleanUpForPartitionedTable(connectionMock, entityFirstOfAlias2.tableName(), LocalDateTime.parse("2019-04-25T00:00:00"));
            verify(partitionRetentionManagerMock).createNewPartitionForTable(connectionMock, entityFirstOfAlias2.tableName(), Set.of("aggregation_begin_time", "agg_1"), LocalDateTime.parse("2019-04-25T00:00:00"));
        }
    }

    @Test
    void whenRetentionIsTriggered_andExceptionOccurredWhileGettingDefinitionsFromDB_thenCorrectExceptionIsThrown() {
        doThrow(new UncheckedSqlException("Exception occurred")).when(kpiDefinitionServiceMock).findAllIncludingSoftDeleted();

        assertThatThrownBy(() -> objectUnderTest.runRetention())
                .isInstanceOf(UncheckedSqlException.class);
    }

    private void runAllOperationRunner(Connection connectionMock) {
        operationRunnerArgumentCaptor.getAllValues().stream().forEach(runner -> {
            try {
                runner.run(connectionMock);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private KpiDefinitionEntity createDefinitionForAttributes(final String alias, final Integer aggregationPeriod, final String[] aggregationElements) {
        return KpiDefinitionEntity.builder()
                .withCollectionId(COLLECTION_ID)
                .withName("name")
                .withExpression("dummyExpression")
                .withAlias(alias)
                .withAggregationPeriod(aggregationPeriod)
                .withAggregationElements(List.of(aggregationElements))
                .withFilters(List.of())
                .build();
    }

}