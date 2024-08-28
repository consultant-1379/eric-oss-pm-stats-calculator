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

import com.codahale.metrics.MetricRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiSchedulerMetricRegistryTest {
    final KpiSchedulerMetricRegistry objectUnderTest = new KpiSchedulerMetricRegistry(new MetricRegistry());

    @Test
    void whenMetricsAreInitialized_thenVerifyMetricCanBeIncremented() {
        final long countBeforeIncrement = objectUnderTest.getMetricRegistry()
                .counter(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT.name())
                .getCount();
        objectUnderTest.incrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT);
        final long countAfterIncrement = objectUnderTest.getMetricRegistry().counter(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT.name())
                .getCount();
        Assertions.assertThat(countAfterIncrement).isEqualTo(countBeforeIncrement + 1);
    }

    @Test
    void whenMetricsAreInitialized_thenVerifyMetricCanBeDecremented() {
        final long countBeforeIncrement = objectUnderTest.getMetricRegistry()
                .counter(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT.name())
                .getCount();
        objectUnderTest.decrementKpiMetric(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT);
        final long countAfterIncrement = objectUnderTest.getMetricRegistry().counter(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT.name())
                .getCount();
        Assertions.assertThat(countAfterIncrement).isEqualTo(countBeforeIncrement - 1);
    }
}
