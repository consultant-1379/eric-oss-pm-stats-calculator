/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with", toBuilder = true)
public class Calculation {
    private UUID calculationId;
    private LocalDateTime timeCreated;
    private LocalDateTime timeCompleted;
    private KpiCalculationState kpiCalculationState;
    private String executionGroup;
    private String parameters;
    private KpiType kpiType;
    private final UUID collectionId;
}
