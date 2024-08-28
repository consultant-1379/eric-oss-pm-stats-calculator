/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.exception;

import lombok.Getter;

/**
 * Exception thrown when an error occurs executing against an EC-SON service REST endpoint.
 */
@Getter
public class RestExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1_048_596_990_110_917_097L;

    private final Integer statusCode;

    public RestExecutionException(final Throwable cause) {
        super(cause);
        statusCode = -1;
    }

    public RestExecutionException(final String message) {
        super(message);
        statusCode = -1;
    }

}
