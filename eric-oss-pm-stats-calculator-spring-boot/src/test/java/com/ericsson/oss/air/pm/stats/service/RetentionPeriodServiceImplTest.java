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

import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectCollectionLevelEntity;
import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectTableLevelEntities;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;
import com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import kpi.model.KpiDefinitionRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetentionPeriodServiceImplTest {

    @Mock
    TableRetentionRepository tableRetentionRepositoryMock;
    @Mock
    CollectionRetentionRepository collectionRetentionRepositoryMock;

    @InjectMocks
    RetentionPeriodServiceImpl objectUnderTest;

    final UUID collectionId = UUID.fromString("89d3713a-5499-4403-8072-1b18235946e9");

    @Test
    void shouldSaveToTableRepository(@Mock final KpiDefinitionRequest definitionRequestMock) {
        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtils = mockStatic(RetentionPeriodUtils.class)) {
            DriverManagerMock.prepare(connectionMock -> {
                retentionPeriodUtils.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(tableEntity()));
                retentionPeriodUtils.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionId)).thenReturn(Optional.empty());

                objectUnderTest.insertRetentionPeriod(definitionRequestMock, collectionId);

                verify(tableRetentionRepositoryMock).saveAll(connectionMock, List.of(tableEntity()));
                verify(collectionRetentionRepositoryMock, never()).save(any(), any());
            });
        }
    }

    @Test
    void shouldSaveToCollectionRepository(@Mock final KpiDefinitionRequest definitionRequestMock) {
        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtils = mockStatic(RetentionPeriodUtils.class)) {
            DriverManagerMock.prepare(connectionMock -> {
                retentionPeriodUtils.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(emptyList());
                retentionPeriodUtils.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionId)).thenReturn(Optional.of(collectionEntity()));

                objectUnderTest.insertRetentionPeriod(definitionRequestMock, collectionId);

                verify(tableRetentionRepositoryMock, never()).saveAll(any(), anyList());
                verify(collectionRetentionRepositoryMock).save(connectionMock, collectionEntity());
            });
        }
    }

    @Test
    void shouldSaveToAllRepository(@Mock final KpiDefinitionRequest definitionRequestMock) {
        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtils = mockStatic(RetentionPeriodUtils.class)) {
            DriverManagerMock.prepare(connectionMock -> {

                retentionPeriodUtils.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(tableEntity()));
                retentionPeriodUtils.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionId)).thenReturn(Optional.of(collectionEntity()));

                objectUnderTest.insertRetentionPeriod(definitionRequestMock, collectionId);

                verify(tableRetentionRepositoryMock).saveAll(connectionMock, List.of(tableEntity()));
                verify(collectionRetentionRepositoryMock).save(connectionMock, collectionEntity());
            });
        }
    }

    @Test
    void shouldReturnTableEntitiesByCollectionId() {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");
        final List<RetentionPeriodTableEntity> periodTableEntities = new ArrayList<>();

        when(tableRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(periodTableEntities);

        final List<RetentionPeriodTableEntity> actual = objectUnderTest.findTablePeriods(collectionId);

        Assertions.assertThat(actual).isEqualTo(periodTableEntities);
    }

    @Test
    void shouldReturnCollectionEntityByCollectionId() {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        final Optional<RetentionPeriodCollectionEntity> retentionPeriodCollectionEntity = Optional.empty();
        when(collectionRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(retentionPeriodCollectionEntity);

        final Optional<RetentionPeriodCollectionEntity> actual = objectUnderTest.findCollectionPeriods(collectionId);

        Assertions.assertThat(actual).isEqualTo(retentionPeriodCollectionEntity);

    }

    @Test
    void shouldDeleteRetentionForTable(@Mock final Connection connectionMock) throws SQLException {
        objectUnderTest.deleteFromTableLevelRetention(connectionMock, "table");

        verify(tableRetentionRepositoryMock).deleteRetentionForTables(connectionMock, "table");
    }

    private static RetentionPeriodTableEntity tableEntity() {
        return RetentionPeriodTableEntity.builder().build();
    }

    private static RetentionPeriodCollectionEntity collectionEntity() {
        return RetentionPeriodCollectionEntity.builder().build();
    }
}