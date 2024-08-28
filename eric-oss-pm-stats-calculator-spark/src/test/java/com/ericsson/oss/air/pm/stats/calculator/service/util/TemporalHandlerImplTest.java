/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static java.time.Duration.ofMinutes;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporalHandlerImplTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.JULY, 19), LocalTime.NOON);

    @Mock CalculationProperties calculationPropertiesMock;

    @InjectMocks TemporalHandlerImpl objectUnderTest;

    @MethodSource("provideGetOffsetTimeStampData")
    @ParameterizedTest(name = "[{index}] With current time: ''{0}'' and schedule increment: ''{1}'' and end of execution ''{2}'' ==> ''{3}'' returned")
    void shouldGetOffsetTimeStamp(final LocalDateTime testTime, final Duration scheduleIncrement, final Duration endOfExecution, final Timestamp expected) {
        try (final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            final Instant instantTestTime = toInstant(testTime);
            instantMockedStatic.when(Instant::now).thenReturn(instantTestTime);

            when(calculationPropertiesMock.getScheduleIncrement()).thenReturn(scheduleIncrement);
            when(calculationPropertiesMock.getEndOfExecutionOffset()).thenReturn(endOfExecution);

            final Timestamp actual = objectUnderTest.getOffsetTimestamp();

            instantMockedStatic.verify(Instant::now, atLeastOnce());
            verify(calculationPropertiesMock).getScheduleIncrement();
            verify(calculationPropertiesMock).getEndOfExecutionOffset();

            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    static Stream<Arguments> provideGetOffsetTimeStampData() {
        final int scheduleIncrement15 = 15;
        final int scheduleIncrement5 = 5;

        final int endOfExecution30 = 30;
        final int endOfExecution5 = 5;

        return Stream.of(
                Arguments.of(nPlusMinute(scheduleIncrement15, 0), ofMinutes(scheduleIncrement15), ofMinutes(endOfExecution30), prepareExpected(11, 29)),
                Arguments.of(nPlusMinute(scheduleIncrement15, 1), ofMinutes(scheduleIncrement15), ofMinutes(endOfExecution30), prepareExpected(11, 44)),
                Arguments.of(nPlusMinute(scheduleIncrement15, 2), ofMinutes(scheduleIncrement15), ofMinutes(endOfExecution30), prepareExpected(11, 59)),
                Arguments.of(nPlusMinute(scheduleIncrement15, 3), ofMinutes(scheduleIncrement15), ofMinutes(endOfExecution30), prepareExpected(12, 14)),
                Arguments.of(nPlusMinute(scheduleIncrement15, 4), ofMinutes(scheduleIncrement15), ofMinutes(endOfExecution30), prepareExpected(12, 29)),

                Arguments.of(nPlusMinute(scheduleIncrement5, 0), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(11, 54)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 1), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(11, 59)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 2), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 4)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 3), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 9)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 4), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 14)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 5), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 19)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 6), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 24)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 7), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 29)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 8), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 34)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 9), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 39)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 10), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 44)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 11), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 49)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 12), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 54)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 13), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(12, 59)),
                Arguments.of(nPlusMinute(scheduleIncrement5, 14), ofMinutes(scheduleIncrement5), ofMinutes(endOfExecution5), prepareExpected(13, 4))
        );
    }

    static Timestamp prepareExpected(final int hour, final int minute) {
        return Timestamp.valueOf(TEST_TIME.withHour(hour).withMinute(minute).withSecond(59).withNano(990_000_000));
    }

    static LocalDateTime nPlusMinute(final int scheduleIncrement, final long n) {
        return TEST_TIME.plusMinutes(n * scheduleIncrement);
    }

    static Instant toInstant(@NonNull final LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}