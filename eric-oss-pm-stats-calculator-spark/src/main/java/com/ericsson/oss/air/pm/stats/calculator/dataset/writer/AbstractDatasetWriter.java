/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.writer;

import static com.ericsson.oss.air.pm.stats.common.spark.sink.JdbcUpsertSink.OPTION_KPI_NAMES_TO_CALCULATE;
import static java.util.stream.Collectors.joining;

import java.util.Properties;
import java.util.regex.Pattern;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api.DatasetWriter;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDatasetWriter implements DatasetWriter {
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    // Not static so child classes can override.
    protected final SparkService sparkService;
    protected final KpiDefinitionService kpiDefinitionService;
    protected final int batchSize;

    @Override
    public void registerJobDescription(final String description) {
        sparkService.registerJobDescription(description);
    }

    @Override
    public void unregisterJobDescription() {
        sparkService.unregisterJobDescription();
    }

    @Override
    public void writeToTargetTable(final @NonNull Dataset<? extends Row> data, final String targetTable, final String primaryKeys) {
        final String kpiJdbcConnection = sparkService.getKpiJdbcConnection();
        final Properties kpiJdbcProperties = sparkService.getKpiJdbcProperties();

        final String filterForNotNullPrimaryKey = convertToFilterNotNullPrimaryKey(primaryKeys);
        final String kpiNamesToCalculate = kpiNamesToCalculate();

        data.filter(filterForNotNullPrimaryKey)
                .write()
                .format("jdbc-sink")
                .option("dbtable", targetTable)
                .option("url", kpiJdbcConnection)
                .option("user", kpiJdbcProperties.getProperty("user"))
                .option("password", kpiJdbcProperties.getProperty("password"))
                .option("driver", kpiJdbcProperties.getProperty("driver"))
                .option("primaryKey", primaryKeys)
                .option("batchSize", batchSize)
                .option(OPTION_KPI_NAMES_TO_CALCULATE, kpiNamesToCalculate)
                .save();
    }

    private static String convertToFilterNotNullPrimaryKey(final String primaryKeys) {
        return String.format("%s is not null", COMMA_PATTERN.matcher(primaryKeys).replaceAll(" is not null and "));
    }

    private String kpiNamesToCalculate() {
        return kpiDefinitionService.loadDefinitionsToCalculate().stream().map(KpiDefinition::getName).collect(joining(","));
    }
}
