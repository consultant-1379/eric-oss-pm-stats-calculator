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

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ParameterParserImplTest {
    ParameterParserImpl objectUnderTest = new ParameterParserImpl(new ObjectMapper());

    @Test
    void whenParametersAreEmpty() {
        final Map<String, String> actual = objectUnderTest.parseParameters(StringUtils.EMPTY);

        Assertions.assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideParseableStrings")
    void whenParametersAreNotEmpty(final String toParse, final Map<String, String> expected) {
        final Map<String, String> actual = objectUnderTest.parseParameters(toParse);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldFailDeserialization() {
        objectUnderTest = new ParameterParserImpl(new ObjectMapper() {
            @Override
            public <T> T readValue(final String content, final TypeReference<T> valueTypeRef) throws JsonProcessingException, JsonMappingException {
                throw JsonMappingException.fromUnexpectedIOE(new IOException("something went wrong"));
            }
        });

        Assertions.assertThatThrownBy(() -> objectUnderTest.parseParameters("{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("Unexpected IOException (of type java.io.IOException): something went wrong");
    }


    static Stream<Arguments> provideParseableStrings() {
        return Stream.of(
                Arguments.of(
                        "{\"key1\":\"value1\"}",
                        Map.of("key1", "value1")
                ),
                Arguments.of(
                        "{\"key1\":\"value1\",\"key2\":\"value2\"}",
                        Map.of("key1", "value1", "key2", "value2")
                ),
                Arguments.of(
                        "{\"a\":\"b\",\"complexValue\":\"one,two\",\"key1\":\"mark\\\"in\",\"key2\":\"\\\"atBeginning\",\"key3\":\"atEnd\\\"\","
                                + "\"emptyValue\":\"\",\"onlyMark\":\"\\\"\",\"onlyDoubleMarks\":\"\\\"\\\"\",\"key4\":\"\\\",val:ue4\"}",
                        Map.of("a", "b",
                               "complexValue", "one,two",
                               "key1", "mark\"in",
                               "key2", "\"atBeginning",
                               "key3", "atEnd\"",
                               "emptyValue", "",
                               "onlyMark", "\"",
                               "onlyDoubleMarks", "\"\"",
                               "key4", "\",val:ue4")
                )
        );
    }
}