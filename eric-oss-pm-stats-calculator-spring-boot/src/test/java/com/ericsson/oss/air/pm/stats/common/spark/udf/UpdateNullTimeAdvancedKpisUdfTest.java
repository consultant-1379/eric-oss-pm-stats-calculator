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
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.assertj.core.api.InstanceOfAssertFactories.array;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for {@link UpdateNullTimeAdvancedKpisUdf}.
 */
class UpdateNullTimeAdvancedKpisUdfTest {

    UpdateNullTimeAdvancedKpisUdf objectUnderTest = new UpdateNullTimeAdvancedKpisUdf();

    @Test
    void whenUpdatingATimeAdvancedKpiValue_AndANullValueIsPassedToTheUDF_ThenAnArrayOfZerosIsReturned() {
        final Object actual = objectUnderTest.call(null, 4);
        assertThat(actual).asInstanceOf(array(Integer[].class)).hasSize(4).containsOnly(0);
    }

    @Test
    void whenUpdatingATimeAdvancedKpiValue_AndTheValuePassedToTheUDFIsNotNull_ThenTheValueIsReturned() {
        final Object actual = objectUnderTest.call(new Integer[]{1, 2, 3}, 3);
        assertThat(actual).asInstanceOf(array(Integer[].class)).containsExactly(1, 2, 3);
    }

    @Test
    void whenUpdatingATimeAdvancedKpiValue_AndTheObjectIsNotNull_ThenTheOriginalObjectIsReturned() {
        final Object actual = objectUnderTest.call(0.5, 1);
        assertThat(actual).asInstanceOf(DOUBLE).isEqualTo(0.5);
    }

    @Test
    void whenUpdatingATimeAdvancedKpiValue_AndTheArrayLengthIsInvalid_ThenNullIsReturned() {
        final Object actual = objectUnderTest.call(null, -1);
        assertThat(actual).isNull();
    }

    @Test
    void whenUpdatingATimeAdvancedKpiValue_AndTheArrayLengthIsZero_ThenAnEmptyArrayIsReturned() {
        final Object actual = objectUnderTest.call(null, 0);
        assertThat(actual).asInstanceOf(array(Integer[].class)).isEmpty();
    }
}