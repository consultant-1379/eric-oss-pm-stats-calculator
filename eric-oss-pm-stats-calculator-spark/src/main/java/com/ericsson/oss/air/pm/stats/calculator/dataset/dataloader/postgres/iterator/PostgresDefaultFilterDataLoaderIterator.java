/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresDefaultFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class PostgresDefaultFilterDataLoaderIterator implements Iterator<TableDatasets> {
    private final Collection<KpiDefinition> kpiDefinitions;
    private final Integer aggregationPeriodInMinutes;

    private final Queue<KpiCalculatorTimeSlot> timeSlots = new LinkedList<>();

    private final PostgresDefaultFilterDataLoaderImpl postgresDefaultFilterDataLoader;

    public PostgresDefaultFilterDataLoaderIterator(final Collection<KpiDefinition> kpiDefinitions,
                                                   @NonNull final CalculationTimeWindow calculationTimeWindow,
                                                   @NonNull final KpiDefinitionHelperImpl kpiDefinitionHelper,
                                                   final PostgresDefaultFilterDataLoaderImpl postgresDefaultFilterDataLoader,
                                                   final SourceDataAvailability sourceDataAvailability) {
        this.kpiDefinitions = Collections.unmodifiableCollection(kpiDefinitions);
        this.postgresDefaultFilterDataLoader = postgresDefaultFilterDataLoader;

        aggregationPeriodInMinutes = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);

        timeSlots.addAll(sourceDataAvailability.collectCalculableTimeSlots(calculationTimeWindow, kpiDefinitions, aggregationPeriodInMinutes));

        if (timeSlots.isEmpty()) {
            log.info("All source data has been included in previous calculations");
        } else {
            log.info("Calculating all defined KPIs between {} and {} for aggregation period: {}",
                     calculationTimeWindow.getStart(),
                     calculationTimeWindow.getEnd(),
                     aggregationPeriodInMinutes);
            log.info("KPI calculation to be executed for '{}' units of source data for aggregation period: '{}'", timeSlots.size(), aggregationPeriodInMinutes);
        }
    }

    @Override
    public boolean hasNext() {
        return CollectionUtils.isNotEmpty(timeSlots);
    }

    @Override
    public TableDatasets next() {
        if (CollectionUtils.isEmpty(timeSlots)) {
            throw new NoSuchElementException();
        }

        final KpiCalculatorTimeSlot kpiCalculatorTimeSlot = timeSlots.poll();
        log.info("Starting KPI calculation between {} and {}", kpiCalculatorTimeSlot.getStartTimestamp(), kpiCalculatorTimeSlot.getEndTimestamp());
        try {
            return postgresDefaultFilterDataLoader.loadDatasetsWithDefaultFilter(kpiDefinitions, kpiCalculatorTimeSlot);
        } catch (final Exception e) {
            log.error("Error loading datasets for aggregation period: {}", aggregationPeriodInMinutes);
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, e);
        }
    }

}
