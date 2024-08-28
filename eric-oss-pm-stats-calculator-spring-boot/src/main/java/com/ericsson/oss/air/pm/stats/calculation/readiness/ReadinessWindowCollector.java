/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness;

import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.readiness.log.ReadinessLogCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.calculation.readiness.window.ReadinessWindowCalculator;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ReadinessWindowCollector {
    @Inject
    private ReadinessWindowCalculator readinessWindowCalculator;
    @Inject
    private ReadinessLogCollector readinessLogCollector;
    @Inject
    private SimpleKpiDependencyCache simpleKpiDependencyCache;

    public Map<DefinitionName, List<ReadinessWindow>> collect(final String complexExecutionGroup, final List<? extends KpiDefinitionEntity> kpiDefinitions) {
        return collect(complexExecutionGroup, CollectionHelpers.transform(kpiDefinitions, KpiDefinitionEntity::name));
    }

    public Map<DefinitionName, List<ReadinessWindow>> collect(final String complexExecutionGroup, final Collection<String> definitionNames) {
        final Map<DefinitionName, Set<KpiDefinitionEntity>> nameToSimpleDependencies = collectSimpleDependencies(definitionNames);
        final Map<DefinitionName, Set<ReadinessLog>> nameToReadinessLogs = readinessLogCollector.collectReadinessLogs(complexExecutionGroup, nameToSimpleDependencies);
        final Map<DataSource, ReadinessWindow> dataSourceToReadinessWindow = readinessWindowCalculator.calculateReadinessWindows(nameToReadinessLogs.values());

        return CollectionHelpers.transformValue(
                nameToReadinessLogs,
                readinessLogs -> connectReadinessLogsToReadinessWindows(readinessLogs, dataSourceToReadinessWindow)
        );
    }

    private Map<DefinitionName, Set<KpiDefinitionEntity>> collectSimpleDependencies(final Collection<String> definitionNames) {
        return CollectionHelpers.transformKey(simpleKpiDependencyCache.loadDependenciesForMultipleDefinitions(definitionNames), DefinitionName::of);
    }

    private static List<ReadinessWindow> connectReadinessLogsToReadinessWindows(
            @NonNull final Collection<? extends ReadinessLog> readinessLogs,
            @NonNull final Map<DataSource, ReadinessWindow> dataSourceToReadinessWindow) {
        return readinessLogs.stream()
                .map(ReadinessLog::getDatasource)
                .map(DataSource::of)
                .map(dataSourceToReadinessWindow::get)
                .collect(Collectors.toList());
    }

}
