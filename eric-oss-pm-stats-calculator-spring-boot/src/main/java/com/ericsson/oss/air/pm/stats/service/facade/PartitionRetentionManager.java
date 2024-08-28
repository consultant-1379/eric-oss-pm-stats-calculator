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


import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionNameEncoder;
import com.ericsson.oss.air.pm.stats.service.api.PartitionService;
import com.ericsson.oss.air.pm.stats.utils.LocalDateTimes;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class PartitionRetentionManager {

    @Inject
    private PartitionService partitionService;
    @Inject
    private PartitionNameEncoder partitionNameEncoder;
    @Inject
    private Clock clock;

    public void runCleanUpForPartitionedTable(Connection connection, final String tableName, final LocalDateTime retentionDate) throws SQLException {
        final List<String> partitionNames = partitionService.getPartitionNamesForTable(tableName);
        final List<String> partitionsToDrop = new ArrayList<>();
        for (final String partitionName : partitionNames) {
            final LocalDate partitionStartDate = partitionNameEncoder.decodePartitionDateFromName(partitionName);
            if (partitionStartDate.isBefore(retentionDate.toLocalDate())) {
                partitionsToDrop.add(partitionName);
            }
        }
        if (!partitionsToDrop.isEmpty()) {
            partitionService.dropPartitions(connection, partitionsToDrop);
        }
    }

    public void createNewPartitionForTable(final Connection connection, final String tableName, final Set<String> aggregationElements, final LocalDateTime retentionDate) throws SQLException {
        final List<LocalDate> partitionDates = getNeededPartitionDates(retentionDate);
        final List<Partition> partitions = getPartitions(tableName, partitionDates, aggregationElements);

        partitionService.createPartitions(connection, partitions);
    }

    public LocalDateTime getRetentionDate(final Integer retentionPeriod) {
        final LocalDate localDate = LocalDateTime.now(clock).toLocalDate().minusDays(retentionPeriod);
        return localDate.atStartOfDay();
    }

    private List<Partition> getPartitions(final String tableName, final List<LocalDate> partitionDates,
                                          final Set<String> uniqueIndexColumns) {
        final List<Partition> partitions = new ArrayList<>(partitionDates.size());

        for (final LocalDate partitionStartDate : partitionDates) {
            final String partitionName = partitionNameEncoder.encodePartitionNameFromDate(tableName, partitionStartDate);
            final Partition partition = new Partition(partitionName, tableName, partitionStartDate, partitionStartDate.plusDays(1),
                    uniqueIndexColumns);
            partitions.add(partition);
        }
        return partitions;
    }

    private List<LocalDate> getNeededPartitionDates(final LocalDateTime retentionDate) {
        final List<LocalDate> partitionsNeeded = new ArrayList<>();
        LocalDateTime dateToAdd = retentionDate;
        final LocalDateTime lastDate = LocalDateTime.now(clock).plusDays(2);

        while (LocalDateTimes.isAfterOrEqual(lastDate, dateToAdd)) {
            partitionsNeeded.add(dateToAdd.toLocalDate());
            dateToAdd = dateToAdd.plusDays(1);
        }

        return partitionsNeeded;
    }
}
