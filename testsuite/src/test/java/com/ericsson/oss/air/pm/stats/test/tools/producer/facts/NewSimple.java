/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.facts;

import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.PmCounterSimple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"agg_column_0",
        "agg_column_1",
        "ropBeginTime",
        "ropEndTime",
        "pmCounters"})
public class NewSimple {
    @JsonProperty("agg_column_0")
    private Integer aggColumn0;

    @JsonProperty("agg_column_1")
    private Integer aggColumn1;

    @JsonProperty("ropBeginTime")
    private String ropBeginTime;

    @JsonProperty("ropEndTime")
    private String ropEndTime;

    @JsonProperty("pmCounters")
    @JsonPropertyOrder({"integerArrayColumn0", "floatArrayColumn0", "integerColumn0", "floatColumn0"})
    private PmCounterSimple pmCounters;
}
