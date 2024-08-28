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

import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

class CalculateMedianValueTest {

    static final CalculateMedianValue UDF = new CalculateMedianValue();

    final Double[] evenAryDouble = { 3.0, 5.0, 1.0, 8.0, 2.0, 6.0, 4.0, 7.0, 9.0, 10.0 };
    final Double[] oddAryDouble = { 11.0, 3.0, 5.0, 1.0, 8.0, 2.0, 6.0, 4.0, 7.0, 9.0, 10.0 };
    final Double[] nullElementAry = { 3.0, 5.0, 1.0, 8.0, 2.0, 6.0, 4.0, 7.0, 9.0, null };
    final Long[] evenAryLong = { 3L, 5L, 1L, 8L, 2L, 6L, 4L, 7L, 9L, 10L };
    final Long[] oddAryLong = { 11L, 3L, 5L, 1L, 8L, 2L, 6L, 4L, 7L, 9L, 10L };
    final Integer[] evenAryInt = { 3, 5, 1, 8, 2, 6, 4, 7, 9, 10 };
    final Integer[] oddAryInt = { 11, 3, 5, 1, 8, 2, 6, 4, 7, 9, 10 };
    final Double[] emptyAry = {};
    final Double[] nullAry = {null, null, null};
    final WrappedArray<Number> evenWrappedArrayDouble = WrappedArray.make(evenAryDouble);
    final WrappedArray<Number> oddWrappedArrayDouble = WrappedArray.make(oddAryDouble);
    final WrappedArray<Number> nullWrappedArray = WrappedArray.make(nullElementAry);
    final WrappedArray<Number> emptyWrappedArray = WrappedArray.make(emptyAry);
    final WrappedArray<Number> nullArray = WrappedArray.make(nullAry);

    final WrappedArray<Number> evenWrappedArrayLong = WrappedArray.make(evenAryLong);
    final WrappedArray<Number> oddWrappedArrayLong = WrappedArray.make(oddAryLong);
    final WrappedArray<Number> evenWrappedArrayInt = WrappedArray.make(evenAryInt);
    final WrappedArray<Number> oddWrappedArrayInt = WrappedArray.make(oddAryInt);

    @Test
    void whenDoubleArrayIsOddLength_ThenRightValueIsReturned() {
        final Double result = UDF.call(oddWrappedArrayDouble);
        assertThat(result).isEqualTo(6.0D);
    }

    @Test
    void whenDoubleArrayIsEvenLength_ThenRightValueIsReturned() {
        final Double result = UDF.call(evenWrappedArrayDouble);
        assertThat(result).isEqualTo(5.5D);
    }

    @Test
    void whenLongArrayIsEvenLength_ThenRightValueIsReturned() {
        final Double result = UDF.call(oddWrappedArrayLong);
        assertThat(result).isEqualTo(6.0D);
    }

    @Test
    void whenLongArrayIsOddLength_ThenRightValueIsReturned() {
        final Double result = UDF.call(evenWrappedArrayLong);
        assertThat(result).isEqualTo(5.5D);
    }

    @Test
    void whenIntArrayIsEvenLength_ThenRightValueIsReturned() {
        final Double result = UDF.call(oddWrappedArrayInt);
        assertThat(result).isEqualTo(6.0D);
    }

    @Test
    void whenIntArrayIsOddLength_ThenRightValueIsReturned() {
        final Double result = UDF.call(evenWrappedArrayInt);
        assertThat(result).isEqualTo(5.5D);
    }

    @Test
    void whenArrayHasNullElements_ThenNullElementsAreIgnored() {
        final Double result = UDF.call(nullWrappedArray);
        assertThat(result).isEqualTo(5.0D);
    }

    @Test
    void whenArrayHasOnlyNulls_ThenNullIsReturned() {
        final Double result = UDF.call(nullArray);
        assertThat(result).isNull();
    }

    @Test
    void whenArrayIsNull_ThenNullIsReturned() {
        final Double result = UDF.call(emptyWrappedArray);
        assertThat(result).isNull();
    }

    @Test
    void whenArrayIsEmpty_ThenNullIsReturned() {
        final Double result = UDF.call(null);
        assertThat(result).isNull();
    }
}