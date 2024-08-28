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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationExecutorFacadeImplTest {
    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock SourceDataAvailability sourceDataAvailabilityMock;

    @InjectMocks CalculationExecutorFacadeImpl objectUnderTest;

    @Nested
    @DisplayName("calculateKpis")
    class CalculateKpis {
        final Set<KpiDefinition> kpiDefinitions = Collections.emptySet();

        @Mock KpiCalculator kpiCalculatorMock;
        @Mock TableDatasets loadedTableDatasets;
        @Mock TableDatasets outputTableDatasets;

        @Test
        void scheduledSimpleKpis() throws Exception {
            when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);
            when(kpiCalculatorMock.calculate()).thenReturn(outputTableDatasets);

            objectUnderTest.calculateKpis(kpiCalculatorMock, loadedTableDatasets);

            verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
            verify(kpiCalculatorMock).calculate();
            verify(loadedTableDatasets).unPersistDatasets();
            verify(outputTableDatasets).unPersistDatasets();
            verify(sparkServiceMock).clearCache();
        }

        @Test
        void nonScheduledSimpleKpis() throws Exception {
            final Set<Datasource> unavailableDataSources = Collections.emptySet();

            when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);
            when(kpiCalculatorMock.calculate()).thenReturn(outputTableDatasets);

            objectUnderTest.calculateKpis(kpiCalculatorMock, loadedTableDatasets);

            verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
            verify(kpiCalculatorMock).calculate();
            verify(loadedTableDatasets).unPersistDatasets();
            verify(outputTableDatasets).unPersistDatasets();
            verify(sparkServiceMock).clearCache();
        }

        @Test
        void shouldRaiseException_whenSomethingGoesWrong() {
            doThrow(new NullPointerException("Something went wrong")).when(kpiDefinitionServiceMock).loadDefinitionsToCalculate();

            Assertions.assertThatThrownBy(() -> objectUnderTest.calculateKpis(kpiCalculatorMock, loadedTableDatasets))
                      .hasRootCauseExactlyInstanceOf(NullPointerException.class)
                      .isInstanceOf(KpiCalculatorException.class)
                      .asInstanceOf(InstanceOfAssertFactories.type(KpiCalculatorException.class))
                      .satisfies(kpiCalculatorException -> {
                          Assertions.assertThat(kpiCalculatorException.getErrorCode()).isEqualTo(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR);
                      });

            verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
        }
    }
}