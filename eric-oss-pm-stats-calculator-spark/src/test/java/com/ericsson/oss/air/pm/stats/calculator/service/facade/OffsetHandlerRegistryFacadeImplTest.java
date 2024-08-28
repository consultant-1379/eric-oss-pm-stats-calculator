/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.OffsetHandlerNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;

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
class OffsetHandlerRegistryFacadeImplTest {
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock PluginRegistry<OffsetHandler, Set<KpiDefinition>> pluginRegistryMock;

    @InjectMocks OffsetHandlerRegistryFacadeImpl objectUnderTest;

    @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

    @Test
    void shouldFindOffsetHandlerImplementation(@Mock final OffsetHandler offsetHandlerMock) {
        final Set<KpiDefinition> kpiDefinitions = Collections.emptySet();

        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);
        when(pluginRegistryMock.getPluginFor(eq(kpiDefinitions), any())).thenReturn(offsetHandlerMock);

        objectUnderTest.offsetHandler();

        verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
        verify(pluginRegistryMock).getPluginFor(eq(kpiDefinitions), any());
    }

    @Test
    void shouldThrowException_whenOffsetHandlerImplementationIsNotFound() {
        final Set<KpiDefinition> kpiDefinitions = Collections.emptySet();

        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);

        objectUnderTest.offsetHandler();

        verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
        verify(pluginRegistryMock).getPluginFor(eq(kpiDefinitions), supplierArgumentCaptor.capture());

        Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                  .isInstanceOf(OffsetHandlerNotFoundException.class)
                  .hasMessage("Offset handler not found in the registry");
    }
}