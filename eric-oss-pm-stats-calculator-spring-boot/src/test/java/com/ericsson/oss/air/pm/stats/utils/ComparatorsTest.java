/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import java.util.Comparator;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ComparatorsTest {
    @MethodSource("provideMinData")
    @ParameterizedTest(name = "[{index}] min(''{0}'', ''{1}'') ==> ''{3}''")
    <E> void shouldReturnMin(final E left, final E right, final Comparator<? super E> comparator, final E expected) {
        final E actual = Comparators.min(left, right, comparator);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideMinData() {
        return Stream.of(
                Arguments.of(1, 1, (Comparator<Integer>) Integer::compareTo, 1),
                Arguments.of(1, 2, (Comparator<Integer>) Integer::compareTo, 1),
                Arguments.of(2, 1, (Comparator<Integer>) Integer::compareTo, 1),
                Arguments.of("hello", "bello", (Comparator<String>) String::compareTo, "bello")
        );
    }
}