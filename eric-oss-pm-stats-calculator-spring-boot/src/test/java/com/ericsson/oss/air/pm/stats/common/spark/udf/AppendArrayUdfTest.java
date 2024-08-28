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
import static scala.collection.mutable.WrappedArray.make;

import java.util.List;

import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

class AppendArrayUdfTest {
    final AppendArrayUdf objectUnderTest = new AppendArrayUdf();

    @Test
    void whenArrayToAppendAndNewDataArrayIsNull_ThenNullReturned() {
        final List<Number> actual = objectUnderTest.call(null, null, 6);
        assertThat(actual).isNull();
    }

    @Test
    void whenLimitIsInvalid_ThenConcatenatedIntegerArrayIsReturned() {
        final List<Number> actual = objectUnderTest.call(array(3, 2, -1), array(2), -2);
        assertThat(actual).containsExactly(3, 2, -1, 2);
    }

    @Test
    void whenArrayToAppendIsNull_ThenReturnNewDataArray() {
        final List<Number> actual = objectUnderTest.call(null, array(2), 6);
        assertThat(actual).containsExactly(2);
    }

    @Test
    void whenArrayToAppendIsNull_AndNewIntegerArrayIsLongerThanTheLimit_ThenReturnShortenedIntegerArray() {
        final List<Number> actual = objectUnderTest.call(null, array(1, 2, 3, 4, 5, 6, 7), 6);
        assertThat(actual).containsExactly(2, 3, 4, 5, 6, 7);
    }

    @Test
    void whenNewDataArrayIsNull_ThenReturnIntegerArrayToAppend() {
        final List<Number> actual = objectUnderTest.call(array(3, 2, -1), null, 6);
        assertThat(actual).containsExactly(3, 2, -1);
    }

    @Test
    void whenNewDataIntegerArrayAndIntegerArrayToAppendNotNull_ThenReturnConcatOfTwoIntegerArrays() {
        final List<Number> actual = objectUnderTest.call(array(3, 2, -1), array(2), 6);
        assertThat(actual).containsExactly(3, 2, -1 ,2);
    }

    @Test
    void whenAppendedIntegerArraysSizeGreaterThanConfigLimit_ThenReturnConcatOfTwoIntegerArraysAndRemoveOldestEntry() {
        final List<Number> actual = objectUnderTest.call(array(3, 2, -1, 1, 3, 3), array(2), 6);
        assertThat(actual).containsExactly(2, -1, 1, 3, 3, 2);
    }

    @Test
    void whenAppendedIntegerArraysSizeGreaterThanConfigLimitAndNewDataArrayIsNull_ThenReturnAppendedIntegerArray() {
        final List<Number> actual = objectUnderTest.call(array(3, 2, -1, 1, 3, 3, 5), null, 6);
        assertThat(actual).containsExactly(2, -1, 1, 3, 3, 5);
    }

    @Test
    void whenAppendedIntegerArraysSizeIsEqualToConfigLimitAndNewDataIntegerArrayIsNotNull_ThenReturnConcatOfTwoIntegerArrays() {
        final List<Number> actual = objectUnderTest.call(array(3, 2, -1, 1, 3), array(2), 6);
        assertThat(actual).containsExactly(3, 2, -1, 1, 3, 2);
    }

    @Test
    void whenLimitIsInvalid_ThenConcatenatedLongArrayIsReturned() {
        final List<Number> actual = objectUnderTest.call(array(3L, 2L, -1L), array(2L), -2);
        assertThat(actual).containsExactly(3L, 2L, -1L, 2L);
    }

    @Test
    void whenArrayToAppendIsNull_AndNewLongArrayIsLongerThanTheLimit_ThenReturnShortenedLongArray() {
        final List<Number> actual = objectUnderTest.call(null, array(1L, 2L, 3L, 4L, 5L, 6L, 7L), 6);
        assertThat(actual).containsExactly(2L, 3L, 4L, 5L, 6L, 7L);
    }

    @Test
    void whenNewDataLongArrayAndLongArrayToAppendNotNull_ThenReturnConcatOfTwoLongArrays() {
        final List<Number> actual = objectUnderTest.call(array(3L, 2L, -1L), array(2L), 6);
        assertThat(actual).containsExactly(3L, 2L, -1L ,2L);
    }

    @Test
    void whenAppendedLongArraysSizeGreaterThanConfigLimit_ThenReturnConcatOfTwoLongArraysAndRemoveOldestEntry() {
        final List<Number> actual = objectUnderTest.call(array(3L, 2L, -1L, 1L, 3L, 3L), array(2L), 6);
        assertThat(actual).containsExactly(2L, -1L, 1L, 3L, 3L, 2L);
    }

    @Test
    void whenAppendedLongArraysSizeIsEqualToConfigLimitAndNewDataLongArrayIsNotNull_ThenReturnConcatOfTwoLongArrays() {
        final List<Number> actual = objectUnderTest.call(array(3L, 2L, -1L, 1L, 3L), array(2L), 6);
        assertThat(actual).containsExactly(3L, 2L, -1L, 1L, 3L, 2L);
    }

    static WrappedArray<Number> array(final Number... values) {
        return make(values);
    }
}