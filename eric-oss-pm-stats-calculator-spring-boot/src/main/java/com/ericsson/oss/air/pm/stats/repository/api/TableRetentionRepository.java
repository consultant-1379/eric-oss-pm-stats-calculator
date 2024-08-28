/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;

@Local
public interface TableRetentionRepository {

    /**
     * Saves all table level retention entities to <strong>retention_configurations_table_level</strong>.
     *
     * @param connection                   {@link Connection} to connect to the database
     * @param retentionPeriodTableEntities entities to persist
     * @throws SQLException if persistence of the provided entities fail
     */
    void saveAll(Connection connection, List<RetentionPeriodTableEntity> retentionPeriodTableEntities) throws SQLException;

    /**
     * Returns {@link RetentionPeriodTableEntity} by collection id.
     *
     * @param collectionId {@link UUID} to find the entity by
     * @return a {@link List} of {@link RetentionPeriodTableEntity}
     */
    List<RetentionPeriodTableEntity> findByCollectionId(UUID collectionId);


    void deleteRetentionForTables(Connection connection, String tableName) throws SQLException;
}
