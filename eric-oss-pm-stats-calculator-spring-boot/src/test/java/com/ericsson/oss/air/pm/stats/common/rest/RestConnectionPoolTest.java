/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class RestConnectionPoolTest {

    private static final String UNSECURED_MAX_CONNECTIONS = "UNSECURED_MAX_CONNECTIONS";
    private static final String UNSECURED_MAX_CONNECTIONS_PER_ROUTE = "UNSECURED_MAX_CONNECTIONS_PER_ROUTE";
    private static final String SECURED_MAX_CONNECTIONS = "SECURED_MAX_CONNECTIONS";
    private static final String SECURED_MAX_CONNECTIONS_PER_ROUTE = "SECURED_MAX_CONNECTIONS_PER_ROUTE";
    private static Registry<ConnectionSocketFactory> socketFactoryRegistry;

    @BeforeAll
    static void setup() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        System.clearProperty(UNSECURED_MAX_CONNECTIONS);
        System.clearProperty(UNSECURED_MAX_CONNECTIONS_PER_ROUTE);
        System.clearProperty(SECURED_MAX_CONNECTIONS);
        System.clearProperty(SECURED_MAX_CONNECTIONS_PER_ROUTE);

       final SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
       final SSLConnectionSocketFactory sslConnectionFactory =
                new SSLConnectionSocketFactory(sslContext.getSocketFactory(),
                        new NoopHostnameVerifier());

        socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .build();
    }

    @Test
    @Order(1)
    void whenEnvironmentVariablesAreNotSet_thenAStandardConnectionPoolWithDefaultValuesIsReturned() {
        final PoolingHttpClientConnectionManager standardConnectionManager = new RestConnectionPool().getUnsecuredConnectionManager();
        assertEquals(20, standardConnectionManager.getMaxTotal());
        assertEquals(2, standardConnectionManager.getDefaultMaxPerRoute());
    }

    @Test
    @Order(2)
    void whenEnvironmentVariablesAreSet_thenAStandardConnectionPoolWithThoseValuesIsReturned() {
        final String max = "50";
        final String maxPerRoute = "1000";
        setUnsecuredPoolEnvironmentVariables(max, maxPerRoute);

        final PoolingHttpClientConnectionManager standardConnectionManager = new RestConnectionPool().getUnsecuredConnectionManager();
        assertEquals(Integer.parseInt(max), standardConnectionManager.getMaxTotal());
        assertEquals(Integer.parseInt(maxPerRoute), standardConnectionManager.getDefaultMaxPerRoute());
    }

    @Test
    @Order(3)
    void whenIntParseFailsOnEnvironmentVariableSetting_thenAStandardConnectionPoolWithDefaultValuesIsReturned() {
        setUnsecuredPoolEnvironmentVariables("un-parsable", "un-parsable");
        final PoolingHttpClientConnectionManager standardConnectionManager = new RestConnectionPool().getUnsecuredConnectionManager();
        assertEquals(20, standardConnectionManager.getMaxTotal());
        assertEquals(2, standardConnectionManager.getDefaultMaxPerRoute());
    }

    @Test
    @Order(4)
    void whenEnvironmentVariablesAreNotSet_thenASecureConnectionPoolWithDefaultValuesIsReturned() {
        final PoolingHttpClientConnectionManager secureConnectionManager = new RestConnectionPool().getSecureConnectionManager(socketFactoryRegistry);
        assertEquals(20, secureConnectionManager.getMaxTotal());
        assertEquals(2, secureConnectionManager.getDefaultMaxPerRoute());
    }

    @Test
    @Order(5)
    void whenEnvironmentVariablesAreSet_thenASecureConnectionPoolWithThoseValuesIsReturned() {
        final String max = "50";
        final String maxPerRoute = "1000";
        setSecuredPoolEnvironmentVariables("50", "1000");

        final PoolingHttpClientConnectionManager secureConnectionManager = new RestConnectionPool().getSecureConnectionManager(socketFactoryRegistry);
        assertEquals(Integer.parseInt(max), secureConnectionManager.getMaxTotal());
        assertEquals(Integer.parseInt(maxPerRoute), secureConnectionManager.getDefaultMaxPerRoute());
    }

    @Test
    @Order(6)
    void whenIntParseFailsOnEnvironmentVariableSetting_thenAnSecureConnectionPoolWithDefaultValuesIsReturned() {
        setSecuredPoolEnvironmentVariables("un-parsable", "un-parsable");
        final PoolingHttpClientConnectionManager secureConnectionManager = new RestConnectionPool().getSecureConnectionManager(socketFactoryRegistry);
        assertEquals(20, secureConnectionManager.getMaxTotal());
        assertEquals(2, secureConnectionManager.getDefaultMaxPerRoute());
    }

    private void setUnsecuredPoolEnvironmentVariables(final String max, final String maxPerRoute) {
        System.setProperty(UNSECURED_MAX_CONNECTIONS, max);
        System.setProperty(UNSECURED_MAX_CONNECTIONS_PER_ROUTE, maxPerRoute);
    }

    private void setSecuredPoolEnvironmentVariables(final String max, final String maxPerRoute) {
        System.setProperty(SECURED_MAX_CONNECTIONS, max);
        System.setProperty(SECURED_MAX_CONNECTIONS_PER_ROUTE, maxPerRoute);
    }
}
