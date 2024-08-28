/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.debug.DatasetLoggerImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterPreparatoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class PostgresFilterDataLoaderImplTest {
    @Mock PostgresDataLoaderFacadeImpl postgresDataLoaderFacadeMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock SparkService sparkServiceMock;
    @Mock Logger loggerMock;
    @Mock DatasetLoggerImpl datasetLoggerMock;
    @Mock FilterPreparatoryImpl filterPreparatoryMock;
    @Mock SparkSession sparkSessionMock;

    @InjectMocks PostgresFilterDataLoaderImpl objectUnderTest;

    @Nested
    @DisplayName("Testing loading filtered dataset")
    class LoadFilteredDataset {
        @Mock Dataset<Row> datasetMock;
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock JdbcDatasource jdbcDatasourceMock;
        SingleTableColumns singleTableColumns = SingleTableColumns.of(Table.of("table"), Collections.singleton(Column.of("column")));

        @Test
        void shouldLogFilteredRows_whenLoggerIsSetToDebug() {
            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(loggerMock.isDebugEnabled()).thenReturn(true);
            when(filterPreparatoryMock.prepareFilter(FilterType.CUSTOM, singleTableColumns.getTable(), "filter")).thenReturn("sourceFilter");
            when(sparkSessionMock.sql("sourceFilter")).thenReturn(datasetMock);
            when(sparkServiceMock.cacheView(singleTableColumns.getTableName(), datasetMock)).thenReturn(TableDataset.of(
                    singleTableColumns,
                    datasetMock
            ));

            final TableDataset actual = objectUnderTest.loadCustomFilterDataset(kpiDefinitionsMock, singleTableColumns, jdbcDatasourceMock, "filter");

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(postgresDataLoaderFacadeMock).loadDatasetWithTry(jdbcDatasourceMock, singleTableColumns, 60);
            verify(loggerMock).isDebugEnabled();
            verify(datasetLoggerMock).logFilteredRows(singleTableColumns.getTable(), "filter");
            verify(filterPreparatoryMock).prepareFilter(FilterType.CUSTOM, singleTableColumns.getTable(), "filter");
            verify(sparkSessionMock).sql("sourceFilter");
            verify(sparkServiceMock).cacheView(singleTableColumns.getTableName(), datasetMock);

            Assertions.assertThat(actual).isEqualTo(TableDataset.of(singleTableColumns, datasetMock));
        }

        @Test
        void shouldLoadCustomFilterDataset() {
            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(loggerMock.isDebugEnabled()).thenReturn(false);
            when(filterPreparatoryMock.prepareFilter(FilterType.CUSTOM, singleTableColumns.getTable(), "filter")).thenReturn("sourceFilter");
            when(sparkSessionMock.sql("sourceFilter")).thenReturn(datasetMock);
            when(sparkServiceMock.cacheView(singleTableColumns.getTableName(), datasetMock)).thenReturn(TableDataset.of(
                    singleTableColumns,
                    datasetMock
            ));

            final TableDataset actual = objectUnderTest.loadCustomFilterDataset(kpiDefinitionsMock, singleTableColumns, jdbcDatasourceMock, "filter");

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(postgresDataLoaderFacadeMock).loadDatasetWithTry(jdbcDatasourceMock, singleTableColumns, 60);
            verify(loggerMock).isDebugEnabled();
            verify(datasetLoggerMock, never()).logFilteredRows(singleTableColumns.getTable(), "filter");
            verify(filterPreparatoryMock).prepareFilter(FilterType.CUSTOM, singleTableColumns.getTable(), "filter");
            verify(sparkSessionMock).sql("sourceFilter");
            verify(sparkServiceMock).cacheView(singleTableColumns.getTableName(), datasetMock);

            Assertions.assertThat(actual).isEqualTo(TableDataset.of(singleTableColumns, datasetMock));
        }

        @Test
        void shouldLoadDefaultFilterDataset() {
            when(kpiDefinitionHelperMock.extractAggregationPeriod(kpiDefinitionsMock)).thenReturn(60);
            when(loggerMock.isDebugEnabled()).thenReturn(false);
            when(filterPreparatoryMock.prepareFilter(FilterType.DEFAULT, singleTableColumns.getTable(), "filter")).thenReturn("sourceFilter");
            when(sparkSessionMock.sql("sourceFilter")).thenReturn(datasetMock);
            when(sparkServiceMock.cacheView(singleTableColumns.getTableName(), datasetMock)).thenReturn(TableDataset.of(
                    singleTableColumns,
                    datasetMock
            ));

            final TableDataset actual = objectUnderTest.loadDefaultFilterDataset(kpiDefinitionsMock, singleTableColumns, jdbcDatasourceMock, "filter");

            verify(kpiDefinitionHelperMock).extractAggregationPeriod(kpiDefinitionsMock);
            verify(postgresDataLoaderFacadeMock).loadDatasetWithTry(jdbcDatasourceMock, singleTableColumns, 60);
            verify(loggerMock).isDebugEnabled();
            verify(datasetLoggerMock, never()).logFilteredRows(singleTableColumns.getTable(), "filter");
            verify(filterPreparatoryMock).prepareFilter(FilterType.DEFAULT, singleTableColumns.getTable(), "filter");
            verify(sparkSessionMock).sql("sourceFilter");
            verify(sparkServiceMock).cacheView(singleTableColumns.getTableName(), datasetMock);

            Assertions.assertThat(actual).isEqualTo(TableDataset.of(singleTableColumns, datasetMock));
        }
    }
}
