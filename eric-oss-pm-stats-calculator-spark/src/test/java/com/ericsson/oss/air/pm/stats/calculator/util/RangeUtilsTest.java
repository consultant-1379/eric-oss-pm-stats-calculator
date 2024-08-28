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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RangeUtilsTest {

    static final LocalDateTime START = LocalDateTime.of(2022, Month.SEPTEMBER, 17, 12, 0);
    static final LocalDateTime END = LocalDateTime.of(2022, Month.SEPTEMBER, 17, 14, 0);

    @Test
    void shouldMakeRangeFromLocalDateTime() {
        final Range<LocalDateTime> actual = RangeUtils.makeRange(START, END);

        assertThat(actual).extracting(Range::getMinimum, Range::getMaximum).containsExactly(START, END);
    }

    @Test
    void shouldMakeRangeFromTimeStamp() {
        final Timestamp first = Timestamp.valueOf(START);
        final Timestamp second = Timestamp.valueOf(END);
        final Range<LocalDateTime> actual = RangeUtils.makeRange(first, second);

        assertThat(actual).extracting(Range::getMinimum, Range::getMaximum).containsExactly(START, END);
    }

    @MethodSource("provideRanges")
    @ParameterizedTest(name = "[{index}] from ranges: {0} and {1} the expected range is: {2}")
    void shouldMergeRanges(final Range<LocalDateTime> left, final Range<LocalDateTime> right, final Range<LocalDateTime> expected){
        final Range<LocalDateTime> actual = RangeUtils.mergeRanges(left, right);

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideRanges() {
        final LocalDateTime lowest = LocalDateTime.of(2022, Month.SEPTEMBER, 17, 12, 0);
        final LocalDateTime mid = LocalDateTime.of(2022, Month.SEPTEMBER, 17, 13, 0);
        final LocalDateTime highest = LocalDateTime.of(2022, Month.SEPTEMBER, 17, 15, 0);
        return Stream.of(
                Arguments.of(RangeUtils.makeRange(lowest, highest), RangeUtils.makeRange(mid, highest), RangeUtils.makeRange(lowest, highest)), //left.min < right.min
                Arguments.of(RangeUtils.makeRange(mid, highest), RangeUtils.makeRange(lowest, highest), RangeUtils.makeRange(lowest, highest)), //right.min < left.min
                Arguments.of(RangeUtils.makeRange(lowest, highest), RangeUtils.makeRange(lowest, mid), RangeUtils.makeRange(lowest, highest)),  //right.max < left.max
                Arguments.of(RangeUtils.makeRange(lowest, mid), RangeUtils.makeRange(lowest, highest), RangeUtils.makeRange(lowest, highest)),  //left.max < right.max
                Arguments.of(RangeUtils.makeRange(lowest, highest), RangeUtils.makeRange(mid, mid), RangeUtils.makeRange(lowest, highest)),  //left.min < right.min && right.max < left.max
                Arguments.of(RangeUtils.makeRange(mid, mid), RangeUtils.makeRange(lowest, highest), RangeUtils.makeRange(lowest, highest))  //right.min < left.min && left.max < right.max
        );
    }
}