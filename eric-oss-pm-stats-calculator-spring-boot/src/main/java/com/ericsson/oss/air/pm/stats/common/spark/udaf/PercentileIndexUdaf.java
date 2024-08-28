/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udaf;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.types.DataTypes;

/**
 * {@link UserDefinedAggregateFunction} The purpose of this UDAF is to return the percentile index of the input array where
 * the sum is greater than or equal to the percentile value. The percentile value of the array is calculated
 * based on the percentile field of the class.
 * For example the input array is [3, 4, 1, 2, 3, 4] and the percentile is 80 then the percentile value
 * is 13.6 and sum of the array is greater than or equal to 13.6 at index 5. So the percentile index is 5.
 * Only 80 and 90 percentiles are supported.
 * The calculation is done in the "evaluate" method.
 * <p>
 * <b>NOTE:</b> Any 'null' values in the array will be converted to '0' before the aggregation.
 */
public class PercentileIndexUdaf extends AbstractDoubleArrayUdaf {

    public static final String PERCENTILE_INDEX_80_UDAF = "PERCENTILE_INDEX_80";
    public static final String PERCENTILE_INDEX_90_UDAF = "PERCENTILE_INDEX_90";

    private static final long serialVersionUID = 6522155921701719219L;
    private final Double percentile;

    public PercentileIndexUdaf(final Double percentileIndex) {
        super(DataTypes.IntegerType);
        percentile = percentileIndex;
    }

    @Override
    public void initialize(final MutableAggregationBuffer buffer) {
        buffer.update(0, null);
    }

    @Override
    public Object evaluate(final Row buffer) {
        if (buffer.isNullAt(0)) {
            return null;
        }

        //Find the array index associated with {@link PercentileIndexUdaf#percentile} percentage of distribution
        final List<Double> binValues = new ArrayList<>(buffer.getList(0));
        final double percentileValue = (binValues.stream().mapToDouble(Double::doubleValue).sum()) * (percentile / 100);
        Integer indexForPercentile = null;
        double tempSum = 0;
        for (int binIndex = 0; binIndex < binValues.size(); binIndex++) {
            tempSum += binValues.get(binIndex);
            if (tempSum >= percentileValue) {
                indexForPercentile = binIndex;
                break;
            }
        }
        return indexForPercentile;
    }
}
