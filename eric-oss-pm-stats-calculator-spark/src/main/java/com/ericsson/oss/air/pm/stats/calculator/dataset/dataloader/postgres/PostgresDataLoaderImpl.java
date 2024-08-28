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

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.api.PostgresDataLoader;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.SparkUtils;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresDataLoaderImpl {

    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final TableColumnsDefinerImpl tableColumnsDefiner;
    private final TempViewCreatorImpl tempViewCreator;

    private final SparkService sparkService;

    @Qualifier("postgresPartitionedLoader") private final PostgresDataLoader postgresPartitionedDataLoader;
    @Qualifier("postgresUnPartitionedLoader") private final PostgresDataLoader postgresUnPartitionedDataLoader;

    public TableDatasets loadUnPartitionedDataset(
            final Collection<KpiDefinition> kpiDefinitions, final Integer aggregationPeriod, final ReadingOptions readingOptions
    ) {
        final TableDatasets tableDatasets = TableDatasets.of();

        kpiDefinitionHelper.extractAliases(kpiDefinitions).forEach(alias -> {
            final SingleTableColumns singleTableColumns = tableColumnsDefiner.defineTableColumns(kpiDefinitions, aggregationPeriod, alias);
            tableDatasets.put(
                    Table.of(alias),
                    loadUnPartitionedDataset(sparkService.getKpiJdbcDatasource(), singleTableColumns, aggregationPeriod, readingOptions)
            );
        });

        return tableDatasets;
    }

    public TableDatasets loadPartitionedDataset(
            final Collection<KpiDefinition> kpiDefinitions, final Integer aggregationPeriod, final ReadingOptions readingOptions
    ) {
        final TableDatasets tableDatasets = TableDatasets.of();

        kpiDefinitionHelper.extractAliases(kpiDefinitions).forEach(alias -> {
            final SingleTableColumns singleTableColumns = tableColumnsDefiner.defineTableColumns(kpiDefinitions, aggregationPeriod, alias);
            final JdbcDatasource kpiJdbcDatasource = sparkService.getKpiJdbcDatasource();
            try {
                tableDatasets.put(TableDataset.of(
                        Table.of(alias),
                        loadPartitionedDataset(kpiJdbcDatasource, singleTableColumns, aggregationPeriod, readingOptions)
                ));
            } catch (final IllegalArgumentException e) { //NOSONAR Exception suitably logged
                log.warn(String.format("Error performing read of '%s' with partitions, database will be read without partitioning",
                                       singleTableColumns.getTableName()),
                         e.getClass());

                tableDatasets.put(TableDataset.of(
                        Table.of(alias),
                        loadUnPartitionedDataset(kpiJdbcDatasource, singleTableColumns, aggregationPeriod, readingOptions)
                ));
            }
        });

        return tableDatasets;
    }

    public Dataset<Row> loadPartitionedDataset(
            final JdbcDatasource jdbcDatasource,
            @NonNull final SingleTableColumns singleTableColumns,
            final Integer aggregationPeriod,
            final ReadingOptions readingOptions
    ) {
        return tempViewCreator.createOrReplaceTempView(singleTableColumns.getTable(), () -> postgresPartitionedDataLoader.loadDataWithJobDescription(
                SparkUtils.buildJobDescription("GET_PARTITION_LIMITS", singleTableColumns.getTableName(), String.valueOf(aggregationPeriod)),
                singleTableColumns, jdbcDatasource, readingOptions
        ));
    }

    public Dataset<Row> loadUnPartitionedDataset(
            final JdbcDatasource jdbcDatasource,
            @NonNull final SingleTableColumns singleTableColumns,
            final Integer aggregationPeriod,
            final ReadingOptions readingOptions
    ) {
        return tempViewCreator.createOrReplaceTempView(singleTableColumns.getTable(), () -> postgresUnPartitionedDataLoader.loadDataWithJobDescription(
                SparkUtils.buildJobDescription("GET_PRIMARY_KEYS", singleTableColumns.getTableName(), String.valueOf(aggregationPeriod)),
                singleTableColumns, jdbcDatasource, readingOptions
        ));
    }

}
