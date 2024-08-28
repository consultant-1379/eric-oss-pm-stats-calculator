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

import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.toJavaList;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import org.apache.spark.sql.api.java.UDF1;
import scala.collection.mutable.WrappedArray;

/**
 * This {@link UDF1} takes in an array, sorts it then the middle element is returned. If the size of array is odd, then returns the element in the
 * middle. If the size of the array is even then the average of the two middle elements is returned.
 */
public class CalculateMedianValue implements UDF1<WrappedArray<Number>, Double> {
    private static final long serialVersionUID = 9187325090419583804L;

    public static final String NAME = "MEDIAN_OF_VALUES";

    @Override
    @Nullable
    public Double call(final WrappedArray<Number> columnValue) {
        if (Objects.isNull(columnValue)) {
            return null;
        }

        final List<Number> arrayValues = requireNonNull(toJavaList(columnValue));
        arrayValues.removeAll(Collections.singleton(null));
        if (arrayValues.isEmpty()) {
            return null;
        }
        arrayValues.sort((num1, num2) -> {
            double val1 = num1.doubleValue();
            double val2 = num2.doubleValue();
            return Double.compare(val1, val2);
        });
        final int arrayLength = arrayValues.size();
        final int middleElement = (int) Math.floor(arrayLength / 2.0);
        if (arrayLength % 2 == 0) {
            return (arrayValues.get(middleElement).doubleValue() + arrayValues.get(middleElement - 1).doubleValue()) / 2.0;
        }
        return arrayValues.get(middleElement).doubleValue();
    }
}