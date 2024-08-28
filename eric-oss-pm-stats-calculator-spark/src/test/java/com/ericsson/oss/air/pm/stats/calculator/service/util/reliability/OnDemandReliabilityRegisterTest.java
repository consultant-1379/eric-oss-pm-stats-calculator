/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.reliability;

import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.OnDemandReliabilityRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnDemandReliabilityRegisterTest {

    @Mock SparkService sparkServiceMock;
    @Mock CalculationRepository calculationRepositoryMock;
    @Mock OnDemandReliabilityRepository onDemandReliabilityRepositoryMock;
    @Mock OnDemandReliabilityCache onDemandReliabilityCacheMock;
    @Mock KpiDefinitionRepository kpiDefinitionRepositoryMock;

    @InjectMocks OnDemandReliabilityRegister objectUnderTest;

    @Test
    void shouldPersist(@Mock Calculation calculationMock) {
        final UUID uuid = UUID.fromString("4f44c7cd-aabf-4104-8cbb-5b7b7f573cb3");
        when(sparkServiceMock.getCalculationId()).thenReturn(uuid);
        when(calculationRepositoryMock.forceFetchById(uuid)).thenReturn(calculationMock);
        when(onDemandReliabilityCacheMock.extractReliabilities(calculationMock)).thenReturn(Collections.emptyList());

        objectUnderTest.persistOnDemandReliabilities();

        verify(sparkServiceMock).getCalculationId();
        verify(calculationRepositoryMock).forceFetchById(uuid);
        verify(onDemandReliabilityCacheMock).extractReliabilities(calculationMock);
        verify(onDemandReliabilityRepositoryMock).saveAll(Collections.emptyList());
    }

    @Nested
    class TestAddReliability {

        @Mock Dataset<Row> datasetMock;
        @Mock Row rowMock;
        @Mock Column minColumnMock;
        @Mock Column maxColumnMock;

        @Test
        void shouldAddReliability(@Mock KpiDefinition kpiDefinitionMock) {
            final Timestamp minTimestamp = Timestamp.valueOf("2022-09-17 15:00:00.00");
            final Timestamp maxTimestamp = Timestamp.valueOf("2022-09-17 19:00:00.00");

            try (final MockedStatic<functions> functionsMockedStatic = mockStatic(functions.class)) {
                final MockedStatic.Verification minColumnVerification = () -> min("aggregation_begin_time");
                final MockedStatic.Verification maxColumnVerification = () -> max("aggregation_end_time");

                functionsMockedStatic.when(minColumnVerification).thenReturn(minColumnMock);
                functionsMockedStatic.when(maxColumnVerification).thenReturn(maxColumnMock);

                when(datasetMock.isEmpty()).thenReturn(false);
                when(sparkServiceMock.getKpisToCalculate()).thenReturn(Arrays.asList("kpi1", "kpi3"));
                when(datasetMock.columns()).thenReturn(new String[]{"kpi1", "kpi2", "aggregation_begin_time", "aggregation_end_time"});
                when(datasetMock.select("kpi1", "aggregation_begin_time", "aggregation_end_time")).thenReturn(datasetMock);
                when(minColumnMock.as("minTimestamp")).thenReturn(minColumnMock);
                when(maxColumnMock.as("maxTimestamp")).thenReturn(maxColumnMock);
                when(datasetMock.agg(minColumnMock, maxColumnMock)).thenReturn(datasetMock);
                when(datasetMock.head()).thenReturn(rowMock);
                when(rowMock.getAs("minTimestamp")).thenReturn(minTimestamp);
                when(rowMock.getAs("maxTimestamp")).thenReturn(maxTimestamp);
                when(kpiDefinitionRepositoryMock.forceFindByName("kpi1")).thenReturn(kpiDefinitionMock);

                objectUnderTest.addReliability(datasetMock);

                functionsMockedStatic.verify(minColumnVerification);
                functionsMockedStatic.verify(maxColumnVerification);

                verify(datasetMock).isEmpty();
                verify(sparkServiceMock).getKpisToCalculate();
                verify(datasetMock).columns();
                verify(datasetMock).select("kpi1", "aggregation_begin_time", "aggregation_end_time");
                verify(minColumnMock).as("minTimestamp");
                verify(maxColumnMock).as("maxTimestamp");
                verify(datasetMock).agg(minColumnMock, maxColumnMock);
                verify(datasetMock).head();
                verify(rowMock).getAs("minTimestamp");
                verify(rowMock).getAs("maxTimestamp");
                verify(kpiDefinitionRepositoryMock).forceFindByName("kpi1");
            }
        }

        @Test
        void shouldReturnOnEmptyDataset() {
            when(datasetMock.isEmpty()).thenReturn(true);

            objectUnderTest.addReliability(datasetMock);

            verify(datasetMock).isEmpty();
            verifyNoMoreInteractions(datasetMock);
            verifyNoInteractions(sparkServiceMock, kpiDefinitionRepositoryMock, onDemandReliabilityCacheMock);
        }
    }
}