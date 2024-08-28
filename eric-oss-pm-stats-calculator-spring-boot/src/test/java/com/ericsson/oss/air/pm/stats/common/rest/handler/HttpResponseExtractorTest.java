/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HttpResponseExtractor}.
 */
class HttpResponseExtractorTest {
    static final Gson GSON = new Gson();

    @Test
    void whenExtractingTheBodyOfAHttpResponse_ThenTheReturnValueIsConvertedToAString() throws IOException {
        final HttpResponse response = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        final TestClassUsedForConversionOfJsonToString input = new TestClassUsedForConversionOfJsonToString("xyz");
        final HttpEntity httpEntity = new StringEntity(GSON.toJson(input));

        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);

        assertThat(HttpResponseExtractor.extractBody(response)).isEqualTo("{\"name\":\"xyz\"}");
        assertThat(input.name).isEqualTo("xyz");
    }

    static class TestClassUsedForConversionOfJsonToString {
        final String name;

        TestClassUsedForConversionOfJsonToString(final String name) {
            this.name = name;
        }
    }
}