/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.factory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresDefaultFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.PostgresDefaultFilterDataLoaderIterator;
import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class FilterIteratorFactoryImplTest {
    SourceDataAvailability sourceDataAvailabilityMock = mock(SourceDataAvailability.class);
    OffsetHandlerRegistryFacade offsetHandlerRegistryFacadeMock = mock(OffsetHandlerRegistryFacade.class);
    PostgresDefaultFilterDataLoaderImpl postgresDefaultFilterDataLoaderMock = mock(PostgresDefaultFilterDataLoaderImpl.class);
    KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);

    OffsetHandler offsetHandlerMock = mock(OffsetHandler.class);

    FilterIteratorFactoryImpl objectUnderTest;

    @BeforeEach
    void setUp() {
        when(offsetHandlerRegistryFacadeMock.offsetHandler()).thenReturn(offsetHandlerMock);

        objectUnderTest = new FilterIteratorFactoryImpl(
                offsetHandlerRegistryFacadeMock,
                sourceDataAvailabilityMock,
                postgresDefaultFilterDataLoaderMock,
                kpiDefinitionHelperMock
        );

        verify(offsetHandlerRegistryFacadeMock).offsetHandler();
    }

    @Nested
    @DisplayName("Testing creating default iterator")
    class CreateDefaultIterator {
        @SuppressWarnings("unchecked") Collection<KpiDefinition> kpiDefinitionsMock = mock(Collection.class);
        CalculationTimeWindow calculationTimeWindowMock = mock(CalculationTimeWindow.class);

        @Test
        void shouldCreateDefaultFilterIterator() {
            try (final MockedConstruction<PostgresDefaultFilterDataLoaderIterator> constructorMock = mockConstruction(
                    PostgresDefaultFilterDataLoaderIterator.class
            )) {
                when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
                when(offsetHandlerMock.getCalculationTimeWindow(60)).thenReturn(calculationTimeWindowMock);

                final PostgresDefaultFilterDataLoaderIterator actual = objectUnderTest.createDefault(kpiDefinitionsMock);

                verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
                verify(offsetHandlerMock).getCalculationTimeWindow(60);

                Assertions.assertThat(constructorMock.constructed()).first().isEqualTo(actual);
            }
        }
    }
}