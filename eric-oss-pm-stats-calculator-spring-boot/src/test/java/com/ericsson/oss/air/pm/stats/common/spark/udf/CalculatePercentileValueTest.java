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
import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static scala.collection.mutable.WrappedArray.empty;
import static scala.collection.mutable.WrappedArray.make;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import scala.collection.mutable.WrappedArray;

class CalculatePercentileValueTest {
    final CalculatePercentileValue objectUnderTest = new CalculatePercentileValue();

    @Test
    void whenArrayAndPercentileIsNull_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(null, null);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsEmpty_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(empty(), 55.0D);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsNull_ThenZeroIsReturned() {
        final Double actual = objectUnderTest.call(null, 95.0D);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsValid_andPercentileValueIsNull_thenZeroIsReturned() {
        final Double actual = objectUnderTest.call(array(3.0, 5.0, 1.0, 8.0, 2.0, 6.0, 4.0, 7.0, 9.0, 10.0), null);
        assertThat(actual).isZero();
    }

    @Test
    void whenPercentileNegative_thenZeroIsReturned() {
        final Double actual = objectUnderTest.call(array(3.0, 5.0, 1.0, 8.0, 2.0, 6.0, 4.0, 7.0, 9.0, 10.0), -1.0D);
        assertThat(actual).isZero();
    }

    @Test
    void whenArrayIsValid_andPercentileValue_101_thenZeroIsReturned() {
        final Double actual = objectUnderTest.call(array(1.0D, 6.0D), 101.0D);
        assertThat(actual).isZero();
    }

    @MethodSource("providePercentileData")
    @ParameterizedTest(name = "[{index}] Percentile: ''{0}'', Dataset: ''{1}'' --> ''{2}''")
    void verifyPercentile(final Double percentile, final WrappedArray<Number> dataset, final Double expected) {
        final Double actual = objectUnderTest.call(dataset, percentile);
        assertThat(actual).isEqualTo(expected, offset(0.001));
    }

    static Stream<Arguments> providePercentileData() {
        return Stream.of(
                arguments(20.0, array(10.0, 10.0, 10.0, 10.0, 10.0, 100.0, 100.0, 100.0, 100.0, 100.0), 10.0),
                arguments(70.0, array(10.0, 10.0, 10.0, 10.0, 10.0, 100.0, 100.0, 100.0, 100.0, 100.0), 100.0),
                arguments(55.0, array(10.0, 10.0, 10.0, 10.0, 10.0, 100.0, 100.0, 100.0, 100.0, 100.0), 95.5),
                arguments(55.0, array(1.0, 5.0, 10.0, 11.0, 13.0, 16.0, 17.0, 58.0, 69.0, 70.0), 15.85),
                arguments(11.0, array(1.0), 1.0),
                arguments(100.0, array(1,2,3,4,5),5.0),
                arguments(100.0, array(1,2,3,4,Long.MAX_VALUE), Long.valueOf(Long.MAX_VALUE).doubleValue())

        );
    }

    static WrappedArray<Number> array(final Number... values) {
        return make(values);
    }
}
