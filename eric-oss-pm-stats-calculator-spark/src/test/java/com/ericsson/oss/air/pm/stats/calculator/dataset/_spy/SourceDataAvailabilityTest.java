/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.api.DataSourceRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SourceDataAvailabilityTest {
    DataSourceRepository dataSourceRepositoryMock = mock(DataSourceRepository.class);
    DatasourceRegistry datasourceRegistryMock = mock(DatasourceRegistry.class);
    KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);
    SparkService sparkServiceMock = mock(SparkService.class);

    @Spy
    SourceDataAvailability objectUnderTest = new SourceDataAvailability(
            dataSourceRepositoryMock,
            datasourceRegistryMock,
            kpiDefinitionHelperMock,
            sparkServiceMock
    );

    @Nested
    @DisplayName("Testing collecting calculable time slots")
    class CollectCalculableTimeSlots {
        @Mock CalculationTimeWindow calculationTimeWindowMock;
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock DatasourceTables datasourceTablesMock;

        @Test
        void shouldCollectCalculableTimeSlots() {
            final KpiCalculatorTimeSlot kpiCalculatorTimeSlot1 = new KpiCalculatorTimeSlot(
                    Timestamp.valueOf("2022-08-01 10:00:00"),
                    Timestamp.valueOf("2022-08-01 10:59:59")
            );
            final KpiCalculatorTimeSlot kpiCalculatorTimeSlot2 = new KpiCalculatorTimeSlot(
                    Timestamp.valueOf("2022-08-01 11:00:00"),
                    Timestamp.valueOf("2022-08-01 11:59:59")
            );
            final KpiCalculatorTimeSlot kpiCalculatorTimeSlot3 = new KpiCalculatorTimeSlot(
                    Timestamp.valueOf("2022-08-01 12:00:00"),
                    Timestamp.valueOf("2022-08-01 12:59:59")
            );
            final KpiCalculatorTimeSlot kpiCalculatorTimeSlot4 = new KpiCalculatorTimeSlot(
                    Timestamp.valueOf("2022-08-01 13:00:00"),
                    Timestamp.valueOf("2022-08-01 13:59:59")
            );

            when(calculationTimeWindowMock.calculateTimeSlots(60)).thenReturn(new LinkedList<>(Arrays.asList(
                    kpiCalculatorTimeSlot1, kpiCalculatorTimeSlot2, kpiCalculatorTimeSlot3, kpiCalculatorTimeSlot4
            )));
            when(kpiDefinitionHelperMock.extractNonInMemoryDatasourceTables(kpiDefinitionsMock)).thenReturn(datasourceTablesMock);

            doReturn(true).when(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot1.getStartTimestamp(),
                    kpiCalculatorTimeSlot1.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );
            doReturn(false).when(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot2.getStartTimestamp(),
                    kpiCalculatorTimeSlot2.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );
            doReturn(true).when(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot3.getStartTimestamp(),
                    kpiCalculatorTimeSlot3.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );
            doReturn(false).when(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot4.getStartTimestamp(),
                    kpiCalculatorTimeSlot4.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );

            final Queue<KpiCalculatorTimeSlot> actual = objectUnderTest.collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60);

            verify(calculationTimeWindowMock).calculateTimeSlots(60);
            verify(kpiDefinitionHelperMock, times(4)).extractNonInMemoryDatasourceTables(kpiDefinitionsMock);
            verify(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot1.getStartTimestamp(),
                    kpiCalculatorTimeSlot1.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );
            verify(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot2.getStartTimestamp(),
                    kpiCalculatorTimeSlot2.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );
            verify(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot3.getStartTimestamp(),
                    kpiCalculatorTimeSlot3.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );
            verify(objectUnderTest).isDataPresentForAnyDatasource(
                    kpiCalculatorTimeSlot4.getStartTimestamp(),
                    kpiCalculatorTimeSlot4.getEndTimestamp(),
                    datasourceTablesMock,
                    kpiDefinitionsMock
            );

            Assertions.assertThat(actual).containsExactlyInAnyOrder(
                    kpiCalculatorTimeSlot1,
                    kpiCalculatorTimeSlot3
            );
        }
    }

    @Nested
    @DisplayName("Testing available data sources")
    class AvailableDataSources {
        @Mock Set<Datasource> dataSourcesMock;
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock DatasourceTables datasourceTablesMock;

        @Test
        void shouldReturnAvailableDataSources() {
            doReturn(dataSourcesMock).when(objectUnderTest).getUnavailableDataSources(kpiDefinitionsMock);
            when(kpiDefinitionHelperMock.extractNonInMemoryDatasourceTables(kpiDefinitionsMock)).thenReturn(datasourceTablesMock);

            objectUnderTest.availableDataSources(kpiDefinitionsMock);

            verify(objectUnderTest).getUnavailableDataSources(kpiDefinitionsMock);
            verify(kpiDefinitionHelperMock).extractNonInMemoryDatasourceTables(kpiDefinitionsMock);
            verify(datasourceTablesMock).filterAvailableDataSources(dataSourcesMock);
        }
    }
}