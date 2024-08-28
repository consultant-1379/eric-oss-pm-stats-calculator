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

public enum KpiMetric implements Metric {
    PERSISTED_CALCULATION_RESULTS {
        @Override
        public void register(final MetricRegistry metricRegistry) {
            metricRegistry.defaultSettableGauge(toString());
        }
    }
}
