/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.calculator._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry.api.DatasetWriterRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.CalculatorHandlerRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.api.CalculatorHandler;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
class CalculatorHandlerRegistryFacadeImplTest {
    @Spy CalculatorHandlerRegistryFacadeImpl objectUnderTest = new CalculatorHandlerRegistryFacadeImpl(
            mock(PluginRegistry.class),
            mock(KpiDefinitionHelperImpl.class),
            mock(DatasetWriterRegistryFacade.class),
            mock(SqlProcessorDelegator.class)
    );

    @Nested
    @DisplayName("Testing returning specific calculator")
    class ReturnSpecificCalculator {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock CalculatorHandler calculatorHandlerMock;
        @Mock KpiCalculator kpiCalculatorMock;

        @Test
        void shouldReturnSpecificCalculator() {
            doReturn(calculatorHandlerMock).when(objectUnderTest).calculator(FilterType.DEFAULT);
            doReturn(kpiCalculatorMock).when(calculatorHandlerMock).calculator(kpiDefinitionsMock);

            final KpiCalculator actual = objectUnderTest.defaultCalculator(kpiDefinitionsMock);

            verify(objectUnderTest).calculator(FilterType.DEFAULT);
            verify(calculatorHandlerMock).calculator(kpiDefinitionsMock);

            Assertions.assertThat(actual).isEqualTo(kpiCalculatorMock);
        }

        @Test
        void shouldReturnCustomCalculator() {
            doReturn(calculatorHandlerMock).when(objectUnderTest).calculator(FilterType.CUSTOM);
            doReturn(kpiCalculatorMock).when(calculatorHandlerMock).calculator(kpiDefinitionsMock);

            final KpiCalculator actual = objectUnderTest.customCalculator(kpiDefinitionsMock);

            verify(objectUnderTest).calculator(FilterType.CUSTOM);
            verify(calculatorHandlerMock).calculator(kpiDefinitionsMock);

            Assertions.assertThat(actual).isEqualTo(kpiCalculatorMock);
        }
    }
}