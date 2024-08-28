/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.factory;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresDefaultFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.PostgresDefaultFilterDataLoaderIterator;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FilterIteratorFactoryImpl {
    private final SourceDataAvailability sourceDataAvailability;
    private final OffsetHandler offsetHandler;
    private final PostgresDefaultFilterDataLoaderImpl postgresDefaultFilterDataLoader;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    public FilterIteratorFactoryImpl(@NonNull final OffsetHandlerRegistryFacade offsetHandlerRegistryFacade,
                                     final SourceDataAvailability sourceDataAvailability,
                                     final PostgresDefaultFilterDataLoaderImpl postgresDefaultFilterDataLoader,
                                     final KpiDefinitionHelperImpl kpiDefinitionHelper) {
        offsetHandler = offsetHandlerRegistryFacade.offsetHandler();
        this.sourceDataAvailability = sourceDataAvailability;
        this.postgresDefaultFilterDataLoader = postgresDefaultFilterDataLoader;
        this.kpiDefinitionHelper = kpiDefinitionHelper;
    }

    public PostgresDefaultFilterDataLoaderIterator createDefault(final Collection<KpiDefinition> kpiDefinitions) {
        final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);

        return new PostgresDefaultFilterDataLoaderIterator(
                kpiDefinitions, //  TODO: We only depend on this, try to later inject Spring managed beans
                offsetHandler.getCalculationTimeWindow(aggregationPeriod),
                kpiDefinitionHelper,
                postgresDefaultFilterDataLoader,
                sourceDataAvailability
        );
    }
}
