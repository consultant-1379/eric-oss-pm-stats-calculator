/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.sql.Timestamp;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TimestampsTest {
    @Test
    void shouldVerifyInitialTimeStamp() {
        final Timestamp actual = Timestamps.initialTimeStamp();

        Assertions.assertThat(actual).hasTime(0);
    }

    @ParameterizedTest
    @MethodSource("provideIsInitialTimeStampData")
    void shouldVerifyIsInitialTimeStamp(final int time, final boolean expected) {
        final boolean actual = Timestamps.isInitialTimestamp(new Timestamp(time));
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideIsNotInitialTimeStampData")
    void shouldVerifyIsNotInitialTimeStamp(final int time, final boolean expected) {
        final boolean actual = Timestamps.isNotInitialTimestamp(new Timestamp(time));
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideIsInitialTimeStampData() {
        return Stream.of(
                Arguments.of(0, true),
                Arguments.of(1, false)
        );
    }

    static Stream<Arguments> provideIsNotInitialTimeStampData() {
        return Stream.of(
                Arguments.of(0, false),
                Arguments.of(1, true)
        );
    }
}