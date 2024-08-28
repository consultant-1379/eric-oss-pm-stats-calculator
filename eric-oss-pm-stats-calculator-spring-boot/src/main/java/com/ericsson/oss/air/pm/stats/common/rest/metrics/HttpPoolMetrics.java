/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.metrics;

/**
 * The HTTP connection pool metrics:
 * <ul>
 * <li>POOL_CONNECTIONS_USED_COUNT: the total number connections used</li>
 * <li>POOL_CONNECTIONS_BLOCKED_COUNT: the total number of blocked instances observed each time a connection in the pool is used.
 * The connection pool should be properly dimensioned so that this metric never goes above <code>0</code></li>
 * </ul>
 */
public enum HttpPoolMetrics {
    POOL_CONNECTIONS_USED_COUNT,
    POOL_CONNECTIONS_BLOCKED_COUNT
}