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

import static com.ericsson.oss.air.pm.stats.service.metric.SparkMetricName.DELIMITER;
import static com.ericsson.oss.air.pm.stats.service.metric.SparkMetricName.DURATION;
import static com.ericsson.oss.air.pm.stats.service.metric.SparkMetricName.END_TIME;
import static com.ericsson.oss.air.pm.stats.service.metric.SparkMetricName.START_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SparkMetricNameTest {

    static String EXECUTION_GROUP = "executionGroup";
    static UUID CALCULATION_ID = UUID.randomUUID();

    @Test
    void shouldCreateNameForStartTime() {

        final String actual = SparkMetricName.createStartTimeName(EXECUTION_GROUP, CALCULATION_ID);

        assertThat(actual).isEqualTo(getExpected(null, START_TIME));
    }

    @ParameterizedTest
    @EnumSource(KpiCalculationState.class)
    void shouldCreateNameForEndTime(final KpiCalculationState state) {

        final String actual = SparkMetricName.createEndTimeName(EXECUTION_GROUP, CALCULATION_ID, state);

        assertThat(actual).isEqualTo(getExpected(state, END_TIME));
    }

    @ParameterizedTest
    @EnumSource(KpiCalculationState.class)
    void shouldCreateNameForDuration(final KpiCalculationState state) {

        final String actual = SparkMetricName.createDurationName(EXECUTION_GROUP, CALCULATION_ID, state);

        assertThat(actual).isEqualTo(getExpected(state, DURATION));
    }

    static String getExpected(final KpiCalculationState state, final String suffix) {
        return String.join(
                DELIMITER,
                EXECUTION_GROUP,
                CALCULATION_ID.toString(),
                suffix.equals(START_TIME)
                        ? suffix
                        : String.join(DELIMITER, state.name(), suffix));
    }

}