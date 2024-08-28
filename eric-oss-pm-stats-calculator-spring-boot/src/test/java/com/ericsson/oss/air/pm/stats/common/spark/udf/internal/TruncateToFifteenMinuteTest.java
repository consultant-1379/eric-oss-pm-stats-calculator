/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf.internal;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TruncateToFifteenMinuteTest {
    TruncateToFifteenMinute objectUnderTest = new TruncateToFifteenMinute();

    @Test
    void shouldReturnNull_whenNulLValuesIsPassed() throws Exception {
        final Timestamp actual = objectUnderTest.call(null);
        Assertions.assertThat(actual).isNull();
    }

    @MethodSource("provideVerifyCallData")
    @ParameterizedTest(name = "[{index}] Timestamp ''{0}'' truncated to ''{1}''")
    void shouldVerifyCall(final Timestamp timestamp, final Timestamp expected) throws Exception {
        final Timestamp actual = objectUnderTest.call(timestamp);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideVerifyCallData() {
        return IntStream.rangeClosed(0, 59).mapToObj(minute -> Arguments.of(testTime(minute), testTime(15 * (minute / 15))));
    }

    static Timestamp testTime(final int minute) {
        return Timestamp.valueOf(LocalDateTime.of(2_023, Month.FEBRUARY, 26, 12, minute));
    }
}