/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util.http;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Alternative implementation of the {@link HttpEntityEnclosingRequestBase} allowing HTTP DELETE with request body.
 */
public final class KpiHttpDelete extends HttpEntityEnclosingRequestBase {

    public KpiHttpDelete(final String uri) {
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return "DELETE";
    }

    @Override
    public KpiHttpDelete clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
