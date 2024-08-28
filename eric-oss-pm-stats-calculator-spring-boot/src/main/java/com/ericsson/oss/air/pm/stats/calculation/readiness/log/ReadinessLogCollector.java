/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.log;

import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.ReadinessLogService;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ReadinessLogCollector {
    @Inject
    private ReadinessLogService readinessLogService;

    public Map<DefinitionName, Set<ReadinessLog>> collectReadinessLogs(
            final String complexExecutionGroup,
            final Map<DefinitionName, ? extends Set<KpiDefinitionEntity>> nameToSimpleDependencies) {
        final Map<DefinitionName, Set<ExecutionGroup>> nameToExecutionGroups = CollectionHelpers.transformValue(
                nameToSimpleDependencies,
                ReadinessLogCollector::extractExecutionGroups
        );

        return CollectionHelpers.transformValue(
                nameToExecutionGroups,
                executionGroup -> readinessLogService.collectLatestReadinessLogs(complexExecutionGroup, executionGroup)
        );
    }

    private static Set<ExecutionGroup> extractExecutionGroups(@NonNull final Collection<? extends KpiDefinitionEntity> kpiDefinitionEntities) {
        return kpiDefinitionEntities.stream()
                .map(KpiDefinitionEntity::executionGroup)
                .collect(Collectors.toSet());
    }
}
