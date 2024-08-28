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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import io.vavr.collection.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mergers {

    public static <T> List<T> symmetricDifference(final Collection<? extends T> left, final Collection<? extends T> right) {
        final Set<? extends T> set1 = Sets.newHashSet(left);
        final Set<? extends T> set2 = Sets.newHashSet(right);

        final SetView<T> union = Sets.union(set1, set2);
        final SetView<? extends T> intersection = Sets.intersection(set1, set2);

        return Lists.newArrayList(Sets.difference(union, intersection));
    }

    public static <T> List<T> mergeSorted(final Collection<? extends T> left, final Collection<? extends T> right, final Comparator<? super T> comparator) {
        return Stream.concat(left, right).sorted(comparator).collect(Collectors.toList());
    }

    public static <T> List<T> merge(final Collection<? extends T> left, final Collection<? extends T> right) {
        return Stream.concat(left, right).collect(Collectors.toList());
    }

    public static <T> List<T> union(final Collection<? extends T> left, final Collection<? extends T> right) {
        return Lists.newArrayList(Sets.union(Sets.newHashSet(left), Sets.newHashSet(right)));
    }

    /**
     * Merges the provided {@link Collection}s based on the provided fieldMapper {@link Function}.
     * <br>
     * No duplicate element appears based on the fieldMapper {@link Function}, but the order of what gets elected on fieldMapper conflict is not
     * defined.
     *
     * @param left        {@link Collection} to merge.
     * @param right       {@link Collection} to merge.
     * @param fieldMapper {@link Function} to extract field based on merging is handled.
     * @param <T>         element type of the provided collections.
     * @param <F>         return type of the field mapper.
     * @return {@link Collection} containing merged left and right based on the field mapper.
     */
    public static <T, F> Collection<T> distinctMergeBy(final Collection<T> left, final Collection<T> right, final Function<? super T, F> fieldMapper) {
        return java.util.stream.Stream.of(left, right)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        fieldMapper,
                        Function.identity(),
                        (firstElement, lastElement) -> firstElement))
                .values();
    }
}
