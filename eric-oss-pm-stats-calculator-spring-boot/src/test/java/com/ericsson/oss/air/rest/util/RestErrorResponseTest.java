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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RestErrorResponseTest {
    @Test
    void shouldGetErrorResponseWhenFailedRequestsAreEmpty() {
        final String message = "Unable to calculate the requested KPIs";
        final Response.StatusType statusType = Response.Status.INTERNAL_SERVER_ERROR;
        final List<Object> failedRequests = new ArrayList<>();

        ErrorResponse actual = RestErrorResponse.getErrorResponse(message, statusType, failedRequests);
        ErrorResponse expected = ErrorResponse.internalServerError(message);

        Assertions.assertThat(actual.status()).isEqualTo(expected.status());
        Assertions.assertThat(actual.error()).isEqualTo(expected.error());
        Assertions.assertThat(actual.message()).isEqualTo(expected.message());
    }

    @Test
    void shouldGetErrorResponseWhenFailedRequestsAreNotEmpty() {
        final String message = "Unable to calculate the requested KPIs";
        final Response.StatusType statusType = Response.Status.INTERNAL_SERVER_ERROR;
        final List<Object> failedRequests =List.of("kpi1","kpi2");

        ErrorResponse actual = RestErrorResponse.getErrorResponse(message, statusType, failedRequests);
        ErrorResponse expected = ErrorResponse.internalServerError(message + " - Error data: kpi1 kpi2");

        Assertions.assertThat(actual.status()).isEqualTo(expected.status());
        Assertions.assertThat(actual.error()).isEqualTo(expected.error());
        Assertions.assertThat(actual.message()).isEqualTo(expected.message());
    }
}