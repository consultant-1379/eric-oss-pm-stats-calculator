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
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.isNotEmpty;
import static com.ericsson.oss.air.pm.stats.common.spark.udf.util.UdfUtils.toJavaList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.api.java.UDF2;
import scala.collection.mutable.WrappedArray;

@Slf4j
public class CalculateWeightedAverageUdf implements UDF2<WrappedArray<Number>, WrappedArray<Number>, Double> {
    private static final long serialVersionUID = 20000822L;

    public static final String NAME = "CALCULATE_WEIGHTED_AVERAGE";

    @Override
    public Double call(final WrappedArray<Number> values, final WrappedArray<Number> weights) {
        if (isEmpty(values)) {
            return null;
        }

        if (isNotEmpty(weights)) {
            final List<Number> arrayValues = requireNonNull(toJavaList(values));
            final List<Number> arrayWeights = requireNonNull(toJavaList(weights));
            if (arrayValues.size() == arrayWeights.size()) {
                return calculateWeightedAverage(arrayValues, arrayWeights);
            } else {
                return 0.0;
            }
        } else {
            log.warn("The array of the weights is empty. Regular average will be computed.");
            final List<Number> arrayValues = requireNonNull(toJavaList(values));
            return calculateAverage(arrayValues);
        }
    }

    private Double calculateWeightedAverage(final List<Number> arrayValues, final List<Number> arrayWeights) {
        double sum = 0.0;
        double weightSum = 0.0;
        for(int i = 0; i < arrayValues.size(); i++) {
            final double weight = arrayWeights.get(i).doubleValue();
            if (!Objects.isNull(arrayValues.get(i))) {
                sum += arrayValues.get(i).doubleValue() * weight;
            }
            weightSum += weight;
        }
        if (weightSum == 0) {
            log.warn("Total weight is zero.");
            return 0.0;
        }
        return sum / weightSum;
    }

    private Double calculateAverage(final List<Number> arrayValues) {
        double sum = 0.0;
        for (final Number value : arrayValues) {
            if (!Objects.isNull(value)) {
                sum += value.doubleValue();
            }
        }
        return sum / arrayValues.size();
    }
}
