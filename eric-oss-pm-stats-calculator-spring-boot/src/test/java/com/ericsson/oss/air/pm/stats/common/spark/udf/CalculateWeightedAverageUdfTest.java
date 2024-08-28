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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

class CalculateWeightedAverageUdfTest {
    static final CalculateWeightedAverageUdf objectUnderTest = new CalculateWeightedAverageUdf();

    static final WrappedArray<Number> WRAPPED_ARRAY = WrappedArray.make(new double[] {10.0, 10.0, 20.0});
    static final WrappedArray<Number> WRAPPED_WEIGHTS = WrappedArray.make(new double[] {1.0, 1.0, 2.0});
    static final WrappedArray<Number> WRAPPED_ZERO_WEIGHTS = WrappedArray.make(new double[] {0.0, 0.0, 0.0});
    static final WrappedArray<Number> WRAPPED_SIMPLE_ARRAY = WrappedArray.make(new double[] {0.0, 2.0});

    @Test
    void whenInputArraysAreValid_thenRightValueIsReturned() {
        final Double expectedWeightedAverage = 15.0;
        final Double actualWeightedAverage = objectUnderTest.call(WRAPPED_ARRAY, WRAPPED_WEIGHTS);
        assertThat(expectedWeightedAverage).isEqualTo(actualWeightedAverage);
    }

    @Test
    void whenWeightsAreZero_thenZeroIsReturned() {
        assertThat(objectUnderTest.call(WRAPPED_ARRAY, WRAPPED_ZERO_WEIGHTS)).isEqualTo(0.0);
    }

    @Test
    void whenValuesArrayIsNull_thenNullIsReturned() {
        final WrappedArray<Number> array = WrappedArray.make(new double[] {1.0});
        assertThat(objectUnderTest.call(null, array)).isNull();
    }

    @Test
    void whenValuesArrayIsEmpty_thenNullIsReturned() {
        final WrappedArray<Number> emptyArray = WrappedArray.make(new double[] {});
        final WrappedArray<Number> array = WrappedArray.make(new double[] {1.0});
        assertThat(objectUnderTest.call(emptyArray, array)).isNull();
    }

    @Test
    void whenWeightsArrayIsNull_thenNormalAverageIsReturned() {
        final Double expectedAverage = 1.0;
        final Double actualAverage = objectUnderTest.call(WRAPPED_SIMPLE_ARRAY, null);
        assertThat(expectedAverage).isEqualTo(actualAverage);
    }

    @Test
    void whenWeightsArrayIsEmpty_thenNormalAverageIsReturned() {
        final Double expectedAverage = 1.0;
        final WrappedArray<Number> array = WrappedArray.make(new double[] {1.0});
        final WrappedArray<Number> emptyArray = WrappedArray.make(new double[] {});
        final Double actualAverage = objectUnderTest.call(array, emptyArray);
        assertThat(expectedAverage).isEqualTo(actualAverage);
    }

    @Test
    void whenArraySizesDiffer_thenZeroIsReturned() {
        final WrappedArray<Number> shorterArray = WrappedArray.make(new double[] {1.0});
        final WrappedArray<Number> longerArray = WrappedArray.make(new double[] {1.0, 2.0});
        assertThat(objectUnderTest.call(shorterArray, longerArray)).isEqualTo(0.0);
    }
}