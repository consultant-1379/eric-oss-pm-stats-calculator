/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util.retention;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity.RetentionPeriodCollectionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity.RetentionPeriodTableEntityBuilder;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RetentionPeriodUtils {

    public static List<RetentionPeriodTableEntity> collectTableLevelEntities(final KpiDefinitionRequest kpiDefinitionRequest, final UUID collectionId) {
        return kpiDefinitionRequest.tables().stream()
                .filter(table -> table.retentionPeriod().value() != null)
                .map(table -> mapRetentionPeriodToTableLevelEntity(collectionId, table))
                .collect(Collectors.toList());
    }

    public static Optional<RetentionPeriodCollectionEntity> collectCollectionLevelEntity(final KpiDefinitionRequest kpiDefinitionRequest, final UUID collectionId) {
        return kpiDefinitionRequest.retentionPeriod().value() != null
                ? Optional.of(mapRetentionPeriodCollectionLevelEntity(kpiDefinitionRequest, collectionId))
                : Optional.empty();
    }

    static RetentionPeriodTableEntity mapRetentionPeriodToTableLevelEntity(final UUID collectionId, final Table table) {
        final RetentionPeriodTableEntityBuilder builder = RetentionPeriodTableEntity.builder();
        builder.withKpiCollectionId(collectionId);
        builder.withTableName(table.tableName());
        builder.withRetentionPeriodInDays(table.retentionPeriod().value());
        return builder.build();
    }

    static RetentionPeriodCollectionEntity mapRetentionPeriodCollectionLevelEntity(final KpiDefinitionRequest kpiDefinitionRequest, final UUID collectionId) {
        final RetentionPeriodCollectionEntityBuilder builder = RetentionPeriodCollectionEntity.builder();
        builder.withKpiCollectionId(collectionId);
        builder.withRetentionPeriodInDays(kpiDefinitionRequest.retentionPeriod().value());
        return builder.build();
    }
}
