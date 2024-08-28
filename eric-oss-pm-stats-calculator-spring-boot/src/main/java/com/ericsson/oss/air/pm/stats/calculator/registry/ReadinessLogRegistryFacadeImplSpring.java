/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.registry;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;
import com.ericsson.oss.air.pm.stats.service.registry.readiness.log.ReadinessLogRegistryFacade;
import com.ericsson.oss.air.pm.stats.service.registry.readiness.log.exception.ReadinessLogRegistryNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadinessLogRegistryFacadeImplSpring implements ReadinessLogRegistryFacade {
    private final CalculationService calculationService;
    private final PluginRegistry<ReadinessLogRegistry, KpiType> pluginRegistry;

    @Override
    public List<ReadinessLog> findByCalculationId(final UUID calculationId) {
        final ReadinessLogRegistry readinessLogRegistry = readinessLogRegistry(calculationId);
        return readinessLogRegistry.findByCalculationId(calculationId);
    }

    @Override
    public ReadinessLogRegistry readinessLogRegistry(final UUID calculationId) {
        final KpiType kpiType = calculationService.forceFetchKpiTypeByCalculationId(calculationId);

        return pluginRegistry.getPluginFor(kpiType, () -> {
            throw new ReadinessLogRegistryNotFoundException(String.format("No '%s' implementation supports '%s' KPI type", ReadinessLogRegistry.class.getSimpleName(), kpiType));
        });
    }
}