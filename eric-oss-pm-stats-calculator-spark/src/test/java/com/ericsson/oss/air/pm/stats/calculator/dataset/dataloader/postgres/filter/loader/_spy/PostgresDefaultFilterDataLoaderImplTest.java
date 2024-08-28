/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader._spy;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.ColumnHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterPreparatoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresDefaultFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.TableColumnExtractor;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class PostgresDefaultFilterDataLoaderImplTest {
    PostgresDataLoaderFacadeImpl postgresDataLoaderFacadeMock = mock(PostgresDataLoaderFacadeImpl.class);
    SourceDataAvailability sourceDataAvailabilityMock = mock(SourceDataAvailability.class);
    DatasourceRegistry datasourceRegistryMock = mock(DatasourceRegistry.class);
    PostgresFilterDataLoaderImpl filterDataLoaderMock = mock(PostgresFilterDataLoaderImpl.class);
    ColumnHandlerImpl columnHandlerMock = mock(ColumnHandlerImpl.class);
    FilterPreparatoryImpl filterPreparatoryMock = mock(FilterPreparatoryImpl.class);
    TableColumnExtractor tableColumnExtractorMock = mock(TableColumnExtractor.class);

    @Spy PostgresDefaultFilterDataLoaderImpl objectUnderTest = new PostgresDefaultFilterDataLoaderImpl(
            postgresDataLoaderFacadeMock,
            sourceDataAvailabilityMock,
            datasourceRegistryMock,
            filterDataLoaderMock,
            columnHandlerMock,
            filterPreparatoryMock,
            tableColumnExtractorMock
    );

    @Nested
    @DisplayName("Testing loading datasets with default filter")
    class LoadDatasetsWithDefaultFilter {
        @Mock Dataset<Row> datasetMock;
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock KpiCalculatorTimeSlot kpiCalculatorTimeSlotMock;

        @Test
        void shouldLoadDatasetsWithDefaultFilter() {
            final Filter filter = Filter.of("filter");
            final ReadingOptions readingOptions = ReadingOptions.empty();

            final TableDatasets right = TableDatasetsUtil.from(singletonMap("right", datasetMock));
            final TableDatasets left = TableDatasetsUtil.from(singletonMap("left", datasetMock));

            when(filterPreparatoryMock.generateTimeBasedFilter(kpiCalculatorTimeSlotMock)).thenReturn(filter);
            doReturn(right).when(objectUnderTest).loadSourceDatasetsWithDefaultFilter(kpiDefinitionsMock, filter);
            when(postgresDataLoaderFacadeMock.loadDataset(kpiDefinitionsMock, readingOptions)).thenReturn(left);

            final TableDatasets actual = objectUnderTest.loadDatasetsWithDefaultFilter(kpiDefinitionsMock, kpiCalculatorTimeSlotMock);

            verify(filterPreparatoryMock).generateTimeBasedFilter(kpiCalculatorTimeSlotMock);
            verify(objectUnderTest).loadSourceDatasetsWithDefaultFilter(kpiDefinitionsMock, filter);
            verify(postgresDataLoaderFacadeMock).loadDataset(kpiDefinitionsMock, readingOptions);

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    "right",datasetMock,
                    "left",datasetMock
            )));
        }
    }

    @Nested
    @DisplayName("Testing loading source datasets with default filter")
    class LoadSourceDatasetsWithDefaultFilter {
        @Mock Dataset<Row> datasetMock;
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock TableColumns tableColumnsMock;

        @Test
        void shouldLoadSourceDatasetsWithDefaultFilter() {
            final Filter filter = Filter.of("filter");

            final TableDatasets tableDatasets1 = TableDatasetsUtil.from(singletonMap("tableDatasets1", datasetMock));
            final TableDatasets tableDatasets2 = TableDatasetsUtil.from(singletonMap("tableDatasets2", datasetMock));

            final Table table1 = Table.of("table1");
            final Table table2 = Table.of("table2");

            final DatasourceTables datasourceTables = DatasourceTables.newInstance();
            datasourceTables.computeIfAbsent(Datasource.of("ds1")).add(table1);
            datasourceTables.computeIfAbsent(Datasource.of("ds2")).add(table2);

            final Set<Table> sourceTables1 = Collections.singleton(table1);
            final Set<Table> sourceTables2 = Collections.singleton(table2);

            final JdbcDatasource jdbcDatasource1Mock = mock(JdbcDatasource.class);
            final JdbcDatasource jdbcDatasource2Mock = mock(JdbcDatasource.class);

            when(tableColumnExtractorMock.extractAllTableColumns(kpiDefinitionsMock)).thenReturn(tableColumnsMock);
            when(sourceDataAvailabilityMock.availableDataSources(kpiDefinitionsMock)).thenReturn(datasourceTables);

            when(datasourceRegistryMock.getJdbcDatasource(Datasource.of("ds1"))).thenReturn(jdbcDatasource1Mock);
            when(datasourceRegistryMock.getJdbcDatasource(Datasource.of("ds2"))).thenReturn(jdbcDatasource2Mock);

            doReturn(tableDatasets1).when(objectUnderTest).loadSourceDatasets(
                    tableColumnsMock,
                    kpiDefinitionsMock,
                    filter,
                    jdbcDatasource1Mock,
                    sourceTables1
            );

            doReturn(tableDatasets2).when(objectUnderTest).loadSourceDatasets(
                    tableColumnsMock,
                    kpiDefinitionsMock,
                    filter,
                    jdbcDatasource2Mock,
                    sourceTables2
            );

            final TableDatasets actual = objectUnderTest.loadSourceDatasetsWithDefaultFilter(kpiDefinitionsMock, filter);

            verify(tableColumnExtractorMock).extractAllTableColumns(kpiDefinitionsMock);
            verify(sourceDataAvailabilityMock).availableDataSources(kpiDefinitionsMock);
            verify(datasourceRegistryMock).getJdbcDatasource(Datasource.of("ds1"));
            verify(datasourceRegistryMock).getJdbcDatasource(Datasource.of("ds2"));
            verify(objectUnderTest).loadSourceDatasets(tableColumnsMock, kpiDefinitionsMock, filter, jdbcDatasource1Mock, sourceTables1);
            verify(objectUnderTest).loadSourceDatasets(tableColumnsMock, kpiDefinitionsMock, filter, jdbcDatasource2Mock, sourceTables2);

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    "tableDatasets1",datasetMock,
                    "tableDatasets2",datasetMock
            )));
        }
    }
}