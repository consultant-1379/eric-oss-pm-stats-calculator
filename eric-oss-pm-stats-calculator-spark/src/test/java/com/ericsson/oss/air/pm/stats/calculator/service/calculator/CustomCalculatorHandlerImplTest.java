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

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorCustomFilter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomCalculatorHandlerImplTest {
    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock SqlProcessorDelegator sqlProcessorDelegator;

    @InjectMocks CustomCalculatorHandlerImpl objectUnderTest;

    @MethodSource("provideSupportData")
    @ParameterizedTest(name = "[{index}] Should support type ''{0}'' ==> ''{1}''")
    void shouldSupport(final FilterType filterType, final boolean expected) {
        final boolean actual = objectUnderTest.supports(filterType);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Nested
    @DisplayName("Testing instantiation appropriate calculator instance")
    class CalculatorInstantiation {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;

        @Test
        void shouldInstantiateAppropriateCalculator() {
            try (final MockedConstruction<KpiCalculatorCustomFilter> mockedConstruction = mockConstruction(KpiCalculatorCustomFilter.class)) {
                when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);

                final KpiCalculator actual = objectUnderTest.calculator(kpiDefinitionsMock);

                verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);

                Assertions.assertThat(actual).isExactlyInstanceOf(KpiCalculatorCustomFilter.class);
                Assertions.assertThat(mockedConstruction.constructed()).first().isEqualTo(actual);
            }
        }
    }

    static Stream<Arguments> provideSupportData() {
        return Stream.of(
                Arguments.of(FilterType.CUSTOM, true),
                Arguments.of(FilterType.DEFAULT, false)
        );
    }
}