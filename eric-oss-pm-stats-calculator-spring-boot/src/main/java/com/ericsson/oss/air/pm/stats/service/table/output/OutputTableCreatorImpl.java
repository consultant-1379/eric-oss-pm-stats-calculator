/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.table.output;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.AGGREGATION_BEGIN_TIME_COLUMN;
import static lombok.AccessLevel.PUBLIC;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.repository.util.PartitionUtils;
import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.api.PartitionService;
import com.ericsson.oss.air.pm.stats.service.table.output.api.OutputTableCreator;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class OutputTableCreatorImpl implements OutputTableCreator {

    @Inject
    private DatabaseService databaseService;
    @Inject
    private PartitionService partitionService;
    @Inject
    private KpiExposureService kpiExposureService;

    @Inject
    private EnvironmentValue<Duration> retentionPeriodDays;

    @Override
    public void createOutputTable(@NonNull final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) {
        log.info("Creating KPI output table '{}'", tableCreationInformation.getTableName());

        try {
            TransactionExecutor.executeInTransaction(connection -> {
                databaseService.createOutputTable(connection, tableCreationInformation, validAggregationElements);

                if (tableCreationInformation.isNonDefaultAggregationPeriod()) {
                    log.info("Creating partitions for KPI output table '{}'", tableCreationInformation.getTableName());
                    partitionService.createPartitions(connection, prepareNonDefaultOutputTablePartitions(tableCreationInformation));
                }
            });
        } catch (final SQLException e) {
            throw new KpiPersistenceException(String.format("Error creating KPI output table: '%s'", tableCreationInformation.getTableName()), e);
        }
        kpiExposureService.exposeCalculationTable(tableCreationInformation.getTableName());
    }

    private List<Partition> prepareNonDefaultOutputTablePartitions(@NonNull final TableCreationInformation tableCreationInformation) {
        final Set<String> aggregationElements = tableCreationInformation.collectAggregationElements();
        aggregationElements.add(AGGREGATION_BEGIN_TIME_COLUMN); //  For non-default output tables Timestamp column is added (AGGREGATION_BEGIN_TIME_COLUMN)

        final List<LocalDate> partitionDatesForRetention = PartitionUtils.getPartitionDatesForRetention(retentionPeriodDays.value());
        return PartitionUtils.getPartitions(tableCreationInformation.getTableName(),
                tableCreationInformation.getTableName(),
                partitionDatesForRetention,
                aggregationElements);
    }
}
