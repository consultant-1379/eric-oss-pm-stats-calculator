/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import java.util.Collections;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.service.util.api.ParameterParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParameterParserImpl implements ParameterParser {
    private final ObjectMapper objectMapper;

    /**
     * Obtains the elements from the parameter key-value collection which is created by the
     * {@link com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload#parameterString()
     * KpiCalculationRequestPayload.parameterString()} method, where keys are guaranteed to be valid, non-empty parameter names.
     *
     * @param parameters string containing the joined parameter collection in json format
     * @return {@link Map} with the key-value pairs
     * @throws IllegalArgumentException if parameters cannot be processed
     */
    @Override
    public Map<String, String> parseParameters(final String parameters) {
        if (StringUtils.isBlank(parameters)) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(parameters, new TypeReference<>() {});
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Could not process parameters '%s'", parameters), e);
        }
    }
}
