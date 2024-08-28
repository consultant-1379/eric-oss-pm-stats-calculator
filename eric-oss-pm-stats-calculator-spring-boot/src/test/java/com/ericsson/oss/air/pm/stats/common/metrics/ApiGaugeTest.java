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

class ApiGaugeTest {

    @ParameterizedTest(name = "[{index}] {0} has metricName: {1}")
    @ArgumentsSource(ApiGaugeTest.ApiGaugeArgumentProvider.class)
    void shouldValueReturned(final ApiGauge metric, final String metricName) {
        Assertions.assertThat(metric.getName()).isEqualTo(metricName);
    }

    @Test
    void shouldValuesReturnsAllEnums() {
        Assertions.assertThat(ApiGauge.values())
                .containsExactlyInAnyOrder(
                        ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES,
                        ApiGauge.CALCULATION_POST_DECOMPRESSED_PAYLOAD_SIZE_IN_BYTES
                );
    }

    private static final class ApiGaugeArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES, ("calculation_post_compressed_payload_size_in_bytes")),
                    Arguments.of(ApiGauge.CALCULATION_POST_DECOMPRESSED_PAYLOAD_SIZE_IN_BYTES, ("calculation_post_decompressed_payload_size_in_bytes"))
            );
        }
    }

}