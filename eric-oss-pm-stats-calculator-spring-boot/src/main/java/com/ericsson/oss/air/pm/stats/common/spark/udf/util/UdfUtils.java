/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf.util;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static scala.collection.JavaConverters.seqAsJavaList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import lombok.NoArgsConstructor;
import scala.collection.mutable.WrappedArray;

@NoArgsConstructor(access = PRIVATE)
public final class UdfUtils {

    @Nullable
    public static <T> List<T> toJavaList(final WrappedArray<? extends T> array) {
        return Objects.isNull(array)
                ? null
                : new ArrayList<>(seqAsJavaList(array));
    }

    public static <T> List<T> concatenate(final WrappedArray<T> array1, final WrappedArray<T> array2) {
        final List<T> list1 = ofNullable(toJavaList(array1)).orElseGet(ArrayList::new);
        final List<T> list2 = ofNullable(toJavaList(array2)).orElseGet(ArrayList::new);

        list1.addAll(list2);
        return list1;
    }

    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(final WrappedArray<?> array) {
        return array == null || array.isEmpty();
    }

    public static boolean isNotEmpty(final WrappedArray<?> array) {
        return !isEmpty(array);
    }

    public static boolean isNotPercentile(final Number value) {
        return !isPercentile(value);
    }

    public static boolean isPercentile(final Number value) {
        if (value == null) {
            return false;
        }

        final double doubleValue = value.doubleValue();
        return 0 <= doubleValue && doubleValue <= 100;
    }
}
