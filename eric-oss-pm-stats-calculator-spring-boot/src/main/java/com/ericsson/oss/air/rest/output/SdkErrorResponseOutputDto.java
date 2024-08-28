/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.output;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Singular;

@Data
@JsonInclude(Include.NON_NULL)
@Builder(setterPrefix = "with")
@JsonPropertyOrder({"userMessage", "httpStatusCode", "developerMessage", "time", "errorData"})
public class SdkErrorResponseOutputDto {
    @JsonProperty("userMessage")
    private final String userMessage;

    @JsonProperty("httpStatusCode")
    private final Integer httpStatus;

    //  TODO: Do we really need two kind of messages?
    @JsonProperty("developerMessage")
    private final String developerMessage;

    @Default
    @JsonProperty("time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", locale = "en")
    private final LocalDateTime time = LocalDateTime.now();

    @Singular("errorData")
    @JsonProperty("errorData")
    private final List<Object> errorData;

    /**
     * Lombok and JavaDoc is not working together in the Maven's <strong>release</strong> phase.
     * By adding the class signature JavaDoc can progress - can find this class.
     * <br>
     * <a href="https://stackoverflow.com/questions/51947791/javadoc-cannot-find-symbol-error-when-using-lomboks-builder-annotation">Issue thread</a>
     */
    @SuppressWarnings("all")
    public static class SdkErrorResponseOutputDtoBuilder { }
}


