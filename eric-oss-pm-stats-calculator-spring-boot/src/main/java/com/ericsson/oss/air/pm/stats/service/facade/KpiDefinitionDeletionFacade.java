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

import static com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor.executeInTransactionSilently;
import static com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers.groupBy;
import static com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers.transform;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiDefinitionDeletionFacade {
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private DatabaseService databaseService;
    @Inject
    private KpiExposureService kpiExposureService;
    @Inject
    private RetentionPeriodService retentionPeriodService;

    public void hardDeleteNecessaryKpiDefinitions(final List<KpiDefinitionEntity> entitiesToDelete) {
        final AtomicBoolean needExposureUpdate = new AtomicBoolean(false);

        executeInTransactionSilently(connection -> {
            deleteKpiDefinitions(connection, entitiesToDelete);
            deleteOutputColumns(connection, entitiesToDelete);
            needExposureUpdate.set(deleteOutputTables(connection, entitiesToDelete));
        });

        if (needExposureUpdate.get()) {
            kpiExposureService.updateExposure();
        }
    }

    private void deleteKpiDefinitions(final Connection connection, final List<KpiDefinitionEntity> entities) {
        final Collection<String> kpiNames = transform(entities, KpiDefinitionEntity::name);
        kpiDefinitionService.deleteKpiDefinitionsByName(connection, kpiNames);
    }

    private boolean deleteOutputTables(final Connection connection, final List<KpiDefinitionEntity> entities) {
        final AtomicBoolean needExposureUpdate = new AtomicBoolean(false);

        transform(entities, KpiDefinitionEntity::table).stream().distinct().forEach(table -> {
            try {
                if (!kpiDefinitionService.doesTableHaveAnyKpisLeft(connection, table)) {
                    databaseService.deleteTables(connection, List.of(table.getName()));
                    //  TODO: once we have multiple rApps we have to filter by collection id
                    retentionPeriodService.deleteFromTableLevelRetention(connection, table.getName());
                    needExposureUpdate.set(true);
                }
            } catch (final SQLException e) { //NO SONAR Exception is suitably logged
                log.error("An error occurred during deletion of output table '{}' ", table.getName(), e);
            }
        });

        return needExposureUpdate.get();
    }

    private void deleteOutputColumns(final Connection connection, final List<KpiDefinitionEntity> entities) {
        groupBy(entities, KpiDefinitionEntity::table).forEach((table, entityList) -> {
            final Collection<String> columnsToDrop = transform(entityList, KpiDefinitionEntity::name);

            try {
                databaseService.deleteColumnsForTable(connection, table.getName(), columnsToDrop);
            } catch (final SQLException e) { //NO SONAR Exception is suitably logged
                log.error("Could not delete columns from table '{}' '{}'", table.getName(), columnsToDrop, e);
            }
        });
    }
}
