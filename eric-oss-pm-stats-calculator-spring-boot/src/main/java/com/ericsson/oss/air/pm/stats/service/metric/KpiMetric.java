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

import com.ericsson.oss.air.pm.stats.common.metrics.Metric;
import com.ericsson.oss.air.pm.stats.common.metrics.MetricRegistry;

/**
 * Defines Scheduler metrics.
 */
public enum KpiMetric implements Metric {
    CURRENT_ON_DEMAND_CALCULATION,
    ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT,
    ON_DEMAND_CALCULATION_QUEUE,
    SCHEDULED_SIMPLE_CALCULATION_QUEUE,
    SCHEDULED_COMPLEX_CALCULATION_QUEUE,
    SCHEDULED_SIMPLE_CALCULATION_QUEUE_REMAINING_WEIGHT,
    ONGOING_CALCULATIONS {
        @Override
        public void register(final MetricRegistry metricRegistry) {
            metricRegistry.defaultSettableGauge(toString());
        }
    };

    @Override
    public void register(final MetricRegistry metricRegistry) {
        metricRegistry.counter(toString());
    }
}