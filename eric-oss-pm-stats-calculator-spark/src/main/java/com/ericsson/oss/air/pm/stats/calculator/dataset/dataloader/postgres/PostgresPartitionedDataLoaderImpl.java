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

import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.api.PostgresDataLoader;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.spark.DatasetUtils;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("postgresPartitionedLoader")
public class PostgresPartitionedDataLoaderImpl implements PostgresDataLoader {
    private final SparkService sparkService;
    private final CalculationProperties calculationProperties;
    private final SparkSession sparkSession;

    @Override
    public void registerJobDescription(final String jobDescription) {
        sparkService.registerJobDescription(jobDescription);
    }

    @Override
    public void unregisterJobDescription() {
        sparkService.unregisterJobDescription();
    }

    @Override
    public Dataset<Row> loadData(
            @NonNull final SingleTableColumns tableColumns,
            @NonNull final JdbcDatasource jdbcDatasource,
            final ReadingOptions readingOptions
    ) {
        return DatasetUtils.loadDatasetWithSpecificColumnsAndPrimaryKeys(
               sparkSession,
               tableColumns.getTableName(),
               tableColumns.getColumns().stream().map(Column::getName).collect(Collectors.toSet()),
               jdbcDatasource.getJbdcConnection(),
               jdbcDatasource.getJdbcProperties(),
               calculationProperties.getSparkParallelism(),
               calculationProperties.getIndexedNumericPartitionColumns(),
               readingOptions
       );
    }

}
