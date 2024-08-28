/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
class CalculationReliabilityThresholdTest {

    @Mock
    CalculationReliabilityService calculationReliabilityServiceMock;

    @InjectMocks
    CalculationReliabilityThreshold objectUnderTest;

    @Test
    void shouldCalculateReliabilityThreshold() {
        final UUID uuid = UUID.fromString("1083cf35-e4ed-4564-ac7a-ffa24f215182");
        when(calculationReliabilityServiceMock.findReliabilityThresholdByCalculationId(uuid)).thenReturn(Collections.emptyMap());

        objectUnderTest.calculateReliabilityThreshold(uuid);

        verify(calculationReliabilityServiceMock).findReliabilityThresholdByCalculationId(uuid);
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