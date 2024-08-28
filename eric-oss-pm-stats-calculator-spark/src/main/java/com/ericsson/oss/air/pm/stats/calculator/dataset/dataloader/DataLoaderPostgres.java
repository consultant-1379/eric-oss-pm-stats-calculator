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
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresCustomFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.factory.FilterIteratorFactoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoaderPostgres implements DataLoader {
    private final PostgresCustomFilterDataLoaderImpl postgresCustomFilterDataLoader;
    private final FilterIteratorFactoryImpl filterIteratorFactory;
    private final KpiDefinitionService kpiDefinitionService;

    @Override
    public boolean supports(@NonNull final Collection<KpiDefinition> delimiter) {
        return kpiDefinitionService.areNonScheduledSimple(delimiter);
    }

    @Override
    public Iterator<TableDatasets> iteratorDefaultFilter(final Collection<KpiDefinition> kpiDefinitions) {
        return filterIteratorFactory.createDefault(kpiDefinitions);
    }

    @Override
    public TableDatasets loadDatasetsWithCustomFilter(final Set<Filter> filters, final Collection<KpiDefinition> kpiDefinitions) {
        return postgresCustomFilterDataLoader.loadDatasetsWithCustomFilter(kpiDefinitions, filters);
    }
}
