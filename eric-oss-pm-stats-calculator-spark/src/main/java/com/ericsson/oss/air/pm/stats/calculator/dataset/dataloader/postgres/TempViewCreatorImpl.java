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

import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.dataset.exception.DataSourceException;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempViewCreatorImpl {
    private final SparkService sparkService;

    public Dataset<Row> createOrReplaceTempView(final Table table, final @NonNull Supplier<? extends Dataset<Row>> datasetSupplier) {
        final Dataset<Row> rowDataset = datasetSupplier.get();

        if (rowDataset.columns().length == 0) {
            throw new DataSourceException(String.format("Error loading Dataset, table '%s' returned an empty Dataset", table.getName()));
        }

        sparkService.cacheView(table.getName(), rowDataset);

        return rowDataset;
    }

}
