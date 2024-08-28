/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.model;

import lombok.Data;

@Data(staticConstructor = "of")
public final class ProcessResult {
    public static final int EXIT_CODE_SUCCESS = 0;
    public static final int EXIT_CODE_DUPLICATED_PROCESS = 250;
    public static final int EXIT_CODE_EXECUTION_EXCEPTION = 251;
    public static final int EXIT_CODE_CANCELLATION_EXCEPTION = 252;
    public static final int EXIT_CODE_INTERRUPTED_EXCEPTION = 253;
    public static final int EXIT_CODE_IO_EXCEPTION = 254;

    private final int exitCode;
    private final String resultLog;

    public static ProcessResult success(final String resultLog) {
        return of(EXIT_CODE_SUCCESS, resultLog);
    }

    public static ProcessResult duplicatedProcessCall() {
        return of(EXIT_CODE_DUPLICATED_PROCESS, "duplicated process call");
    }

    public static ProcessResult executionException(final String resultLog) {
        return of(EXIT_CODE_EXECUTION_EXCEPTION, resultLog);
    }

    public static ProcessResult cancellationException(final String resultLog) {
        return of(EXIT_CODE_CANCELLATION_EXCEPTION, resultLog);
    }

    public static ProcessResult interruptedException(final String resultLog) {
        return of(EXIT_CODE_INTERRUPTED_EXCEPTION, resultLog);
    }

    public static ProcessResult ioException(final String resultLog) {
        return of(EXIT_CODE_IO_EXCEPTION, resultLog);
    }
}
