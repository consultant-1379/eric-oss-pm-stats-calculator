/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.reliability;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.CalculationReliability;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.util.RangeUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnDemandReliabilityCacheTest {

    @InjectMocks OnDemandReliabilityCache objectUnderTest;

    @Test
    void shouldExtractReliabilities(@Mock final Calculation calculationMock, @Mock final KpiDefinition kpiDefinitionMock) {
        final LocalDateTime min = LocalDateTime.of(2_022, Month.SEPTEMBER, 18, 20, 0, 0);
        final LocalDateTime max = LocalDateTime.of(2_022, Month.SEPTEMBER, 18, 22, 0, 0);

        objectUnderTest.merge(
                kpiDefinitionMock,
                RangeUtils.makeRange(min, max),
                RangeUtils::mergeRanges
        );

        final List<CalculationReliability> actual = objectUnderTest.extractReliabilities(calculationMock);

        assertThat(actual).first()
                          .extracting(CalculationReliability::getCalculation,
                                      CalculationReliability::getKpiDefinition,
                                      CalculationReliability::getCalculationStartTime,
                                      CalculationReliability::getReliabilityThreshold)
                          .containsExactly(calculationMock, kpiDefinitionMock, min, max);
    }
}