/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.metrics;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class ApiCounterTest {

    @ParameterizedTest(name = "[{index}] {0} has metricName: {1}")
    @ArgumentsSource(ApiCounterArgumentProvider.class)
    void shouldValueReturned(final ApiCounter metric, final String metricName) {
        Assertions.assertThat(metric.getName()).isEqualTo(metricName);
    }

    @Test
    void shouldValuesReturnsAllEnums() {
        Assertions.assertThat(ApiCounter.values())
                .containsExactlyInAnyOrder(
                        ApiCounter.DEFINITION_PERSISTED_KPI,
                        ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_JSON,
                        ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_CSV,
                        ApiCounter.CALCULATION_POST_WITH_TABULAR_PARAM
                );
    }

    private static final class ApiCounterArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(ApiCounter.DEFINITION_PERSISTED_KPI, ("definition_persisted_kpi")),
                    Arguments.of(ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_JSON, ("calculation_post_tabular_param_format_json")),
                    Arguments.of(ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_CSV, ("calculation_post_tabular_param_format_csv")),
                    Arguments.of(ApiCounter.CALCULATION_POST_WITH_TABULAR_PARAM, ("calculation_post_with_tabular_param"))
            );
        }
    }
}