/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CustomMetricTest {

    @Test
    void whenCreatingCustomMetric_thenSourceNameIsSaved(){
        final CustomMetric objectUnderTest = new CustomMetric("source_1");
        assertEquals("source_1", objectUnderTest.sourceName());
    }

    @Test
    void whenCreatingCustomMetric_thenNoGaugesInItsMetricRegistry(){
        final CustomMetric objectUnderTest = new CustomMetric("source_1");
        assertEquals(0, objectUnderTest.metricRegistry().getGauges().size());
    }
}
