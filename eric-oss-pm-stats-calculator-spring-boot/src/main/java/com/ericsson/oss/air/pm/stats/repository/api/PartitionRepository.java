/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;

@Local
public interface PartitionRepository {

    /**
     * Create Postgres DB partitions.
     *
     * @param connection {@link Connection} to the database.
     * @param partitions {@link List} of {@link Partition} to be created
     * @throws SQLException thrown if there was any problem executing the query
     */
    void createPartitions(Connection connection, List<? extends Partition> partitions) throws SQLException;

    /**
     * Drop partitions.
     *
     * @param connection     {@link Connection} to the database.
     * @param partitionNames {@link List} of {@link String} of partition names
     * @throws SQLException thrown if there was any problem executing the query
     */
    void dropPartitions(Connection connection, List<String> partitionNames) throws SQLException;

    /**
     * Get partition names of the table.
     *
     * @param tableName table to get partition names for
     * @return {@link List} of partition names for given table
     */
    List<String> getPartitionNamesForTable(String tableName);

    /**
     * Drop unique index.
     *
     * @param connection           {@link Connection} to the database.
     * @param partitionUniqueIndex {@link PartitionUniqueIndex} to drop
     * @throws SQLException thrown if there was any problem executing the query
     */
    void dropUniqueIndex(Connection connection, PartitionUniqueIndex partitionUniqueIndex) throws SQLException;

    /**
     * Create unique index.
     *
     * @param connection  {@link Connection} to the database.
     * @param uniqueIndex {@link PartitionUniqueIndex} to create unique index for
     * @throws SQLException thrown if there was any problem executing the query
     */
    void createUniqueIndex(Connection connection, PartitionUniqueIndex uniqueIndex) throws SQLException;

    /**
     * Get partitions of a table.
     *
     * @param table {@link Table} to get unique index
     * @return {@link List} of {@link PartitionUniqueIndex} for the given table
     */
    List<PartitionUniqueIndex> findAllPartitionUniqueIndexes(Table table);
}
