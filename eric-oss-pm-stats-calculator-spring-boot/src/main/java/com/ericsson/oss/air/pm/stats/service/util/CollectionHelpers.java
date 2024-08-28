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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionHelpers {

    public static <T> boolean anyMissing(@NonNull final Collection<? extends T> base, final Collection<T> from) {
        return base.stream().anyMatch(Predicates.isMissing(from));
    }

    public static <T, F> Map<F, List<T>> groupBy(@NonNull final Collection<? extends T> collection, final Function<? super T, ? extends F> classifier) {
        return collection.stream().collect(Collectors.groupingBy(classifier));
    }

    public static <T, F> Set<F> collectDistinctBy(@NonNull final Collection<T> collection, final Function<? super T, ? extends F> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <T> List<T> allMissingElements(@NonNull final Collection<? extends T> base, final Collection<T> from) {
        return base
                .stream()
                .filter(Predicates.isMissing(from))
                .collect(Collectors.toList());
    }

    public static <I, O, V> Map<O, V> transformKey(@NonNull final Map<I, ? extends V> map, final Function<? super I, ? extends O> keyMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(key -> keyMapper.apply(key.getKey()), Entry::getValue));
    }

    public static <I, O, K> Map<K, O> transformValue(@NonNull final Map<? extends K, ? extends I> map, final Function<? super I, ? extends O> valueMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, value -> valueMapper.apply(value.getValue())));
    }

    public static <K, V> Set<V> flattenDistinctValues(@NonNull final Map<K, ? extends Collection<V>> map) {
        return map.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static <I, O> Collection<O> transform(@NonNull final Collection<? extends I> collection, final Function<I, ? extends O> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T extends Object & Comparable<? super T>> T min(final T element, final Collection<? extends T> collection) {
        final List<T> copy = new ArrayList<>(collection);
        copy.add(element);
        return Collections.min(copy);
    }

    public static long sumExact(@NonNull final Stream<Long> stream) {
        return stream.reduce(0L, Math::addExact);
    }

}
