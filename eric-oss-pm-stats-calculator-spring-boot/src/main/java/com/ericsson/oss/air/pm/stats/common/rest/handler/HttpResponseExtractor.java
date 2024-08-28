/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.handler;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * Class used to handle the {@link HttpResponse} for further processing.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpResponseExtractor {

    /**
     * Converts the body of a {@link HttpResponse} to a {@link String}.
     *
     * @param restResponse
     *            the {@link HttpResponse} received from a REST Request
     * @return the body of the {@link HttpResponse} to a {@link String}
     * @throws IOException
     *             thrown if there is any issue converting the {@link HttpResponse} to a {@link String}
     */
    public static String extractBody(final HttpResponse restResponse) throws IOException {
        final ResponseHandler<String> handler = new BasicResponseHandler();
        return handler.handleResponse(restResponse);
    }
}
