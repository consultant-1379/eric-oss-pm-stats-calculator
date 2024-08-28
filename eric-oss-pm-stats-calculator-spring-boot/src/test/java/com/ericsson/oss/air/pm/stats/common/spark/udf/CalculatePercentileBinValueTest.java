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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static scala.collection.mutable.WrappedArray.make;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import scala.collection.mutable.WrappedArray;

class CalculatePercentileBinValueTest {
    final CalculatePercentileBinValue objectUnderTest = new CalculatePercentileBinValue();

    WrappedArray<Number> testArray;

    @BeforeEach
    void setUp() {
        final Number[] ary = new Number[129];
        Arrays.setAll(ary, x -> x + 1);
        testArray = array(ary);
    }

    @Test
    void whenArrayIsValid_andPercentileValue_90_ThenValidBinIsReturned() {
        final Double actual = objectUnderTest.call(testArray, 90);
        assertThat(actual).isEqualTo(122.35365853658537);
    }

    @Test
    void whenArrayIsValid_andPercentileValue_80_ThenValidBinIsReturned() {
        final Double actual = objectUnderTest.call(testArray, 80);
        assertThat(actual).isEqualTo(115.32758620689656);
    }

    @Test
    void whenArrayIsValid_andPercentileValue_100_ThenValidBinIsReturned() {
        final Double actual = objectUnderTest.call(testArray, 100);
        assertThat(actual).isEqualTo(129.0);
    }

    @Test
    void whenArrayIsValid_andPercentileValue_0_ThenValidBinIsReturned() {
        final Double actual = objectUnderTest.call(array(6, 5, 9), 0);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsValid_andPercentileValueIsNull_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(array(1, 5, 9), null);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsNull_andPercentileValueIsValid_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(null, 100);
        assertThat(actual).isZero();
    }

    @Test
    void whenPercentileIsInvalid_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(testArray, -10);
        assertThat(actual).isZero();
    }

    @Test
    void whenPercentileIsOver100_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(testArray, 101);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsAllOneAndPercentIsLowerThanFirstBin_ThenValidPercentIsReturned() {
        final Double actual = objectUnderTest.call(array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 1);
        assertThat(actual).isEqualTo(0.1);
    }

    @MethodSource("provideVerificationData")
    @ParameterizedTest(name = "[{index}] Dataset: ''{0}'' percentile: ''{1}'' --> ''{2}''")
    void verifyAlgorithm(final WrappedArray<Number> dataset, final Integer percentile, final Double expected) {
        final Double actual = objectUnderTest.call(dataset, percentile);
        assertThat(actual).isEqualTo(expected, offset(0.01));
    }

    static Stream<Arguments> provideVerificationData() {
        return Stream.of(
                arguments(array(2, 6, 4, 8, 10), 40, 3.00),
                arguments(array(5, 3, 7, 10, 2), 20, 1.13),
                arguments(array(4, 1, 2, 5, 7), 80, 4.46),
                arguments(array(4L, 1L, 2L, 5L, Long.MAX_VALUE), 80, 4.8)
        );
    }

    static WrappedArray<Number> array(final Number... values) {
        return make(values);
    }

}
