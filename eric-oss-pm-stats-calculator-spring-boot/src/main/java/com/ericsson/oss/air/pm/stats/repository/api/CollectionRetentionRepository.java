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
import java.util.Optional;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;

@Local
public interface CollectionRetentionRepository {

    /**
     * Saves a collection level retention period entity to <strong>retention_configurations_collection_level</strong> table.
     *
     * @param connection                      {@link Connection} to connect to the database
     * @param retentionPeriodCollectionEntity {@link RetentionPeriodCollectionEntity} to persist
     * @return the generated key for the persisted record
     * @throws SQLException if persistence of the provided {@link RetentionPeriodCollectionEntity} fails
     */
    long save(Connection connection, RetentionPeriodCollectionEntity retentionPeriodCollectionEntity) throws SQLException;

    /**
     * Finds a collection level retention period if that exists by collection id.
     *
     * @param collectionId {@link UUID} to find the entity by
     * @return {@link RetentionPeriodCollectionEntity} with all attributes
     */
    Optional<RetentionPeriodCollectionEntity> findByCollectionId(UUID collectionId);
}
