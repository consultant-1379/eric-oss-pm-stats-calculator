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
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import lombok.RequiredArgsConstructor;

public final class GzipHttpRequestWrapper extends HttpServletRequestWrapper {

    private final ByteArrayInputStream inputStream;

    public GzipHttpRequestWrapper(final HttpServletRequest request, final ByteArrayInputStream inputStream) {
        super(request);
        this.inputStream = inputStream;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new GzipServletInputStream(inputStream);
    }


    @RequiredArgsConstructor
    static class GzipServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream decompressedData;
        private ReadListener readListener;


        @Override
        public int read() {
            return decompressedData.read();
        }

        @Override
        public boolean isFinished() {
            return decompressedData.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(final ReadListener readListener) {
            this.readListener = readListener;
        }
    }
}
