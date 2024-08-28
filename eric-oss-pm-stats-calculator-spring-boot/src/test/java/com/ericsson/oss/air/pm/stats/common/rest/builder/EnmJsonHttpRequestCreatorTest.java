/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.builder;

import static com.ericsson.oss.air.pm.stats.common.rest.builder.EnmJsonHttpPostRequestCreator.ENM_ADMINISTRATOR;
import static com.ericsson.oss.air.pm.stats.common.rest.builder.EnmJsonHttpPostRequestCreator.ENM_USER_ID;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;

class EnmJsonHttpRequestCreatorTest {
    static final String url = "/zxy";
    static final String requestBody = "{ \"field\": \"value\" }";


    @Test
    void whenCreatingAnEnmJsonHttpPostRequest_thenTheReturnedRequestHasValidUriHeadersAndContentType() throws IOException {
        final StringEntity stringEntity = new StringEntity(requestBody);
        final HttpPost result = EnmJsonHttpPostRequestCreator.createPostRequest(url, stringEntity);

        assertThat(result.getURI().toString()).isEqualTo(url);
        assertThat(result.getHeaders(ENM_USER_ID)[0].getValue()).isEqualTo(ENM_ADMINISTRATOR);
        assertThat(result.getHeaders(CONTENT_TYPE)[0].getValue()).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(result.getHeaders(ACCEPT)[0].getValue()).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(IOUtils.toString(result.getEntity().getContent(), StandardCharsets.UTF_8.displayName())).isEqualTo(requestBody);
    }

    @Test
    void whenCreatingAnEnmJsonHttpPutRequest_thenTheReturnedRequestHasValidUriHeadersAndContentType() throws IOException {
        final StringEntity stringEntity = new StringEntity(requestBody);
        final HttpPut result = EnmJsonHttpPutRequestCreator.createPutRequest(url, stringEntity);

        assertThat(result.getURI().toString()).isEqualTo(url);
        assertThat(result.getHeaders(ENM_USER_ID)[0].getValue()).isEqualTo(ENM_ADMINISTRATOR);
        assertThat(result.getHeaders(CONTENT_TYPE)[0].getValue()).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(result.getHeaders(ACCEPT)[0].getValue()).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(IOUtils.toString(result.getEntity().getContent(), StandardCharsets.UTF_8.displayName())).isEqualTo(requestBody);
    }

}