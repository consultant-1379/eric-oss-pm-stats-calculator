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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates {@link KeyStore}
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class KeyStoreCreator {

    private static final int SOCKET_TIMEOUT = 10_000;
    private static final String DEFAULT_HTTPS_PROTOCOL = "TLS";
    private static final Integer DEFAULT_SSL_PORT = 443;


    /**
     * Creates {@link KeyStore}, gets certificates from the specified host and adds them to the created {@link KeyStore}
     *
     * @param hostname
     *            the host from which to retrieve certificates
     * @param port
     *            the port on the host from which to retrieve certificates
     * @return the {@link KeyStore} created with get certificates from host
     * @throws IOException
     *             an I/O exception of some sort has occurred
     */
    static KeyStore create(final String hostname, final Integer port) throws IOException {
        try {
            final KeyStore keyStore = getInstanceKeyStore();
            final X509Certificate[] chains = getCertificatesFromHost(keyStore, hostname, port);
            addCertificateToKeyStore(chains, keyStore, hostname);
            return keyStore;
        } catch (final NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IOException(e);
        }
    }

    private static KeyStore getInstanceKeyStore() throws IOException {
        try {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, "".toCharArray());
            return keyStore;
        } catch (final KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    private static void startSslSocketHandshake(final SSLSocketFactory sslSocketFactory, final String host, final Integer port) throws IOException {
        final int hostPort = (port == null) ? DEFAULT_SSL_PORT : port;

        log.debug("Opening connection to {}:{}", host, port);
        try (final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, hostPort)) {
            socket.setSoTimeout(SOCKET_TIMEOUT);
            log.debug("Starting SSL handshake...");
            socket.getSession();
        } catch (final SSLException e) {
            log.warn("The server host {} on port {} is not yet trusted", host, port, e);
        }
    }

    private static void addCertificateToKeyStore(final X509Certificate[] chains, final KeyStore keyStore, final String host)
            throws IOException {
        try {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            log.debug("Server sent {} certificate(s)", chains.length);

            for (int i = 0; i < chains.length; i++) {
                final X509Certificate cert = chains[i];

                sha1.update(cert.getEncoded());
                md5.update(cert.getEncoded());

                final String alias = host + "-" + (i + 1);
                keyStore.setCertificateEntry(alias, cert);
            }
        } catch (final NoSuchAlgorithmException | CertificateEncodingException | KeyStoreException e) {
            throw new IOException(e);
        }

    }

    private static X509Certificate[] getCertificatesFromHost(final KeyStore keyStore, final String host, final Integer port)
            throws NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException {
        final SSLContext sslContext = getSslContext();
        final TrustManagerFactory trustManagerFactory = getInstanceTrustManagerFactory(keyStore);
        final X509TrustManager x509TrustManager = getTrustManager(trustManagerFactory);
        final SavingTrustManager savingTrustManager = createSavingTrustManager(x509TrustManager);
        final SSLSocketFactory sslSocketFactory = getSocketFactory(sslContext, savingTrustManager);

        startSslSocketHandshake(sslSocketFactory, host, port);
        return savingTrustManager.getChain();
    }

    private static SSLContext getSslContext() throws NoSuchAlgorithmException {
        return SSLContext.getInstance(DEFAULT_HTTPS_PROTOCOL);
    }

    private static TrustManagerFactory getInstanceTrustManagerFactory(final KeyStore keyStore) throws KeyStoreException, NoSuchAlgorithmException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory;
    }

    private static X509TrustManager getTrustManager(final TrustManagerFactory trustManagerFactory) {
        return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
    }

    private static SavingTrustManager createSavingTrustManager(final X509TrustManager defaultTrustManager) {
        return new SavingTrustManager(defaultTrustManager);
    }

    private static SSLSocketFactory getSocketFactory(final SSLContext context, final SavingTrustManager savingTrustManager)
            throws KeyManagementException {
        context.init(null, new TrustManager[] { savingTrustManager }, null);
        return context.getSocketFactory();
    }
}
