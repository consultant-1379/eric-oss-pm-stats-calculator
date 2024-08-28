/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import com.ericsson.oss.air.pm.stats.common.rest.exception.RestExecutionException;

import org.junit.jupiter.api.Test;

class GzipUtilsTest {

    @Test
    void testDecompression() {
        String testPayload = "Test payload";
        byte[] compressedPayload = compressPayload(testPayload);
        byte[] decompressedPayload = GzipUtils.decompressPayload(compressedPayload);

        assertThat(testPayload.getBytes()).isEqualTo(decompressedPayload);
    }

    @Test
    void decompressionShouldThrowException() {
        byte[] testPayload = {1,2,3,4,5};
        assertThatThrownBy(() -> GzipUtils.decompressPayload(testPayload))
                .hasRootCauseInstanceOf(ZipException.class)
                .isInstanceOf(RestExecutionException.class);
    }

    byte[] compressPayload(final String payload) {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(payload.getBytes());
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {throw new RestExecutionException(e);}
    }

}