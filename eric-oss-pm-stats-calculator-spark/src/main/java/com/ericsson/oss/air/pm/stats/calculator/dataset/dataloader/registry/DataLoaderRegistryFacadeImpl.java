/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.registry;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.DataLoader;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoaderRegistryFacadeImpl {
    private final PluginRegistry<DataLoader, Collection<KpiDefinition>> dataLoaderPluginRegistry;

    public Iterator<TableDatasets> defaultFilterIterator(final Collection<KpiDefinition> kpiDefinitions) {
        return dataLoader(kpiDefinitions).iteratorDefaultFilter(kpiDefinitions);
    }

    public TableDatasets customFilterDatasets(final Collection<KpiDefinition> kpiDefinitions, final Set<Filter> filters) {
        return dataLoader(kpiDefinitions).loadDatasetsWithCustomFilter(filters, kpiDefinitions);
    }

    private DataLoader dataLoader(final Collection<KpiDefinition> kpiDefinitions) {
        return dataLoaderPluginRegistry.getRequiredPluginFor(kpiDefinitions);
    }
}
