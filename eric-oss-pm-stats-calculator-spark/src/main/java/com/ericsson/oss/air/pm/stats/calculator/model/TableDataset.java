/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@Slf4j
@Data(staticConstructor = "of")
public final class TableDataset {
    private final Table table;
    private final Dataset<Row> dataset;

    public static TableDataset of(@NonNull final SingleTableColumns singleTableColumns, final Dataset<Row> dataset) {
        return of(singleTableColumns.getTable(), dataset);
    }
}
