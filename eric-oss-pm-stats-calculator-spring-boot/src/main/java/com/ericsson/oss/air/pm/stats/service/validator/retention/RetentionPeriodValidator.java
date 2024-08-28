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

import static com.ericsson.oss.air.pm.stats.model.exception.RetentionPeriodValidationException.badRequest;
import static com.ericsson.oss.air.pm.stats.model.exception.RetentionPeriodValidationException.conflict;
import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectCollectionLevelEntity;
import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectTableLevelEntities;
import static java.lang.String.format;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.lang3.compare.ComparableUtils.gt;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;

import kpi.model.KpiDefinitionRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class RetentionPeriodValidator {

    @Inject
    private EnvironmentValue<Duration> retentionPeriodConfiguredMax;

    @Inject
    private RetentionPeriodService retentionPeriodService;

    //Kpi Collection level validations will be used later. Please DO NOT remove them
    public void validateRetentionPeriod(final KpiDefinitionRequest kpiDefinitionRequest, final UUID collectionId) {

        validateCollectionLevelRetentionPeriodByCollectionId(kpiDefinitionRequest, collectionId);

        validateTableLevelRetentionPeriodByCollectionId(kpiDefinitionRequest, collectionId);
    }

    private void validateTableLevelRetentionPeriodByCollectionId(KpiDefinitionRequest kpiDefinitionRequest, UUID collectionId) {
        final List<RetentionPeriodTableEntity> tableRetentionPeriods = retentionPeriodService.findTablePeriods(collectionId);
        final Map<String, Integer> tablesWithRetention = new HashMap<>();

        collectTableLevelEntities(kpiDefinitionRequest, collectionId).forEach(retentionPeriodTableEntity -> {
            validateRetentionPeriodMaxValue(retentionPeriodTableEntity.getRetentionPeriodInDays());

            final String tableName = retentionPeriodTableEntity.getTableName();
            final Integer userDefinedPeriod = retentionPeriodTableEntity.getRetentionPeriodInDays();

            if (tablesWithRetention.containsKey(tableName) && !tablesWithRetention.get(tableName).equals(userDefinedPeriod)) {
                throw conflict(format(
                    "Retention period is already defined for table '%s'.",
                    tableName
                ));
            }
            tablesWithRetention.put(tableName, userDefinedPeriod);

            filterByTableName(tableRetentionPeriods, tableName).ifPresent(persistedTableEntity -> {
                final Integer persistedPeriod = persistedTableEntity.getRetentionPeriodInDays();

                if (persistedPeriod.equals(userDefinedPeriod)) {
                    return;
                }
                throw conflict(format(
                    "Retention period is already defined for table '%s' with value '%d'. You cannot change it to '%d'",
                    tableName, persistedPeriod, userDefinedPeriod
                ));
            });
        });
    }

    private void validateCollectionLevelRetentionPeriodByCollectionId(KpiDefinitionRequest kpiDefinitionRequest, UUID collectionId) {
        collectCollectionLevelEntity(kpiDefinitionRequest, collectionId).ifPresent(retentionPeriodCollectionEntity -> {
            validateRetentionPeriodMaxValue(retentionPeriodCollectionEntity.getRetentionPeriodInDays());

            retentionPeriodService.findCollectionPeriods(collectionId).ifPresent(persistedCollectionEntity -> {
                final Integer persistedPeriod = persistedCollectionEntity.getRetentionPeriodInDays();
                final Integer userDefinedPeriod = retentionPeriodCollectionEntity.getRetentionPeriodInDays();

                if (persistedPeriod.equals(userDefinedPeriod)) {
                    return;
                }

                throw conflict(format(
                        "Retention period is already defined for collection '%s' with value '%d'. You cannot change it to '%d'",
                        collectionId, persistedPeriod, userDefinedPeriod
                ));
            });
        });
    }

    private void validateRetentionPeriodMaxValue(final Integer retentionPeriodInDays) {
        final Duration maxRetentionPeriod = retentionPeriodConfiguredMax.value();
        final Duration retentionPeriod = Duration.ofDays(retentionPeriodInDays);

        if (gt(maxRetentionPeriod).test(retentionPeriod)) {
            throw badRequest(format("Retention period '%s' is greater than max value '%s'", retentionPeriod.toDays(), maxRetentionPeriod.toDays()));
        }
    }

    private static Optional<RetentionPeriodTableEntity> filterByTableName(final List<RetentionPeriodTableEntity> entities, final String tableName) {
        return entities.stream().filter(entity -> entity.getTableName().equals(tableName)).findFirst();
    }

}
