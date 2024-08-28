/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.service.api.ComplexReadinessLogService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ReadinessLogManagerFacade {
    @Inject
    private ComplexReadinessLogService complexReadinessLogService;
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private SimpleKpiDependencyCache simpleKpiDependencyCache;

    public void persistComplexReadinessLog(@NonNull final KpiCalculationJob kpiCalculationJob) {
        if (kpiCalculationJob.isComplex()) {
            final UUID calculationId = kpiCalculationJob.getCalculationId();
            final Collection<String> simpleExecutionGroups = collectSimpleDependencyExecutionGroupNames(kpiCalculationJob.getExecutionGroup());

            log.info(
                    "Calculation '{}' transitively depends on simple execution groups '{}'",
                    kpiCalculationJob.getExecutionGroup(),
                    simpleExecutionGroups
            );

            complexReadinessLogService.save(calculationId, simpleExecutionGroups);
        }
    }

    private Collection<String> collectSimpleDependencyExecutionGroupNames(final String complexExecutionGroup) {
        final Collection<String> complexDefinitionNames = kpiDefinitionService.findKpiDefinitionNamesByExecutionGroup(complexExecutionGroup);
        return simpleKpiDependencyCache.transitiveDependencyExecutionGroupsOf(complexDefinitionNames);
    }
}
