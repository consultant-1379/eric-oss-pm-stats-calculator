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

import java.util.List;

import lombok.Data;

@Data
public class PmCounterLimitedAggregation {
    private Integer integerColumn0;
    private Double integerColumn1;
    private List<Integer> integerArrayColumn;
}