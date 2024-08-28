/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PredicatesTest {
    @MethodSource("provideIsMissingData")
    @ParameterizedTest(name = "[{index}] Collection: ''{0}'' missing: ''{1}'' ==>  ''{2}''")
    void shouldVerifyIsMissing(final List<Integer> collection, final Integer element, final boolean expected) {
        final Predicate<Integer> predicate = Predicates.isMissing(collection);

        final boolean actual = predicate.test(element);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldNegatePredicate() {
        final Predicate<Integer> isNotEven = Predicates.not(number -> number % 2 == 0);

        final boolean actual = isNotEven.test(2);

        Assertions.assertThat(actual).isFalse();
    }

    private static Stream<Arguments> provideIsMissingData() {
        return Stream.of(Arguments.of(Arrays.asList(1, 2, 3), 3, false),
                Arguments.of(Arrays.asList(1, 2, 3), 4, true));
    }
}