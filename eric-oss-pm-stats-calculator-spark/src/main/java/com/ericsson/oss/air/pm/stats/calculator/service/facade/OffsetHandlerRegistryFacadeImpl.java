/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.facade;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.OffsetHandlerNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetHandlerRegistryFacadeImpl implements OffsetHandlerRegistryFacade {
    private final KpiDefinitionService kpiDefinitionService;
    private final PluginRegistry<OffsetHandler, Set<KpiDefinition>> pluginRegistry;

    @Override
    public OffsetHandler offsetHandler() {
        return pluginRegistry.getPluginFor(kpiDefinitionService.loadDefinitionsToCalculate(), () -> {
            final String message = "Offset handler not found in the registry";
            return new OffsetHandlerNotFoundException(message);
        });
    }
}
