/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.LATEST_SOURCE_DATA;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.LATEST_SOURCE_DATA_PRIMARY_KEY;
import static org.apache.spark.sql.functions.lit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.dataset.util.DatasetExecutor;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.DatasetWriterImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlCreator;
import com.ericsson.oss.air.pm.stats.calculator.util.SparkUtils;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetPersistencyPostgres implements OffsetPersistency {
    private final Map<Table, Dataset<Row>> maxTimestampsBySourceTable = new HashMap<>();

    private final SparkService sparkService;
    private final DatasetWriterImpl datasetWriter;
    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final SparkSession sparkSession;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    @Override
    public void calculateMaxOffsetsForSourceTables(final Map<Integer, List<KpiDefinition>> kpisByStage,
                                                   final DatasourceTables datasourceTables,
                                                   final int aggregationPeriodInMinutes) {
        calculateMaxTimestampsForSourceTables(kpisByStage, datasourceTables, aggregationPeriodInMinutes);
    }

    @Override
    public void persistMaxOffsets(final int aggregationPeriodInMinutes) {
        persistMaxTimestamps(aggregationPeriodInMinutes);
    }

    @Override
    public boolean supports(@NonNull final Collection<KpiDefinition> delimiter){
        return !KpiDefinitionHandler.areKpiDefinitionsSimpleKpis(delimiter);
    }

    private void calculateMaxTimestampsForSourceTables(final Map<Integer, List<KpiDefinition>> kpisByStage,
                                                       final DatasourceTables datasourceTables,
                                                       final int aggregationPeriodInMinutes) {
        final Set<Table> sourceTables = kpiDefinitionHelper.getTablesFromStagedKpis(kpisByStage);
        final Set<Table> sourceTablesToExclude = new HashSet<>(0);
        for (final Map.Entry<Datasource, Set<Table>> sourceTablesForDatasource : datasourceTables.entrySet()) {
            final Boolean isDimDatasource = DatasourceRegistry.getInstance().isDim(sourceTablesForDatasource.getKey(), false);
            if (Boolean.TRUE.equals(isDimDatasource)) {
                sourceTablesToExclude.addAll(sourceTablesForDatasource.getValue());
            }
        }
        sourceTables.removeAll(sourceTablesToExclude);

        final DatasetExecutor datasetExecutor = new DatasetExecutor(
                sparkService,
                new SqlCreator(aggregationPeriodInMinutes, sqlProcessorDelegator, kpiDefinitionHelper),
                new SqlExpressionHelperImpl(sqlProcessorDelegator),
                kpiDefinitionHelper,
                aggregationPeriodInMinutes,
                sparkSession);

        final String executionGroup = sparkService.getExecutionGroup();
        for (final Table sourceTable : sourceTables) {
            maxTimestampsBySourceTable.put(sourceTable, getLatestSourceData(datasetExecutor, sourceTable, executionGroup, aggregationPeriodInMinutes));
        }
    }

    private Dataset<Row> getLatestSourceData(
            final DatasetExecutor datasetExecutor,
            final Table sourceTable,
            final String executionGroup,
            final int aggregationPeriodInMinutes) {
        Dataset<Row> latestSourceData = datasetExecutor.getMaxUtcTimestamp(sourceTable.getName());
        latestSourceData = latestSourceData.withColumn("source", lit(sourceTable.getName()));
        latestSourceData = latestSourceData.withColumn("aggregation_period_minutes", lit(aggregationPeriodInMinutes));
        latestSourceData = latestSourceData.withColumn("execution_group", lit(executionGroup));
        return latestSourceData;
    }

    private void persistMaxTimestamps(final int aggregationPeriodInMinutes) {
        maxTimestampsBySourceTable.forEach((table, dataset) -> {
            final Dataset<Row> tableExcludingNullTimes = dataset.filter(dataset.col("latest_time_collected").isNotNull());
            sparkService.registerJobDescription(SparkUtils.buildJobDescription(
                    "REMOVE_NULL_TIMESTAMPS",
                    table.getName(),
                    String.valueOf(aggregationPeriodInMinutes)
            ));
            final boolean isEmpty = tableExcludingNullTimes.isEmpty();
            sparkService.unregisterJobDescription();
            if (!isEmpty) {
                log.info("Persisting latest timestamp for source table '{}'", table);

                datasetWriter.writeToTargetTable(
                        SparkUtils.buildJobDescription("PERSIST_TIMESTAMPS", table.getName(), String.valueOf(aggregationPeriodInMinutes)),
                        tableExcludingNullTimes,
                        LATEST_SOURCE_DATA,
                        LATEST_SOURCE_DATA_PRIMARY_KEY
                );
            }
        });
    }
}
