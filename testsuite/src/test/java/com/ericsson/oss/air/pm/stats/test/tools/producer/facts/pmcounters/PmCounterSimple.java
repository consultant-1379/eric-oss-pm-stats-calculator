/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters;

import com.ericsson.oss.air.pm.stats.test.tools.producer.serializer.FloatArraySerializer;
import com.ericsson.oss.air.pm.stats.test.tools.producer.serializer.IntegerArraySerializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"integerArrayColumn0",
        "floatArrayColumn0",
        "integerColumn0",
        "floatColumn0"})
public class PmCounterSimple {
    @JsonProperty("integerArrayColumn0")
    @JsonSerialize(using = IntegerArraySerializer.class)
    private String integerArrayColumn0;

    @JsonProperty("floatArrayColumn0")
    @JsonSerialize(using = FloatArraySerializer.class)
    private String floatArrayColumn0;

    @JsonProperty("integerColumn0")
    private Integer integerColumn0;

    @JsonProperty("floatColumn0")
    private Double floatColumn0;
}
