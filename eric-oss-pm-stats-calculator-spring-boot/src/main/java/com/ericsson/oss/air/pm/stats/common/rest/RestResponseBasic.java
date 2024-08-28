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

import java.io.IOException;
import java.net.URI;

import com.ericsson.oss.air.pm.stats.common.rest.handler.HttpResponseExtractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

/**
 * Implementation of {@link RestResponse}, that contains a {@link org.apache.http.HttpEntity} as {@link String}.
 */
@Slf4j
public final class RestResponseBasic extends RestResponse<String> {

    private static final int HTTP_ERROR_CODE_START_RANGE = HttpStatus.SC_MULTIPLE_CHOICES;

    /**
     * Constructor of {@link RestResponseBasic} with a {@link org.apache.http.HttpEntity} as {@link String} entityBody.
     * <p>
     *
     * @param entityBody
     *            {@link org.apache.http.HttpEntity} converted to {@link String}
     * @param statusCode
     *            {@link HttpResponse} status code
     * @param headers
     *            array of {@link Header}
     */
    private RestResponseBasic(final String entityBody, final String uri, final int statusCode, final Header[] headers) {
        super(entityBody, uri, statusCode, headers);
    }

    /**
     * Creates an instance of {@link RestResponse} from the input {@link HttpResponse}.
     * <p>
     * Extracts the message body and status code, and parses the URI.
     *
     * @param response
     *            the {@link HttpResponse}
     * @param uri
     *            the input URI
     * @return the new {@link RestResponse}
     * @throws IOException
     *             thrown if there is any issue creating {@link RestResponse}
     */
    public static RestResponse<String> create(final HttpResponse response, final URI uri) throws IOException {
        final String body = extractEntity(response);
        final int statusCode = extractStatusCode(response);
        final Header[] header = response.getAllHeaders();
        return new RestResponseBasic(body, uri == null ? null : uri.toString(), statusCode, header);
    }

    private static String extractEntity(final HttpResponse response) throws IOException {
        try {
            if (response.getStatusLine().getStatusCode() < HTTP_ERROR_CODE_START_RANGE) {
                return HttpResponseExtractor.extractBody(response);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (final Exception e) {
            log.debug("Error extracting message body from {}", response, e);
            throw new IOException("Error extracting message body from HttpEntity", e);
        }
    }
}
