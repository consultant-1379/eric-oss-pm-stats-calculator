/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * Class used to create a JSON Put Request with default user ID for ENM.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnmJsonHttpPutRequestCreator {

    public static final String ENM_USER_ID = "X-Tor-UserID";
    public static final String ENM_ADMINISTRATOR = "administrator";


    /**
     * Creates a {@link HttpPut} based on an address and provided {@link StringEntity} payload. The payload is created with the following headers:
     * <ul>
     * <li>{@link #ENM_USER_ID}: {@link #ENM_ADMINISTRATOR}</li>
     * <li>{@link HttpHeaders#CONTENT_TYPE}: "application/json"</li>
     * <li>{@link HttpHeaders#ACCEPT}: "application/json"</li>
     * </ul>
     *
     * @param putUrl
     *            the URL of the {@link HttpPut} REST request
     * @param payload
     *            the JSON payload sent as part of the {@link HttpPut} REST request
     * @return the {@link HttpPut} ready to be sent
     */
    public static HttpPut createPutRequest(final String putUrl, final StringEntity payload) {
        final HttpPut httpPutRequest = new HttpPut(putUrl);
        httpPutRequest.setHeader(ENM_USER_ID, ENM_ADMINISTRATOR);
        httpPutRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPutRequest.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpPutRequest.setEntity(payload);
        return httpPutRequest;
    }
}
