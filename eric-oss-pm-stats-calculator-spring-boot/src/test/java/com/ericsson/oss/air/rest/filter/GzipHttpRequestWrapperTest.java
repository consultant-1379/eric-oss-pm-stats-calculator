/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GzipHttpRequestWrapperTest {
    @Mock HttpServletRequest httpServletRequestMock;

    @Test
    void testGetInputStream() {
        final ByteArrayInputStream mockInputStream = new ByteArrayInputStream(new byte[0]);
        final GzipHttpRequestWrapper gzipHttpRequestWrapper = new GzipHttpRequestWrapper(httpServletRequestMock, mockInputStream);

        final ServletInputStream actual = gzipHttpRequestWrapper.getInputStream();

        assertNotNull(actual);
        assertTrue(actual instanceof GzipHttpRequestWrapper.GzipServletInputStream);
    }

    @Test
    void testGzipServletInputStream() {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("TestData".getBytes());
        final GzipHttpRequestWrapper.GzipServletInputStream gzipInputStream = new GzipHttpRequestWrapper.GzipServletInputStream(inputStream);

        assertEquals(84, gzipInputStream.read());
        assertFalse(gzipInputStream.isFinished());
        assertTrue(gzipInputStream.isReady());
    }
}