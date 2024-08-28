/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator._test;

import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableDatasetsUtil {
    public static TableDatasets from(@NonNull final Map<String, ? extends Dataset<Row>> datasets) {
        final TableDatasets result = TableDatasets.of();

        datasets.forEach((table, dataset) -> result.put(TableDataset.of(Table.of(table), dataset)));

        return result;
    }
}
