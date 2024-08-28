/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.util.List;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.OnDemandParameter;

@Local
public interface TabularParameterService {

    /**
     * Persists {@link OnDemandParameter}s from the given {@link KpiDefinitionRequest} payload.
     *
     * @param kpiDefinition {@link KpiDefinitionRequest} to be its parameters persisted.
     */
    void insert(KpiDefinitionRequest kpiDefinition, UUID collectionId);

    /**
     * Finds all tabular parameters stored in Tabular Parameters Table
     *
     * @return a List containing all tabular parameters
     */
    List<TabularParameter> findAllTabularParameters();
}
