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

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class CalculationReliability {
    private final Integer id;
    private final LocalDateTime calculationStartTime;
    private final LocalDateTime reliabilityThreshold;
    private final UUID kpiCalculationId;
    private final Integer kpiDefinitionId;
}
