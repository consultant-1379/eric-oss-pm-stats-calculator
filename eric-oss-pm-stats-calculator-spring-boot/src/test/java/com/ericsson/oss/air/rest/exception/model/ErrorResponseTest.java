/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.exception.model;

import static java.util.Collections.emptyList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.rest.exception.model._assert.ErrorResponseAssertions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {
    @Nested
    class Serialization {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        @Test
        void shouldSerialize_whenMessageIsMissing() throws Exception {
            final ErrorResponse errorResponse = ErrorResponse.badRequest(List.of("message1", "message2"));
            final String actual = objectMapper.writeValueAsString(errorResponse);

            final Map<String, Object> values = objectMapper.readValue(actual, new TypeReference<>() {});
            Assertions.assertThat(values)
                    .doesNotContainKeys("message")
                    .containsEntry("messages", List.of("message1", "message2"));
        }

        @Test
        void shouldSerialize_whenMessagesIsMissing() throws Exception {
            final ErrorResponse errorResponse = ErrorResponse.badRequest("message");
            final String actual = objectMapper.writeValueAsString(errorResponse);

            final Map<String, Object> values = objectMapper.readValue(actual, new TypeReference<>() {});
            Assertions.assertThat(values)
                    .doesNotContainKeys("messages")
                    .containsEntry("message", "message");
        }
    }

    @Test
    void shouldFailInstantiation() {
        Assertions.assertThatThrownBy(() -> ErrorResponse.of(BAD_REQUEST, emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldVerifyBadRequest() {
        final ErrorResponse actual = ErrorResponse.badRequest("BAD_REQUEST");
        ErrorResponseAssertions.assertThat(actual).isBadRequestWithMessage("BAD_REQUEST");
    }

    @Test
    void shouldVerifyNotFound() {
        final ErrorResponse actual = ErrorResponse.notFound("NOT_FOUND");
        ErrorResponseAssertions.assertThat(actual).isNotFoundWithMessage("NOT_FOUND");
    }

    @Test
    void shouldVerifyConflict() {
        final ErrorResponse actual = ErrorResponse.conflict("CONFLICT");
        ErrorResponseAssertions.assertThat(actual).isConflictWithMessage("CONFLICT");
    }

    @Test
    void shouldVerifyInternalServerError() {
        final ErrorResponse actual = ErrorResponse.internalServerError("INTERNAL_SERVER_ERROR");
        ErrorResponseAssertions.assertThat(actual).isInternalServerErrorWithMessage("INTERNAL_SERVER_ERROR");
    }

    @Test
    void shouldVerifyGatewayTimeout() {
        final ErrorResponse actual = ErrorResponse.gatewayTimeout("GATEWAY_TIMEOUT");
        ErrorResponseAssertions.assertThat(actual).isGatewayTimeoutErrorWithMessage("GATEWAY_TIMEOUT");
    }
}