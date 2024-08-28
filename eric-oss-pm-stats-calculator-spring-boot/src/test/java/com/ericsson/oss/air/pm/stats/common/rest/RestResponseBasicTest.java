/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RestResponseBasic} class.
 */
class RestResponseBasicTest {

    static final String VOID = "";
    static final URI DEFAULT_URI = URI.create("localhost");
    static final BasicStatusLine BASIC_STATUS_LINE = new BasicStatusLine(new ProtocolVersion(VOID, 1, 1), 1, VOID);

    final HttpResponse httpResponseMock = mock(HttpResponse.class);

    @BeforeEach
    void setUp() {
        when(httpResponseMock.getEntity()).thenReturn(null);
        when(httpResponseMock.getStatusLine()).thenReturn(BASIC_STATUS_LINE);
        when(httpResponseMock.getAllHeaders()).thenReturn(null);
    }

    @Test
    void whenHttpResponseContainsEntity_thenCreatedBasicResponseHasSameEntity() throws Exception {
        final String contents = "contents";
        when(httpResponseMock.getEntity()).thenReturn(new StringEntity(contents));

        final Response result = RestResponseBasic.create(httpResponseMock, DEFAULT_URI);

        assertSoftly(softly -> {
            softly.assertThat(result.getEntity()).isEqualTo(contents);
            softly.assertThat(result).asString().isEqualTo("RestResponseBasic:: {statusCode: '1', entity: 'contents', headersByHeaderName: '{}'}");
        });

    }

    @Test
    void whenCreateWithUri_thenCreatedResponseHasLocationAndContent_Location() throws Exception {
        final Response result = RestResponseBasic.create(httpResponseMock, DEFAULT_URI);

        assertSoftly(softly -> {
            softly.assertThat(result.getLocation()).isEqualTo(DEFAULT_URI);
            softly.assertThat(result.getHeaders()).containsEntry("Content-Location", Arrays.asList(new URI[]{DEFAULT_URI}));
        });
    }

    @Test
    void whenHttpResponseHasNoEntity_thenCreatedBasicResponseHasNoEntity() throws Exception {
        final Response result = RestResponseBasic.create(httpResponseMock, DEFAULT_URI);

        assertThat(result.hasEntity()).isFalse();
    }

    @Test
    void whenHttpResponseContainsHeaders_thenCreatedBasicResponseHasSameHeaders() throws Exception {
        when(httpResponseMock.getAllHeaders()).thenReturn(new Header[]{
                new BasicHeader("n1", "v1a"),
                new BasicHeader("n1", "v1b"),
                new BasicHeader("n2", "v2")});

        final URI uri = new URI("localhost");
        final Response result = RestResponseBasic.create(httpResponseMock, uri);

        assertSoftly(softly -> {
            softly.assertThat(result.getHeaders())
                    .hasSize(4)
                    .containsEntry("n1", Arrays.asList(new String[]{"v1a", "v1b"}))
                    .containsEntry("n2", Arrays.asList(new String[]{"v2"}))
                    .containsEntry("Content-Location", Arrays.asList(new URI[]{uri}))
                    .containsEntry("Location", Arrays.asList(new URI[]{uri}));
            softly.assertThat(result).asString()
                    .isEqualTo("RestResponseBasic:: {statusCode: '1', entity: 'null', headersByHeaderName: '{n1=[n1: v1a, n1: v1b], n2=[n2: v2]}'}");
        });
    }

}
