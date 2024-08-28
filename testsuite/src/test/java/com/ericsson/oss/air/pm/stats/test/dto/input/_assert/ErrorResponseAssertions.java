/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.dto.input._assert;

import java.time.LocalDateTime;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.ericsson.oss.air.pm.stats.test.dto.input.ErrorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorResponseAssertions {
    public static ErrorResponseAssert assertThat(final ErrorResponse actual) {
        return new ErrorResponseAssert(actual, ErrorResponseAssert.class);
    }

    public static class ErrorResponseAssert extends AbstractAssert<ErrorResponseAssert, ErrorResponse> {
        protected ErrorResponseAssert(final ErrorResponse actual, final Class<?> selfType) {
            super(actual, selfType);
        }

        public ErrorResponseAssert isBadRequestWithMessages(final List<String> messages) {
            isNotNull();
            doAssert(messages, Status.BAD_REQUEST);
            return myself;
        }

        public ErrorResponseAssert isBadRequestWithMessagesInAnyOrder(final List<String> messages) {
            isNotNull();
            doAssertInAnyOrder(messages, Status.BAD_REQUEST);
            return myself;
        }

        public ErrorResponseAssert isBadRequestWithMessage(final String message) {
            isNotNull();
            doAssert(message, Status.BAD_REQUEST);
            return myself;
        }

        public ErrorResponseAssert isNotFoundWithMessage(final String message) {
            isNotNull();
            doAssert(message, Status.NOT_FOUND);
            return myself;
        }

        public ErrorResponseAssert isConflictWithMessage(final String message) {
            isNotNull();
            doAssert(message, Status.CONFLICT);
            return myself;
        }

        public ErrorResponseAssert isInternalServerErrorWithMessage(final String message) {
            isNotNull();
            doAssert(message, Status.INTERNAL_SERVER_ERROR);
            return myself;
        }

        public ErrorResponseAssert isGatewayTimeoutErrorWithMessage(final String message) {
            isNotNull();
            doAssert(message, Status.GATEWAY_TIMEOUT);
            return myself;
        }

        private void doAssert(final List<String> messages, @NonNull final StatusType status) {
            baseAssert(status);
            Assertions.assertThat(actual.messages()).as("messages").containsExactlyElementsOf(messages);
        }

        private void doAssertInAnyOrder(final List<String> messages, @NonNull final StatusType status) {
            baseAssert(status);
            Assertions.assertThat(actual.messages()).as("messages").containsExactlyInAnyOrderElementsOf(messages);
        }

        private void doAssert(final String message, @NonNull final StatusType status) {
            baseAssert(status);
            Assertions.assertThat(actual.message()).as("message").isEqualTo(message);
        }

        private void baseAssert(final @NonNull StatusType status) {
            Assertions.assertThat(actual.timestamp()).as("timestamp").isBeforeOrEqualTo(LocalDateTime.now());
            Assertions.assertThat(actual.status()).as("status").isEqualTo(status.getStatusCode());
            Assertions.assertThat(actual.error()).as("error").isEqualTo(status.getReasonPhrase());
        }
    }
}
