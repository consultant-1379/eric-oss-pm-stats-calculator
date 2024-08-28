/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator.api;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;

import kpi.model.KpiDefinitionRequest;

public interface DatabaseValidator {

    /**
     * Checks if a given kpi is not in Database.
     * <br>
     * check is performed on the basis of <strong>aliases</strong> and <strong>aggregation_periods</strong> of KPIs.
     *
     * @param kpiDefinition
     *      a {@link KpiDefinitionRequest} to validate.
     */
    void validateKpiAliasAggregationPeriodNotConflictingWithDb(KpiDefinitionRequest kpiDefinition);

    /**
     *This method validates that the name of a KpiDefinition is unique for all definitions in the database.
     * @param kpiDefinition The KpiDefinition to validate.
     * @throws KpiDefinitionValidationException If the name of the KpiDefinition is not unique.
     */
    void validateNameIsUnique(KpiDefinitionRequest kpiDefinition, UUID collectionId);

    /**
     * This method validates that there is no match between any Simple KPI's and any Complex KPI's execution group.
     * @param kpiDefinition The KpiDefinition to validate.
     * @throws KpiDefinitionValidationException If there is a match.
     */
    void validateExecutionGroup(KpiDefinitionRequest kpiDefinition);
}
