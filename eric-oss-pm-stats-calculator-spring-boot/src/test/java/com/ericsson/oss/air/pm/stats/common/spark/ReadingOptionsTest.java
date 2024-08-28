/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ReadingOptionsTest {
    ReadingOptions objectUnderTest = ReadingOptions.empty();

    @Test
    void testGetAggregationBeginTimeLimit() {
        objectUnderTest.setAggregationBeginTimeLimit(LocalDateTime.parse("2023-11-08T13:29"));

        assertThat(objectUnderTest.getAggregationBeginTimeLimit()).hasValue(LocalDateTime.parse("2023-11-08T13:29"));
    }

    @Test
    void testGetAggregationLimitWhenItsNotExist() {
        assertThat(objectUnderTest.getAggregationBeginTimeLimit()).isEmpty();
    }

    @Test
    void testMergeAggregationTimeLimit(){
        objectUnderTest.setAggregationBeginTimeLimit(LocalDateTime.parse("2023-11-08T13:29"));
        objectUnderTest.setAggregationBeginTimeLimit(LocalDateTime.parse("2023-11-08T13:27"));

        assertThat(objectUnderTest.getAggregationBeginTimeLimit()).hasValue(LocalDateTime.parse("2023-11-08T13:27"));
    }

    @Test
    void testMergeAggregationTimeLimitNotChange(){
        objectUnderTest.setAggregationBeginTimeLimit(LocalDateTime.parse("2023-11-08T13:29"));
        objectUnderTest.setAggregationBeginTimeLimit(LocalDateTime.parse("2023-11-08T13:30"));

        assertThat(objectUnderTest.getAggregationBeginTimeLimit()).hasValue(LocalDateTime.parse("2023-11-08T13:29"));
    }
}