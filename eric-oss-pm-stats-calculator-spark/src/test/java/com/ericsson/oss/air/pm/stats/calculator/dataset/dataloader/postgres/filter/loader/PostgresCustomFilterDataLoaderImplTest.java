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

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTableFilters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.ColumnHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.TableColumnExtractor;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
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
class PostgresCustomFilterDataLoaderImplTest {
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock DatasourceRegistry datasourceRegistryMock;
    @Mock SqlExpressionHelperImpl sqlExpressionHelperMock;
    @Mock PostgresFilterDataLoaderImpl filterDataLoaderMock;
    @Mock ColumnHandlerImpl columnHandlerMock;
    @Mock TableColumnExtractor tableColumnExtractorMock;

    @InjectMocks PostgresCustomFilterDataLoaderImpl objectUnderTest;

    @Nested
    @DisplayName("Testing loading source datasets")
    class LoadSourceDatasets {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock TableColumns tableColumnsMock;
        @Mock JdbcDatasource jdbcDatasourceMock;
        @Mock Dataset<Row> datasetMock;

        @Test
        void shouldLoadSourceDatasets() {
            final Datasource datasource1 = Datasource.of("datasource1");
            final Datasource datasource2 = Datasource.of("datasource2");

            final Table table1 = Table.of("table1");
            final Table table2 = Table.of("table2");

            final Filter filter1 = Filter.of("filter1");
            final Filter filter2 = Filter.of("filter2");

            final List<Filter> filters1 = singletonList(filter1);
            final List<Filter> filters2 = singletonList(filter2);

            final DatasourceTableFilters datasourceTableFilters = DatasourceTableFilters.newInstance();
            datasourceTableFilters.put(datasource1, singletonMap(table1, filters1));
            datasourceTableFilters.put(datasource2, singletonMap(table2, filters2));

            when(tableColumnExtractorMock.extractAllTableColumns(kpiDefinitionsMock)).thenReturn(tableColumnsMock);
            when(datasourceRegistryMock.getJdbcDatasource(datasource1)).thenReturn(jdbcDatasourceMock);

            final Set<Column> columns1 = new HashSet<>();
            final Set<Column> columns2 = new HashSet<>();

            final SingleTableColumns filterColumn1 = SingleTableColumns.of(table1, singleton(Column.of("filterColumn")));
            final SingleTableColumns filterColumn2 = SingleTableColumns.of(table2, Collections.emptySet());

            //  First iteration - Filterable
            when(columnHandlerMock.readColumns(jdbcDatasourceMock, tableColumnsMock, table1)).thenReturn(columns1);
            when(kpiDefinitionHelperMock.extractColumns(filters1)).thenReturn(singleton(Column.of("filterColumn")));
            when(sqlExpressionHelperMock.filter(filters1)).thenReturn("filterSQL");
            when(filterDataLoaderMock.loadCustomFilterDataset(kpiDefinitionsMock, filterColumn1, jdbcDatasourceMock, "filterSQL")).thenReturn(TableDataset.of(table1, datasetMock));

            //  Second iteration - Non-filterable
            when(columnHandlerMock.readColumns(jdbcDatasourceMock, tableColumnsMock, table2)).thenReturn(columns2);
            when(filterDataLoaderMock.loadCustomFilterDataset(kpiDefinitionsMock, filterColumn2, jdbcDatasourceMock, StringUtils.EMPTY)).thenReturn(TableDataset.of(table2, datasetMock));

            final TableDatasets actual = objectUnderTest.loadSourceDatasets(
                    kpiDefinitionsMock,
                    datasourceTableFilters,
                    datasource1,
                    Sets.newLinkedHashSet(table1, table2)
            );

            verify(tableColumnExtractorMock).extractAllTableColumns(kpiDefinitionsMock);
            verify(datasourceRegistryMock).getJdbcDatasource(datasource1);
            verify(columnHandlerMock).readColumns(jdbcDatasourceMock, tableColumnsMock, table1);
            verify(columnHandlerMock).readColumns(jdbcDatasourceMock, tableColumnsMock, table2);
            verify(kpiDefinitionHelperMock).extractColumns(filters1);
            verify(sqlExpressionHelperMock).filter(filters1);
            verify(filterDataLoaderMock).loadCustomFilterDataset(kpiDefinitionsMock, filterColumn1, jdbcDatasourceMock, "filterSQL");
            verify(filterDataLoaderMock).loadCustomFilterDataset(kpiDefinitionsMock, filterColumn2, jdbcDatasourceMock, StringUtils.EMPTY);

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    table1.getName(),datasetMock,
                    table2.getName(),datasetMock
            )));
        }
    }
}