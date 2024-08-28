/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.exception.util;

import static com.ericsson.oss.air.rest.exception.model.ErrorResponse.internalServerError;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.exception.model._assert.ErrorResponseAssertions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ResponseHandlersTest {
    @Test
    void shouldVerifyAsResponse() {
        final ResponseEntity<ErrorResponse> actual = ResponseHandlers.asResponse(internalServerError("Something went wrong"));

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Assertions.assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class)).satisfies(errorResponse -> {
            ErrorResponseAssertions.assertThat(errorResponse).isInternalServerErrorWithMessage("Something went wrong");
        });
    }
}