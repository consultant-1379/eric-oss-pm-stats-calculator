/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf;

import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.toJavaList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.spark.sql.api.java.UDF3;
import scala.collection.mutable.WrappedArray;

/**
 * This {@link UDF3} takes in a number array, a number value and a size limit for the array.
 * If the array is considered full, then the value to append is added and earlier entries are removed to satisfy the limit.
 * @param <T> an object type that extends {@link Number}
 */
public class AddValueToArrayUdf<T extends Number> implements UDF3<WrappedArray<T>, T, Integer, List<T>> {
    private static final long serialVersionUID = -8487308385652655902L;

    public static final String DOUBLE_NAME = "ADD_DOUBLE_TO_ARRAY_WITH_LIMIT";
    public static final String INTEGER_NAME = "ADD_INTEGER_TO_ARRAY_WITH_LIMIT";
    public static final String LONG_NAME = "ADD_LONG_TO_ARRAY_WITH_LIMIT";

    @Override
    public List<T> call(final WrappedArray<T> numberWrappedArray, final T aNumber, final Integer maxSize) {
        if (maxSize == null || maxSize <= 0) {
            return new ArrayList<>();
        }

        if (Objects.isNull(numberWrappedArray)) {
            final List<T> newArrayList = new ArrayList<>();
            newArrayList.add(aNumber);
            return newArrayList;
        }

        final List<T> inputArrayList = requireNonNull(toJavaList(numberWrappedArray));

        if (numberWrappedArray.size() >= maxSize) {
            final List<T> subList = new ArrayList<>(inputArrayList.subList(inputArrayList.size() - maxSize + 1, inputArrayList.size()));
            subList.add(aNumber);
            return subList;
        }

        inputArrayList.add(aNumber);
        return inputArrayList;
    }

}
