/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.util;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.StatusType;

import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility for standard error responses.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestErrorResponse {

    /**
     * Creates an error response object that conforms to the REST SDK.
     *
     * @param message
     *            A descriptive message including as much information as possible. Do not add stack trace error in the developerMessage, this should
     *            be only in the server logs for security purposes
     * @param statusType
     *            Should return the proper status type
     * @param failedRequests
     *            Additional data that is useful to the error presentation or troubleshooting
     * @return the {@link RestErrorResponse} which encloses the error encountered
     */
    public static ErrorResponse getErrorResponse(final String message, final StatusType statusType,
                                                 final List<Object> failedRequests) {
        final String errorData = message + (failedRequests.isEmpty() ? "" : String.join(" ", " - Error data:", failedRequests.stream().map(Object::toString).collect(Collectors.joining(" "))));
        return ErrorResponse.of(statusType, errorData);
    }
}