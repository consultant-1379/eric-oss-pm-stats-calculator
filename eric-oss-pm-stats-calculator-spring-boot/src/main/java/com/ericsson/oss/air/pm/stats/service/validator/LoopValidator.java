/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.graph.utils.GraphCycleRepresentationUtils.processExecutionGroupRepresentations;
import static com.ericsson.oss.air.pm.stats.graph.utils.GraphCycleRepresentationUtils.processKpiDefinitionRepresentations;
import static com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException.conflict;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.DependencyFinder;
import com.ericsson.oss.air.pm.stats.graph.KpiGroupLoopDetector;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionGraph;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ScheduledComplex;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class LoopValidator {

    @Inject
    private DependencyFinder dependencyFinder;
    @Inject
    private KpiGroupLoopDetector kpiGroupLoopDetector;
    @Inject
    private KpiDefinitionService kpiDefinitionService;

    public void validateNoCircle(final KpiDefinitionRequest kpiDefinition) {
        final ScheduledComplex scheduledComplex = kpiDefinition.scheduledComplex();

        if (scheduledComplex.isEmpty()) {
            return;
        }

        validateDependencyGraph(dependencyFinder.dependencyFinder(scheduledComplex));
    }

    public void validateNoCircle(final KpiDefinitionEntity entityToModify) {
        final Set<KpiDefinitionEntity> kpiDefinitionEntitySet = kpiDefinitionService.findComplexKpis()
                .stream()
                .map(generalEntity -> generalEntity.name().equals(entityToModify.name())
                        ? entityToModify
                        : generalEntity)
                .collect(Collectors.toSet());

        validateDependencyGraph(dependencyFinder.dependencyFinder(kpiDefinitionEntitySet));
    }

    private void validateDependencyGraph(final KpiDefinitionGraph kpiDefinitionGraph) {
        validateGraphOnExecutionGroups(kpiDefinitionGraph);
        validateGraphOnKpiDefinitions(kpiDefinitionGraph);
    }

    private void validateGraphOnExecutionGroups(final KpiDefinitionGraph kpiDefinitionGraph) {
        final List<Collection<KpiDefinitionVertex>> loops = kpiGroupLoopDetector.collectLoopsOnExecutionGroups(kpiDefinitionGraph);

        if (isNotEmpty(loops)) {
            final List<String> cycleRepresentations = processExecutionGroupRepresentations(loops);
            throw conflict(String.format("Following execution groups have circular definition: %s", cycleRepresentations));
        }
    }

    private void validateGraphOnKpiDefinitions(final KpiDefinitionGraph kpiDefinitionGraph) {
        final List<Collection<KpiDefinitionVertex>> loops = kpiGroupLoopDetector.collectLoopsOnKpiDefinitions(kpiDefinitionGraph);

        if (isNotEmpty(loops)) {
            final List<String> cycleRepresentations = processKpiDefinitionRepresentations(loops);
            throw conflict(String.format("Following KPIs in the same group have circular definition: %s", cycleRepresentations));
        }
    }
}