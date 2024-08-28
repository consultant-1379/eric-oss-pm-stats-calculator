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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MergersTest {
    @Test
    void shouldVerifySymmetricDifference() {
        final List<Integer> left = Arrays.asList(1, 2, 3, 4, 5);
        final List<Integer> right = Arrays.asList(3, 4, 5, 6, 7);

        final List<Integer> actual = Mergers.symmetricDifference(left, right);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(1, 2, 6, 7);
    }

    @Test
    void shouldMergeSorted() {
        final List<Integer> left = Arrays.asList(1, 4, 6, 2, 3);
        final List<Integer> right = Arrays.asList(5, 4, 2, 1, 6);

        final List<Integer> actual = Mergers.mergeSorted(left, right, Comparator.naturalOrder());
        Assertions.assertThat(actual).containsExactly(1, 1, 2, 2, 3, 4, 4, 5, 6, 6);
    }

    @Test
    void shouldMerge() {
        final List<Integer> left = Arrays.asList(1, 2, 3);
        final List<Integer> right = Arrays.asList(4, 5, 6);

        final List<Integer> actual = Mergers.merge(left, right);
        Assertions.assertThat(actual).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void shouldUnion() {
        final List<Integer> left = Arrays.asList(1, 2, 3, 4, 5);
        final List<Integer> right = Arrays.asList(3, 4, 5, 6, 7);

        final List<Integer> actual = Mergers.union(left, right);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7);
    }

    @Nested
    class DistinctMergeBy {

        @Test
        void shouldDistinctMergeBy() {
            final Employee john1 = Employee.of(1, "John");
            final Employee john2 = Employee.of(2, "John");
            final Employee elizabeth1 = Employee.of(3, "Elizabeth");
            final Employee mark1 = Employee.of(4, "Mark");
            final Employee james1 = Employee.of(5, "James");
            final Employee elizabeth2 = Employee.of(6, "Elizabeth");

            final List<Employee> left = List.of(john1, elizabeth1, mark1, elizabeth2);
            final List<Employee> right = List.of(john2, james1);

            final Collection<Employee> actual = Mergers.distinctMergeBy(left, right, Employee::getName);

            Assertions.assertThat(actual).map(Employee::getName).containsExactlyInAnyOrder(
                    "John",
                    "Elizabeth",
                    "Mark",
                    "James"
            );
        }

    }

    @Data(staticConstructor = "of")
    static final class Employee {
        final int id;
        final String name;
    }
}
