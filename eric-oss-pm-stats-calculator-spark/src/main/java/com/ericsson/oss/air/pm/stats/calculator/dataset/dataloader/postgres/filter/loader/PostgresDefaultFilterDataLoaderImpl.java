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
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.ColumnHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterPreparatoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.TableColumnExtractor;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
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
public class PostgresDefaultFilterDataLoaderImpl {
    private final PostgresDataLoaderFacadeImpl postgresDataLoaderFacade;
    private final SourceDataAvailability sourceDataAvailability;
    private final DatasourceRegistry datasourceRegistry;
    private final PostgresFilterDataLoaderImpl filterDataLoader;
    private final ColumnHandlerImpl columnHandler;
    private final FilterPreparatoryImpl filterPreparatory;
    private final TableColumnExtractor tableColumnExtractor;

    public TableDatasets loadDatasetsWithDefaultFilter(final Collection<KpiDefinition> kpiDefinitions, final KpiCalculatorTimeSlot kpiCalculatorTimeSlot) {
        return TableDatasets.merge(
                postgresDataLoaderFacade.loadDataset(kpiDefinitions, ReadingOptions.empty()),
                loadSourceDatasetsWithDefaultFilter(kpiDefinitions, filterPreparatory.generateTimeBasedFilter(kpiCalculatorTimeSlot))
        );
    }

    //  TODO: Leave methods public to be able to test is easily. Later on further extract the model to isolate it perfectly

    public TableDatasets loadSourceDatasetsWithDefaultFilter(final Collection<KpiDefinition> kpiDefinitions, final Filter filter) {
        final TableDatasets tableDatasets = TableDatasets.of();

        final TableColumns tableColumns = tableColumnExtractor.extractAllTableColumns(kpiDefinitions);

       sourceDataAvailability.availableDataSources(kpiDefinitions).forEach((datasource, tables) ->
           tableDatasets.putAll(loadSourceDatasets(
                   tableColumns,
                   kpiDefinitions,
                   filter,
                   datasourceRegistry.getJdbcDatasource(datasource),
                   tables
           )));


        return tableDatasets;
    }

    public TableDatasets loadSourceDatasets(final TableColumns tableColumns,
                                            final Collection<KpiDefinition> kpiDefinitions,
                                            final Filter filter,
                                            final JdbcDatasource jdbcDatasource,
                                            final Set<Table> sourceTables) {
        final TableDatasets tableDatasets = TableDatasets.of();

        for (final Table sourceTable : sourceTables) {
            final boolean isDimTable = datasourceRegistry.isDimTable(jdbcDatasource.getDatasource(), sourceTable);

            final TableDataset tableDataset = filterDataLoader.loadDefaultFilterDataset(
                    kpiDefinitions,
                    SingleTableColumns.of(
                            sourceTable,
                            columnHandler.readColumns(jdbcDatasource, tableColumns, sourceTable)
                    ),
                    jdbcDatasource,
                    isDimTable ? StringUtils.EMPTY : filter.getName()
            );

            tableDatasets.put(tableDataset);
        }

        return tableDatasets;
    }
}
