/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiCounter;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import kpi.model.KpiDefinitionRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionServiceImplTest {
    @Mock private KpiDefinitionRepository kpiDefinitionRepositoryMock;
    @Mock private SimpleKpiDependencyCache simpleKpiDependencyCacheMock;
    @Mock private KpiDefinitionAdapter kpiDefinitionAdapterMock;
    @Mock private DatabaseService databaseServiceMock;
    @Spy private ApiMetricRegistry apiMetricRegistrySpy;

    @InjectMocks private KpiDefinitionServiceImpl objectUnderTest;

    @MethodSource("provideDoesContainData")
    @ParameterizedTest(name = "[{index}] Should return ''{1}'' when database has rows: ''{0}''")
    void shouldVerify_doesContainMethod(final Long numberOfRows, final boolean expectedValue) {
        when(kpiDefinitionRepositoryMock.count()).thenReturn(numberOfRows);

        final boolean actual = objectUnderTest.doesContainData();

        verify(kpiDefinitionRepositoryMock).count();

        Assertions.assertThat(actual).isEqualTo(expectedValue);
    }

    @Test
    void shouldSaveDefinition_onUpsert(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinitionMock)).thenReturn(List.of(kpiDefinitionEntityMock));

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.upsert(kpiDefinitionMock));

            verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinitionMock);
            verify(kpiDefinitionRepositoryMock).save(connectionMock, kpiDefinitionEntityMock);
            verify(simpleKpiDependencyCacheMock).populateCache();
        });
    }

    @Test
    void shouldUpdate_onUpsert(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinitionMock)).thenReturn(List.of(kpiDefinitionEntityMock));

            doThrow(new SQLException("Already exists")).when(kpiDefinitionRepositoryMock).save(connectionMock, kpiDefinitionEntityMock);

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.upsert(kpiDefinitionMock));

            verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinitionMock);
            verify(kpiDefinitionRepositoryMock).save(connectionMock, kpiDefinitionEntityMock);
            verify(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);
            verify(simpleKpiDependencyCacheMock).populateCache();
        });
    }

    @Test
    void shouldThrowKpiPersistenceException_whenCannotSaveOrUpdate_onUpsert(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinitionMock)).thenReturn(List.of(kpiDefinitionEntityMock));
            doThrow(new SQLException("Already exists")).when(kpiDefinitionRepositoryMock).save(connectionMock, kpiDefinitionEntityMock);
            doThrow(new SQLException("Field cannot be null")).when(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.upsert(kpiDefinitionMock))
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("Field cannot be null")
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Error persisting into 'kpi_definition' table.");

            verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinitionMock);
            verify(kpiDefinitionRepositoryMock).save(connectionMock, kpiDefinitionEntityMock);
            verify(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);
        });
    }

    @Test
    void shouldSaveDefinition_onInsert(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock final KpiDefinitionEntity kpiDefinitionEntityMock,
                                       @Mock final UUID collectionIdMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionAdapterMock.toListOfCollectionEntities(kpiDefinitionMock, collectionIdMock)).thenReturn(List.of(kpiDefinitionEntityMock));

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.insert(kpiDefinitionMock, collectionIdMock));

            verify(kpiDefinitionAdapterMock).toListOfCollectionEntities(kpiDefinitionMock,collectionIdMock);
            verify(kpiDefinitionRepositoryMock).saveAll(connectionMock, List.of(kpiDefinitionEntityMock));
            verify(apiMetricRegistrySpy).counter(ApiCounter.DEFINITION_PERSISTED_KPI);
            verify(simpleKpiDependencyCacheMock).populateCache();
        });
    }

    @Test
    void shouldThrowKpiPersistenceException_whenCannotSave(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock final KpiDefinitionEntity kpiDefinitionEntityMock,
                                                           @Mock final UUID collectionIdMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionAdapterMock.toListOfCollectionEntities(kpiDefinitionMock, collectionIdMock)).thenReturn(List.of(kpiDefinitionEntityMock));
            doThrow(new SQLException("Already exists")).when(kpiDefinitionRepositoryMock).saveAll(connectionMock,
                    List.of(kpiDefinitionEntityMock));

            Assertions.assertThatThrownBy(() -> objectUnderTest.insert(kpiDefinitionMock, collectionIdMock))
                      .hasRootCauseInstanceOf(SQLException.class)
                      .isInstanceOf(KpiPersistenceException.class)
                      .hasMessage("Error persisting into 'kpi_definition' table.");

            verify(kpiDefinitionAdapterMock).toListOfCollectionEntities(kpiDefinitionMock, collectionIdMock);
            verify(kpiDefinitionRepositoryMock).saveAll(connectionMock, List.of(kpiDefinitionEntityMock));
            verifyNoInteractions(simpleKpiDependencyCacheMock);
        });
    }

    @Test
    void shouldThrowKpiPersistenceException_whenCannotUpdateTimeDeletedField(@Mock final List<String> kpiDefinitionNames) {
        DriverManagerMock.prepare(connectionMock -> {
            doThrow(new SQLException("Already exists")).when(kpiDefinitionRepositoryMock).softDelete(connectionMock, kpiDefinitionNames, DEFAULT_COLLECTION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.softDelete(kpiDefinitionNames, DEFAULT_COLLECTION_ID))
                    .hasRootCauseInstanceOf(SQLException.class)
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Error updating time_deleted field for kpi definitions.");

            verify(kpiDefinitionRepositoryMock).softDelete(connectionMock, kpiDefinitionNames, DEFAULT_COLLECTION_ID);
            verifyNoInteractions(simpleKpiDependencyCacheMock);
        });
    }

    @Test
    void shouldUpdateTimeDeletedWithCurrent_NoExceptionShouldBeThrown(@Mock final List<String> kpiDefinitionNames) {
        DriverManagerMock.prepare(connectionMock -> {
            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.softDelete(kpiDefinitionNames, DEFAULT_COLLECTION_ID));

            verify(kpiDefinitionRepositoryMock).softDelete(connectionMock, kpiDefinitionNames, DEFAULT_COLLECTION_ID);
            verify(simpleKpiDependencyCacheMock).populateCache();
        });
    }

    @Test
    void shouldUpdateWithoutColumnTypeChange(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        DriverManagerMock.prepare(connectionMock -> {
            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.updateWithoutColumnTypeChange(kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID));

            verify(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);
            verify(simpleKpiDependencyCacheMock).populateCache();
            verifyNoInteractions(databaseServiceMock);
        });
    }

    @Test
    void shouldUpdateWithColumnTypeChange(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        DriverManagerMock.prepare(connectionMock -> {
            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.updateWithColumnTypeChange(kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID));

            verify(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);
            verify(databaseServiceMock).changeColumnType(connectionMock, kpiDefinitionEntityMock);
            verify(simpleKpiDependencyCacheMock).populateCache();
        });
    }

    @Test
    void shouldThrowKpiPersistenceException_whenCannotUpdate(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        DriverManagerMock.prepare(connectionMock -> {
            doThrow(new SQLException("Field cannot be null")).when(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);
            Assertions.assertThatThrownBy(() -> objectUnderTest.updateWithColumnTypeChange(kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID))
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("Field cannot be null")
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Error persisting into 'kpi_definition' table.");

            verify(kpiDefinitionRepositoryMock).update(connectionMock, kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);
            verifyNoInteractions(simpleKpiDependencyCacheMock);
        });
    }

    @Test
    void shouldFindAll() {
        when(kpiDefinitionRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        final List<KpiDefinitionEntity> actual = objectUnderTest.findAll();

        verify(kpiDefinitionRepositoryMock).findAll();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllWithCollectionId() {
        final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");
        when(kpiDefinitionRepositoryMock.findAll(collectionId)).thenReturn(Collections.emptyList());

        final List<KpiDefinitionEntity> actual = objectUnderTest.findAll(collectionId);

        verify(kpiDefinitionRepositoryMock).findAll(collectionId);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllIncludingSoftDeleted() {
        when(kpiDefinitionRepositoryMock.findAllIncludingSoftDeleted()).thenReturn(Collections.emptyList());

        final List<KpiDefinitionEntity> actual = objectUnderTest.findAllIncludingSoftDeleted();

        verify(kpiDefinitionRepositoryMock).findAllIncludingSoftDeleted();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllIncludingSoftDeletedWithCollectionId() {
        final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");

        when(kpiDefinitionRepositoryMock.findAllIncludingSoftDeleted(collectionId)).thenReturn(Collections.emptyList());

        final List<KpiDefinitionEntity> actual = objectUnderTest.findAllIncludingSoftDeleted(collectionId);

        verify(kpiDefinitionRepositoryMock).findAllIncludingSoftDeleted(collectionId);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllKpiNames(@Mock final Set<String> kpiNamesMock) {
        when(kpiDefinitionRepositoryMock.findAllKpiNames()).thenReturn(kpiNamesMock);

        final Set<String> actual = objectUnderTest.findAllKpiNames();

        verify(kpiDefinitionRepositoryMock).findAllKpiNames();

        Assertions.assertThat(actual).isEqualTo(kpiNamesMock);
    }

    @Test
    void shouldFindAllKpiNamesWithCollectionId(@Mock final Set<String> kpiNamesMock) {
        when(kpiDefinitionRepositoryMock.findAllKpiNames(DEFAULT_COLLECTION_ID)).thenReturn(kpiNamesMock);

        final Set<String> actual = objectUnderTest.findAllKpiNames(DEFAULT_COLLECTION_ID);

        verify(kpiDefinitionRepositoryMock).findAllKpiNames(DEFAULT_COLLECTION_ID);

        Assertions.assertThat(actual).isEqualTo(kpiNamesMock);
    }

    @Test
    void shouldFindAllSimpleExecutionGroups(@Mock final Set<String> simpleExecutionGroupsMock) {
        when(kpiDefinitionRepositoryMock.findAllSimpleExecutionGroups()).thenReturn(simpleExecutionGroupsMock);

        final Set<String> actual = objectUnderTest.findAllSimpleExecutionGroups();

        verify(kpiDefinitionRepositoryMock).findAllSimpleExecutionGroups();

        Assertions.assertThat(actual).isEqualTo(simpleExecutionGroupsMock);
    }

    @Test
    void shouldFindAllComplexExecutionGroups(@Mock final Set<String> complexExecutionGroupsMock) {
        when(kpiDefinitionRepositoryMock.findAllComplexExecutionGroups()).thenReturn(complexExecutionGroupsMock);

        final Set<String> actual = objectUnderTest.findAllComplexExecutionGroups();

        verify(kpiDefinitionRepositoryMock).findAllComplexExecutionGroups();

        Assertions.assertThat(actual).isEqualTo(complexExecutionGroupsMock);
    }

    @Test
    void shouldFindAllOnDemandAliasAndAggregationPeriods(@Mock final Set<Pair<String, Integer>> onDemandAliasAndAggregationPeriodMock) {
        when(kpiDefinitionRepositoryMock.findOnDemandAliasAndAggregationPeriods()).thenReturn(onDemandAliasAndAggregationPeriodMock);

        final Set<Pair<String, Integer>> actual = objectUnderTest.findOnDemandAliasAndAggregationPeriods();

        verify(kpiDefinitionRepositoryMock).findOnDemandAliasAndAggregationPeriods();

        Assertions.assertThat(actual).isEqualTo(onDemandAliasAndAggregationPeriodMock);
    }

    @Test
    void shouldFindAllScheduledAliasAndAggregationPeriods(@Mock final Set<Pair<String, Integer>> scheduledAliasAndAggregationPeriodsMock) {
        when(kpiDefinitionRepositoryMock.findScheduledAliasAndAggregationPeriods()).thenReturn(scheduledAliasAndAggregationPeriodsMock);

        final Set<Pair<String, Integer>> actual = objectUnderTest.findScheduledAliasAndAggregationPeriods();

        verify(kpiDefinitionRepositoryMock).findScheduledAliasAndAggregationPeriods();

        Assertions.assertThat(actual).isEqualTo(scheduledAliasAndAggregationPeriodsMock);
    }

    @Test
    void shouldFindAllSimpleKpiNamesByExecutionGroups() {
        when(kpiDefinitionRepositoryMock.findAllSimpleKpiNamesGroupedByExecGroups()).thenReturn(Collections.emptyMap());

        final Map<String, List<String>> actual = objectUnderTest.findAllSimpleKpiNamesGroupedByExecGroups();

        verify(kpiDefinitionRepositoryMock).findAllSimpleKpiNamesGroupedByExecGroups();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllComplexKpiNamesAndExecutionGroups() {
        when(kpiDefinitionRepositoryMock.findAllComplexKpiNamesGroupedByExecGroups()).thenReturn(Collections.emptyMap());

        final Map<String, List<String>> actual = objectUnderTest.findAllComplexKpiNamesGroupedByExecGroups();

        verify(kpiDefinitionRepositoryMock).findAllComplexKpiNamesGroupedByExecGroups();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllKpiNamesInSameTable() {
        when(kpiDefinitionRepositoryMock.findAllKpiNamesWithAliasAndAggregationPeriod("alias", 60)).thenReturn(Collections.emptyList());

        final List<String> actual = objectUnderTest.findAllKpiNamesFromSameTableAsKpiDefinition(KpiDefinitionEntity.builder().withAlias("alias").withAggregationPeriod(60).build());

        verify(kpiDefinitionRepositoryMock).findAllKpiNamesWithAliasAndAggregationPeriod("alias", 60);

        Assertions.assertThat(actual).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] table name: ''{0}'', count: ''{1}'', expected: ''{2}''")
    @CsvSource(value = {"kpi_alias_60,69,true", "kpi_alias_1440,0,false", "kpi_alias_,420,true"})
    void shouldCheckIfTableHasAnyKpisLeft(final String tableName, final Integer count, final boolean expected) {
        final Connection connectionMock = mock(Connection.class);
        final Table table = Table.of(tableName);
        when(kpiDefinitionRepositoryMock.countKpisWithAliasAndAggregationPeriod(connectionMock, table)).thenReturn(count);

        final Boolean actual = objectUnderTest.doesTableHaveAnyKpisLeft(connectionMock, table);

        verify(kpiDefinitionRepositoryMock).countKpisWithAliasAndAggregationPeriod(connectionMock, table);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldFindAllKpisByExecutionGroup(@Mock final List<KpiDefinitionEntity> definitionsMock) {
        final String executionGroup = "execution-group";

        when(kpiDefinitionRepositoryMock.findKpiDefinitionsByExecutionGroup(executionGroup)).thenReturn(definitionsMock);

        final List<KpiDefinitionEntity> actual = objectUnderTest.findKpiDefinitionsByExecutionGroup(executionGroup);

        Assertions.assertThat(actual).isEqualTo(definitionsMock);
    }

    @Test
    void shouldFindAllOnDemandKpisByCalculationId(@Mock final List<KpiDefinitionEntity> definitionsMock) {
        final UUID calculationId = UUID.fromString("d3c4822a-6fdf-48ef-9588-1fab4af409a7");

        when(kpiDefinitionRepositoryMock.findOnDemandKpiDefinitionsByCalculationId(calculationId)).thenReturn(definitionsMock);

        final List<KpiDefinitionEntity> actual = objectUnderTest.findOnDemandKpiDefinitionsByCalculationId(calculationId);

        verify(kpiDefinitionRepositoryMock).findOnDemandKpiDefinitionsByCalculationId(calculationId);

        Assertions.assertThat(actual).isEqualTo(definitionsMock);
    }


    @Test
    void shouldForceFindByName(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        when(kpiDefinitionRepositoryMock.forceFindByName("name", DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntityMock);

        final KpiDefinitionEntity actual = objectUnderTest.forceFindByName("name", DEFAULT_COLLECTION_ID);

        verify(kpiDefinitionRepositoryMock).forceFindByName("name", DEFAULT_COLLECTION_ID);

        Assertions.assertThat(actual).isEqualTo(kpiDefinitionEntityMock);
    }

    @Test
    void shouldSaveRelationsBetweenCalculationAndOnDemandKpis() {
        final UUID calculationId = UUID.fromString("d3c4822a-6fdf-48ef-9588-1fab4af409a7");

        objectUnderTest.saveOnDemandCalculationRelation(Collections.emptySet(), calculationId);

        verify(kpiDefinitionRepositoryMock).saveOnDemandCalculationRelation(Collections.emptySet(), calculationId);
    }

    @Test
    void shouldCountComplexKpi() {
        when(kpiDefinitionRepositoryMock.countComplexKpi()).thenReturn(10L);

        final long actual = objectUnderTest.countComplexKpi();

        verify(kpiDefinitionRepositoryMock).countComplexKpi();

        Assertions.assertThat(actual).isEqualTo(10L);
    }

    @Test
    void shouldDeleteKpiDefinition(@Mock final Connection connectionMock) {
        final List<String> kpiNames = List.of("kpi1", "kpi2");

        objectUnderTest.deleteKpiDefinitionsByName(connectionMock, kpiNames);

        verify(kpiDefinitionRepositoryMock).deleteKpiDefinitionsByName(connectionMock, kpiNames);
    }

    private static Stream<Arguments> provideDoesContainData() {
        return Stream.of(Arguments.of(5L, true),
                Arguments.of(0L, false));
    }
}