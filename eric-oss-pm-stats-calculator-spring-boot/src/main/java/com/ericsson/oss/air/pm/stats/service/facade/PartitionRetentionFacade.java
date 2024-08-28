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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_BEGIN_TIME_COLUMN;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.getAggregationElements;
import static lombok.AccessLevel.PUBLIC;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodManager;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodMemoizer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class PartitionRetentionFacade {

    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private KpiDefinitionDeletionFacade definitionDeletionFacade;
    @Inject
    private RetentionPeriodManager retentionPeriodManager;
    @Inject
    private PartitionRetentionManager partitionRetentionManager;

    public void runRetention() {
        final List<KpiDefinitionEntity> kpiDefinitionEntities = kpiDefinitionService.findAllIncludingSoftDeleted();

        final List<String> processedTables = new ArrayList<>();
        final List<KpiDefinitionEntity> entitiesToDelete = new ArrayList<>();
        final RetentionPeriodMemoizer retentionPeriodMemoizer = retentionPeriodManager.retentionPeriodMemoizer();
        for (final KpiDefinitionEntity entity : kpiDefinitionEntities) {
            final Integer retentionPeriodInDays = retentionPeriodMemoizer.computeRetentionPeriod(entity);

            final LocalDateTime retentionDate = partitionRetentionManager.getRetentionDate(retentionPeriodInDays);

            if (isDefinitionForDelete(entity, retentionDate)) {
                entitiesToDelete.add(entity);
            } else {
                if (entity.isDefaultAggregationPeriod()) {
                    log.info("'{}' is not cleaned by the retention period as it has default aggregation period", entity.tableName());
                } else if (!processedTables.contains(entity.tableName())) {
                    log.info("Retention period is '{}' for '{}'", retentionPeriodInDays, entity.tableName());
                    runRetentionForTable(retentionDate, entity.tableName(), entity.aggregationElements());
                    processedTables.add(entity.tableName());
                }
            }
        }
        definitionDeletionFacade.hardDeleteNecessaryKpiDefinitions(entitiesToDelete);
    }

    private boolean isDefinitionForDelete(KpiDefinitionEntity entity, LocalDateTime retentionDate) {
        return entity.isDeleted() && entity.timeDeleted().isBefore(retentionDate);
    }

    private void runRetentionForTable(final LocalDateTime retentionDate, final String tableName, final List<String> elements) {
        try {
            TransactionExecutor.executeInTransaction(connection -> {
                partitionRetentionManager.runCleanUpForPartitionedTable(connection, tableName, retentionDate);
                final Set<String> aggregationElements = getAggregationElements(elements);
                aggregationElements.add(AGGREGATION_BEGIN_TIME_COLUMN);
                partitionRetentionManager.createNewPartitionForTable(connection, tableName, aggregationElements, retentionDate);
            });
        } catch (final SQLException e) {
            log.error("Error occurred on retention activity for {} table", tableName, e);
        }
    }
}