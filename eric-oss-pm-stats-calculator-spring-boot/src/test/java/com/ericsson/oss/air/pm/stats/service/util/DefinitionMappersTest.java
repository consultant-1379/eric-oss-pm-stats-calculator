/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DefinitionMappersTest {

    @ParameterizedTest
    @MethodSource("provideCheckBooleanValueData")
    void testBooleanValueWithString(final Object value) {
        final String key = "exportable";
        final Definition definition = new Definition(Map.of(key, value));
        final Boolean actual = DefinitionMappers.checkBooleanValue(definition, key);
        Assertions.assertThat(actual).isTrue();
    }

    static Stream<Arguments> provideCheckBooleanValueData() {
        return Stream.of(
                arguments(Named.named("Type String", "true")),
                arguments(Named.named("Type Boolean", true))
        );
    }

    @ParameterizedTest
    @MethodSource("provideCheckIntegerValueData")
    void testIntegerValueWithString(final Object value) {
        final String key = "aggregation_period";
        final Definition definition = new Definition(Map.of(key, value));
        final Integer actual = DefinitionMappers.checkInteger(definition, key);
        Assertions.assertThat(actual).isEqualTo(5);
    }

    static Stream<Arguments> provideCheckIntegerValueData() {
        return Stream.of(
                arguments(Named.named("Type String", "5")),
                arguments(Named.named("Type Integer", 5))
        );
    }
}