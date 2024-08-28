/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset.registry;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistency;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.OffsetPersistencyNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetPersistencyRegistryFacadeImpl {
    private final PluginRegistry<OffsetPersistency, Collection<KpiDefinition>> offsetPersistencyPluginRegistry;

    public OffsetPersistency offsetPersistency(final Collection<KpiDefinition> kpiDefinitions) {
        return offsetPersistencyPluginRegistry.getPluginFor(kpiDefinitions, () -> {
            final String message = String.format("%s for KPI Definitions %s not found", OffsetPersistency.class.getSimpleName(), kpiDefinitions);
            return new OffsetPersistencyNotFoundException(message);
        });
    }
}
