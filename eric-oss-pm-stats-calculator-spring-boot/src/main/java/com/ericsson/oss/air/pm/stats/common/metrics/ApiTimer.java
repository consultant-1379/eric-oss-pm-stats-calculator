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

/**
 * Defines API-related Timer metrics.
 * Timers measure both the rate that a particular piece of code is called and the distribution of its duration
 */
@RequiredArgsConstructor
@Getter
public enum ApiTimer implements Metric {
    DEFINITION_POST_SERVICE_TIME("definition_post_endpoint_duration_ms"),
    DEFINITION_GET_SERVICE_TIME("definition_get_endpoint_duration_ms"),
    DEFINITION_PATCH_SERVICE_TIME("definition_patch_endpoint_duration_ms"),
    DEFINITION_DELETE_SERVICE_TIME("definition_delete_endpoint_duration_ms");

    private final String name;

    @Override
    public void register(final MetricRegistry metricRegistry) {
        metricRegistry.timer(name);
    }
}