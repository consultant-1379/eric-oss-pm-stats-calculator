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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.ericsson.oss.air.pm.stats.common.rest.exception.RestExecutionException;
import com.ericsson.oss.air.pm.stats.common.rest.utils.GzipUtils;
import com.ericsson.oss.air.pm.stats.model.exception.DecompressedRequestSizeTooBigException;
import com.ericsson.oss.air.rest.metric.TabularParamMetrics;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GzipDecompressionFilterTest {
    @Mock TabularParamMetrics tabularParamMetricsMock;
    @Mock ServletInputStream inputStreamMock;
    @Mock HttpServletRequest servletRequestMock;
    @Mock ServletResponse servletResponseMock;
    @Mock FilterChain filterChainMock;
    @InjectMocks GzipDecompressionFilter objectUnderTest;

    @SneakyThrows
    @Test
    void testFilterWithContentEncodingGzip() {
        when(servletRequestMock.getHeader("Content-Encoding")).thenReturn("gzip");
        when(servletRequestMock.getMethod()).thenReturn("POST");
        when(servletRequestMock.getInputStream()).thenReturn(inputStreamMock);
        when(servletRequestMock.getInputStream().readAllBytes()).thenReturn(compressPayload("Test payload"));

        objectUnderTest.doFilter(servletRequestMock, servletResponseMock, filterChainMock);

        verify(tabularParamMetricsMock, times(1)).setMetricCompressedPayloadSizeInBytes(anyInt());
        verify(tabularParamMetricsMock, times(1)).setMetricDeCompressedPayloadSizeInBytes(anyInt());
        verify(filterChainMock, times(1)).doFilter(any(GzipHttpRequestWrapper.class), eq(servletResponseMock));
        verify(filterChainMock, times(1)).doFilter(servletRequestMock, servletResponseMock);
    }

    @SneakyThrows
    @Test
    void testFilterWhenRequestMethodIsGet() {
        when(servletRequestMock.getHeader("Content-Encoding")).thenReturn("gzip");
        when(servletRequestMock.getMethod()).thenReturn("GET");

        objectUnderTest.doFilter(servletRequestMock, servletResponseMock, filterChainMock);

        verify(filterChainMock, never()).doFilter(any(GzipHttpRequestWrapper.class), eq(servletResponseMock));
        verify(filterChainMock, times(1)).doFilter(servletRequestMock, servletResponseMock);
    }

    @Test
    void testFilterWithNonGzipContentEncoding() throws ServletException, IOException {
        when(servletRequestMock.getHeader("Content-Encoding")).thenReturn("dummy");

        objectUnderTest.doFilter(servletRequestMock, servletResponseMock, filterChainMock);

        verify(tabularParamMetricsMock, times(0)).setMetricCompressedPayloadSizeInBytes(anyInt());
        verify(tabularParamMetricsMock, times(0)).setMetricDeCompressedPayloadSizeInBytes(anyInt());
        verify(filterChainMock, never()).doFilter(any(GzipHttpRequestWrapper.class), eq(servletResponseMock));
        verify(filterChainMock, times(1)).doFilter(servletRequestMock, servletResponseMock);
    }

    @SneakyThrows
    @Test
    void testFilterError() {
        when(servletRequestMock.getHeader("Content-Encoding")).thenReturn("gzip");
        when(servletRequestMock.getMethod()).thenReturn("POST");
        when(servletRequestMock.getInputStream()).thenReturn(inputStreamMock);
        when(servletRequestMock.getInputStream().readAllBytes()).thenThrow(new IOException("Simulated error"));

        assertThrows(RestExecutionException.class, () -> objectUnderTest.doFilter(servletRequestMock, servletResponseMock, filterChainMock));
        verify(servletRequestMock).getHeader("Content-Encoding");
        verify(servletRequestMock, times(2)).getInputStream();
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenDecompressedSizeIsLargerThanAllowedSize() {
        try (final MockedStatic<GzipUtils> gzipUtilsMockedStatic = mockStatic(GzipUtils.class)) {
            final HttpServletRequest servletRequestMock = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
            final byte[] compressedData = new byte[0];
            final byte[] decompressedData = new byte[megabyteAsByte(340)];

            when(servletRequestMock.getHeader("Content-Encoding")).thenReturn("gzip");
            when(servletRequestMock.getMethod()).thenReturn("POST");
            when(servletRequestMock.getInputStream().readAllBytes()).thenReturn(compressedData);

            gzipUtilsMockedStatic.when(() -> GzipUtils.decompressPayload(compressedData)).thenReturn(decompressedData);

            assertThatThrownBy(() -> objectUnderTest.doFilter(servletRequestMock, servletResponseMock, filterChainMock))
                    .isInstanceOf(DecompressedRequestSizeTooBigException.class)
                    .hasMessage("Data size after decompression 340 MB (356 515 840 bytes) is larger than the allowed size 300 MB (314 572 800 bytes)");
        }
    }

    private static int megabyteAsByte(final int megabyte) {
        return megabyte * 1_024 * 1_024;
    }

    byte[] compressPayload(final String payload) {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(payload.getBytes());
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RestExecutionException(e);
        }
    }

}