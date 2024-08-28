/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringUtilsTest {

    @Test
    void shouldCommaJoinList() {
        final String actual = StringUtils.commaJoinEnquote(Arrays.asList(Column.of("hello"), Column.of("everyone")));
        Assertions.assertThat(actual).isEqualTo("\"hello\", \"everyone\"");
    }

    @Test
    void shouldGeneratePrimaryKeyName() {
        final String actual = StringUtils.generatePrimaryKeyName(Table.of("table"));
        Assertions.assertThat(actual).isEqualTo("table_pkey");
    }

    @Test
    void shouldEnquote() {
        final String actual = StringUtils.enquoteLiteral("columName");
        Assertions.assertThat(actual).isEqualTo("\"columName\"");
    }

    @Test
    void shouldEnquoteCollection() {
        final Collection<String> actual = StringUtils.enquoteCollectionElements(List.of("hello", "everyone"));
        Assertions.assertThat(actual).containsExactlyInAnyOrder("\"hello\"", "\"everyone\"");
    }

    @ParameterizedTest
    @MethodSource("provideEnsureData")
    void shouldEnsureQuoted(final String literal, final String expected) {
        final String actual = StringUtils.ensureQuoted(literal);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideEnsureData() {
        return Stream.of(
                Arguments.of("noQuote", "\"noQuote\""),
                Arguments.of("\"preQuote", "\"\"preQuote\""),
                Arguments.of("\"quoted\"", "\"quoted\""),
                Arguments.of("postQuote\"", "\"postQuote\"\"")
        );
    }
}