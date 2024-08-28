/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.plugin.core.Plugin;

public interface DatasetWriter extends Plugin<Integer> {
    void registerJobDescription(String description);

    void unregisterJobDescription();

    void cacheOnDemandReliability(Dataset<Row> data);

    void writeToTargetTable(Dataset<? extends Row> data, String targetTable, String primaryKeys);

    default void writeToTargetTable(final String description, final Dataset<? extends Row> data, final String targetTable, final String primaryKeys) {
        registerJobDescription(description);

        writeToTargetTable(data, targetTable, primaryKeys);

        unregisterJobDescription();
    }

    default void writeToTargetTableWithCache(final String description, final Dataset<Row> data, final String targetTable, final String primaryKeys) {
        writeToTargetTable(description, data, targetTable, primaryKeys);

        cacheOnDemandReliability(data);
    }
}
