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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ApiGauge implements Metric {
    CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES("calculation_post_compressed_payload_size_in_bytes"),
    CALCULATION_POST_DECOMPRESSED_PAYLOAD_SIZE_IN_BYTES("calculation_post_decompressed_payload_size_in_bytes");

    private final String name;

    @Override
    public void register(final MetricRegistry metricRegistry) {
        metricRegistry.gauge(name);
    }
}
