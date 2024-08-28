/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.retention;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity.RetentionPeriodCollectionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity.RetentionPeriodTableEntityBuilder;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RetentionPeriodMemoizerTest {
    final TableRetentionRepository tableRetentionRepositoryMock = mock(TableRetentionRepository.class);
    final CollectionRetentionRepository collectionRetentionRepositoryMock = mock(CollectionRetentionRepository.class);
    final EnvironmentValue<Duration> retentionPeriodDays = EnvironmentValue.of(Duration.ofDays(5));

    final RetentionPeriodMemoizer objectUnderTest = new RetentionPeriodMemoizer(
            tableRetentionRepositoryMock, collectionRetentionRepositoryMock, retentionPeriodDays
    );

    @Test
    void shouldReturnTableRetentionPeriod_forKpiEntity() {
        final UUID collectionId = UUID.fromString("2d1c153f-8792-4e2a-8daf-e37bfa24f804");
        final String tableName = "kpi_sector_60";

        when(tableRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(List.of(
                tableEntity(1L, collectionId, "random_table", 13),
                tableEntity(2L, collectionId, tableName, 11)
        ));

        final KpiDefinitionEntityBuilder entityBuilder = KpiDefinitionEntity.builder();
        entityBuilder.withCollectionId(collectionId);
        entityBuilder.withAlias("sector");
        entityBuilder.withAggregationPeriod(60);
        final KpiDefinitionEntity kpiDefinitionEntity = entityBuilder.build();

        final int actual = objectUnderTest.computeRetentionPeriod(kpiDefinitionEntity);

        verify(collectionRetentionRepositoryMock, never()).findByCollectionId(any(UUID.class));

        Assertions.assertThat(actual).isEqualTo(11);
    }

    @Test
    void shouldReturnTableRetentionPeriod_whenRetentionPeriodForTableIsDefined() {
        final UUID collectionId = UUID.fromString("2d1c153f-8792-4e2a-8daf-e37bfa24f804");
        final String tableName = "tableName";

        when(tableRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(List.of(
                tableEntity(1L, collectionId, "random_table", 13),
                tableEntity(2L, collectionId, tableName, 11)
        ));

        final int actual = objectUnderTest.computeRetentionPeriod(collectionId, tableName);

        verify(collectionRetentionRepositoryMock, never()).findByCollectionId(any(UUID.class));

        Assertions.assertThat(actual).isEqualTo(11);
    }

    @Test
    void shouldReturnCollectionRetentionPeriod_whenRetentionPeriodOnlyDefinedForCollection() {
        final UUID collectionId = UUID.fromString("2d1c153f-8792-4e2a-8daf-e37bfa24f804");
        final String tableName = "tableName";

        when(tableRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(List.of(
                tableEntity(1L, collectionId, "random_table", 13)
        ));
        when(collectionRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(Optional.of(
                collectionEntity(10L, collectionId, 9)
        ));

        final int actual = objectUnderTest.computeRetentionPeriod(collectionId, tableName);

        Assertions.assertThat(actual).isEqualTo(9);
    }

    @Test
    void shouldReturnDefaultRetentionPeriod_whenRetentionPeriodIsNotDefined() {
        final UUID collectionId = UUID.fromString("2d1c153f-8792-4e2a-8daf-e37bfa24f804");
        final String tableName = "tableName";

        when(tableRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(emptyList());
        when(collectionRetentionRepositoryMock.findByCollectionId(collectionId)).thenReturn(Optional.empty());

        final int actual = objectUnderTest.computeRetentionPeriod(collectionId, tableName);

        Assertions.assertThat(actual).isEqualTo(5);
    }

    static RetentionPeriodCollectionEntity collectionEntity(final Long id, final UUID kpiCollectionId, final Integer retentionPeriodInDays) {
        final RetentionPeriodCollectionEntityBuilder builder = RetentionPeriodCollectionEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(kpiCollectionId);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }

    static RetentionPeriodTableEntity tableEntity(final Long id, final UUID kpiCollectionId, final String tableName, final Integer retentionPeriodInDays) {
        final RetentionPeriodTableEntityBuilder builder = RetentionPeriodTableEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(kpiCollectionId);
        builder.withTableName(tableName);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }
}