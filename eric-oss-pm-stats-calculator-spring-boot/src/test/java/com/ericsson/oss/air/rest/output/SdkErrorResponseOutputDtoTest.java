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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class SdkErrorResponseOutputDtoTest {
    static final String SDK_ERROR_RESPONSE = "json/sdk_error_response.json";
    ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test
    void shouldSerialize() {
        SdkErrorResponseOutputDto sdkErrorResponseOutputDto =
                SdkErrorResponseOutputDto.builder()
                                         .withUserMessage("message")
                                         .withHttpStatus(404)
                                         .withDeveloperMessage("developerMessage")
                                         .withTime(LocalDateTime.of(2024, 5, 2, 10, 0))
                                         .withErrorData(Arrays.asList("data1", "data2"))
                                         .build();

        final String actual = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sdkErrorResponseOutputDto);
        final String expected = ResourceLoaderUtils.getClasspathResourceAsString(SDK_ERROR_RESPONSE);

        assertThat(actual).isEqualToIgnoringNewLines(expected);
    }
}