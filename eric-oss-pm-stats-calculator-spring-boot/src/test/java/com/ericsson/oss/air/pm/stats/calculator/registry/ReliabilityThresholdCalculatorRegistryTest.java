/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.exception.ReliabilityThresholdCalculatorNotFound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
class ReliabilityThresholdCalculatorRegistryTest {
    @Mock PluginRegistry<ReliabilityThresholdCalculator, KpiType> reliabilityThresholdCalculatorPluginRegistryMock;
    @InjectMocks ReliabilityThresholdCalculatorRegistrySpring objectUnderTest;
    @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

    @Test
    void shouldLocate(@Mock ReliabilityThresholdCalculator reliabilityThresholdCalculatorMock) {
        when(reliabilityThresholdCalculatorPluginRegistryMock.getPluginFor(eq(KpiType.ON_DEMAND), any())).thenReturn(reliabilityThresholdCalculatorMock);

        objectUnderTest.calculator(List.of(entity()));

        verify(reliabilityThresholdCalculatorPluginRegistryMock).getPluginFor(eq(KpiType.ON_DEMAND), any());
    }

    @Test
    void shouldThrowException() {
        objectUnderTest.calculator(List.of(entity()));

        verify(reliabilityThresholdCalculatorPluginRegistryMock).getPluginFor(eq(KpiType.ON_DEMAND), supplierArgumentCaptor.capture());

        Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                  .isInstanceOf(ReliabilityThresholdCalculatorNotFound.class)
                  .hasMessage("KPI type 'ON_DEMAND' is not supported");
    }

    static KpiDefinitionEntity entity() {
        return KpiDefinitionEntity.builder().build();
    }
}