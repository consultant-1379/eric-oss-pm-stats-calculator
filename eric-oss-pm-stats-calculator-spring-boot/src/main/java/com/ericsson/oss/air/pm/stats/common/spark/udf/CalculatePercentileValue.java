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

import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.isEmpty;
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.isNotPercentile;
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.toJavaList;
import static com.google.common.math.Quantiles.percentiles;

import java.util.List;

import org.apache.spark.sql.api.java.UDF2;
import scala.collection.mutable.WrappedArray;

/**
 * This {@link UDF2} takes in an array and a percentile value, and returns the interpolated value at the percentile provided.
 */
public class CalculatePercentileValue implements UDF2<WrappedArray<Number>, Number, Double> {
    private static final long serialVersionUID = 9187325090419583874L;

    private static final double ZERO = 0.0D;

    public static final String NAME = "CALCULATE_PERCENTILE_VALUE";

    @Override
    public Double call(final WrappedArray<Number> columnValue, final Number percentile) {
        if (isNotPercentile(percentile)) {
            return ZERO;
        }

        final List<? extends Number> values = toJavaList(columnValue);
        if (isEmpty(values)) {
            return ZERO;
        }

        return computePercentile(percentile, values);
    }

    @SuppressWarnings("UnstableApiUsage")
    private double computePercentile(final Number percentile, final List<? extends Number> values) {
        return percentiles().index(percentile.intValue()).compute(values);
    }
}
