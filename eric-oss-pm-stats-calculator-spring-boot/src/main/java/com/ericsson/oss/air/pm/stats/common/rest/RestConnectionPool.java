/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import com.ericsson.oss.air.pm.stats.common.env.Environment;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Factory class used to create HttpClient connection pool.
 * The environment variables UNSECURED_MAX_CONNECTIONS, UNSECURED_MAX_CONNECTIONS_PER_ROUTE,
 * SECURED_MAX_CONNECTIONS and SECURED_MAX_CONNECTIONS_PER_ROUTE are used to set the max
 * connection and the max connection per route in the connection
 * pool. They need to be set in System environment variables, if different from default required:
 * <ul>
 * <li><code>UNSECURED_MAX_CONNECTIONS</code></li>
 * <li><code>UNSECURED_MAX_CONNECTIONS_PER_ROUTE</code></li>
 * <li><code>SECURED_MAX_CONNECTIONS</code></li>
 * <li><code>SECURED_MAX_CONNECTIONS_PER_ROUTE</code></li>
 * </ul>
 */
@Slf4j
@NoArgsConstructor
final class RestConnectionPool {

    private final int totalMaxConnections = getEnvironmentVariable("UNSECURED_MAX_CONNECTIONS", "20");
    private final int maxConnectionsPerRoute = getEnvironmentVariable("UNSECURED_MAX_CONNECTIONS_PER_ROUTE", "2");
    private final int totalSecureMaxConnections = getEnvironmentVariable("SECURED_MAX_CONNECTIONS", "20");
    private final int maxSecureConnectionsPerRoute = getEnvironmentVariable("SECURED_MAX_CONNECTIONS_PER_ROUTE", "2");

    PoolingHttpClientConnectionManager getUnsecuredConnectionManager() {
        return getConnectionManager(totalMaxConnections, maxConnectionsPerRoute);
    }

    PoolingHttpClientConnectionManager getSecureConnectionManager(final Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        return getConnectionManager(totalSecureMaxConnections, maxSecureConnectionsPerRoute, socketFactoryRegistry);
    }

    private static int getEnvironmentVariable(final String environmentVariableName, final String environmentVariableValue) {
        try {
            return Integer
                    .parseInt(Environment.getEnvironmentValue(environmentVariableName, environmentVariableValue));
        } catch (final NumberFormatException e) {
            log.warn("Failed to set environment variable: '{}', using default value: '{}'", environmentVariableName, environmentVariableValue);
            return Integer.parseInt(environmentVariableValue);
        }
    }

    /**
     * Initializes and returns a HttpClientConnectionManager HTTP connection.
     *
     * @param maxPoolSize
     *            the Maximum number of connections allowed on all routes
     * @param maxPerRoute
     *            the Maximum number of connections allowed per route
     * @return the {@link PoolingHttpClientConnectionManager}
     */
    private static PoolingHttpClientConnectionManager getConnectionManager(final int maxPoolSize, final int maxPerRoute) {
        final PoolingHttpClientConnectionManager httpConnectionManager = new PoolingHttpClientConnectionManager();
        httpConnectionManager.setMaxTotal(maxPoolSize);
        httpConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        return httpConnectionManager;
    }

    /**
     * Initializes and returns a HttpClientConnectionManager for a HTTPS connection.
     *
     * @param maxPoolSize
     *            the Maximum number of connections allowed on all routes
     * @param maxPerRoute
     *            the Maximum number of connections allowed per route
     * @param socketFactoryRegistry
     *            the Socket Factory Registry containing SSL socket
     * @return the {@link PoolingHttpClientConnectionManager}
     */
    private static PoolingHttpClientConnectionManager getConnectionManager(final int maxPoolSize, final int maxPerRoute,
            final Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        final PoolingHttpClientConnectionManager httpConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        httpConnectionManager.setMaxTotal(maxPoolSize);
        httpConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        return httpConnectionManager;
    }
}
