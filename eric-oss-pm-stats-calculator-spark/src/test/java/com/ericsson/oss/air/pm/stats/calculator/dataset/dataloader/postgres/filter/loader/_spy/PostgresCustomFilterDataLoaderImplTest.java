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

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTableFilters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.ColumnHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresCustomFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.TableColumnExtractor;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class PostgresCustomFilterDataLoaderImplTest {
    PostgresDataLoaderFacadeImpl postgresDataLoaderFacadeMock = mock(PostgresDataLoaderFacadeImpl.class);
    SourceDataAvailability sourceDataAvailabilityMock = mock(SourceDataAvailability.class);
    KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);
    DatasourceRegistry datasourceRegistryMock = mock(DatasourceRegistry.class);
    SqlExpressionHelperImpl sqlExpressionHelperMock = mock(SqlExpressionHelperImpl.class);
    PostgresFilterDataLoaderImpl filterDataLoaderMock = mock(PostgresFilterDataLoaderImpl.class);
    ColumnHandlerImpl columnHandlerMock = mock(ColumnHandlerImpl.class);
    TableColumnExtractor tableColumnExtractorMock = mock(TableColumnExtractor.class);

    @Spy PostgresCustomFilterDataLoaderImpl objectUnderTest = new PostgresCustomFilterDataLoaderImpl(
            postgresDataLoaderFacadeMock,
            sourceDataAvailabilityMock,
            kpiDefinitionHelperMock,
            datasourceRegistryMock,
            sqlExpressionHelperMock,
            filterDataLoaderMock,
            columnHandlerMock,
            tableColumnExtractorMock
    );

    @Nested
    @DisplayName("Testing loading datasets with custom filter")
    class LoadDatasetsWithCustomFilter {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock Dataset<Row> datasetMock;

        @Test
        void shouldLoadDatasetsWithCustomFilter() {
            final Set<Filter> filters = emptySet();

            final TableDatasets right = TableDatasetsUtil.from(singletonMap("right", datasetMock));
            final TableDatasets left = TableDatasetsUtil.from(singletonMap("left", datasetMock));

            final ReadingOptions readingOptions = ReadingOptions.empty();
            when(postgresDataLoaderFacadeMock.loadDataset(kpiDefinitionsMock, readingOptions)).thenReturn(left);
            doReturn(right).when(objectUnderTest).loadSourceDatasetsWithCustomFilter(kpiDefinitionsMock, filters);

            final TableDatasets actual = objectUnderTest.loadDatasetsWithCustomFilter(kpiDefinitionsMock, filters);

            verify(postgresDataLoaderFacadeMock).loadDataset(kpiDefinitionsMock, readingOptions);
            verify(objectUnderTest).loadSourceDatasetsWithCustomFilter(kpiDefinitionsMock, filters);


            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    "right",datasetMock,
                    "left",datasetMock
            )));
        }
    }

    @Nested
    @DisplayName("Testing loading source datasets with custom filter")
    class LoadSourceDatasetsWithCustomFilter {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock Dataset<Row> datasetMock;
        @Mock DatasourceTableFilters datasourceTableFiltersMock;

        @Test
        void shouldLoadSourceDatasetsWithCustomFilter() {
            final Set<Filter> filters = Sets.newLinkedHashSet(
                    Filter.of("kpi_inmemory://table1.filter1"),
                    Filter.of("kpi_inmemory://table2.filter2"),
                    Filter.of("external://table3.filter3")
            );

            final TableDatasets tableDatasets1 = TableDatasetsUtil.from(singletonMap("table1", datasetMock));
            final TableDatasets tableDatasets2 = TableDatasetsUtil.from(singletonMap("table2", datasetMock));
            final TableDatasets tableDatasets3 = TableDatasetsUtil.from(singletonMap("table3", datasetMock));

            final Datasource datasource1 = Datasource.of("kpi");
            final Datasource datasource2 = Datasource.of("external");

            final Set<Table> tables1 = Sets.newLinkedHashSet(Table.of("table1"), Table.of("table2"));
            final Set<Table> tables2 = Sets.newLinkedHashSet(Table.of("table3"));

            final DatasourceTables datasourceTables = DatasourceTables.newInstance();
            datasourceTables.computeIfAbsent(datasource1).addAll(tables1);
            datasourceTables.computeIfAbsent(datasource2).addAll(tables2);

            when(kpiDefinitionHelperMock.groupFilters(filters)).thenReturn(datasourceTableFiltersMock);
            when(sourceDataAvailabilityMock.availableDataSources(kpiDefinitionsMock)).thenReturn(datasourceTables);

            //  First iteration
            doReturn(TableDatasets.merge(tableDatasets2, tableDatasets1)).when(objectUnderTest).loadSourceDatasets(kpiDefinitionsMock, datasourceTableFiltersMock, datasource1, tables1);

            //  Second iteration
            doReturn(tableDatasets3).when(objectUnderTest).loadSourceDatasets(kpiDefinitionsMock, datasourceTableFiltersMock, datasource2, tables2);

            final TableDatasets actual = objectUnderTest.loadSourceDatasetsWithCustomFilter(kpiDefinitionsMock, filters);

            verify(kpiDefinitionHelperMock).groupFilters(filters);
            verify(sourceDataAvailabilityMock).availableDataSources(kpiDefinitionsMock);
            verify(objectUnderTest).loadSourceDatasets(kpiDefinitionsMock, datasourceTableFiltersMock, datasource1, tables1);
            verify(objectUnderTest).loadSourceDatasets(kpiDefinitionsMock, datasourceTableFiltersMock, datasource2, tables2);


            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    "table1", datasetMock,
                    "table2", datasetMock,
                    "table3", datasetMock
            )));
        }
    }
}