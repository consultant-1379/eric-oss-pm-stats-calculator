/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.registry.readiness.log;

import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;
import com.ericsson.oss.air.pm.stats.service.registry.readiness.log.exception.ReadinessLogRegistryNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ReadinessLogRegistryFacadeImpl implements ReadinessLogRegistryFacade {
    @Inject
    private CalculationService calculationService;

    @Inject
    @Any
    private Instance<ReadinessLogRegistry> readinessLogRegistries;

    @Override
    public List<ReadinessLog> findByCalculationId(final UUID calculationId) {
        final ReadinessLogRegistry readinessLogRegistry = readinessLogRegistry(calculationId);
        return readinessLogRegistry.findByCalculationId(calculationId);
    }

    @Override
    public ReadinessLogRegistry readinessLogRegistry(final UUID calculationId) {
        final KpiType kpiType = calculationService.forceFetchKpiTypeByCalculationId(calculationId);

        return readinessLogRegistries.stream()
                .filter(readinessLogRegistry -> readinessLogRegistry.doesSupport(kpiType))
                .findFirst()
                .orElseThrow(() -> new ReadinessLogRegistryNotFoundException(String.format(
                        "No '%s' implementation supports '%s' KPI type",
                        ReadinessLogRegistry.class.getSimpleName(),
                        kpiType
                )));
    }
}
