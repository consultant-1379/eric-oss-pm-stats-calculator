/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;

import java.sql.Timestamp;
import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog.ReadinessLogCache;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadinessLogRegisterImpl {
    private final CalculationRepository calculationRepository;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final ReadinessLogCache readinessLogCache;
    private final SparkService sparkService;

    public void registerReadinessLog(@NonNull final Dataset<Row> sourceDataset, final Collection<KpiDefinition> kpiDefinitions) {
        final Row row = sourceDataset.agg(min("aggregation_begin_time").as("minTimestamp"),
                                          max("aggregation_begin_time").as("maxTimestamp"))
                                     .head();

        if (row.anyNull()) {
            return;
        }
        Timestamp minTimestamp = row.getAs("minTimestamp");
        Timestamp maxTimestamp = row.getAs("maxTimestamp");

        final ReadinessLog readinessLog = new ReadinessLog();
        readinessLog.setDatasource(IterableUtils.first(kpiDefinitions).datasource());
        readinessLog.setEarliestCollectedData(minTimestamp.toLocalDateTime());
        readinessLog.setLatestCollectedData(maxTimestamp.toLocalDateTime());
        readinessLog.setCollectedRowsCount(sourceDataset.count());
        readinessLog.setKpiCalculationId(calculationRepository.getReferenceById(sparkService.getCalculationId()));

        final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);
        if (kpiDefinitionHelper.areCustomFilterDefinitions(kpiDefinitions)) {
            /* TODO: Also add Filters to the cache key otherwise it will override multiple times */
            readinessLogCache.put(readinessLog, aggregationPeriod);
        } else {
            readinessLogCache.merge(readinessLog, aggregationPeriod);
        }
    }
}