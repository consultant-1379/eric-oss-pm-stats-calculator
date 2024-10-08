/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/


package com.ericsson.oss.air.pm.stats.calculator.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

public class CustomMetricSupplier implements MetricRegistry.MetricSupplier {
    @Override
    public Metric newMetric() {
        return new SettableGauge<Long>();
    }
}
