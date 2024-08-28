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
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import lombok.NonNull;

@Local
public interface SchemaDetailRepository {

    /**
     * Finds the {@link SchemaDetail} entity by its id.
     *
     * @param id the primary key of the entity
     * @return {@link SchemaDetail} the found schema detail
     */
    Optional<SchemaDetail> findById(Integer id);

    /**
     * Finds the {@link SchemaDetail} entity by its fields.
     *
     * @param topic     the topic field of the entity
     * @param namespace the namespace where the schema can be located
     * @return {@link SchemaDetail} the found schema detail
     */
    Optional<SchemaDetail> findBy(String topic, String namespace);

    /**
     * Saves a {@link SchemaDetail} entity.
     *
     * @param connection   {@link Connection} to the database.
     * @param schemaDetail {@link SchemaDetail} the entity to save
     * @return the id of the saved {@link SchemaDetail}
     */
    Integer save(Connection connection, SchemaDetail schemaDetail) throws SQLException;

    /**
     * Saves or finds an already present {@link SchemaDetail} entity.
     *
     * @param connection   {@link Connection} to the database.
     * @param schemaDetail {@link SchemaDetail} the entity to save
     * @return the id of the saved {@link SchemaDetail}
     */
    default Integer getOrSave(final Connection connection, @NonNull final SchemaDetail schemaDetail) throws SQLException {
        final Optional<SchemaDetail> optionalSchemaDetail = findBy(schemaDetail.getTopic(), schemaDetail.getNamespace());
        return optionalSchemaDetail.isPresent()
                ? optionalSchemaDetail.get().getId()
                : save(connection, schemaDetail);
    }
}