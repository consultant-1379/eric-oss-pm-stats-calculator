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

import static org.apache.commons.lang3.ArrayUtils.addFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static scala.collection.mutable.WrappedArray.empty;
import static scala.collection.mutable.WrappedArray.make;

import java.util.List;

import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

class AppendArrayGivenCardinalityAndLimitUdfTest {
    static final AppendArrayGivenCardinalityAndLimitUdf UDF = new AppendArrayGivenCardinalityAndLimitUdf();

    @Test
    void whenArrayToAppendAndNewDataArrayAreNull_ThenNullReturned() {
        final List<Number> actual = UDF.call(null, null, empty(), 2);
        assertThat(actual).isNull();
    }

    @Test
    void whenArrayToAppendIsNull_ThenReturnNewDataArray() {
        final List<Number> actual = UDF.call(null, array(0.3, 0.7, 0.22), empty(), 2);
        assertThat(actual).containsExactly(0.3, 0.7, 0.22);
    }

    @Test
    void whenNewDataArrayIsNull_ThenReturnArrayToAppend() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), null, array(2, 2), 2);
        assertThat(actual).containsExactly(9.0, 9.22, 9.1);
    }

    @Test
    void whenArrayToAppendAndNewDataArrayAreNotNull_ThenReturnConcatOfTwoArrays() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), empty(), 2);
        assertThat(actual).containsExactly(9.0, 9.22, 9.1, 0.3, 0.7, 0.22);
    }

    @Test
    void whenCardinalityArrayIsNull_ThenReturnConcatOfTwoArrays() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), null, 2);
        assertThat(actual).containsExactly(9.0, 9.22, 9.1, 0.3, 0.7, 0.22);
    }

    @Test
    void whenCardinalityArrayContainsInvalidValues_ThenReturnConcatOfTwoArrays() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), array(-3, -5), 1);
        assertThat(actual).containsExactly(9.0, 9.22, 9.1, 0.3, 0.7, 0.22);
    }

    @Test
    void whenSizeLimitIsNull_ThenReturnConcatOfTwoArrays() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), array(3, 3), null);
        assertThat(actual).containsExactly(9.0, 9.22, 9.1, 0.3, 0.7, 0.22);
    }

    @Test
    void whenSizeLimitIsInvalid_ThenReturnConcatOfTwoArrays() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), array(3, 3), -5);
        assertThat(actual).containsExactly(9.0, 9.22, 9.1, 0.3, 0.7, 0.22);
    }

    @Test
    void whenAppendedSizeGreaterThanSizeLimit_ThenReturnConcatOfTwoArraysByRemovingTheOldestEntries() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), array(3, 3), 1);
        assertThat(actual).containsExactly(0.3, 0.7, 0.22);
    }

    @Test
    void whenCardinalityArrayIsBigger_ThenReturnAnEmptyArray() {
        final List<Number> actual = UDF.call(array(9.0, 9.22, 9.1), array(0.3, 0.7, 0.22), array(10, 10), 1);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenInputArrayTypesAreLong_ThenReturnConcatenatedLongArray() {
        final List<Number> actual = UDF.call(longArray(3L, 9L), longArray(5L, 66L, 99L, Long.MAX_VALUE), array(10, 10), null);
        assertThat(actual).containsExactly(3L, 9L, 5L, 66L, 99L, Long.MAX_VALUE);;
    }

    @Test
    void whenInputArrayTypesAreInt_ThenReturnConcatenatedIntArray() {
        final List<Number> actual = UDF.call(intArray(3, 9, 4), intArray(5, 66, 99), array(2, 2), 1);
        assertThat(actual).containsExactly(4, 5, 66, 99);;
    }

    static WrappedArray<Number> array(final Number value, final Number... values) {
        return make(addFirst(values, value));
    }

    static WrappedArray<Number> intArray(final Integer value, final Integer... values) {
        return make(addFirst(values, value));
    }

    static WrappedArray<Number> longArray(final Long value, final Long... values) {
        return make(addFirst(values, value));
    }

    static WrappedArray<Integer> array(final Integer value, final Integer... values) {
        return make(addFirst(values, value));
    }
}