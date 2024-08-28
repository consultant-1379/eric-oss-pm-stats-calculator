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
import javax.net.ssl.SSLContext;

/**
 * Interface for creating {@link SSLContext}.
 */
public interface SslContextInterface {

    /**
     * Create an {@link SSLContext} instance using the certificates retrieved from the host.
     *
     * @param hostUri
     *            the {@link String} hostname of the server that is used to make the connection
     * @param sslPort
     *            the {@link Integer} https port. If it is null, the default HTTPS port (443) is used
     * @return {@link SSLContext}
     * @throws IOException
     *             thrown if an I/O exception of some sort has occurred
     */
    SSLContext createSslContext(URI hostUri, Integer sslPort) throws IOException;
}
