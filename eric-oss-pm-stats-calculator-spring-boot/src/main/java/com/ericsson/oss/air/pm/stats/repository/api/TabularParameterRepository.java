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

import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;

import kpi.model.ondemand.OnDemandTabularParameter;

@Local
public interface TabularParameterRepository {

    /**
     * Finds all tabular parameters stored in tabular Parameters Table
     *
     * @return a List containing all tabular parameters
     */
    List<TabularParameter> findAllTabularParameters();

    /**
     * Saves a {@link OnDemandTabularParameter} entity.
     *
     * @param connection       {@link Connection} to the database.
     * @param tabularParameter {@link OnDemandTabularParameter} tabular parameter to save.
     * @throws SQLException If there was any problem executing the query
     */
    void saveTabularParameter(Connection connection, OnDemandTabularParameter tabularParameter, UUID collectionId) throws SQLException;
}
