/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionHelpers {
    public static long sumExact(@NonNull final Stream<Long> stream) {
        return stream.reduce(0L, Math::addExact);
    }

    public static <T extends Serializable> List<T> deepCopy(@NonNull final Collection<? extends T> collection) {
        return collection.stream().map(SerializationUtils::clone).collect(Collectors.toList());
    }

    public static <I, O, V> Map<O, V> transformKey(@NonNull final Map<I, ? extends V> map, final Function<? super I, ? extends O> keyMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(key -> keyMapper.apply(key.getKey()), Entry::getValue));
    }

    public static <I, O, K> Map<K, O> transformValue(@NonNull final Map<? extends K, ? extends I> map, final Function<? super I, ? extends O> valueMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, value -> valueMapper.apply(value.getValue())));
    }
}
