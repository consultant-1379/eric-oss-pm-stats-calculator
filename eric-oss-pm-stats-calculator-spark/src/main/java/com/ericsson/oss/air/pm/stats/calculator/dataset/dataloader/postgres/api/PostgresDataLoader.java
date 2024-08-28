/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.api;

import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface PostgresDataLoader {
    void registerJobDescription(String jobDescription);

    void unregisterJobDescription();

    Dataset<Row> loadData(SingleTableColumns tableColumns, JdbcDatasource jdbcDatasource, ReadingOptions readingOptions);

    default Dataset<Row> loadDataWithJobDescription(
            final String jobDescription,
            final SingleTableColumns tableColumns,
            final JdbcDatasource jdbcDatasource,
            final ReadingOptions readingOptions
    ) {
        registerJobDescription(jobDescription);

        final Dataset<Row> result = loadData(tableColumns, jdbcDatasource, readingOptions);

        unregisterJobDescription();

        return result;
    }
}
