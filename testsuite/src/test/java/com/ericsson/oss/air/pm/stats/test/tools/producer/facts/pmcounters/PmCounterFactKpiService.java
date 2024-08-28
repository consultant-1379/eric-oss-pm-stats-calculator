/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PmCounterFactKpiService {
    @JsonProperty("integerArrayColumn0")
    @JsonSerialize(using = IntegerArraySerializer.class)
    private String integerArrayColumn0;

    @JsonProperty("integerArrayColumn1")
    @JsonSerialize(using = IntegerArraySerializer.class)
    private String integerArrayColumn1;

    @JsonProperty("integerArrayColumn2")
    @JsonSerialize(using = IntegerArraySerializer.class)
    private String integerArrayColumn2;

    @JsonProperty("floatArrayColumn0")
    @JsonSerialize(using = FloatArraySerializer.class)
    private String floatArrayColumn0;

    @JsonProperty("floatArrayColumn1")
    @JsonSerialize(using = FloatArraySerializer.class)
    private String floatArrayColumn1;

    @JsonProperty("floatArrayColumn2")
    @JsonSerialize(using = FloatArraySerializer.class)
    private String floatArrayColumn2;

    @JsonProperty("integerColumn0")
    private Integer integerColumn0;

    @JsonProperty("floatColumn0")
    private Double floatColumn0;
}
