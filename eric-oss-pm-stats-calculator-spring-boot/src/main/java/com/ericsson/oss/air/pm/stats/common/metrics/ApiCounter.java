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
 * Defines API-related Counter metrics.Counter value can be incremented or decremented.
 * Reporter automatically adds JMX domain name as prefix and <strong>_count</strong> as suffix.
 */
@RequiredArgsConstructor
@Getter
public enum ApiCounter implements Metric {
    DEFINITION_PERSISTED_KPI("definition_persisted_kpi"),
    CALCULATION_POST_TABULAR_PARAM_FORMAT_JSON("calculation_post_tabular_param_format_json"),
    CALCULATION_POST_TABULAR_PARAM_FORMAT_CSV("calculation_post_tabular_param_format_csv"),
    CALCULATION_POST_WITH_TABULAR_PARAM("calculation_post_with_tabular_param");

    private final String name;

    @Override
    public void register(final MetricRegistry metricRegistry) {
        metricRegistry.counter(name);
    }
}