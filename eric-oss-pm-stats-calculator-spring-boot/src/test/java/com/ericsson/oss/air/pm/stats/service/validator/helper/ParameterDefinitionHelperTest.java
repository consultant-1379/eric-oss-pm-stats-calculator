/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ParameterDefinitionHelperTest {
    ParameterDefinitionHelper objectUnderTest = new ParameterDefinitionHelper();

    @ParameterizedTest
    @MethodSource("parameterTestData")
    void shouldFindParameterNames(final String param, final Collection<String> expected) {
        final Set<String> actual = objectUnderTest.collectParameterNames(param);
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("listParametersTestData")
    void shouldFindAllParameters(final List<String> params, final Collection<String> expected) {
        final Set<String> actual = objectUnderTest.collectParameterNames(params);
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    private static Stream<Arguments> parameterTestData() {
        return Stream.of(
                arguments(" , ${}", emptySet()),
                arguments(
                        "'${param.date_start2}', '${param.date_end}', '${param.inv@lid12}', '${valid12}', '${12param}', ${paramNoQuote}, '${paramQuote}'",
                        Set.of("param.date_start2", "param.date_end", "valid12", "paramQuote")
                )
        );
    }

    private static Stream<Arguments> listParametersTestData() {
        return Stream.of(
                arguments(emptyList(), emptySet()),
                arguments(List.of("'${param.invalid_pram 123}', '${param.invalid_p@ram}'"), emptySet()),
                arguments(
                        List.of("'${param.date_start}', '${param.date_end}'", "'${param.date_end}', '${param.num_of_rows}', ${param.no_quote}"),
                        Set.of("param.date_start", "param.date_end", "param.num_of_rows")
                )
        );
    }
}