/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.dto.input;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HealthCheckRestResponseInputDto {
    private final String state;
    private final String appName;
    private final Map<String, Object> additionalData;

    @JsonCreator
    public HealthCheckRestResponseInputDto(@JsonProperty("state") final String state,
                                           @JsonProperty("name") final String appName,
                                           @JsonProperty("additionalData") final Map<String, Object> additionalData) {
        this.state = state;
        this.appName = appName;
        this.additionalData = Collections.unmodifiableMap(additionalData);
    }
}
