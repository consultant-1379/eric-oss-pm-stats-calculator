/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;
import org.apache.commons.collections4.SetUtils;

@NoArgsConstructor(access = PRIVATE)
public final class SparkCollections {

    public static <T> Set<T> union(final Set<? extends T> set1, final Set<? extends T> set2) {
        return new HashSet<>(SetUtils.union(set1, set2));
    }

    public static <T> Collection<T> asJavaCollection(final scala.collection.Iterable<T> iterable) {
        return scala.collection.JavaConverters.asJavaCollection(iterable);
    }

    public static <T> List<T> asJavaList(final scala.collection.Iterable<? extends T> iterable) {
        return asJavaStream(iterable).collect(toList());
    }

    public static <T> Stream<T> asJavaStream(final scala.collection.Iterable<T> iterable) {
        return asJavaCollection(iterable).stream();
    }
}
