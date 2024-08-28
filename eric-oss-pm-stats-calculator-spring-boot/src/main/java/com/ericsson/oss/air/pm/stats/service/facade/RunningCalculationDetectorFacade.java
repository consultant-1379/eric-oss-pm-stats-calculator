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

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class RunningCalculationDetectorFacade {
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private CalculationService calculationService;

    public boolean isAnySimpleCalculationRunning() {
        final Set<String> simpleExecutionGroups = kpiDefinitionService.findAllSimpleExecutionGroups();
        return calculationService.isAnyCalculationRunning(simpleExecutionGroups);
    }

    public boolean isAnyComplexCalculationRunning() {
        final Set<String> complexExecutionGroups = kpiDefinitionService.findAllComplexExecutionGroups();
        return calculationService.isAnyCalculationRunning(complexExecutionGroups);
    }
}
