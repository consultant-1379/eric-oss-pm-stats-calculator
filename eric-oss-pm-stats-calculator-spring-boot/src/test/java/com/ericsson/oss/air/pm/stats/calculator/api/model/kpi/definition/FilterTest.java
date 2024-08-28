/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class FilterTest {
    @ParameterizedTest(name = "[{index}] {0} {1} is conflicting: {2}")
    @ArgumentsSource(ProvideFilterConflictingArgument.class)
    void whenFilterConflicting(Filter filter1, Filter filter2, boolean expected) {
        boolean isConflicting = filter1.isConflicting(filter2);
        assertEquals(isConflicting, expected);
    }

    @ParameterizedTest(name = "[{index}] {0} isEmpty: {1}")
    @ArgumentsSource(ProvideFilterEmptyArgument.class)
    void whenFilterIsEmpty(Filter filter, boolean expected) {
        boolean isEmpty = filter.isEmpty();
        assertEquals(isEmpty, expected);
    }

    private static final class ProvideFilterEmptyArgument implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new Filter("filter"), false),
                    Arguments.of(new Filter(null), true),
                    Arguments.of(new Filter(""), true));
        }
    }

    private static final class ProvideFilterConflictingArgument implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new Filter("filter1.filter2"), new Filter("filter1.filter2"), false),
                    Arguments.of(new Filter("filter1.filter2"), new Filter("filter1.filter3"), true),
                    Arguments.of(new Filter("filter1.filter2"), new Filter("filter2.filter3"), false),
                    Arguments.of(new Filter("filter1.filter2"), new Filter("filter2.filter2"), false));
        }
    }
}
