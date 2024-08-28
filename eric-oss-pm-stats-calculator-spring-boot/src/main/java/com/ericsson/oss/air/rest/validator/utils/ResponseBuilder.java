/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator.utils;

import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.DefinitionErrorResponse;
import com.ericsson.oss.air.rest.output.SdkErrorResponseOutputDto;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseBuilder {
    private final Status status;
    private final SdkErrorResponseOutputDto.SdkErrorResponseOutputDtoBuilder sdkErrorResponseOutputDtoBuilder;

    public static ResponseBuilder badRequest() {
        return new ResponseBuilder(Status.BAD_REQUEST,
                                   SdkErrorResponseOutputDto.builder()
                                                            .withHttpStatus(Status.BAD_REQUEST.getStatusCode()));
    }

    public static ResponseBuilder conflict() {
        return new ResponseBuilder(Status.CONFLICT,
                                   SdkErrorResponseOutputDto.builder()
                                                            .withHttpStatus(Status.CONFLICT.getStatusCode()));
    }

    public static ResponseBuilder internalServerError() {
        return new ResponseBuilder(Status.INTERNAL_SERVER_ERROR,
                                   SdkErrorResponseOutputDto.builder()
                                                            .withHttpStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    public ResponseBuilder withUserMessage(final String userMessage) {
        sdkErrorResponseOutputDtoBuilder.withUserMessage(userMessage);
        return this;
    }

    public ResponseBuilder withDeveloperMessage(final String developerMessage) {
        sdkErrorResponseOutputDtoBuilder.withDeveloperMessage(developerMessage);
        return this;
    }

    public ResponseBuilder withErrorData(final Collection<Definition> errorData) {
        sdkErrorResponseOutputDtoBuilder.withErrorData(errorData);
        return this;
    }

    public ResponseBuilder withDefinitionErrorResponseData(final Collection<DefinitionErrorResponse> errorData) {
        sdkErrorResponseOutputDtoBuilder.withErrorData(errorData);
        return this;
    }

    public ResponseBuilder withErrorData(final List<DefinitionErrorResponse> errorData) {
        sdkErrorResponseOutputDtoBuilder.withErrorData(errorData);
        return this;
    }

    public Response build() {
        return Response.status(status)
                       .entity(sdkErrorResponseOutputDtoBuilder.build())
                       .build();
    }
}
