/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CollectionHelpersTest {
    @MethodSource("provideAnyMissingData")
    @ParameterizedTest(name = "[{index}] Base: ''{0}'' missing any: ''{2}'' from: ''{1}''")
    void shouldVerifyAnyMissing(final List<Integer> base, final List<? super Integer> from, final boolean expected) {
        final boolean actual = CollectionHelpers.anyMissing(base, from);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGroupBy() {
        final Employee john1 = Employee.of(1, "John");
        final Employee john2 = Employee.of(2, "John");
        final Employee elizabeth1 = Employee.of(3, "Elizabeth");
        final Employee mark1 = Employee.of(4, "Mark");
        final Employee james1 = Employee.of(5, "James");
        final Employee elizabeth2 = Employee.of(6, "Elizabeth");

        final List<Employee> collection = List.of(john1, john2, elizabeth1, mark1, james1, elizabeth2);

        final Map<String, List<Employee>> actual = CollectionHelpers.groupBy(collection, Employee::getName);

        Assertions.assertThat(actual).containsExactly(
                entry("Elizabeth", List.of(elizabeth1, elizabeth2)),
                entry("James", List.of(james1)),
                entry("John", List.of(john1, john2)),
                entry("Mark", List.of(mark1))
        );
    }

    @Test
    void shouldCollectDistinctBy() {
        final Employee john1 = Employee.of(1, "John");
        final Employee john2 = Employee.of(2, "John");
        final Employee elizabeth1 = Employee.of(3, "Elizabeth");
        final Employee mark1 = Employee.of(4, "Mark");
        final Employee james1 = Employee.of(5, "James");
        final Employee elizabeth2 = Employee.of(6, "Elizabeth");

        final List<Employee> collection = List.of(john1, john2, elizabeth1, mark1, james1, elizabeth2);

        final Set<String> actual = CollectionHelpers.collectDistinctBy(collection, Employee::getName);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                "John",
                "Elizabeth",
                "Mark",
                "James"
        );
    }

    @Test
    void shouldGiveBackMissingElements() {
        final List<String> expected = List.of("missing1", "missing2");
        final List<String> actual = CollectionHelpers.allMissingElements(
                List.of("contained1", "missing1", "contained2", "missing2", "contained3"),
                List.of("contained1", "contained2", "contained3")
        );
        Assertions.assertThat(actual).containsExactlyInAnyOrder(expected.toArray(String[]::new));
    }

    @Test
    void shouldTransformKey() {
        final Map<String, String> actual = CollectionHelpers.transformKey(Map.of(1, "value_1", 2, "value_2"), String::valueOf);

        Assertions.assertThat(actual).containsOnly(
                entry("1", "value_1"),
                entry("2", "value_2")
        );
    }

    @Test
    void shouldTransformValue() {
        final Map<String, String> actual = CollectionHelpers.transformValue(Map.of("key_1", 1, "key_2", 2), String::valueOf);

        Assertions.assertThat(actual).containsOnly(
                entry("key_1", "1"),
                entry("key_2", "2")
        );
    }

    @Test
    void shouldFlattenDistinctValues() {
        final Set<Integer> actual = CollectionHelpers.flattenDistinctValues(Map.of(
                1, List.of(1, 2, 3, 4, 5),
                2, List.of(1, 2, 3),
                3, List.of(4, 5),
                4, List.of(6)
        ));

        Assertions.assertThat(actual).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
    }

    @Test
    void shouldTransform() {
        final Collection<String> actual = CollectionHelpers.transform(List.of(1, 2), String::valueOf);

        Assertions.assertThat(actual).containsExactly("1", "2");
    }

    @MethodSource("provideMinData")
    @ParameterizedTest(name = "[{index}] Collection: ''{0}'' additional: ''{1}'' expected: ''{2}''")
    void shouldCalculateTheMinimum(final List<Integer> collection, final Integer additional, final Integer expected) {

        final Integer actual = CollectionHelpers.min(additional, collection);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Nested
    class SumExact {
        @Test
        void shouldSumExact() {
            final long actual = CollectionHelpers.sumExact(Stream.of(1L, 2L, 3L, 4L, 5L));
            Assertions.assertThat(actual).isEqualTo(15);
        }

        @Test
        void shouldRaiseExceptionOnOverflow() {
            Assertions.assertThatThrownBy(() -> CollectionHelpers.sumExact(Stream.of(Long.MAX_VALUE, 1L)))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessage("long overflow");
        }
    }

    private static Stream<Arguments> provideMinData() {
        return Stream.of(Arguments.of(List.of(1, 2, 3), 4, 1),
                Arguments.of(List.of(2, 3, 4), 1, 1),
                Arguments.of(List.of(1, 2, 3, 4), 1, 1),
                Arguments.of(List.of(), 1, 1)
        );
    }

    private static Stream<Arguments> provideAnyMissingData() {
        return Stream.of(Arguments.of(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3, 4, 5), false),
                Arguments.of(Arrays.asList(1, 2, 3), Arrays.asList(1, 2), true));
    }

    @Data(staticConstructor = "of")
    static final class Employee {
        final int id;
        final String name;
    }
}