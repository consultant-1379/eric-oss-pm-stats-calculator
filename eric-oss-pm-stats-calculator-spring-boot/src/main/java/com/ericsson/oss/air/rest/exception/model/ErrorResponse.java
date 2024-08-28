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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@JsonInclude(NON_EMPTY)
@Accessors(fluent = true)
@Getter(onMethod_ = @JsonProperty)
public final class ErrorResponse {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", shape = Shape.STRING)
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final List<String> messages = new ArrayList<>();

    private ErrorResponse(@NonNull final StatusType statusType, @Nullable final String message, @NonNull final Collection<String> messages) {
        checkArgument((nonNull(message) && isEmpty(messages)) || (isNull(message) && isNotEmpty(messages)), String.format(
                "At most one parameter must be supplied but 'message' ('%s') and 'messages' ('%s')", message, messages
        ));

        timestamp = LocalDateTime.now();
        status = statusType.getStatusCode();
        error = statusType.getReasonPhrase();
        this.messages.addAll(messages);
        this.message = message;
    }

    public static ErrorResponse of(final StatusType statusType, final String message) {
        return new ErrorResponse(statusType, message, emptyList());
    }

    public static ErrorResponse of(final StatusType statusType, final Collection<String> messages) {
        return new ErrorResponse(statusType, null, messages);
    }

    public static ErrorResponse badRequest(final Collection<String> messages) {
        return of(Status.BAD_REQUEST, messages);
    }

    public static ErrorResponse badRequest(final String message) {
        return of(Status.BAD_REQUEST, message);
    }

    public static ErrorResponse notFound(final String message) {
        return of(Status.NOT_FOUND, message);
    }

    public static ErrorResponse conflict(final String message) {
        return of(Status.CONFLICT, message);
    }

    public static ErrorResponse internalServerError(final String message) {
        return of(Status.INTERNAL_SERVER_ERROR, message);
    }

    public static ErrorResponse gatewayTimeout(final String message) {
        return of(Status.GATEWAY_TIMEOUT, message);
    }
}
