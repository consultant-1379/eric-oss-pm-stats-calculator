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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.X509TrustManager;

/**
 * Custom implementation of {@link X509TrustManager} that provides access to the chain of {@link X509Certificate}.
 */
final class SavingTrustManager implements X509TrustManager {

    private final X509TrustManager x509TrustManager;

    private X509Certificate[] chain;

    SavingTrustManager(final X509TrustManager x509TrustManager) {
        this.x509TrustManager = x509TrustManager;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        throw new UnsupportedOperationException("Operation not supported on this X509TrustManager implementation");
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        throw new UnsupportedOperationException("Operation not supported on this X509TrustManager implementation");
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        this.chain = Arrays.copyOf(chain, chain.length);
        x509TrustManager.checkServerTrusted(chain, authType);
    }

    X509Certificate[] getChain() {
        return Arrays.copyOf(chain, chain.length);
    }
}
