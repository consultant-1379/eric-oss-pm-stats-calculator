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

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.ColumnHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class PostgresDefaultFilterDataLoaderImplTest {
    @Mock DatasourceRegistry datasourceRegistryMock;
    @Mock PostgresFilterDataLoaderImpl filterDataLoaderMock;
    @Mock ColumnHandlerImpl columnHandlerMock;

    @InjectMocks PostgresDefaultFilterDataLoaderImpl objectUnderTest;

    @Nested
    @DisplayName("Testing loading source datasets")
    class LoadSourceDatasets {
        @Mock Dataset<Row> datasetMock;
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock TableColumns tableColumnsMock;
        @Mock JdbcDatasource jdbcDatasourceMock;

        @Test
        void shouldLoadSourceDatasets() {
            final Filter filter = Filter.of("filter");

            final Table table1 = Table.of("table1");
            final Table table2 = Table.of("table2");

            final Set<Table> sourceTables = Sets.newLinkedHashSet(table1, table2);

            final Datasource datasourceDIM = Datasource.of("DIM_source");
            final Datasource datasourceFACT = Datasource.of("FACT_source");

            final TableDataset tableDataset1 = TableDataset.of(table1, datasetMock);
            final TableDataset tableDataset2 = TableDataset.of(table2, datasetMock);

            final SingleTableColumns singleTableColumns1 = SingleTableColumns.of(table1, emptySet());
            final SingleTableColumns singleTableColumns2 = SingleTableColumns.of(table2, emptySet());

            when(jdbcDatasourceMock.getDatasource()).thenReturn(datasourceDIM, datasourceFACT);

            //  First iteration - DIM
            when(datasourceRegistryMock.isDimTable(datasourceDIM, table1)).thenReturn(true);
            when(columnHandlerMock.readColumns(jdbcDatasourceMock, tableColumnsMock, table1)).thenReturn(emptySet());
            when(filterDataLoaderMock.loadDefaultFilterDataset(kpiDefinitionsMock, singleTableColumns1, jdbcDatasourceMock, StringUtils.EMPTY)).thenReturn(tableDataset1);

            //  Second iteration - FACT
            when(datasourceRegistryMock.isDimTable(datasourceFACT, table2)).thenReturn(false);
            when(columnHandlerMock.readColumns(jdbcDatasourceMock, tableColumnsMock, table2)).thenReturn(emptySet());
            when(filterDataLoaderMock.loadDefaultFilterDataset(kpiDefinitionsMock, singleTableColumns2, jdbcDatasourceMock, filter.getName())).thenReturn(tableDataset2);

            final TableDatasets actual = objectUnderTest.loadSourceDatasets(tableColumnsMock, kpiDefinitionsMock, filter, jdbcDatasourceMock, sourceTables);

            verify(jdbcDatasourceMock, times(2)).getDatasource();
            verify(datasourceRegistryMock).isDimTable(datasourceDIM, table1);
            verify(datasourceRegistryMock).isDimTable(datasourceFACT, table2);
            verify(columnHandlerMock).readColumns(jdbcDatasourceMock, tableColumnsMock, table1);
            verify(columnHandlerMock).readColumns(jdbcDatasourceMock, tableColumnsMock, table2);
            verify(filterDataLoaderMock).loadDefaultFilterDataset(kpiDefinitionsMock, singleTableColumns1, jdbcDatasourceMock, StringUtils.EMPTY);
            verify(filterDataLoaderMock).loadDefaultFilterDataset(kpiDefinitionsMock, singleTableColumns2, jdbcDatasourceMock, filter.getName());

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    table1.getName(), datasetMock,
                    table2.getName(), datasetMock
            )));
        }
    }
}