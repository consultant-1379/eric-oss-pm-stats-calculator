/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class CalculationLimit {
    public static final CalculationLimit DEFAULT = builder().build();

    @Default
    private final LocalDateTime calculationStartTime = LocalDateTime.MIN;
    @Default
    private final LocalDateTime calculationEndTime = LocalDateTime.MIN;
}