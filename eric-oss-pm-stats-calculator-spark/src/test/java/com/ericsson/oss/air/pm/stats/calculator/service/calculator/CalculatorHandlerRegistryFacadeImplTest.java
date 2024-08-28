/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.calculator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry.DatasetWriterRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.CalculatorHandlerNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.api.CalculatorHandler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
class CalculatorHandlerRegistryFacadeImplTest {
    @Mock PluginRegistry<CalculatorHandler, FilterType> calculatorHandlerRegistryMock;
    @Mock DatasetWriterRegistryFacadeImpl datasetWriterRegistryFacadeMock;

    @InjectMocks CalculatorHandlerRegistryFacadeImpl objectUnderTest;

    @Nested
    @DisplayName("Testing calculator handler")
    class CalculatorHandlerTest {
        @Mock CalculatorHandler calculatorHandlerMock;

        @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

        @Test
        void shouldLocateHandler() {
            when(calculatorHandlerRegistryMock.getPluginFor(eq(FilterType.DEFAULT), any())).thenReturn(calculatorHandlerMock);

            objectUnderTest.calculator(FilterType.DEFAULT);

            verify(calculatorHandlerRegistryMock).getPluginFor(eq(FilterType.DEFAULT), any());
        }

        @Test
        void shouldRaiseException_whenHandlerIsNotRegisteredUnderType() {
            objectUnderTest.calculator(null);

            verify(calculatorHandlerRegistryMock).getPluginFor(any(), supplierArgumentCaptor.capture());

            Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                      .isInstanceOf(CalculatorHandlerNotFoundException.class)
                      .hasMessage("CalculatorHandler with type null not found");
        }
    }
}