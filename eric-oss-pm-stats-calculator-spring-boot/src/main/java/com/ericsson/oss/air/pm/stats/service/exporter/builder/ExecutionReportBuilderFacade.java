/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.builder;

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.ReliabilityThresholdCalculatorRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculation.start.time.api.StartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.registry.StartTimeCalculatorRegistryFacade;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.model.Kpi;
import com.ericsson.oss.air.pm.stats.service.exporter.model.Table;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ExecutionReportBuilderFacade {

    @Inject
    private CalculationService calculationService;
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private DatabaseService databaseService;
    @Inject
    private ReliabilityThresholdCalculatorRegistryFacade reliabilityThresholdCalculatorRegistry;
    @Inject
    private StartTimeCalculatorRegistryFacade startTimeCalculatorRegistry;

    public ExecutionReport buildExecutionReport(@NonNull final UUID calculationId) {
        final Calculation calculation = calculationService.findCalculationReadyToBeExported(calculationId);

        final ExecutionReport executionReport = new ExecutionReport(calculation);
        executionReport.setTables(
                executionReport.isScheduled()
                        ? mapToTable(kpiDefinitionService.findKpiDefinitionsByExecutionGroup(executionReport.getExecutionGroup()), calculationId)
                        : mapToTable(kpiDefinitionService.findOnDemandKpiDefinitionsByCalculationId(calculationId), calculationId)
        );

        return executionReport;
    }

    private List<Table> mapToTable(final List<KpiDefinitionEntity> kpiDefinitions, final UUID calculationId) {
        final Map<String, Table> tables = new HashMap<>();
        final ReliabilityThresholdCalculator calculator = reliabilityThresholdCalculatorRegistry.calculator(kpiDefinitions);
        final Map<String, LocalDateTime> kpiReliabilities = calculator.calculateReliabilityThreshold(calculationId);

        final StartTimeCalculator startTimeCalculator = startTimeCalculatorRegistry.calculator(kpiDefinitions);
        final Map<String, LocalDateTime> kpiStartTimes = startTimeCalculator.calculateStartTime(calculationId);


        kpiDefinitions.stream()
                .filter(entity -> kpiReliabilities.containsKey(entity.name()))
                .forEach(kpi -> {
                    final String tableName = kpi.tableName();

                    tables.computeIfAbsent(tableName, name -> {
                        final List<String> listOfKpis = kpiDefinitionService.findAllKpiNamesFromSameTableAsKpiDefinition(kpi);

                        final Table table = Table.of(kpi);
                        table.setListOfKpis(listOfKpis);
                        table.setListOfDimensions(CollectionHelpers.allMissingElements(
                                databaseService.findColumnNamesForTable(tableName),
                                kpiDefinitionService.findAllKpiNames()));
                        return table;
                    }).addKpi(Kpi.of(
                            kpi.name(),
                            kpiReliabilities.get(kpi.name()).toEpochSecond(ZoneOffset.UTC),
                            kpiStartTimes.get(kpi.name()).toEpochSecond(ZoneOffset.UTC),
                            kpi.reexportLateData(),
                            kpi.exportable()
                    ));
                });

        return new ArrayList<>(tables.values());
    }
}
