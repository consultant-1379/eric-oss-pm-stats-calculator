/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregationPeriodCreatorTest {
    @Spy
    AggregationPeriodCreator objectUnderTest;

    @MethodSource("provideDoesSupportData")
    @ParameterizedTest(name = "[{index}] ''{0}'' supports ''{1}'' aggregation period ==> ''{2}''")
    void shouldVerifyDoesSupport(final Duration supportedAggregationPeriod, final long aggregationPeriod, final boolean expected) {
        doReturn(supportedAggregationPeriod).when(objectUnderTest).getSupportedAggregationPeriod();

        final boolean actual = objectUnderTest.doesSupport(aggregationPeriod);

        verify(objectUnderTest).getSupportedAggregationPeriod();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideDoesSupportData")
    @ParameterizedTest(name = "[{index}] ''{0}'' supports ''{1}'' aggregation period ==> ''{2}''")
    void shouldVerifyDoesSupports(final Duration supportedAggregationPeriod, final long aggregationPeriod, final boolean expected) {
        doReturn(supportedAggregationPeriod).when(objectUnderTest).getSupportedAggregationPeriod();

        final boolean actual = objectUnderTest.supports(aggregationPeriod);

        verify(objectUnderTest).getSupportedAggregationPeriod();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideDoesSupportData() {
        return Stream.of(
                Arguments.of(Duration.ofMinutes(60), 60, true),
                Arguments.of(Duration.ofMinutes(60), 1_440, false)
        );
    }
}