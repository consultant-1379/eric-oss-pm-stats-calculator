/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresDataLoaderFacadeImpl {
    private final CalculationProperties calculationProperties;
    private final PostgresDataLoaderImpl postgresDataLoader;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    public TableDatasets loadDataset(final Collection<KpiDefinition> kpiDefinitions, final ReadingOptions readingOptions) {
        return doLoadDataset(kpiDefinitions, readingOptions);
    }

    public TableDatasets loadDataset(final Collection<KpiDefinition> kpiDefinitions)  {
        return doLoadDataset(kpiDefinitions, ReadingOptions.empty());
    }

    public Dataset<Row> loadDatasetWithTry(final JdbcDatasource jdbcDatasource, final SingleTableColumns tableColumns, final Integer aggregationPeriod) {
        return calculationProperties.isPartitionDatasetLoad()
                ? tryToLoadPartitionedDataset(jdbcDatasource, tableColumns, aggregationPeriod)
                : loadUnPartitionedDataset(jdbcDatasource, tableColumns, aggregationPeriod);
    }

    /**
     * Tries to load partitioned dataset.
     * <br>
     * <strong>If partitioned dataset is not loadable then it tries to read it as un-partitioned dataset</strong>
     *
     * @param jdbcDatasource
     *         {@link JdbcDatasource} containing JDBC related information.
     * @param singleTableColumns
     *         {@link SingleTableColumns} containing information on the table and the columns.
     * @param aggregationPeriod
     *         aggregation period.
     * @return the loaded dataset.
     */
    public Dataset<Row> tryToLoadPartitionedDataset(final JdbcDatasource jdbcDatasource,
                                                    final SingleTableColumns singleTableColumns,
                                                    final Integer aggregationPeriod) {
        try {
            return loadPartitionedDataset(jdbcDatasource, singleTableColumns, aggregationPeriod);
        } catch (final IllegalArgumentException e) { //NOSONAR Exception suitably logged
            log.warn(String.format("Error performing read of '%s' with partitions, database will be read without partitioning",
                                   singleTableColumns.getTableName()),
                     e.getClass());
            return loadUnPartitionedDataset(jdbcDatasource, singleTableColumns, aggregationPeriod);
        }
    }

    public Dataset<Row> loadPartitionedDataset(
            final JdbcDatasource jdbcDatasource, final SingleTableColumns singleTableColumns, final Integer aggregationPeriod
    ) {
        return postgresDataLoader.loadPartitionedDataset(jdbcDatasource, singleTableColumns, aggregationPeriod, ReadingOptions.empty());
    }

    public Dataset<Row> loadUnPartitionedDataset(
            final JdbcDatasource jdbcDatasource, final SingleTableColumns singleTableColumns, final Integer aggregationPeriod
    ) {
        return postgresDataLoader.loadUnPartitionedDataset(jdbcDatasource, singleTableColumns, aggregationPeriod, ReadingOptions.empty());
    }

    private TableDatasets doLoadDataset(final Collection<KpiDefinition> kpiDefinitions, final ReadingOptions readingOptions) {
        final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);
        return calculationProperties.isPartitionDatasetLoad()
                ? postgresDataLoader.loadPartitionedDataset(kpiDefinitions, aggregationPeriod, readingOptions)
                : postgresDataLoader.loadUnPartitionedDataset(kpiDefinitions, aggregationPeriod, readingOptions);
    }
}
