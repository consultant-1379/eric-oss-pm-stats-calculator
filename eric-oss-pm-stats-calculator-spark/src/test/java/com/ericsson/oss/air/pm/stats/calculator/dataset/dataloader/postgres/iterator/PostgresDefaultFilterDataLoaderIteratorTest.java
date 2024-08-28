/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresDefaultFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class PostgresDefaultFilterDataLoaderIteratorTest {
    @Mock Collection<KpiDefinition> kpiDefinitionsMock;
    @Mock CalculationTimeWindow calculationTimeWindowMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock PostgresDefaultFilterDataLoaderImpl postgresDefaultFilterDataLoaderMock;
    @Mock SourceDataAvailability sourceDataAvailabilityMock;

    PostgresDefaultFilterDataLoaderIterator objectUnderTest;

    @Nested
    @DisplayName("Testing object creation")
    class ShouldCreateInstance {
        @Test
        void shouldCreateObjectWithNoAvailableTimeSlots(@NonNull final CapturedOutput capturedOutput) {
            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(sourceDataAvailabilityMock.collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60)).thenReturn(new LinkedList<>());

            objectUnderTest = new PostgresDefaultFilterDataLoaderIterator(
                    kpiDefinitionsMock,
                    calculationTimeWindowMock,
                    kpiDefinitionHelperMock,
                    postgresDefaultFilterDataLoaderMock,
                    sourceDataAvailabilityMock
            );

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(sourceDataAvailabilityMock).collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60);

            Assertions.assertThat(capturedOutput.getOut()).contains("All source data has been included in previous calculations");
        }

        @Test
        void shouldCreateObjectWithAvailableTimeSlots(@NonNull final CapturedOutput capturedOutput) {
            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(sourceDataAvailabilityMock.collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60)).thenReturn(new LinkedList<>(
                    Collections.singletonList(new KpiCalculatorTimeSlot(
                            Timestamp.valueOf("2022-08-01 13:00:00"),
                            Timestamp.valueOf("2022-08-01 13:59:59")
                    ))
            ));
            when(calculationTimeWindowMock.getStart()).thenReturn(Timestamp.valueOf("2022-08-01 13:00:00"));
            when(calculationTimeWindowMock.getEnd()).thenReturn(Timestamp.valueOf("2022-08-01 13:59:59"));

            objectUnderTest = new PostgresDefaultFilterDataLoaderIterator(
                    kpiDefinitionsMock,
                    calculationTimeWindowMock,
                    kpiDefinitionHelperMock,
                    postgresDefaultFilterDataLoaderMock,
                    sourceDataAvailabilityMock
            );

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(sourceDataAvailabilityMock).collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60);
            verify(calculationTimeWindowMock).getStart();
            verify(calculationTimeWindowMock).getEnd();

            Assertions.assertThat(capturedOutput.getOut()).contains(
                    "Calculating all defined KPIs between 2022-08-01 13:00:00.0 and 2022-08-01 13:59:59.0 for aggregation period: 60"
            );
            Assertions.assertThat(capturedOutput.getOut()).contains(
                    "KPI calculation to be executed for '1' units of source data for aggregation period: '60'"
            );
        }
    }

    @Nested
    @DisplayName("Should verify has next and next functionality")
    class ShouldVerifyHasNextAndNextFunctionality {
        @Captor ArgumentCaptor<Collection<KpiDefinition>> kpiDefinitionsCollectionCaptor;

        @Test
        void shouldVerifyHasNextAndNextFunctionality() {
            final KpiCalculatorTimeSlot firstSlot = new KpiCalculatorTimeSlot(
                    Timestamp.valueOf("2022-08-01 13:00:00"),
                    Timestamp.valueOf("2022-08-01 13:59:59")
            );
            final KpiCalculatorTimeSlot secondSlot = new KpiCalculatorTimeSlot(
                    Timestamp.valueOf("2022-08-01 14:00:00"),
                    Timestamp.valueOf("2022-08-01 14:59:59")
            );

            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(sourceDataAvailabilityMock.collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60)).thenReturn(new LinkedList<>(
                    Arrays.asList(firstSlot, secondSlot)
            ));
            when(calculationTimeWindowMock.getStart()).thenReturn(Timestamp.valueOf("2022-08-01 13:00:00"));
            when(calculationTimeWindowMock.getEnd()).thenReturn(Timestamp.valueOf("2022-08-01 14:59:59"));

            objectUnderTest = new PostgresDefaultFilterDataLoaderIterator(
                    kpiDefinitionsMock,
                    calculationTimeWindowMock,
                    kpiDefinitionHelperMock,
                    postgresDefaultFilterDataLoaderMock,
                    sourceDataAvailabilityMock
            );

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(sourceDataAvailabilityMock).collectCalculableTimeSlots(calculationTimeWindowMock, kpiDefinitionsMock, 60);
            verify(calculationTimeWindowMock).getStart();
            verify(calculationTimeWindowMock).getEnd();

            Assertions.assertThat(objectUnderTest.hasNext()).as("First Read").isTrue();
            when(postgresDefaultFilterDataLoaderMock.loadDatasetsWithDefaultFilter(kpiDefinitionsCollectionCaptor.capture(), eq(firstSlot))).thenThrow(new NullPointerException("oops"));
            Assertions.assertThatThrownBy(() -> objectUnderTest.next())
                    .isInstanceOf(KpiCalculatorException.class)
                    .hasMessage("java.lang.NullPointerException: oops");
            verify(postgresDefaultFilterDataLoaderMock).loadDatasetsWithDefaultFilter(kpiDefinitionsCollectionCaptor.getValue(), firstSlot);

            Assertions.assertThat(objectUnderTest.hasNext()).as("Second Read").isTrue();
            when(postgresDefaultFilterDataLoaderMock.loadDatasetsWithDefaultFilter(kpiDefinitionsCollectionCaptor.capture(), eq(secondSlot))).thenThrow(new NullPointerException("oops"));
            Assertions.assertThatThrownBy(() -> objectUnderTest.next())
                      .isInstanceOf(KpiCalculatorException.class)
                      .hasMessage("java.lang.NullPointerException: oops");
            verify(postgresDefaultFilterDataLoaderMock).loadDatasetsWithDefaultFilter(kpiDefinitionsCollectionCaptor.getValue(), secondSlot);

            Assertions.assertThat(objectUnderTest.hasNext()).as("Third Read").isFalse();
            Assertions.assertThatThrownBy(() -> objectUnderTest.next())
                      .isInstanceOf(NoSuchElementException.class);
        }
    }
}