/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.model;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.Calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO class representation of the JSON message, the service will send on the kafka topic.
 */
@Data
@NoArgsConstructor
public class ExecutionReport {

    @JsonProperty("execution_id")
    private UUID executionId;

    @JsonProperty("scheduled")
    private boolean scheduled;

    @JsonProperty("execution_start")
    private Long executionStart;

    @JsonProperty("execution_end")
    private Long executionEnd;

    @JsonProperty("execution_group")
    private String executionGroup;

    @JsonProperty("tables")
    private List<Table> tables;

    public ExecutionReport(final Calculation calculation) {
        executionId = calculation.getCalculationId();
        scheduled = !calculation.getExecutionGroup().equals(EXECUTION_GROUP_ON_DEMAND_CALCULATION);
        executionStart = calculation.getTimeCreated().toEpochSecond(ZoneOffset.UTC);
        executionEnd = calculation.getTimeCompleted().toEpochSecond(ZoneOffset.UTC);
        executionGroup = calculation.getExecutionGroup();
    }
}
