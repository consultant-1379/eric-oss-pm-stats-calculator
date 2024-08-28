/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;
import com.ericsson.oss.air.pm.stats.repository.ExecutionGroupGenerator;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.rest.validator.api.DatabaseValidator;

import kpi.model.KpiDefinitionRequest;
import kpi.model.KpiDefinitionTable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

/**
 * Validation methods using database query
 */
@RequiredArgsConstructor
@Component
public class DatabaseValidatorImpl implements DatabaseValidator {

    private final KpiDefinitionService kpiDefinitionService;
    private final KpiDefinitionAdapter kpiDefinitionAdapter;
    private final ExecutionGroupGenerator executionGroupGenerator;

    @Override
    public void validateKpiAliasAggregationPeriodNotConflictingWithDb(final KpiDefinitionRequest kpiDefinition) {
        if (!kpiDefinition.scheduledSimple().isEmpty() || !kpiDefinition.scheduledComplex().isEmpty()) {
            final Set<Pair<String, Integer>> onDemandAliasAndAggregationPeriods = kpiDefinitionService.findOnDemandAliasAndAggregationPeriods();
            validateNotConflictingWithTables(kpiDefinition.scheduledSimple(), onDemandAliasAndAggregationPeriods);
            validateNotConflictingWithTables(kpiDefinition.scheduledComplex(), onDemandAliasAndAggregationPeriods);
        }
        if (!kpiDefinition.onDemand().isEmpty()) {
            validateNotConflictingWithTables(kpiDefinition.onDemand(), kpiDefinitionService.findScheduledAliasAndAggregationPeriods());
        }
    }

    private void validateNotConflictingWithTables(final KpiDefinitionTable<? extends kpi.model.api.table.Table> kpiDefinitionTable,
                                                  final Set<Pair<String, Integer>> aliasAndAggregationPeriodPairs) {
        for (final kpi.model.api.table.Table table : kpiDefinitionTable.kpiOutputTables()) {
            if (aliasAndAggregationPeriodPairs.contains(Pair.of(table.alias().value(), table.aggregationPeriod().value()))) {
                throw KpiDefinitionValidationException.conflict(
                        String.format("Kpi with an alias '%s' and aggregation period '%s' already exists in database for a conflicting KPI type",
                        table.alias().value(), table.aggregationPeriod().value()));
            }
        }
    }

    @Override
    public void validateNameIsUnique(final KpiDefinitionRequest payloadKpiDefinition, final UUID collectionId) {
        final Set<kpi.model.api.table.definition.KpiDefinition> payloadDefinitions = payloadKpiDefinition.definitions();
        final Set<String> databaseKpiNames = kpiDefinitionService.findAllKpiNames(collectionId);
        final Set<String> payloadKpiNames = new HashSet<>(payloadDefinitions.size());
        payloadDefinitions.forEach(definition -> {
            final String definitionName = definition.name().value();
            if (!payloadKpiNames.add(definitionName)) {
                throw KpiDefinitionValidationException.conflict(
                        String.format("KPI name must be unique but '%s' is duplicated in the payload", definitionName));
            }
            if (!databaseKpiNames.add(definitionName)) {
                throw KpiDefinitionValidationException.conflict(
                        String.format("KPI name must be unique but '%s' is already defined in the database", definitionName));
            }
        });
    }

    @Override
    public void validateExecutionGroup(final KpiDefinitionRequest kpiDefinitionRequest) {
        final Set<String> simpleExecutionGroups = kpiDefinitionService.findAllSimpleExecutionGroups();
        final Set<String> complexExecutionGroups = kpiDefinitionService.findAllComplexExecutionGroups();

        kpiDefinitionAdapter.toListOfEntities(kpiDefinitionRequest).forEach(entity -> {
            if (entity.isSimple()) {
                simpleExecutionGroups.add(executionGroupGenerator.generateOrGetExecutionGroup(entity));
            }
            if (entity.isComplex()) {
                complexExecutionGroups.add(entity.executionGroup().name());
            }
        });

        final Set<String> conflictingExecutionGroup = complexExecutionGroups.stream()
                                                                            .filter(simpleExecutionGroups::contains)
                                                                            .collect(Collectors.toSet());

        if (!conflictingExecutionGroup.isEmpty()) {
            throw KpiDefinitionValidationException.conflict(
                    String.format("Complex execution group cannot be the same as a generated Simple execution group. " +
                            "Conflicting execution group(s): %s", conflictingExecutionGroup));
        }
    }
}
