/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model;

import java.sql.Timestamp;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AggregationPeriodWindowTest {
    @Test
    void shouldReturnNewStartCopy() {
        final Timestamp start = new Timestamp(10);
        final AggregationPeriodWindow aggregationPeriodWindow = AggregationPeriodWindow.of(start, new Timestamp(0));

        final Timestamp actual = aggregationPeriodWindow.getStart();

        Assertions.assertThat(actual).isNotSameAs(start).isEqualTo(start);
    }

    @Test
    void shouldReturnNewEndCopy() {
        final Timestamp end = new Timestamp(10);
        final AggregationPeriodWindow aggregationPeriodWindow = AggregationPeriodWindow.of(new Timestamp(0), end);

        final Timestamp actual = aggregationPeriodWindow.getEnd();

        Assertions.assertThat(actual).isNotSameAs(end).isEqualTo(end);
    }
}