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

import java.util.Arrays;

import org.apache.spark.sql.api.java.UDF2;

/**
 * This {@link UDF2} takes in a Time Advanced KPI and if null returns its value with an Integer array of 0s. If the value is not null it returns the
 * value.
 */
public class UpdateNullTimeAdvancedKpisUdf implements UDF2<Object, Integer, Object> {
    private static final long serialVersionUID = -7198279090419316975L;

    public static final String NAME = "UPDATE_NULL_TIME_ADVANCED_KPIS";

    @Override
    public Object call(final Object columnValue, final Integer arraySize) {
        if (columnValue == null && arraySize >= 0) {
            return createIntegerArrayWithNullValues(arraySize);
        }
        return columnValue;
    }

    private static Integer[] createIntegerArrayWithNullValues(final Integer arraySize) {
        final Integer[] intArray = new Integer[arraySize];
        Arrays.fill(intArray, 0);
        return intArray;
    }
}
