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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@Slf4j
@Getter(AccessLevel.NONE)
@Data(staticConstructor = "of")
public final class TableDatasets {
    private final Map<Table, Dataset<Row>> datasets = new HashMap<>();

    public static TableDatasets merge(final TableDatasets left, final TableDatasets right) {
        final TableDatasets result = of();

        result.putAll(left);
        result.putAll(right);

        return result;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return datasets.isEmpty();
    }

    public boolean hasNoEmptyValue() {
        return datasets.values().stream().noneMatch(Dataset::isEmpty);
    }

    public void cacheDatasets() {
        datasets.forEach((table, dataset) -> dataset.cache());
    }

    public void unPersistDatasets() {
        datasets.forEach((table, dataset) -> {
            dataset.unpersist();
            log.info("Table: '{}' has been un-persisted", table.getName());
        });
    }

    public Dataset<Row> get(final Table table) {
        return datasets.get(table);
    }

    public void put(final Table table, final Dataset<Row> dataset) {
        datasets.put(table, dataset);
    }

    public void put(@NonNull final TableDataset tableDataset) {
        put(tableDataset.getTable(), tableDataset.getDataset());
    }

    public void putAll(@NonNull final TableDatasets tableDatasets) {
        datasets.putAll(tableDatasets.datasets);
    }

    public Integer getAggregationPeriodOfTable(){
        return IterableUtils.first(datasets.keySet()).getAggregationPeriod();
    }
}
