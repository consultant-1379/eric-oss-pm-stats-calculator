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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;


/**
 * Response object from a REST Request executed through {@link RestExecutor}.
 *
 * @param <T>
 *            the type of {@link org.apache.http.HttpEntity} that should be retrieved, can be {@link String} or {@link InputStream}
 */
@Slf4j
public class RestResponse<T> extends ResponseImpl {
    private final Map<String, List<Header>> headersByHeaderName = new HashMap<>();

    protected RestResponse(final T entity, final String uri, final int statusCode, final Header[] headers) {
        super(entity, uri, statusCode, headers);
        addHeaders(headers);
    }

    private void addHeaders(final Header[] headers) {
        if (headers != null) {
            for (final Header header : headers) {
                if (headersByHeaderName.containsKey(header.getName())) {
                    final List<Header> currentHeader = headersByHeaderName.get(header.getName());
                    currentHeader.add(header);
                } else {
                    headersByHeaderName.put(header.getName(), new ArrayList<>(Collections.singletonList(header)));
                }
            }
        }
    }

    protected static int extractStatusCode(final HttpResponse response) {
        try {
            return response.getStatusLine().getStatusCode();
        } catch (final Exception e) {
            log.debug("Error extracting status code from {}", response, e);
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Returns a {@link org.apache.http.HttpEntity} as {@link String} or as {@link InputStream} depending on the implementation.
     *
     * @return entity {@link String} or {@link InputStream}
     */
    @Override
    public T getEntity() {
        return (T) super.getEntity();
    }

    @Override
    public String toString() {
        return String.format("%s:: {statusCode: '%s', entity: '%s', headersByHeaderName: '%s'}", getClass().getSimpleName(),
                getStatus(), String.valueOf(getEntity()), headersByHeaderName.toString());
    }
}
