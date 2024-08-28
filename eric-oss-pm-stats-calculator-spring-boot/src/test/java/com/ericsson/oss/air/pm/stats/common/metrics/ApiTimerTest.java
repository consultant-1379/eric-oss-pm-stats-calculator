/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

class ApiTimerTest {

    @ParameterizedTest(name = "[{index}] {0} has metricName: {1}")
    @ArgumentsSource(ApiTimerArgumentProvider.class)
    void shouldValueReturned(final ApiTimer meter, final String metricName) {
        Assertions.assertThat(meter.getName()).isEqualTo(metricName);
    }

    @Test
    void shouldValuesReturnsAllEnums() {
        Assertions.assertThat(ApiTimer.values())
                .containsExactlyInAnyOrder(ApiTimer.DEFINITION_POST_SERVICE_TIME,
                        ApiTimer.DEFINITION_DELETE_SERVICE_TIME,
                        ApiTimer.DEFINITION_PATCH_SERVICE_TIME,
                        ApiTimer.DEFINITION_GET_SERVICE_TIME);
    }

    private static final class ApiTimerArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(ApiTimer.DEFINITION_POST_SERVICE_TIME, ("definition_post_endpoint_duration_ms"),
                    Arguments.of(ApiTimer.DEFINITION_GET_SERVICE_TIME, "definition_get_endpoint_duration_ms"),
                    Arguments.of(ApiTimer.DEFINITION_PATCH_SERVICE_TIME, "definition_patch_endpoint_duration_ms"),
                    Arguments.of(ApiTimer.DEFINITION_DELETE_SERVICE_TIME, "definition_delete_endpoint_duration_ms")));
        }
    }
}