/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.HourlyAggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.AggregationPeriodCreatorRegistry;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregationPeriodSupporterTest {
    @Mock
    AggregationPeriodCreatorRegistry aggregationPeriodCreatorRegistryMock;

    @InjectMocks
    AggregationPeriodSupporter objectUnderTest;

    @MethodSource("provideAreBoundsInDifferentAggregationPeriodsData")
    @ParameterizedTest(name = "[{index}] Lower Readiness Bound ''{0}'' and Upper Readiness Bound ''{1}'' are in different aggregation periods ==> ''{2}''")
    void shouldVerifyAreBoundsInDifferentAggregationPeriods(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound, final boolean expected) {
        when(aggregationPeriodCreatorRegistryMock.aggregationPeriod(60)).thenReturn(new HourlyAggregationPeriodCreator());

        final boolean actual = objectUnderTest.areBoundsInDifferentAggregationPeriods(lowerReadinessBound, upperReadinessBound, 60);

        verify(aggregationPeriodCreatorRegistryMock).aggregationPeriod(60);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideAreBoundsInDifferentAggregationPeriodsData() {
        return Stream.of(
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(11, 59), false),
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(12, 0), false),
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(12, 1), false),
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(12, 59), false),
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(13, 0), true),
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(13, 1), true),
                Arguments.of(lowerReadinessBound(12, 30), upperReadinessBound(14, 0), true)
        );
    }

    @NonNull
    static LocalDateTime lowerReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    @NonNull
    static LocalDateTime upperReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }


    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 10, hour, minute);
    }
}