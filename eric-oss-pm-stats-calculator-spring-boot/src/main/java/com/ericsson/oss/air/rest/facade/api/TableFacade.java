/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.facade.api;

import java.util.Optional;
import java.util.UUID;

import kpi.model.KpiDefinitionRequest;

public interface TableFacade {
    /**
     * Create or update KPI output tables and schema based on the {@link KpiDefinitionRequest} and the collectionId
     * @param kpiDefinition
     * @param collectionId
     */
    void createOrUpdateOutputTable(KpiDefinitionRequest kpiDefinition, Optional<UUID> collectionId);
}
