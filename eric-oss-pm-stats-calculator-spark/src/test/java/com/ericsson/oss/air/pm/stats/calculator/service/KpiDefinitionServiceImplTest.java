/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DimensionTablesService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionAdapterImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.OnDemandParameterCastUtils;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionServiceImplTest {
    @Mock KpiDefinitionAdapterImpl kpiDefinitionAdapterMock;
    @Mock KpiDefinitionRepository kpiDefinitionRepositoryMock;
    @Mock SparkService sparkServiceMock;
    @Mock DimensionTablesService dimensionTablesServiceMock;
    @Mock OnDemandParameterCastUtils onDemandParameterCastUtilsMock;

    @InjectMocks KpiDefinitionServiceImpl objectUnderTest;

    @Test
    void shouldLoadKpiDefinitionToCalculateForScheduled() {
        when(sparkServiceMock.getKpisToCalculate()).thenReturn(Collections.emptyList());
        when(sparkServiceMock.isOnDemand()).thenReturn(false);
        when(kpiDefinitionRepositoryMock.findByNameIn(any())).thenReturn(Collections.emptyList());
        when(kpiDefinitionAdapterMock.convertKpiDefinitions(Collections.emptyList())).thenReturn(Collections.emptyList());

        objectUnderTest.loadDefinitionsToCalculate();

        verify(sparkServiceMock).getKpisToCalculate();
        verify(sparkServiceMock).isOnDemand();
        verify(kpiDefinitionRepositoryMock).findByNameIn(any());
        verify(kpiDefinitionAdapterMock).convertKpiDefinitions(Collections.emptyList());
        verifyNoInteractions(dimensionTablesServiceMock);
    }

    @Test
    void shouldLoadKpiDefinitionToCalculateForOnDemand() {
        when(sparkServiceMock.getKpisToCalculate()).thenReturn(Collections.emptyList());
        when(sparkServiceMock.isOnDemand()).thenReturn(true);
        when(dimensionTablesServiceMock.getTabularParameterTableNamesByCalculationId(any())).thenReturn(Collections.emptyList());
        when(sparkServiceMock.getKpiDefinitionParameters()).thenReturn(Collections.emptyMap());
        when(kpiDefinitionRepositoryMock.findByNameIn(any())).thenReturn(Collections.emptyList());
        when(kpiDefinitionAdapterMock.convertKpiDefinitions(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(onDemandParameterCastUtilsMock.applyParametersToKpiDefinitions(Collections.emptyMap(), Collections.emptyList())).thenReturn(Collections.emptySet());

        objectUnderTest.loadDefinitionsToCalculate();

        verify(sparkServiceMock).getKpisToCalculate();
        verify(sparkServiceMock).isOnDemand();
        verify(dimensionTablesServiceMock).getTabularParameterTableNamesByCalculationId(any());
        verify(sparkServiceMock).getKpiDefinitionParameters();
        verify(kpiDefinitionRepositoryMock).findByNameIn(any());
        verify(kpiDefinitionAdapterMock).convertKpiDefinitions(Collections.emptyList());
        verify(onDemandParameterCastUtilsMock).applyParametersToKpiDefinitions(any(),any());
    }

    @Test
    void shouldVerifyKpiDefinitionsAreScheduled() {
        final boolean actual = objectUnderTest.areScheduled(Collections.singletonList(KpiDefinition.builder().withExecutionGroup("group").build()));

        Assertions.assertThat(actual).isTrue();
    }

    @Nested
    class AggregationPeriod {
        @Test
        void shouldVerifyDefaultAggregationPeriod() {
            final boolean actual = objectUnderTest.isDefaultAggregationPeriod(-1);
            Assertions.assertThat(actual).isTrue();
        }

        @ValueSource(ints = {60, 1_440})
        @ParameterizedTest(name = "[{index}] Aggregation period ''{0}'' is not default")
        void shouldVerifyNonAggregationPeriod(final int aggregationPeriod) {
            final boolean actual = objectUnderTest.isDefaultAggregationPeriod(aggregationPeriod);
            Assertions.assertThat(actual).isFalse();
        }
    }

    @Nested
    @DisplayName("verify KPI Definitions for type")
    class VerifyKpiDefinitionsForType {
        @Test
        void shouldVerifyKpiDefinitionsAreScheduledSimple() {
            try (final MockedStatic<KpiDefinitionHandler> kpiDefinitionHandlerMockedStatic = mockStatic(KpiDefinitionHandler.class)) {
                final Verification verification = () -> KpiDefinitionHandler.areKpiDefinitionsSimpleKpis(Collections.emptyList());

                kpiDefinitionHandlerMockedStatic.when(verification).thenReturn(false);

                final boolean actual = objectUnderTest.areScheduledSimple(Collections.emptyList());

                kpiDefinitionHandlerMockedStatic.verify(verification);

                Assertions.assertThat(actual).isFalse();
            }
        }

        @Nested
        @DisplayName("non Schedule Simple")
        class NonScheduleSimple {
            @Test
            void shouldReturnTrue_whenNonScheduleSimple() {
                try (final MockedStatic<KpiDefinitionHandler> kpiDefinitionHandlerMockedStatic = mockStatic(KpiDefinitionHandler.class)) {
                    final Verification verification = () -> KpiDefinitionHandler.areKpiDefinitionsSimpleKpis(Collections.emptyList());

                    kpiDefinitionHandlerMockedStatic.when(verification).thenReturn(false);

                    final boolean actual = objectUnderTest.areNonScheduledSimple(Collections.emptyList());

                    kpiDefinitionHandlerMockedStatic.verify(verification);

                    Assertions.assertThat(actual).isTrue();
                }
            }

            @Test
            void shouldReturnFalse_whenScheduleSimple() {
                try (final MockedStatic<KpiDefinitionHandler> kpiDefinitionHandlerMockedStatic = mockStatic(KpiDefinitionHandler.class)) {
                    final Verification verification = () -> KpiDefinitionHandler.areKpiDefinitionsSimpleKpis(Collections.emptyList());

                    kpiDefinitionHandlerMockedStatic.when(verification).thenReturn(true);

                    final boolean actual = objectUnderTest.areNonScheduledSimple(Collections.emptyList());

                    kpiDefinitionHandlerMockedStatic.verify(verification);

                    Assertions.assertThat(actual).isFalse();
                }
            }
        }
    }
}