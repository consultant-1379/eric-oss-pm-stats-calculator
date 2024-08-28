/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StartOffsetTest {

    @Test
    void shouldInstantiateFrom() {
        final StartOffset startOffset = StartOffset.from(LatestProcessedOffset.builder().withTopicPartitionOffset(10L).build());

        final long actual = startOffset.number();
        Assertions.assertThat(actual).isEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("provideCompareToData")
    void shouldCompareTo(final StartOffset left, final StartOffset right, final int expected) {
        final int actual = left.compareTo(right);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideCompareToData() {
        return Stream.of(
                Arguments.of(StartOffset.of(0), StartOffset.of(1), -1),
                Arguments.of(StartOffset.of(1), StartOffset.of(1), 0),
                Arguments.of(StartOffset.of(1), StartOffset.of(0), 1)
        );
    }
}