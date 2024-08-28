/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderFacadeImplTest {
    final CalculationProperties calculationPropertiesMock = mock(CalculationProperties.class);
    final PostgresDataLoaderImpl postgresDataLoaderMock = mock(PostgresDataLoaderImpl.class);
    final KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);

    @Spy PostgresDataLoaderFacadeImpl objectUnderTest = new PostgresDataLoaderFacadeImpl(
            calculationPropertiesMock,
            postgresDataLoaderMock,
            kpiDefinitionHelperMock
    );

    @Nested
    @DisplayName("Testing dataset loading with try")
    class LoadDatasetWithTry {
        @Mock Dataset<Row> datasetMock;
        @Mock JdbcDatasource jdbcDatasourceMock;
        @Mock SingleTableColumns singleTableColumnsMock;

        @Test
        void shouldTryToLoadPartitionedDataset_whenCalculationPropertiesAreSetToLoadPartitionedData() {
            doReturn(true).when(calculationPropertiesMock).isPartitionDatasetLoad();
            doReturn(datasetMock).when(objectUnderTest).tryToLoadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            objectUnderTest.loadDatasetWithTry(jdbcDatasourceMock, singleTableColumnsMock, 60);

            verify(calculationPropertiesMock).isPartitionDatasetLoad();
            verify(objectUnderTest).tryToLoadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);
        }

        @Test
        void shouldLoadUnPartitionedDataset_whenCalculationPropertiesAreNotSetToLoadPartitionedData() {
            when(calculationPropertiesMock.isPartitionDatasetLoad()).thenReturn(false);
            doReturn(datasetMock).when(objectUnderTest).loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            objectUnderTest.loadDatasetWithTry(jdbcDatasourceMock, singleTableColumnsMock, 60);

            verify(calculationPropertiesMock).isPartitionDatasetLoad();
            verify(objectUnderTest).loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);
        }
    }

    @Nested
    @DisplayName("Testing partitioned dataset loading with try")
    class TryToLoadPartitionedDataset {
        @Mock Dataset<Row> datasetMock;
        @Mock JdbcDatasource jdbcDatasourceMock;
        @Mock SingleTableColumns singleTableColumnsMock;

        @Test
        void shouldReadPartitionedDatasetIPossible() {
            doReturn(datasetMock).when(objectUnderTest).loadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            objectUnderTest.tryToLoadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            verify(objectUnderTest).loadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);
        }

        @Test
        void shouldReadUnPartitionedDatasetILoadingPartitionedDatasetFails() {
            doThrow(IllegalArgumentException.class).when(objectUnderTest).loadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);
            doReturn(datasetMock).when(objectUnderTest).loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            objectUnderTest.tryToLoadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);

            verify(objectUnderTest).loadPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);
            verify(objectUnderTest).loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumnsMock, 60);
        }
    }
}