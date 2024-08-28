/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.util.DatasetExecutor;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;

/**
 * Class used to calculate KPIs with custom filter. Calculation of KPIs is agnostic to the aggregation period.
 */
@Slf4j
public class KpiCalculatorCustomFilter extends KpiCalculator {
    public KpiCalculatorCustomFilter(final SparkService sparkService,
                                     final SqlProcessorDelegator sqlProcessorDelegator,
                                     final int aggregationPeriodInMinutes,
                                     final Collection<KpiDefinition> customFilterKpis,
                                     final SparkSession sparkSession) {
        this.sparkService = sparkService;
        kpiDefinitions = customFilterKpis;
        datasetExecutor = new DatasetExecutor(
                sparkService,
                new KpiDefinitionHierarchy(kpiDefinitions),
                sqlProcessorDelegator,
                aggregationPeriodInMinutes, sparkSession);
    }

    @Override
    public TableDatasets calculate() {
        log.info("Starting calculation of KPIs which use a custom filter");
        final Map<Integer, List<KpiDefinition>> kpisByStage = kpiDefinitionHelper.groupKpisByStage(kpiDefinitions);
        final Set<String> aliases = kpiDefinitionHelper.extractAliases(kpiDefinitions);
        final TableDatasets kpiTablesByAlias = calculateKpis(aliases, kpisByStage, new HashMap<>(), TableDatasets.of());

        if (kpiTablesByAlias.isNotEmpty()) {
            return writeResults(aliases, kpisByStage, kpiTablesByAlias);
        }
        return TableDatasets.of();
    }
}
