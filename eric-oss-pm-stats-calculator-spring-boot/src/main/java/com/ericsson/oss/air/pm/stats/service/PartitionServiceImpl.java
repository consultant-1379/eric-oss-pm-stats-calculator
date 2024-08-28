/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.repository.api.PartitionRepository;
import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.PartitionService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class PartitionServiceImpl implements PartitionService {
    @Inject
    private PartitionRepository partitionRepository;

    @Override
    public List<String> getPartitionNamesForTable(final String tableName) {
        return partitionRepository.getPartitionNamesForTable(tableName);
    }

    @Override
    public void dropPartitions(final Connection connection, final List<String> partitionNames) throws SQLException {
        partitionRepository.dropPartitions(connection, partitionNames);
    }

    @Override
    public void dropPartitions(final List<String> partitionNames) throws SQLException {
        TransactionExecutor.executeInTransaction(connection -> partitionRepository.dropPartitions(connection, partitionNames));
    }

    @Override
    public void createPartitions(final List<? extends Partition> partitions) throws SQLException {
        TransactionExecutor.execute(connection -> partitionRepository.createPartitions(connection, partitions));
    }

    @Override
    public void createPartitions(final Connection connection, final List<? extends Partition> partitions) throws SQLException {
        partitionRepository.createPartitions(connection, partitions);
    }

    @Override
    public List<PartitionUniqueIndex> getUniqueIndexForTablePartitions(final Table table) {
        return partitionRepository.findAllPartitionUniqueIndexes(table);
    }
}
