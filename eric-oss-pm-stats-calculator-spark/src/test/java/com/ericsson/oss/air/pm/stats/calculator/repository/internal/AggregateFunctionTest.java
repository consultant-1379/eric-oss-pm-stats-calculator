/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import static com.ericsson.oss.air.pm.stats.calculator.repository.internal.AggregateFunction.MAX;
import static com.ericsson.oss.air.pm.stats.calculator.repository.internal.AggregateFunction.MIN;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AggregateFunctionTest {

    @ParameterizedTest
    @MethodSource("provideSurroundData")
    void shouldVerifySurround(final AggregateFunction aggregateFunction, final Column column, final String expected) {
        final String actual = aggregateFunction.surround(column);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideSurroundData() {
        return Stream.of(
                Arguments.of(MIN, Column.of("column"), "MIN(column)"),
                Arguments.of(MAX, Column.of("column"), "MAX(column)")
        );
    }
}