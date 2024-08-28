/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HttpPoolMetricReporterTest {

    static final int BLOCKED_WAITING_FREE_POOLED_CONNECTION_EXPECTED_COUNT_AFTER_INC = 5;
    static final int TOTAL_LEASED_POOLED_CONNECTIONS_EXPECTED_COUNT_AFTER_INC = 5;

    @BeforeAll
    static void clearEnvironment() {
        resetMetrics();
    }

    @AfterAll
    static void tearDown() {
        resetMetrics();
    }

    static void resetMetrics() {
        HttpPoolMetricReporter.getInstance().dec(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT,
                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT));
        HttpPoolMetricReporter.getInstance().dec(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT,
                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.UNSECURE,
                        HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT));

        HttpPoolMetricReporter.getInstance().dec(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT,
                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT));
        HttpPoolMetricReporter.getInstance().dec(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT,
                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.SECURE,
                        HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT));
    }

    @Test
    void whenTotalBlockedWaitingFreePooledConnectionMetricForUnsecureIsIncremented_thenTheMetricCountIsIncrementedByExpectedAmount() {

        HttpPoolMetricReporter.getInstance().inc(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT, 5);
        assertEquals(BLOCKED_WAITING_FREE_POOLED_CONNECTION_EXPECTED_COUNT_AFTER_INC, HttpPoolMetricReporter.getInstance()
                .getMetricCount(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT));
    }

    @Test
    void whenTotalLeasedPooledConnectionMetricForUnsecureIsIncremented_thenTheMetricCountIsIncrementedByExpectedAmount() {

        HttpPoolMetricReporter.getInstance().inc(HttpPoolType.UNSECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT, 5);
        assertEquals(TOTAL_LEASED_POOLED_CONNECTIONS_EXPECTED_COUNT_AFTER_INC,
                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.UNSECURE,HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT));
    }

    @Test
    void whenTotalBlockedWaitingFreePooledConnectionMetricForSecureIsIncremented_thenTheMetricCountIsIncrementedByExpectedAmount() {

        HttpPoolMetricReporter.getInstance().inc(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT, 5);
        assertEquals(BLOCKED_WAITING_FREE_POOLED_CONNECTION_EXPECTED_COUNT_AFTER_INC, HttpPoolMetricReporter.getInstance()
                .getMetricCount(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT));
    }

    @Test
    void whenTotalLeasedPooledConnectionMetricForSecureIsIncremented_thenTheMetricCountIsIncrementedByExpectedAmount() {

        HttpPoolMetricReporter.getInstance().inc(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT, 5);
        assertEquals(TOTAL_LEASED_POOLED_CONNECTIONS_EXPECTED_COUNT_AFTER_INC,
                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.SECURE, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT));
    }
}
