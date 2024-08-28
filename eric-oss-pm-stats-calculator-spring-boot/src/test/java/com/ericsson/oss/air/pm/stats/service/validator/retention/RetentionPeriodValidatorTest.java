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

import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectCollectionLevelEntity;
import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectTableLevelEntities;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity.RetentionPeriodCollectionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity.RetentionPeriodTableEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.exception.RetentionPeriodValidationException;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;
import com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils;

import kpi.model.KpiDefinitionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetentionPeriodValidatorTest {
    @Mock EnvironmentValue<Duration> retentionPeriodConfiguredMaxMock;
    @Mock RetentionPeriodService retentionPeriodServiceMock;

    @InjectMocks RetentionPeriodValidator objectUnderTest;

    @Test
    void testValidateRetentionPeriod_WhenRetentionsAreValid(@Mock final KpiDefinitionRequest definitionRequestMock, @Mock final UUID collectionIdMock) {
        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionIdMock)).thenReturn(tableLevelEntities(
                    List.of(2, 2, 2)
            ));

            assertThatNoException().isThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionIdMock));
        }
    }

    @Test
    void testValidateRetentionPeriod_WhenRetentionIsAlreadyDefinedValid(@Mock final KpiDefinitionRequest definitionRequestMock, @Mock UUID collectionIdMock) {
        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionIdMock)).thenReturn(tableLevelEntities(
                    List.of(2, 4, 14)
            ));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionIdMock))
                    .isInstanceOf(RetentionPeriodValidationException.class)
                    .hasMessage("Retention period is already defined for table 'null'.");
        }
    }

    @Test
    void testValidateRetentionPeriod_WhenRetentionInvalid(@Mock final KpiDefinitionRequest definitionRequestMock, @Mock final UUID collectionIdMock) {
        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionIdMock)).thenReturn(tableLevelEntities(
                    List.of(2, 16, 14)
            ));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionIdMock))
                    .isInstanceOf(RetentionPeriodValidationException.class)
                    .hasMessage("Retention period '16' is greater than max value '15'");
        }
    }

    @Test
    void testValidateRetentionPeriodCollectionLevel_WhenValid(@Mock final KpiDefinitionRequest definitionRequestMock, @Mock final UUID collectionIdMock) {
        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionIdMock)).thenReturn(Optional.of(
                    collectionEntity(null, null, 15)
            ));

            assertThatNoException().isThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionIdMock));
        }
    }

    @Test
    void testValidateRetentionPeriodCollectionLevel_WhenInvalid(@Mock final KpiDefinitionRequest definitionRequestMock, @Mock final UUID collectionIdMock) {
        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionIdMock)).thenReturn(Optional.of(
                    collectionEntity(null, null, 16)
            ));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionIdMock))
                    .isInstanceOf(RetentionPeriodValidationException.class)
                    .hasMessage("Retention period '16' is greater than max value '15'");
        }
    }

    @Test
    void testValidateWhenTableRetentionNotInDB(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));
        when(retentionPeriodServiceMock.findTablePeriods(collectionId)).thenReturn(List.of(
                tableLevelEntity(1L, collectionId, "kpi_sector_60", 10)
        ));

        final RetentionPeriodTableEntity table_1 = tableLevelEntity(null, collectionId, "kpi_sector_1440", 5);
        final RetentionPeriodTableEntity table_2 = tableLevelEntity(null, collectionId, "kpi_cell_60", 9);

        try(final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(table_1, table_2));

            assertThatNoException().isThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId));
        }

        verify(retentionPeriodServiceMock).findTablePeriods(collectionId);
    }

    @Test
    void testValidateWhenTableRetentionAlreadyInDB_withDifferentValue(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));
        when(retentionPeriodServiceMock.findTablePeriods(collectionId)).thenReturn(List.of(
                tableLevelEntity(1L, collectionId, "kpi_sector_60", 10)
        ));

        final RetentionPeriodTableEntity table_1 = tableLevelEntity(null, collectionId, "kpi_sector_1440", 5);
        final RetentionPeriodTableEntity table_2 = tableLevelEntity(null, collectionId, "kpi_sector_60", 9);

        try(final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(table_1, table_2));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId))
                    .isInstanceOf(RetentionPeriodValidationException.class)
                    .hasMessage("Retention period is already defined for table 'kpi_sector_60' with value '10'. You cannot change it to '9'");
        }

        verify(retentionPeriodServiceMock).findTablePeriods(collectionId);
    }

    @Test
    void testValidateWhenInSameDefinition_withDifferentValue(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        final RetentionPeriodTableEntity table_1 = tableLevelEntity(null, collectionId, "kpi_sector_60", 10);
        final RetentionPeriodTableEntity table_2 = tableLevelEntity(null, collectionId, "kpi_sector_60", 9);

        try(final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(table_1, table_2));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId))
                .isInstanceOf(RetentionPeriodValidationException.class)
                .hasMessage("Retention period is already defined for table 'kpi_sector_60'.");
        }

        verify(retentionPeriodServiceMock).findTablePeriods(collectionId);
    }

    @Test
    void testValidateWhenInSameDefinition_withDifferentTablesMatchingRetention(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        final RetentionPeriodTableEntity table_1 = tableLevelEntity(null, collectionId, "kpi_sector_60", 5);
        final RetentionPeriodTableEntity table_2 = tableLevelEntity(null, collectionId, "kpi_sector_1440", 10);
        final RetentionPeriodTableEntity table_3 = tableLevelEntity(null, collectionId, "kpi_sector_60", 10);

        try(final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(table_1, table_2, table_3));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId))
                .isInstanceOf(RetentionPeriodValidationException.class)
                .hasMessage("Retention period is already defined for table 'kpi_sector_60'.");
        }

        verify(retentionPeriodServiceMock).findTablePeriods(collectionId);
    }
    @Test
    void testValidateWhenInSameDefinition_withSameValue(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));

        final RetentionPeriodTableEntity table_1 = tableLevelEntity(null, collectionId, "kpi_sector_60", 10);
        final RetentionPeriodTableEntity table_2 = tableLevelEntity(null, collectionId, "kpi_sector_60", 10);

        try(final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(table_1, table_2));

            assertThatNoException().isThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId));
        }

        verify(retentionPeriodServiceMock).findTablePeriods(collectionId);
    }

    @Test
    void testValidateWhenTableRetentionAlreadyInDB_withSameValue(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));
        when(retentionPeriodServiceMock.findTablePeriods(collectionId)).thenReturn(List.of(
                tableLevelEntity(1L, collectionId, "kpi_sector_60", 10)
        ));

        final RetentionPeriodTableEntity table_1 = tableLevelEntity(null, collectionId, "kpi_sector_1440", 5);
        final RetentionPeriodTableEntity table_2 = tableLevelEntity(null, collectionId, "kpi_sector_60", 10);

        try(final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectTableLevelEntities(definitionRequestMock, collectionId)).thenReturn(List.of(table_1, table_2));

            assertThatNoException().isThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId));
        }

        verify(retentionPeriodServiceMock).findTablePeriods(collectionId);
    }

    @Test
    void shouldFail_whenCollectionRetentionPeriodIsAlreadyPresentWithDifferentValue(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));
        when(retentionPeriodServiceMock.findCollectionPeriods(collectionId)).thenReturn(Optional.of(collectionEntity(1L, collectionId, 10)));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionId)).thenReturn(Optional.of(
                    collectionEntity(null, null, 15)
            ));

            assertThatThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId))
                    .isInstanceOf(RetentionPeriodValidationException.class)
                    .hasMessage("Retention period is already defined for collection '6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808' with value '10'. You cannot change it to '15'");
        }
    }

    @Test
    void shouldFail_whenCollectionRetentionPeriodIsAlreadyPresentWithSameValue(@Mock final KpiDefinitionRequest definitionRequestMock) {
        final UUID collectionId = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");

        when(retentionPeriodConfiguredMaxMock.value()).thenReturn(Duration.ofDays(15));
        when(retentionPeriodServiceMock.findCollectionPeriods(collectionId)).thenReturn(Optional.of(collectionEntity(1L, collectionId, 15)));

        try (final MockedStatic<RetentionPeriodUtils> retentionPeriodUtilsMock = mockStatic(RetentionPeriodUtils.class)) {
            retentionPeriodUtilsMock.when(() -> collectCollectionLevelEntity(definitionRequestMock, collectionId)).thenReturn(Optional.of(
                    collectionEntity(null, null, 15)
            ));

            assertThatNoException().isThrownBy(() -> objectUnderTest.validateRetentionPeriod(definitionRequestMock, collectionId));
        }
    }

    static RetentionPeriodCollectionEntity collectionEntity(final Long id, final UUID kpiCollectionId, final int retentionPeriodInDays) {
        final RetentionPeriodCollectionEntityBuilder builder = RetentionPeriodCollectionEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(kpiCollectionId);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }

    static List<RetentionPeriodTableEntity> tableLevelEntities(final List<Integer> retentionPeriods) {
        return retentionPeriods.stream().map(actualPeriod -> tableLevelEntity(null, null, null, actualPeriod)).collect(Collectors.toList());
    }

    static RetentionPeriodTableEntity tableLevelEntity(final Long id, final UUID collectionId, final String tableName, final int retentionPeriodInDays) {
        final RetentionPeriodTableEntityBuilder builder = RetentionPeriodTableEntity.builder();
        builder.withId(id);
        builder.withTableName(tableName);
        builder.withKpiCollectionId(collectionId);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }

}