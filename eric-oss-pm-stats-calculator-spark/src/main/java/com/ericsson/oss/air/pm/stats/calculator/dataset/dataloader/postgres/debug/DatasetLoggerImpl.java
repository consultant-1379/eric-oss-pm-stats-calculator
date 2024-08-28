/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.debug;

import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlFilterCreator;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetLoggerImpl {
    private final SparkSession sparkSession;

    public void logFilteredRows(@NonNull final Table table, final String filter) {
        final SqlFilterCreator sqlFilterCreator = new SqlFilterCreator();
        final String sourceFilter = sqlFilterCreator.selectAll(table.getName()).where().addFilter(filter).build();
        sparkSession.sql(sourceFilter).foreach((Row row) -> log.debug("Filtering out row '{}'", row));
    }
}
