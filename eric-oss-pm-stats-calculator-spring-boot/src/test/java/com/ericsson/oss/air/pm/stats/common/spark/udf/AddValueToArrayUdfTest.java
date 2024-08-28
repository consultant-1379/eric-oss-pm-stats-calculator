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

import java.util.List;

import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

/**
 * Unit tests for {@link AddValueToArrayUdf}.
 */
class AddValueToArrayUdfTest {
    static final AddValueToArrayUdf<Number> objUnderTest = new AddValueToArrayUdf<>();
    static final Double[] INPUT_DOUBLE_ARRAY = {98.9, 97.6, 98.9, 99.0};
    static final WrappedArray<Number> DOUBLE_WRAPPED_ARRAY = WrappedArray.make(INPUT_DOUBLE_ARRAY);

    static final Long[] INPUT_LONG_ARRAY = {98L, 97L, 98L, 99L};
    static final WrappedArray<Number> LONG_WRAPPED_ARRAY = WrappedArray.make(INPUT_LONG_ARRAY);

    @Test
    void whenMaxSizeIsNull_ThenEmptyDoubleArrayReturned() {
        final List<Number> returnedArray = objUnderTest.call(DOUBLE_WRAPPED_ARRAY, 98.0, null);
        assertThat(returnedArray).isEmpty();
    }

    @Test
    void whenMaxSizeIsNegative_ThenEmptyDoubleArrayReturned() {
        final List<Number> returnedArray = objUnderTest.call(DOUBLE_WRAPPED_ARRAY, 98.0, -5);
        assertThat(returnedArray).isEmpty();
    }

    @Test
    void whenMaxSizeIsZero_ThenEmptyDoubleArrayReturned() {
        final List<Number> returnedArray = objUnderTest.call(DOUBLE_WRAPPED_ARRAY, 98.0, 0);
        assertThat(returnedArray).isEmpty();
    }

    @Test
    void whenDoubleArrayIsNullAndThresholdPassedIn_ThenArrayReturnedWithThreshold() {
        final List<Number> returnedArray = objUnderTest.call(null, 98.0, 14);
        assertThat(returnedArray).containsExactly(98.0);
    }

    @Test
    void whenDoubleArrayIsNull_AndThresholdIsNull_ThenArrayReturnedWithNull() {
        final List<Number> returnedArray = objUnderTest.call(null, null, 14);
        assertThat(returnedArray).containsOnlyNulls();
    }

    @Test
    void whenDoubleArrayIsNotNull_AndThresholdIsNull_ThenArrayReturnedWithNullAtTheEnd() {
        final List<Number> returnedArray = objUnderTest.call(DOUBLE_WRAPPED_ARRAY, null, 14);
        assertThat(returnedArray).containsExactly(98.9, 97.6, 98.9, 99.0, null);
    }

    @Test
    void whenDoubleArrayIsNotNull_AndThresholdIsNotNull_ThenArrayReturnedWithThreshold() {
        final List<Number> returnedArray = objUnderTest.call(DOUBLE_WRAPPED_ARRAY, 98.7, 14);
        assertThat(returnedArray).containsExactly(98.9, 97.6, 98.9, 99.0, 98.7);
    }

    @Test
    void whenDoubleArrayContainsMaxValues_ThenFirstValueRemoved_AndNewThresholdAdded() {
        final List<Number> returnedArray = objUnderTest.call(DOUBLE_WRAPPED_ARRAY, 91.0, 4);
        assertThat(returnedArray).containsExactly(97.6, 98.9, 99.0, 91.0);
    }

    @Test
    void whenMaxSizeIsNull_ThenEmptyLongArrayReturnedL() {
        final List<Number> returnedArray = objUnderTest.call(LONG_WRAPPED_ARRAY, 98L, null);
        assertThat(returnedArray).isEmpty();
    }

    @Test
    void whenMaxSizeIsZero_ThenEmptyLongArrayReturned() {
        final List<Number> returnedArray = objUnderTest.call(LONG_WRAPPED_ARRAY, 98L, 0);
        assertThat(returnedArray).isEmpty();
    }

    @Test
    void whenLongArrayIsNullAndThresholdPassedIn_ThenArrayReturnedWithThreshold() {
        final List<Number> returnedArray = objUnderTest.call(null, 98L, 14);
        assertThat(returnedArray).containsExactly(98L);
    }

    @Test
    void whenLongArrayIsNotNull_AndThresholdIsNotNull_ThenArrayReturnedWithThreshold() {
        final List<Number> returnedArray = objUnderTest.call(LONG_WRAPPED_ARRAY, 98L, 14);
        assertThat(returnedArray).containsExactly(98L, 97L, 98L, 99L, 98L);
    }

    @Test
    void whenLongArrayContainsMaxValues_ThenFirstValueRemoved_AndNewThresholdAdded() {
        final List<Number> returnedArray = objUnderTest.call(LONG_WRAPPED_ARRAY, 91L, 4);
        assertThat(returnedArray).containsExactly(97L, 98L, 99L, 91L);
    }
}
