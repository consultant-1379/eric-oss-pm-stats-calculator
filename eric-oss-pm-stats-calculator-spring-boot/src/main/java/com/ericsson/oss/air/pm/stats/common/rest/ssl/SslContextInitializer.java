/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.ssl;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import lombok.extern.slf4j.Slf4j;


/**
 * Initializes {@link SSLContext} instances.
 */
@Slf4j
public class SslContextInitializer implements SslContextInterface {

    private static final String DEFAULT_HTTPS_PROTOCOL = "TLS";
    private static final String TRUST_MANAGEMENT_ALGORITHM = "X509";

    /**
     * Create an {@link SSLContext} instance using the certificates retrieved from the host.
     *
     * @param hostUri
     *            the {@link String} hostname of the server that is used to make the connection
     * @param sslPort
     *            the {@link Integer} https port. If it is null, the default HTTPS port (443) is used
     * @return {@link SSLContext}
     * @throws IOException
     *             Signals that an I/O exception of some sort has occurred
     */
    @Override
    public SSLContext createSslContext(final URI hostUri, final Integer sslPort) throws IOException {
        if (hostUri == null) {
            throw new IllegalArgumentException("Host URI is null");
        }
        final KeyStore keyStore = createKeyStore(hostUri.getHost(), sslPort);

        try {
            return initialise(keyStore);
        } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            log.debug("Error initializing SSLContext for {} on port {}", hostUri.getHost(), sslPort, e);
            log.error("Error initializing SSLContext for {} on port {}: {}", hostUri.getHost(), sslPort, e.getMessage());

            throw new IOException(e);
        }
    }

    /**
     * A {@link TrustManagerFactory} is created with the {@value TRUST_MANAGEMENT_ALGORITHM} trust management algorithm. The created
     * {@link TrustManagerFactory} is initialized with keystore which is given as a parameter. An {@link SSLContext} is created with
     * {@value DEFAULT_HTTPS_PROTOCOL} protocol and initialized with an instance of {@link TrustManagerFactory}.
     *
     * @param keystore
     *            {@link KeyStore} created with get certificates from host
     * @return SSLContext {@link SSLContext} created with the specified {@link KeyStore}
     * @throws KeyManagementException
     *             the general key management exception for all operations dealing with key management
     * @throws NoSuchAlgorithmException
     *             when a particular cryptographic algorithm is requested but is not available in the environment
     * @throws KeyStoreException
     *             generic KeyStore exception
     */
    private static SSLContext initialise(final KeyStore keystore)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGEMENT_ALGORITHM);
        trustManagerFactory.init(keystore);
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        final SSLContext sslContext = SSLContext.getInstance(DEFAULT_HTTPS_PROTOCOL);
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    private static KeyStore createKeyStore(final String host, final Integer sslPort) throws IOException {
        return KeyStoreCreator.create(host, sslPort);
    }
}