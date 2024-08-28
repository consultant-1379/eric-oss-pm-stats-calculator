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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTableFilters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.ColumnHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.TableColumnExtractor;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresCustomFilterDataLoaderImpl {
    private final PostgresDataLoaderFacadeImpl postgresDataLoaderFacade;
    private final SourceDataAvailability sourceDataAvailability;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final DatasourceRegistry datasourceRegistry;
    private final SqlExpressionHelperImpl sqlExpressionHelper;
    private final PostgresFilterDataLoaderImpl filterDataLoader;
    private final ColumnHandlerImpl columnHandler;
    private final TableColumnExtractor tableColumnExtractor;

    public TableDatasets loadDatasetsWithCustomFilter(final Collection<KpiDefinition> kpiDefinitions, final Set<Filter> filters) {
        return TableDatasets.merge(
                postgresDataLoaderFacade.loadDataset(kpiDefinitions, ReadingOptions.empty()),
                loadSourceDatasetsWithCustomFilter(kpiDefinitions, filters));
    }

    //  TODO: Extract these methods to be able to test it in isolation and leave spying object

    public TableDatasets loadSourceDatasetsWithCustomFilter(final Collection<KpiDefinition> kpiDefinitions, final Set<Filter> filters) {
        final TableDatasets tableDatasets = TableDatasets.of();
        final DatasourceTableFilters datasourceTableFilters = kpiDefinitionHelper.groupFilters(filters);

        sourceDataAvailability.availableDataSources(kpiDefinitions).forEach((datasource, tables) ->
            tableDatasets.putAll(loadSourceDatasets(
                    kpiDefinitions,
                    datasourceTableFilters,
                    datasource,
                    tables)));

        return tableDatasets;
    }

    public TableDatasets loadSourceDatasets(final Collection<KpiDefinition> kpiDefinitions,
                                             final DatasourceTableFilters allFiltersByTableByDatasource,
                                             final Datasource datasource,
                                             final Set<Table> sourceTablesName) {
        final TableDatasets tableDatasets = TableDatasets.of();
        final TableColumns tableColumns = tableColumnExtractor.extractAllTableColumns(kpiDefinitions);
        final JdbcDatasource jdbcDatasource = datasourceRegistry.getJdbcDatasource(datasource);

        for (final Table sourceTable : sourceTablesName) {
            final Set<Column> columns = columnHandler.readColumns(jdbcDatasource, tableColumns, sourceTable);
            String filter = StringUtils.EMPTY;

            if (allFiltersByTableByDatasource.isFilterable(datasource, sourceTable)) {
                final List<Filter> filterList = allFiltersByTableByDatasource.getFilters(datasource, sourceTable);

                columns.addAll(kpiDefinitionHelper.extractColumns(filterList));
                filter = sqlExpressionHelper.filter(filterList);
            }

            tableDatasets.put(filterDataLoader.loadCustomFilterDataset(
                    kpiDefinitions,
                    SingleTableColumns.of(sourceTable, columns),
                    jdbcDatasource,
                    filter
            ));
        }

        return tableDatasets;
    }

}
