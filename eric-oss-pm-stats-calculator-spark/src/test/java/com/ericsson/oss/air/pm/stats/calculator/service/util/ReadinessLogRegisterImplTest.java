/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog.ReadinessLogCache;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogRegisterImplTest {

    @Mock CalculationRepository calculationRepositoryMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock ReadinessLogCache readinessLogCacheMock;
    @Mock SparkService sparkServiceMock;

    @InjectMocks ReadinessLogRegisterImpl objectUnderTest;

    @Nested
    class RegisterReadinessLog {
        @Mock Dataset<Row> sourceDatasetMock;
        @Mock Dataset<Row> aggregationResultDatasetMock;
        @Mock Row resultRowMock;
        @Mock KpiDefinition kpiDefinitionMock;
        @Mock Calculation calculationMock;
        @Mock Column minColumnMock;
        @Mock Column resultMinColumnMock;
        @Mock Column maxColumnMock;
        @Mock Column resultMaxColumnMock;
        @Mock Timestamp testTimeMinimumMock;
        @Mock Timestamp testTimeMaximumMock;

        @Captor ArgumentCaptor<ReadinessLog> readinessLogArgumentCaptor;

        @Test
        void shouldRegisterReadinessLog() {
            try (final MockedStatic<functions> functionsMockedStatic = mockStatic(functions.class)) {
                final UUID calculationId = UUID.fromString("4903fab3-6f73-458f-9f37-962afd2624a6");

                final Verification minColumnVerification = () -> min("aggregation_begin_time");
                final Verification maxColumnVerification = () -> max("aggregation_begin_time");

                functionsMockedStatic.when(minColumnVerification).thenReturn(minColumnMock);
                functionsMockedStatic.when(maxColumnVerification).thenReturn(maxColumnMock);

                when(minColumnMock.as("minTimestamp")).thenReturn(resultMinColumnMock);
                when(maxColumnMock.as("maxTimestamp")).thenReturn(resultMaxColumnMock);
                when(sourceDatasetMock.agg(resultMinColumnMock, resultMaxColumnMock)).thenReturn(aggregationResultDatasetMock);
                when(aggregationResultDatasetMock.head()).thenReturn(resultRowMock);
                when(resultRowMock.anyNull()).thenReturn(false);
                when(kpiDefinitionMock.datasource()).thenReturn("datasource");
                when(resultRowMock.getAs("minTimestamp")).thenReturn(testTimeMinimumMock);
                when(resultRowMock.getAs("maxTimestamp")).thenReturn(testTimeMaximumMock);
                when(sourceDatasetMock.count()).thenReturn(100L);
                when(sparkServiceMock.getCalculationId()).thenReturn(calculationId);
                when(calculationRepositoryMock.getReferenceById(calculationId)).thenReturn(calculationMock);
                when(kpiDefinitionHelperMock.extractAggregationPeriod(Collections.singletonList(kpiDefinitionMock))).thenReturn(60);
                when(kpiDefinitionHelperMock.areCustomFilterDefinitions(Collections.singletonList(kpiDefinitionMock))).thenReturn(false);

                objectUnderTest.registerReadinessLog(sourceDatasetMock, Collections.singletonList(kpiDefinitionMock));

                functionsMockedStatic.verify(minColumnVerification);
                functionsMockedStatic.verify(maxColumnVerification);

                verify(minColumnMock).as("minTimestamp");
                verify(maxColumnMock).as("maxTimestamp");
                verify(sourceDatasetMock).agg(resultMinColumnMock, resultMaxColumnMock);
                verify(aggregationResultDatasetMock).head();
                verify(resultRowMock).anyNull();
                verify(kpiDefinitionMock).datasource();
                verify(sourceDatasetMock).count();
                verify(sparkServiceMock).getCalculationId();
                verify(calculationRepositoryMock).getReferenceById(calculationId);
                verify(kpiDefinitionHelperMock).extractAggregationPeriod(Collections.singletonList(kpiDefinitionMock));
                verify(kpiDefinitionHelperMock).areCustomFilterDefinitions(Collections.singletonList(kpiDefinitionMock));
                verify(readinessLogCacheMock).merge(readinessLogArgumentCaptor.capture(), eq(60));

                final ReadinessLog readinessLogSavedIntoTheCache = readinessLogArgumentCaptor.getValue();

                Assertions.assertThat(readinessLogSavedIntoTheCache.getDatasource()).isEqualTo("datasource");
                Assertions.assertThat(readinessLogSavedIntoTheCache.getEarliestCollectedData()).isEqualTo(testTimeMinimumMock.toLocalDateTime());
                Assertions.assertThat(readinessLogSavedIntoTheCache.getLatestCollectedData()).isEqualTo(testTimeMaximumMock.toLocalDateTime());
                Assertions.assertThat(readinessLogSavedIntoTheCache.getCollectedRowsCount()).isEqualTo(100L);
                Assertions.assertThat(readinessLogSavedIntoTheCache.getKpiCalculationId()).isEqualTo(calculationMock);
            }
        }

        @Test
        void shouldRegisterReadinessLogForFiltered() {
            try (final MockedStatic<functions> functionsMockedStatic = mockStatic(functions.class)) {
                final UUID calculationId = UUID.fromString("4903fab3-6f73-458f-9f37-962afd2624a6");

                final Verification minColumnVerification = () -> min("aggregation_begin_time");
                final Verification maxColumnVerification = () -> max("aggregation_begin_time");

                functionsMockedStatic.when(minColumnVerification).thenReturn(minColumnMock);
                functionsMockedStatic.when(maxColumnVerification).thenReturn(maxColumnMock);

                when(minColumnMock.as("minTimestamp")).thenReturn(resultMinColumnMock);
                when(maxColumnMock.as("maxTimestamp")).thenReturn(resultMaxColumnMock);
                when(sourceDatasetMock.agg(resultMinColumnMock, resultMaxColumnMock)).thenReturn(aggregationResultDatasetMock);
                when(aggregationResultDatasetMock.head()).thenReturn(resultRowMock);
                when(resultRowMock.anyNull()).thenReturn(false);
                when(kpiDefinitionMock.datasource()).thenReturn("datasource");
                when(resultRowMock.getAs("minTimestamp")).thenReturn(testTimeMinimumMock);
                when(resultRowMock.getAs("maxTimestamp")).thenReturn(testTimeMaximumMock);
                when(sourceDatasetMock.count()).thenReturn(100L);
                when(sparkServiceMock.getCalculationId()).thenReturn(calculationId);
                when(calculationRepositoryMock.getReferenceById(calculationId)).thenReturn(calculationMock);
                when(kpiDefinitionHelperMock.extractAggregationPeriod(Collections.singletonList(kpiDefinitionMock))).thenReturn(60);
                when(kpiDefinitionHelperMock.areCustomFilterDefinitions(Collections.singletonList(kpiDefinitionMock))).thenReturn(true);

                objectUnderTest.registerReadinessLog(sourceDatasetMock, Collections.singletonList(kpiDefinitionMock));

                functionsMockedStatic.verify(minColumnVerification);
                functionsMockedStatic.verify(maxColumnVerification);

                verify(minColumnMock).as("minTimestamp");
                verify(maxColumnMock).as("maxTimestamp");
                verify(sourceDatasetMock).agg(resultMinColumnMock, resultMaxColumnMock);
                verify(aggregationResultDatasetMock).head();
                verify(resultRowMock).anyNull();
                verify(kpiDefinitionMock).datasource();
                verify(sourceDatasetMock).count();
                verify(sparkServiceMock).getCalculationId();
                verify(calculationRepositoryMock).getReferenceById(calculationId);
                verify(kpiDefinitionHelperMock).extractAggregationPeriod(Collections.singletonList(kpiDefinitionMock));
                verify(kpiDefinitionHelperMock).areCustomFilterDefinitions(Collections.singletonList(kpiDefinitionMock));
                verify(readinessLogCacheMock).put(readinessLogArgumentCaptor.capture(), eq(60));

                final ReadinessLog readinessLogSavedIntoTheCache = readinessLogArgumentCaptor.getValue();

                Assertions.assertThat(readinessLogSavedIntoTheCache.getDatasource()).isEqualTo("datasource");
                Assertions.assertThat(readinessLogSavedIntoTheCache.getEarliestCollectedData()).isEqualTo(testTimeMinimumMock.toLocalDateTime());
                Assertions.assertThat(readinessLogSavedIntoTheCache.getLatestCollectedData()).isEqualTo(testTimeMaximumMock.toLocalDateTime());
                Assertions.assertThat(readinessLogSavedIntoTheCache.getCollectedRowsCount()).isEqualTo(100L);
                Assertions.assertThat(readinessLogSavedIntoTheCache.getKpiCalculationId()).isEqualTo(calculationMock);
            }
        }

        @Test
        void shouldNotRegisterReadinessLog() {
            try (final MockedStatic<functions> functionsMockedStatic = mockStatic(functions.class)) {

                final Verification minColumnVerification = () -> min("aggregation_begin_time");
                final Verification maxColumnVerification = () -> max("aggregation_begin_time");

                functionsMockedStatic.when(minColumnVerification).thenReturn(minColumnMock);
                functionsMockedStatic.when(maxColumnVerification).thenReturn(maxColumnMock);

                when(minColumnMock.as("minTimestamp")).thenReturn(resultMinColumnMock);
                when(maxColumnMock.as("maxTimestamp")).thenReturn(resultMaxColumnMock);
                when(sourceDatasetMock.agg(resultMinColumnMock, resultMaxColumnMock)).thenReturn(aggregationResultDatasetMock);
                when(aggregationResultDatasetMock.head()).thenReturn(resultRowMock);
                when(resultRowMock.anyNull()).thenReturn(true);

                objectUnderTest.registerReadinessLog(sourceDatasetMock, Collections.singletonList(kpiDefinitionMock));

                verify(minColumnMock).as("minTimestamp");
                verify(maxColumnMock).as("maxTimestamp");
                verify(sourceDatasetMock).agg(resultMinColumnMock, resultMaxColumnMock);
                verify(aggregationResultDatasetMock).head();
                verify(resultRowMock).anyNull();

                verifyNoMoreInteractions(resultMinColumnMock, resultMaxColumnMock, kpiDefinitionMock,
                        sourceDatasetMock, sparkServiceMock, calculationRepositoryMock, readinessLogCacheMock);
            }
        }
    }
}
