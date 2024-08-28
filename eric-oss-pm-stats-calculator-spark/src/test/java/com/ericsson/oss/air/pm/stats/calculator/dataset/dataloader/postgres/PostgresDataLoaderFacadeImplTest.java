/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderFacadeImplTest {
    @Mock CalculationProperties calculationPropertiesMock;
    @Mock PostgresDataLoaderImpl postgresDataLoaderMock;
    @Mock KpiDefinitionHelperImpl  kpiDefinitionHelperMock;

    @InjectMocks PostgresDataLoaderFacadeImpl objectUnderTest;

    @Nested
    @DisplayName("Testing data loading for partitioned - un-partitioned")
    class LoadPartitionedAndUnPartitionedDataset {
        @Mock JdbcDatasource jdbcDatasourceMock;
        @Mock SingleTableColumns singleTableColumnsMock;

        @Test
        void shouldLoadPartitionedDataset() {
            objectUnderTest.loadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            verify(postgresDataLoaderMock).loadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60, ReadingOptions.empty());
        }

        @Test
        void shouldLoadUnPartitionedDataset() {
            objectUnderTest.loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            verify(postgresDataLoaderMock).loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60, ReadingOptions.empty());
        }
    }

    @Nested
    @DisplayName("Testing data loading")
    class LoadDataset {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;

        @Test
        void shouldLoadPartitionedDataset() {
            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(calculationPropertiesMock.isPartitionDatasetLoad()).thenReturn(true);

            final ReadingOptions readingOptions = ReadingOptions.empty();
            objectUnderTest.loadDataset(kpiDefinitionsMock, readingOptions);

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(calculationPropertiesMock).isPartitionDatasetLoad();
            verify(postgresDataLoaderMock).loadPartitionedDataset(kpiDefinitionsMock, 60, readingOptions);
        }

        @Nested
        @DisplayName("Load un partitioned data")
        class LoadUnPartitionedData {
            @Test
            void shouldLoadUnPartitionedDataset_whenItIsNotPartitionDatasetLoad() {
                when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
                when(calculationPropertiesMock.isPartitionDatasetLoad()).thenReturn(false);

                objectUnderTest.loadDataset(kpiDefinitionsMock);

                verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
                verify(calculationPropertiesMock).isPartitionDatasetLoad();
                verify(postgresDataLoaderMock).loadUnPartitionedDataset(kpiDefinitionsMock, 60, ReadingOptions.empty());
            }
        }

    }
}