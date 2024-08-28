/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.facade.api;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;

public interface OffsetHandlerRegistryFacade {
    default void calculateOffsets(final Set<KpiDefinition> kpiDefinitions) {
        offsetHandler().calculateOffsets(kpiDefinitions);
    }

    default void saveOffsets() {
        offsetHandler().saveOffsets();
    }

    /**
     * Returns {@link OffsetHandler} implementation based on the provided KPI Definitions to calculate the offsets.
     *
     * @return {@link OffsetHandler} implementation.
     */
    OffsetHandler offsetHandler();
}
