/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.api;

import java.util.Collection;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.KpiDefinitionRequest;

public interface KpiValidator {

    /**
     * Validates {@link KpiDefinitionRequest}.
     *
     * @param kpiDefinition
     *      a {@link KpiDefinitionRequest} to validate.
     *
     */
    void validate(KpiDefinitionRequest kpiDefinition, UUID collectionId);

    /**
     * Validates a single {@link KpiDefinitionEntity} that has been changed.
     *
     * @param kpiDefinitionEntity
     *      a {@link KpiDefinitionEntity} to validate.
     *
     */
    void validate(KpiDefinitionEntity kpiDefinitionEntity);

    /**
     * Validates a collection of KPI definition names wit collectionId
     *
     * @param kpiDefinitionNames a Collection of String, containing the KPI names need to be validated.
     */
    void validate(Collection<String> kpiDefinitionNames, UUID collectionId);
}
