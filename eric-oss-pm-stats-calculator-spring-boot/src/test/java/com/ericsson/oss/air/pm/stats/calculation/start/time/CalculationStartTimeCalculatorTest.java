/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationStartTimeCalculatorTest {

    @Mock
    CalculationReliabilityService calculationReliabilityServiceMock;

    @InjectMocks
    CalculationStartTimeCalculator objectUnderTest;

    @Test
    void shouldGiveBackCorrectCalculationTypes(@Mock final Map<String, LocalDateTime> startTimeMock) {
        final UUID calculationId = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");
        doReturn(startTimeMock).when(calculationReliabilityServiceMock).findStartTimeByCalculationId(calculationId);

        final Map<String, LocalDateTime> actual = objectUnderTest.calculateStartTime(calculationId);

        verify(calculationReliabilityServiceMock).findStartTimeByCalculationId(calculationId);

        Assertions.assertThat(actual).isEqualTo(startTimeMock);
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_SIMPLE")
    void shouldNotSupport(final KpiType kpiType) {
        final boolean actual = objectUnderTest.doesSupport(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_COMPLEX", "ON_DEMAND"})
    void shouldSupport(final KpiType kpiType) {
        final boolean actual = objectUnderTest.doesSupport(kpiType);
        Assertions.assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_SIMPLE")
    void shouldNotSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_COMPLEX", "ON_DEMAND"})
    void shouldSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isTrue();
    }
}
