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

import com.ericsson.oss.air.pm.stats.model.entity.Parameter;

import kpi.model.ondemand.OnDemandParameter;

@Local
public interface ParameterRepository {

    /**
     * Finds all {@link Parameter}s stored in Parameters Table
     *
     * @return a List containing all {@link Parameter}s
     */
    List<Parameter> findAllParameters();

    /**
     * Finds all {@link Parameter}s stored in Parameters Table by collectionId
     *
     * @return a List containing all {@link Parameter}s
     */
    List<Parameter> findAllParameters(UUID collectionId);

    /**
     * Finds all Single {@link Parameter}s stored in Parameters Table
     *
     * @return a List containing all Single {@link Parameter}s
     */
    List<Parameter> findAllSingleParameters();

    /**
     * Saves {@link OnDemandParameter}s.
     *
     * @param connection {@link Connection} to the database.
     * @param parameters {@link List} of {@link OnDemandParameter}s to save.
     * @throws SQLException If any error occurs during persistence of <strong>definition</strong>.
     */
    void saveAllParameters(Connection connection, List<OnDemandParameter> parameters, UUID collectionId) throws SQLException;

    /**
     * Saves Parameters connected to tabular parameter.
     *
     * @param connection {@link Connection} to the database.
     * @param parameters {@link List} of {@link OnDemandParameter}s to save.
     * @param id         tabular parameter id to save.
     * @throws SQLException If any error occurs during persistence of <strong>definition</strong>.
     */
    void saveParametersWithTabularParameterId(Connection connection, List<OnDemandParameter> parameters, Integer id, UUID collectionId) throws SQLException;

    /**
     * Finds {@link Parameter}s which are related to a given tabular parameter
     *
     * @param tabularParameterName the name of the tabular parameter
     * @return a List containing all tabular parameters with the given name
     */
    List<Parameter> findParametersForTabularParameter(String tabularParameterName);

    /**
     * Finds {@link Parameter}s which are related to a given tabular parameters
     *
     * @param tabularParameterDataSourceTableNames {@link List} of the tabular parameter names
     * @return a {@link List} containing all tabular parameters with the given name
     */
    List<Parameter> findParametersForListOfTabularParameter(List<String> tabularParameterDataSourceTableNames);
}
