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

import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.exception.AggregationPeriodNotSupportedException;

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
class AggregationPeriodCreatorRegistrySpringTest {
    @Mock PluginRegistry<AggregationPeriodCreator, Long> aggregationPeriodCreatorPluginRegistryMock;
    @InjectMocks AggregationPeriodCreatorRegistrySpring objectUnderTest;
    @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

    @Test
    void shouldLocateCreator(@Mock AggregationPeriodCreator aggregationPeriodCreatorMock) {
        when(aggregationPeriodCreatorPluginRegistryMock.getPluginFor(eq(60L), any())).thenReturn(aggregationPeriodCreatorMock);

        objectUnderTest.aggregationPeriod(60L);

        verify(aggregationPeriodCreatorPluginRegistryMock).getPluginFor(eq(60L), any());
    }

    @Test
    void shouldThrowException() {
        objectUnderTest.aggregationPeriod(60L);

        verify(aggregationPeriodCreatorPluginRegistryMock).getPluginFor(eq(60L), supplierArgumentCaptor.capture());

        Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                  .isInstanceOf(AggregationPeriodNotSupportedException.class)
                  .hasMessage("Aggregation period '60' is not supported");
    }
}