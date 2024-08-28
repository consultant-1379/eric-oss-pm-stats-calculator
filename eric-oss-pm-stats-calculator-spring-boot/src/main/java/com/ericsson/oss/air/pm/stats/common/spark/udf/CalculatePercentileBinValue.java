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

import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.isEmpty;
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.isNotPercentile;
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.toJavaList;

import java.math.BigInteger;
import java.util.List;

import org.apache.spark.sql.api.java.UDF2;
import scala.collection.mutable.WrappedArray;

/**
 * This {@link UDF2} takes in an array and a percentile value, and returns the interpolated bin value at the percentile provided.
 */
public class CalculatePercentileBinValue implements UDF2<WrappedArray<Number>, Integer, Double> {
    private static final long serialVersionUID = 9187325090419583874L;

    public static final String NAME = "CALCULATE_PERCENTILE_BIN";

    @Override
    public Double call(final WrappedArray<Number> columnValue, final Integer percentile) {
        if (isNotPercentile(percentile)) {
            return 0.0;
        }

        final List<Number> values = toJavaList(columnValue);
        if (isEmpty(values)) {
            return 0.0;
        }

        return calculateInterpolatedBinNumber(values, percentile);
    }

    private double calculateInterpolatedBinNumber(final List<Number> values, final Integer percentile) {
        final BigInteger sumOfAllValuesInArray = values.stream().map(value -> BigInteger.valueOf(value.longValue())).reduce(BigInteger.ZERO, BigInteger::add);
        if(sumOfAllValuesInArray.equals(BigInteger.ZERO)) {
            return 0.0D;
        }

        final double percentileValue = sumOfAllValuesInArray.doubleValue() * (percentile / 100.0D);

        double sumOfWholeBins = 0;
        double sumOfArrayBins = 0;
        boolean done = false;
        int binIndex = 0;
        while (!done) {
            sumOfArrayBins += values.get(binIndex).doubleValue();
            if (sumOfArrayBins >= percentileValue) {
                done = true;
            } else {
                sumOfWholeBins = sumOfArrayBins;
                binIndex++;
            }
        }
        final double percentageOfFinalBin = (percentileValue - sumOfWholeBins) / values.get(binIndex).doubleValue();

        return binIndex + percentageOfFinalBin;
    }
}
