/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.table.output.api;

import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

public interface OutputTableUpdater {
    /**
     * Update the output table for {@link KpiDefinitionEntity}s.
     *
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to update.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     */
    void updateOutputTable(TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements);
}
