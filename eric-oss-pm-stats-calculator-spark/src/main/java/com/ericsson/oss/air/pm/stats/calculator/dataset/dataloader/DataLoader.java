/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;

import org.springframework.plugin.core.Plugin;

public interface DataLoader extends Plugin<Collection<KpiDefinition>> {
    /**
     * Returns an iterator to get the output datasets and the source datasets sliced by time based filtering or by kafka offsets, depending on the
     * underlying implementation (Postgres or kafka)
     *
     * @param kpiDefinitions
     *         {@link Collection} of {@link KpiDefinition}s to load data for.
     * @return iterator
     */
    Iterator<TableDatasets> iteratorDefaultFilter(Collection<KpiDefinition> kpiDefinitions);

    /**
     * Loads the output datasets and the source datasets with a custom filter.
     *
     * @param filters
     *         the {@link List} of filters to be applied to the source tables
     * @param kpiDefinitions
     *         {@link Collection} of {@link KpiDefinition}s to load data for.
     * @return the {@link TableDatasets} keyed by their datasource
     */
    TableDatasets loadDatasetsWithCustomFilter(Set<Filter> filters, Collection<KpiDefinition> kpiDefinitions);
}
