/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.SparkKafkaHelper;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.SparkKafkaLoader;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.KafkaDefaultFilterDataLoaderIterator;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetCalculationHelper;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetCalculatorFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlFilterCreator;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoaderKafka implements DataLoader {
    private final OffsetCalculatorFacadeImpl offsetCalculator;
    private final OffsetCalculationHelper offsetCalculationHelper;
    private final SparkService sparkService;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final PostgresDataLoaderFacadeImpl postgresDataLoaderFacade;
    private final KpiDefinitionService kpiDefinitionService;
    private final SparkKafkaLoader sparkKafkaLoader;
    private final SparkKafkaHelper sparkKafkaHelper;

    private static String buildFilterExpression(final List<Filter> filters) {
        final SqlFilterCreator filterCreator = new SqlFilterCreator();
        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                filterCreator.and();
            }
            filterCreator.addFilter("(" + filters.get(i).getName() + ")");
        }
        return filterCreator.build();
    }

    @Override
    public boolean supports(@NonNull final Collection<KpiDefinition> delimiter) {
        return kpiDefinitionService.areScheduledSimple(delimiter);
    }

    @Override
    public Iterator<TableDatasets> iteratorDefaultFilter(final Collection<KpiDefinition> kpiDefinitions) {
        return new KafkaDefaultFilterDataLoaderIterator(
                postgresDataLoaderFacade,
                kpiDefinitionHelper,
                sparkService,
                offsetCalculator,
                offsetCalculationHelper,
                sparkKafkaLoader,
                kpiDefinitions,
                sparkKafkaHelper
        );
    }

    @Override
    public TableDatasets loadDatasetsWithCustomFilter(final Set<Filter> filters, final Collection<KpiDefinition> kpiDefinitions) {
        return TableDatasets.merge(
                postgresDataLoaderFacade.loadDataset(kpiDefinitions, ReadingOptions.empty()),
                loadSourceDatasetsWithCustomFilter(filters, kpiDefinitions)
        );
    }

    private TableDatasets loadSourceDatasetsWithCustomFilter(final Set<Filter> filters, final Collection<KpiDefinition> kpiDefinitions) {
        final TableDatasets tableDatasets = TableDatasets.of();

        kpiDefinitionHelper.groupByDataIdentifier(kpiDefinitions).forEach((identifier, definitions) -> {
            /* TODO: Custom filter KPI Calculation differs since not only aggregation period is involved but also the filters.
                     We need to adjust the cache for this to cover the filter as well  */
            final String startingOffset = "earliest";
            final String endingOffset = "latest";

            tableDatasets.put(sparkKafkaLoader.loadTableDataset(
                    identifier,
                    buildFilterExpression(new ArrayList<>(filters)),
                    kpiDefinitions,
                    startingOffset,
                    endingOffset
            ));
        });

        return tableDatasets;
    }

}
