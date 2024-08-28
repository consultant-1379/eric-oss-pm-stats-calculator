/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.rest.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "calculationId",
        "status",
        "executionGroup",
        "readinessLogs"
})
public class CalculationStateResponse {

    @JsonProperty(value = "calculationId", required = true)
    private UUID calculationId;
    @JsonProperty(value = "status", required = true)
    private String status;
    @JsonProperty(value = "executionGroup", required = true)
    private String executionGroup;
    @JsonProperty(value = "readinessLogs")
    private Collection<ReadinessLogResponse> readinessLogs;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

}
