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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;

import org.apache.commons.lang3.concurrent.Memoizer;

public class RetentionPeriodMemoizer {
    private final EnvironmentValue<Duration> retentionPeriodDays;

    private final Memoizer<UUID, List<RetentionPeriodTableEntity>> tableEntityMemoizer;
    private final Memoizer<UUID, Optional<RetentionPeriodCollectionEntity>> collectionEntityMemoizer;

    public RetentionPeriodMemoizer(
            final TableRetentionRepository tableRetentionRepository,
            final CollectionRetentionRepository collectionRetentionRepository,
            final EnvironmentValue<Duration> retentionPeriodDays
    ) {
        this.retentionPeriodDays = retentionPeriodDays;

        tableEntityMemoizer = new Memoizer<>(tableRetentionRepository::findByCollectionId);
        collectionEntityMemoizer = new Memoizer<>(collectionRetentionRepository::findByCollectionId);
    }

    public int computeRetentionPeriod(final KpiDefinitionEntity kpiDefinitionEntity) {
        return computeRetentionPeriod(kpiDefinitionEntity.collectionId(), kpiDefinitionEntity.tableName());
    }

    public int computeRetentionPeriod(final UUID uuid, final String tableName) {
        return findTableRetentionPeriod(uuid, tableName).or(() -> findCollectionRetentionPeriod(uuid)).orElseGet(this::defaultRetentionPeriod);
    }

    private int defaultRetentionPeriod() {
        return Math.toIntExact(retentionPeriodDays.value().toDays());
    }

    private Optional<Integer> findTableRetentionPeriod(final UUID uuid, final String tableName) {
        return findTableEntity(uuid, tableName).map(RetentionPeriodTableEntity::getRetentionPeriodInDays);
    }

    private Optional<Integer> findCollectionRetentionPeriod(final UUID uuid) {
        return findCollectionEntity(uuid).map(RetentionPeriodCollectionEntity::getRetentionPeriodInDays);
    }

    private Optional<RetentionPeriodCollectionEntity> findCollectionEntity(final UUID uuid) {
        return findAndMemoizeEntity(() -> collectionEntityMemoizer.compute(uuid));
    }

    private Optional<RetentionPeriodTableEntity> findTableEntity(final UUID uuid, final String tableName) {
        return findAndMemoizeEntity(() -> tableEntityMemoizer.compute(uuid).stream().filter(retentionPeriodTableEntity -> {
            final String actualTableName = retentionPeriodTableEntity.getTableName();
            return actualTableName.equals(tableName);
        }).findFirst());
    }

    private static <T> Optional<T> findAndMemoizeEntity(final MemoComputer<Optional<T>> memoComputer) {
        try {
            return memoComputer.compute();
        } catch (final InterruptedException e) { //NOSONAR
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface MemoComputer<T> {
        T compute() throws InterruptedException;
    }
}
