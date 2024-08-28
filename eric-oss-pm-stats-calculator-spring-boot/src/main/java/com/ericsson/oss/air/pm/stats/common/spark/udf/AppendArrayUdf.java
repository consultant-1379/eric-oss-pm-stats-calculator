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

import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.concatenate;
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.isEmpty;

import java.util.List;
import javax.annotation.Nullable;

import org.apache.spark.sql.api.java.UDF3;
import scala.collection.mutable.WrappedArray;

/**
 * This {@link UDF3} takes in two arrays, and 1 limit integer parameter. Returns the concatenation of the arrays and removes
 * oldest entries in the array when size of the array exceeds array limit
 */
public class AppendArrayUdf implements UDF3<WrappedArray<Number>, WrappedArray<Number>, Integer, List<Number>> {
    private static final long serialVersionUID = -1132872644044361534L;

    public static final String INTEGER_NAME = "APPEND_INTEGER_ARRAY";
    public static final String LONG_NAME = "APPEND_LONG_ARRAY";

    @Override
    @Nullable
    public List<Number> call(final WrappedArray<Number> existingArray, final WrappedArray<Number> newArray, final Integer arraySizeLimit) {
        if (isEmpty(existingArray) && isEmpty(newArray)) {
            return null;
        }

        final List<Number> mergedValues = concatenate(existingArray, newArray);

        if (arraySizeLimit == null || arraySizeLimit <= 0) {
            return mergedValues;
        }

        final int numberOfElementsToRemove = mergedValues.size() - arraySizeLimit;
        return 0 < numberOfElementsToRemove
                ? mergedValues.subList(numberOfElementsToRemove, mergedValues.size())
                : mergedValues;
    }
}
