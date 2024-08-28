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
import java.util.Map;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.OnDemandParameter;

@Local
public interface ParameterService {

    /**
     * Persists {@link OnDemandParameter}s from the given {@link KpiDefinitionRequest} payload.
     *
     * @param kpiDefinition {@link KpiDefinitionRequest} to be its parameters persisted.
     */
    void insert(KpiDefinitionRequest kpiDefinition, UUID collectionId);

    /**
     * Finds all Single {@link Parameter}s stored in Parameters Table through the repository layer.
     *
     * @return a List containing all Single {@link Parameter}s
     */
    List<Parameter> findAllSingleParameters();

    /**
     * Finds column name and types of a {@link List} of {@link AggregationElement}
     *
     * @param aggregationElements {@link List} of {@link AggregationElement}
     * @return {@link Map} of aggregation element column name and type.
     */
    Map<String, KpiDataType> findAggregationElementTypeForTabularParameter(List<AggregationElement> aggregationElements);

    List<Parameter> findAllParameters();

    List<Parameter> findAllParameters(UUID collectionId);
}
