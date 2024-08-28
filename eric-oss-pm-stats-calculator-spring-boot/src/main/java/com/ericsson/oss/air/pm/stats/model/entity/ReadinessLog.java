/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(setterPrefix = "with")
public class ReadinessLog {

    private final Integer id;
    private final String datasource;
    private final Long collectedRowsCount;
    private final LocalDateTime earliestCollectedData;
    private final LocalDateTime latestCollectedData;
    private final UUID kpiCalculationId;
    private final UUID collectionId;

    public boolean hasSameDatasource(@NonNull final KpiDefinitionEntity kpiDefinition) {
        return datasource.equals(DataIdentifier.of(kpiDefinition.schemaDataSpace(), kpiDefinition.schemaCategory(), kpiDefinition.schemaName()).getName());
    }

}
