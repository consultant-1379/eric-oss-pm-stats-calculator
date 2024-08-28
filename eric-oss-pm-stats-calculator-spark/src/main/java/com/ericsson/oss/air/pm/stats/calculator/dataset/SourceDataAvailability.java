/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.api.DataSourceRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;

import io.vavr.Predicates;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
//  TODO: Add cache and expose interface
public class SourceDataAvailability {
    private final DataSourceRepository dataSourceRepository;
    private final DatasourceRegistry datasourceRegistry;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final SparkService sparkService;

    /**
     * Collects all {@link KpiCalculatorTimeSlot} which are calculable.
     * <br>
     * A {@link KpiCalculatorTimeSlot} is calculable if:
     * <ol>
     *     <li>Timeslot is not excluded by the excluded hour constraint.</li>
     *     <li>Timeslot contains data to calculate.</li>
     * </ol>
     *
     * @param calculationTimeWindow
     *         {@link CalculationTimeWindow} containing start and end timestamp for the calculation.
     * @param kpiDefinitions
     *         {@link Collection} of {@link KpiDefinition} to check for available data.
     * @return {@link Queue} of {@link KpiCalculatorTimeSlot} that can be calculated.
     */
    public Queue<KpiCalculatorTimeSlot> collectCalculableTimeSlots(@NonNull final CalculationTimeWindow calculationTimeWindow,
                                                                   final Collection<KpiDefinition> kpiDefinitions,
                                                                   final int aggregationPeriod) {
        return calculationTimeWindow
                .calculateTimeSlots(aggregationPeriod)
                .stream()
                .filter(kpiCalculatorTimeSlot -> isDataNotPresent(kpiCalculatorTimeSlot, kpiDefinitions))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public DatasourceTables availableDataSources(final Collection<? extends KpiDefinition> kpiDefinitions) {
        final Set<Datasource> unavailableDataSources = getUnavailableDataSources(kpiDefinitions);
        return kpiDefinitionHelper.extractNonInMemoryDatasourceTables(kpiDefinitions).filterAvailableDataSources(unavailableDataSources);
    }

    public Set<Datasource> getUnavailableDataSources(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        final Set<Datasource> dataSources = kpiDefinitionHelper.extractNonInMemoryDataSources(kpiDefinitions);

        return dataSources.stream()
                          .filter(datasourceRegistry::containsDatasource)
                          .filter(Predicates.not(dataSourceRepository::isAvailable))
                          .collect(Collectors.toSet());
    }

    /**
     * This method checks if source has data in specific interval for all given source tables.
     *
     * @param startTimestamp
     *         interval start time
     * @param endTimestamp
     *         interval end time
     * @param datasourceTables
     *         {@link Map} of source tables by data source
     * @param kpiDefinitions
     *         {@link Collection} of {@link KpiDefinition}s to check if their data soruces are available ot not
     * @return returns true if source has data in specific interval else false is returned
     */
    public boolean isDataPresentForAnyDatasource(final Timestamp startTimestamp,
                                                 final Timestamp endTimestamp,
                                                 final DatasourceTables datasourceTables,
                                                 final Collection<KpiDefinition> kpiDefinitions) {
        for (final Entry<Datasource, Set<Table>> entry : datasourceTables.entrySet()) {
            final Datasource datasource = entry.getKey();
            final Set<Table> tables = entry.getValue();

            if (isFactDataSource(datasource)) {
                final Set<Datasource> unavailableDataSources = getUnavailableDataSources(kpiDefinitions);  //  TODO: Cache this later on

                if (!unavailableDataSources.contains(datasource)) {
                    for (final Table table : tables) {
                        final boolean doesTableContainData = dataSourceRepository.doesTableContainData(
                                datasourceRegistry.getJdbcDatasource(datasource),
                                table.getName(),
                                startTimestamp.toLocalDateTime(),
                                endTimestamp.toLocalDateTime()
                        );

                        if (doesTableContainData) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isDataNotPresent(@NonNull final KpiCalculatorTimeSlot kpiCalculatorTimeSlot,
                                     final Collection<KpiDefinition> kpiDefinitions) {
        if (!isDataPresentForAnyDatasource(
                kpiCalculatorTimeSlot.getStartTimestamp(),
                kpiCalculatorTimeSlot.getEndTimestamp(),
                kpiDefinitionHelper.extractNonInMemoryDatasourceTables(kpiDefinitions),
                kpiDefinitions)) {
            log.warn("No data exists in FACT source tables between {} and {}",
                     kpiCalculatorTimeSlot.getStartTimestamp(),
                     kpiCalculatorTimeSlot.getEndTimestamp());
            return false;
        }
        return true;
    }

    private boolean isFactDataSource(@NonNull final Datasource datasource) {
        //  TODO: We consider unknown datasource to Fact Data Source?
        return datasourceRegistry.isFact(datasource, true);
    }

}
