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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.ericsson.oss.air.pm.stats.common.rest.exception.RestExecutionException;
import com.ericsson.oss.air.pm.stats.common.rest.utils.GzipUtils;
import com.ericsson.oss.air.pm.stats.model.exception.DecompressedRequestSizeTooBigException;
import com.ericsson.oss.air.rest.metric.TabularParamMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GzipDecompressionFilter implements Filter {

    private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    private static final String CONTENT_ENCODING_HEADER_VALUE = "gzip";
    private static final int MAX_SIZE_IN_BYTES_AFTER_DECOMPRESSION = 314572800;

    private final TabularParamMetrics tabularParamMetrics;


    private static String prettySize(final int bytes) {
        return String.format(
                "%s (%s bytes)",
                FileUtils.byteCountToDisplaySize(bytes),
                String.format(Locale.US, "%,d", bytes).replace(",", " ")
        );
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        final String contentEncoding = httpServletRequest.getHeader(CONTENT_ENCODING_HEADER);

        if (!"GET".equalsIgnoreCase(httpServletRequest.getMethod()) && contentEncoding != null && contentEncoding.contains(CONTENT_ENCODING_HEADER_VALUE)) {

            final byte[] compressedData;
            try {
                compressedData = httpServletRequest.getInputStream().readAllBytes();
                tabularParamMetrics.setMetricCompressedPayloadSizeInBytes(compressedData.length);
            } catch (IOException e) {
                throw new RestExecutionException(e);
            }
            final byte[] decompressedData = GzipUtils.decompressPayload(compressedData);
            tabularParamMetrics.setMetricDeCompressedPayloadSizeInBytes(decompressedData.length);
            log.info("Decompression was successful!");

            if (decompressedData.length > MAX_SIZE_IN_BYTES_AFTER_DECOMPRESSION) {
                throw new DecompressedRequestSizeTooBigException(
                        String.format(
                                "Data size after decompression %s is larger than the allowed size %s",
                                prettySize(decompressedData.length),
                                prettySize(MAX_SIZE_IN_BYTES_AFTER_DECOMPRESSION)
                        )
                );
            }

            chain.doFilter(new GzipHttpRequestWrapper(httpServletRequest, new ByteArrayInputStream(decompressedData)), response);
        }

        chain.doFilter(request, response);

    }
}
