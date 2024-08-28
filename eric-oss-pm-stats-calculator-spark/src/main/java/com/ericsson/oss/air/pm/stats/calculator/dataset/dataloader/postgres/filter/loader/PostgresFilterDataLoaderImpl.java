/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.debug.DatasetLoggerImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterPreparatoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresFilterDataLoaderImpl {
    private final Logger logger;
    private final PostgresDataLoaderFacadeImpl postgresDataLoaderFacade;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final SparkService sparkService;
    private final DatasetLoggerImpl datasetLogger;
    private final FilterPreparatoryImpl filterPreparatory;
    private final SparkSession sparkSession;

    public TableDataset loadCustomFilterDataset(final Collection<KpiDefinition> kpiDefinitions,
                                                final SingleTableColumns tableColumns,
                                                final JdbcDatasource jdbcDatasource,
                                                final String filter) {
        return loadDataset(kpiDefinitions, tableColumns, jdbcDatasource, filter, FilterType.CUSTOM);
    }

    public TableDataset loadDefaultFilterDataset(final Collection<KpiDefinition> kpiDefinitions,
                                                final SingleTableColumns tableColumns,
                                                final JdbcDatasource jdbcDatasource,
                                                final String filter) {
        return loadDataset(kpiDefinitions, tableColumns, jdbcDatasource, filter, FilterType.DEFAULT);
    }

    //  TODO: Further extract this method later on
    private TableDataset loadDataset(final Collection<KpiDefinition> kpiDefinitions,
                                     final SingleTableColumns tableColumns,
                                     final JdbcDatasource jdbcDatasource,
                                     final String filter,
                                     final FilterType filterType) {
        final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);

        postgresDataLoaderFacade.loadDatasetWithTry(jdbcDatasource, tableColumns, aggregationPeriod);

        if (logger.isDebugEnabled()) {
            datasetLogger.logFilteredRows(tableColumns.getTable(), filter);
        }

        final String sourceFilter = filterPreparatory.prepareFilter(filterType, tableColumns.getTable(), filter);

        final Dataset<Row> sourceDataset = sparkSession.sql(sourceFilter);

        return sparkService.cacheView(tableColumns.getTableName(), sourceDataset);
    }

}
