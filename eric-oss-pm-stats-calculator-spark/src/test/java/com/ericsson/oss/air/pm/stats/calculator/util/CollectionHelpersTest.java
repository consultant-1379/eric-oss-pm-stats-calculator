/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

class CollectionHelpersTest {
    @Nested
    class SumExact {
        @Test
        void shouldSumExact() {
            final long actual = CollectionHelpers.sumExact(Stream.of(1L, 2L, 3L, 4L, 5L));
            Assertions.assertThat(actual).isEqualTo(15);
        }

        @Test
        void shouldRaiseExceptionOnOverflow() {
            Assertions.assertThatThrownBy(() -> CollectionHelpers.sumExact(Stream.of(Long.MAX_VALUE, 1L)))
                      .isInstanceOf(ArithmeticException.class)
                      .hasMessage("long overflow");
        }
    }

    @Nested
    class DeepCopy {
        @Test
        void shouldDeepCopy() {
            final LatestProcessedOffset latestProcessedOffset = LatestProcessedOffset.builder().build();

            final List<LatestProcessedOffset> actual = CollectionHelpers.deepCopy(Collections.singleton(latestProcessedOffset));

            Assertions.assertThat(actual).first().satisfies(copiedLatestProcessedOffset -> {
                Assertions.assertThat(copiedLatestProcessedOffset)
                          .usingRecursiveComparison()
                          .isNotSameAs(latestProcessedOffset)
                          .isEqualTo(latestProcessedOffset);
            });
        }
    }

    @Test
    void shouldTransformKey() {
        final Map<String, String> actual = CollectionHelpers.transformKey(ImmutableMap.of(1, "value_1", 2, "value_2"), String::valueOf);

        Assertions.assertThat(actual).containsOnly(
                entry("1", "value_1"),
                entry("2", "value_2")
        );
    }

    @Test
    void shouldTransformValue() {
        final Map<String, String> actual = CollectionHelpers.transformValue(ImmutableMap.of("key_1", 1, "key_2", 2), String::valueOf);

        Assertions.assertThat(actual).containsOnly(
                entry("key_1", "1"),
                entry("key_2", "2")
        );
    }
}