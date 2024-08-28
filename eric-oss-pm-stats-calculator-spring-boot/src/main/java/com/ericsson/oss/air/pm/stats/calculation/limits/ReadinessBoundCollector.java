/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits;

import static lombok.AccessLevel.PUBLIC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculation.readiness.ReadinessWindowCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ReadinessBoundCollector {
    @Inject
    private ReadinessWindowCollector readinessWindowCollector;

    public Map<KpiDefinitionEntity, ReadinessBound> calculateReadinessBounds(final String complexExecutionGroup, final List<? extends KpiDefinitionEntity> kpiDefinitions) {
        final Map<KpiDefinitionEntity, List<ReadinessWindow>> mergeDefinitionAndDatasource = definitionToReadinessWindows(complexExecutionGroup, kpiDefinitions);
        final Map<KpiDefinitionEntity, ReadinessBound> result = new HashMap<>(mergeDefinitionAndDatasource.size());

        mergeDefinitionAndDatasource.forEach((kpiDefinition, readinessWindows) -> {
            if (CollectionUtils.isEmpty(readinessWindows)) {
                return;
            }
            final ReadinessBound readinessBound = ReadinessBound.fromWindows(kpiDefinition.dataReliabilityOffset(), readinessWindows);

            result.put(kpiDefinition, readinessBound);
        });

        return result;
    }

    private Map<KpiDefinitionEntity, List<ReadinessWindow>> definitionToReadinessWindows(final String complexExecutionGroup, final List<? extends KpiDefinitionEntity> kpiDefinitions) {
        final Map<DefinitionName, List<ReadinessWindow>> definitionNameToReadinessWindows = readinessWindowCollector.collect(complexExecutionGroup, kpiDefinitions);

        return CollectionHelpers.transformKey(
                definitionNameToReadinessWindows,
                definitionName -> IterableUtils.find(kpiDefinitions, definitionName::hasSameName)
        );
    }
}
