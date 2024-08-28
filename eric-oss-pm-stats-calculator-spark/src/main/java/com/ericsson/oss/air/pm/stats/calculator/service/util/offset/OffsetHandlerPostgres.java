/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.pm.stats.common.util.LocalDateTimeTruncates.truncateToAggregationPeriod;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiCalculationPeriodUtils;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.AggregationPeriodWindow;
import com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetHandlerPostgres implements OffsetHandler {
    private final KpiDefinitionService kpiDefinitionService;

    private final KpiCalculationPeriodUtils kpiCalculationPeriodUtils;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final SparkService sparkService;

    @Qualifier("aggregationPeriodStartTimeStamp") private final AggregationTimestampCache aggregationTimestampStart;
    @Qualifier("aggregationPeriodEndTimeStamp") private final AggregationTimestampCache aggregationTimestampEnd;

    @Override
    public boolean supports(@NonNull final Set<KpiDefinition> kpiDefinitions) {
        return kpiDefinitionService.areNonScheduledSimple(kpiDefinitions);
    }

    @Override
    public void calculateOffsets(final Set<KpiDefinition> defaultFilterKpis) {
        kpiDefinitionHelper.groupByAggregationPeriod(defaultFilterKpis).forEach((aggregationPeriod, kpiDefinitions) -> {
             final AggregationPeriodWindow aggregationPeriodWindow = calculateAggregationPeriodWindow(
                    aggregationPeriod, kpiDefinitions, defaultFilterKpis
            );

            aggregationTimestampStart.put(aggregationPeriod, aggregationPeriodWindow.getStart());
            aggregationTimestampEnd.put(aggregationPeriod, aggregationPeriodWindow.getEnd());
        });
    }

    @Override
    public Timestamp getStartTimestamp(final Integer aggregationPeriodInMinutes) {
        return aggregationTimestampStart.get(aggregationPeriodInMinutes);
    }

    @Override
    public Timestamp getEndTimestamp(final Integer aggregationPeriodInMinutes) {
        return aggregationTimestampEnd.get(aggregationPeriodInMinutes);
    }

    @Override
    public List<KpiDefinition> getKpisForAggregationPeriodWithTimestampParameters(final Integer aggPeriodInMinutes,
                                                                                  final Collection<KpiDefinition> kpiDefinitions) {
        final List<KpiDefinition> kpisForAggregationPeriod;
        if (kpiDefinitionService.areScheduled(kpiDefinitions)) {
            if (!areValidTimestampParameters(aggPeriodInMinutes)) {
                return Collections.emptyList();
            }

            kpisForAggregationPeriod = applyTimestampParametersToCustomScheduledKpis(
                    kpiDefinitionHelper.filterByAggregationPeriod(aggPeriodInMinutes, kpiDefinitions),
                    aggPeriodInMinutes
            );
        } else {
            kpisForAggregationPeriod = kpiDefinitionHelper.filterByAggregationPeriod(
                    aggPeriodInMinutes,
                    kpiDefinitions
            );
        }

        if (kpisForAggregationPeriod.isEmpty()) {
            log.warn("No Filter KPI definitions found for aggregation period of {} minutes", aggPeriodInMinutes);
        }
        return kpisForAggregationPeriod;
    }

    @Override
    public void saveOffsets() {
        //  Kafka-like offsets are not saved for Postgres
    }

    private AggregationPeriodWindow calculateAggregationPeriodWindow(final Integer aggregationPeriod,
                                                                     final List<? extends KpiDefinition> kpiDefinitions,
                                                                     final Set<KpiDefinition> defaultFilterKpis) {
        if (sparkService.isOnDemand()) {
            final DatasourceTables datasourceTables = kpiDefinitionHelper.extractNonInMemoryDatasourceTables(kpiDefinitions);

            return AggregationPeriodWindow.of(
                    kpiCalculationPeriodUtils.getKpiCalculationStartTimeStampInUtc(aggregationPeriod, datasourceTables, defaultFilterKpis),
                    kpiCalculationPeriodUtils.getEndTimestampInUtc(datasourceTables, defaultFilterKpis)
            );
        }

        final AggregationPeriodWindow complexWindow = sparkService.getComplexAggregationPeriodWindow();
        if (aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT) {
            return complexWindow;
        }

        final LocalDateTime startTime = truncateToAggregationPeriod(complexWindow.getStart().toLocalDateTime(), aggregationPeriod);
        return AggregationPeriodWindow.of(Timestamp.valueOf(startTime), complexWindow.getEnd());
    }

    private boolean areValidTimestampParameters(final int aggPeriodInMinutes) {
        final Timestamp startTimestamp = aggregationTimestampStart.get(aggPeriodInMinutes);
        final Timestamp endTimestamp = aggregationTimestampEnd.get(aggPeriodInMinutes);
        if (startTimestamp == null || endTimestamp == null) {
            log.error("Start and/or End timestamp params for scheduled custom filters are null. " +
                    "Skipping calculation for aggregation period of {} minutes.", aggPeriodInMinutes);
            return false;
        }

        if (startTimestamp.after(endTimestamp)) {
            log.info("All source data has been included in previous calculations");
            return false;
        }

        return true;
    }

    private List<KpiDefinition> applyTimestampParametersToCustomScheduledKpis(final Collection<KpiDefinition> kpisForAggregationPeriod,
                                                                              final Integer aggPeriodInMinutes) {
        final Timestamp startTimestamp = aggregationTimestampStart.get(aggPeriodInMinutes);
        final Timestamp endTimestamp = aggregationTimestampEnd.get(aggPeriodInMinutes);
        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put("param.start_date_time", startTimestamp.toString());
        parameters.put("param.end_date_time", endTimestamp.toString());

        return new ArrayList<>(
                KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(
                        KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(parameters, kpisForAggregationPeriod)));
    }

    @Slf4j
    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    public static final class AggregationTimestampCache {
        @Delegate
        private final Map<Integer, Timestamp> cache = new ConcurrentHashMap<>();
    }
}
