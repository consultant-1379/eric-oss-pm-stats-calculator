/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.toJavaList;
import static org.apache.commons.lang3.ObjectUtils.allNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import lombok.NonNull;
import org.apache.spark.sql.api.java.UDF4;
import scala.collection.mutable.WrappedArray;

/**
 * This {@link UDF4} takes in 2 arrays, 1 integer cardinality array, and 1 limit integer parameter. Returns the concatenation of the two data
 * arrays and removes the oldest data based on the corresponding cardinality from the cardinality array when the length of the cardinality array is
 * greater than limit parameter.
 */
public class AppendArrayGivenCardinalityAndLimitUdf
        implements UDF4<WrappedArray<Number>, WrappedArray<Number>, WrappedArray<Integer>, Integer, List<Number>> {
    private static final long serialVersionUID = 5848811097500919141L;

    public static final String DOUBLE_NAME = "APPEND_DOUBLE_ARRAY_GIVEN_CARDINALITY_AND_LIMIT";
    public static final String INT_NAME = "APPEND_INTEGER_ARRAY_GIVEN_CARDINALITY_AND_LIMIT";
    public static final String LONG_NAME = "APPEND_LONG_ARRAY_GIVEN_CARDINALITY_AND_LIMIT";


    @Override
    @Nullable
    public List<Number> call(
            final WrappedArray<Number> existingArray,
            final WrappedArray<Number> newArray,
            final WrappedArray<Integer> cardinalityArray,
            final Integer sizeLimit
    ) {
        if (allNull(existingArray, newArray)) {
            return null;
        }

        final List<Number> mergedValues = concatenate(existingArray, newArray);

        if (sizeLimit == null || sizeLimit <= 0) {
            return mergedValues;
        }

        final List<Integer> cardinalityValues = toJavaList(cardinalityArray);
        if (isEmpty(cardinalityValues) || containsNegativeNumber(cardinalityValues)) {
            return mergedValues;
        }

        return removeOlderData(mergedValues, cardinalityValues, sizeLimit);
    }

    private List<Number> removeOlderData(final List<Number> mergedValues, final List<Integer> cardinalityValues, final Integer sizeLimit) {
        final int cardinalityToRemove = cardinalityValues.size() - sizeLimit;

        if (cardinalityToRemove > 0) {
            final List<Integer> cardinalityToRemoveList = cardinalityValues.subList(0, cardinalityToRemove);
            final int numberOfElementsToRemove = cardinalityToRemoveList.stream().mapToInt(Integer::intValue).sum();
            if (numberOfElementsToRemove > mergedValues.size()) {
                return new ArrayList<>();
            }
            return new ArrayList<>(mergedValues.subList(numberOfElementsToRemove, mergedValues.size()));
        }

        return mergedValues;
    }

    private boolean containsNegativeNumber(@NonNull final Collection<Integer> collection) {
        return collection.stream().anyMatch(number -> number < 0);
    }

}
