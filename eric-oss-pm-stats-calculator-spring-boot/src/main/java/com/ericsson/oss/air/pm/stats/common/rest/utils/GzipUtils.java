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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.ericsson.oss.air.pm.stats.common.rest.exception.RestExecutionException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class GzipUtils {

    public static byte[] decompressPayload(final byte[] compressedPayload) {
        try {
            final ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedPayload);
            try (final GZIPInputStream gzipStream = new GZIPInputStream(byteStream)) {return gzipStream.readAllBytes();}
        } catch (IOException e) {
            throw new RestExecutionException(e);
        }
    }
}
