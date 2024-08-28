/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

/**
 * Unit tests for {@link UdfUtils}.
 */
class UdfUtilsTest {

    @Test
    void whenExtractingAnArray_thenCorrectArrayIsReturned() {
        final WrappedArray<Integer> wrappedArray = WrappedArray.make(new Integer[] {1, 2, 3});
        assertThat(UdfUtils.toJavaList(wrappedArray)).contains(1, 2, 3);
    }

    @Test
    void whenExtractingANullArray_thenNullIsReturned() {
        final WrappedArray<Integer> nullArray = WrappedArray.make(null);
        assertThat(UdfUtils.toJavaList(nullArray)).isNull();
    }

    @Test
    void whenExtractingANull_thenNullIsReturned() {
        assertThat(UdfUtils.toJavaList(null)).isNull();
    }

    @Test
    void whenExtractingAnEmptyArray_thenEmptyArrayIsReturned() {
        final WrappedArray<Integer> emptyArray = WrappedArray.make(new Integer[] {});
        assertThat(UdfUtils.toJavaList(emptyArray)).isEmpty();
    }
}
