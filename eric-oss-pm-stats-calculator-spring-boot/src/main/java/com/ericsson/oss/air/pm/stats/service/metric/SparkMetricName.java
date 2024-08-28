/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.metric;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SparkMetricName {

    public static final String DOMAIN = "pm_stats_calculator_spark";
    public static final String START_TIME = "calculation_start_time";
    public static final String END_TIME = "calculation_end_time";
    public static final String DURATION = "calculation_duration";

    public static final String DELIMITER = ".";


    public static String createStartTimeName(final String executionGroup, final UUID calculationId) {
        return createName(executionGroup, calculationId, null, START_TIME);
    }

    public static String createEndTimeName(final String executionGroup, final UUID calculationId, final KpiCalculationState state) {
        return createName(executionGroup, calculationId, state, END_TIME);
    }

    public static String createDurationName(final String executionGroup, final UUID calculationId, final KpiCalculationState state) {
        return createName(executionGroup, calculationId, state, DURATION);
    }

    private static String createName(final String executionGroup, final UUID calculationId, final KpiCalculationState state, final String metricSuffix) {
        return String.join(
                SparkMetricName.DELIMITER,
                executionGroup,
                calculationId.toString(),
                state == null
                    ? metricSuffix
                    : String.join(SparkMetricName.DELIMITER, state.name(), metricSuffix));
    }
}
