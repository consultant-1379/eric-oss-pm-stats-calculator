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
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

@Local
public interface ExecutionGroupRepository {
    /**
     * Saves or finds an already present {@link ExecutionGroup} entity.
     *
     * @param connection          {@link Connection} to the database.
     * @param kpiDefinitionEntity {@link KpiDefinitionEntity} which provides the execution group's elements
     * @return the id of the saved executionGroup
     * @throws SQLException If there was any problem executing the query
     */
    Long getOrSave(Connection connection, KpiDefinitionEntity kpiDefinitionEntity) throws SQLException;

    /**
     * Finds execution group by ID.
     *
     * @param connection {@link Connection} to the database.
     * @param id         The id of the execution group.
     * @return the execution group
     * @throws SQLException If there was any problem executing the query
     */
    ExecutionGroup findExecutionGroupById(Connection connection, Integer id) throws SQLException;
}
