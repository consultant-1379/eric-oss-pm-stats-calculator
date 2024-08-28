/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.facts;

import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.PmCounterLimitedAggregation;

import lombok.Data;

@Data
public class LimitedAggregation {
    private Integer ossID;
    private String ropBeginTime;
    private String ropEndTime;
    private PmCounterLimitedAggregation pmCounters;
}