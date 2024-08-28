/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder(setterPrefix = "with")
public final class AggregationElement {
    private final String expression;
    private final String sourceTable;
    private final String sourceColumn;
    private final String targetColumn;
    private final Boolean isParametric;

    public boolean isNotParametric() {
        return !isParametric;
    }
}