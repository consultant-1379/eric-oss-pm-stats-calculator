/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static com.ericsson.oss.air.pm.stats.calculator.util.Timestamps.initialTimeStamp;
import static com.ericsson.oss.air.pm.stats.calculator.util.Timestamps.isInitialTimestamp;
import static com.ericsson.oss.air.pm.stats.calculator.util.Timestamps.isNotInitialTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestSourceDataRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DataSourceService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.util.Timestamps;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiCalculationPeriodUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S", Locale.UK).withZone(ZoneOffset.UTC);

    private final DatasourceRegistryHandlerImp datasourceRegistryHandlerImpl;
    private final TemporalHandlerImpl temporalHandler;
    private final SourceDataAvailability sourceDataAvailability;
    private final DataSourceService dataSourceService;
    private final SparkService sparkService;
    private final CalculationProperties calculationProperties;
    private final LatestSourceDataRepository latestSourceDataRepository;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    /**
     * Get the start timestamp for the KPI calculation in {@link String} format.
     *
     * @param aggregationPeriodInMinutes
     *            the current calculations aggregation period
     * @param datasourceTables
     *            {@link Map} of all source tables keyed by data source
     * @return the {@link Timestamp} to be used as a start time for the calculation
     * @throws KpiCalculatorException
     *             thrown if the database is unreachable
     */
    public Timestamp getKpiCalculationStartTimeStampInUtc(final int aggregationPeriodInMinutes,
                                                          final DatasourceTables datasourceTables,
                                                          final Set<KpiDefinition> kpiDefinitions) {
        final List<Timestamp> startTimestampList = new ArrayList<>(2);
        final List<Timestamp> timesForLookBackAndLastProcessedTime = new ArrayList<>(2);
        final List<Timestamp> utcTimeStampsForSource = new ArrayList<>(2);
        final Map<DatasourceType, List<Datasource>> datasourceByType = datasourceRegistryHandlerImpl.groupDatabaseExpressionTagsByType();
        final Set<Datasource> unavailableDataSources = sourceDataAvailability.getUnavailableDataSources(kpiDefinitions);

        datasourceTables.forEach((datasource, tables) -> tables.forEach(table -> {
            if (isSourceAvailable(table, unavailableDataSources, datasourceTables)
                && sourceRequiredForKpiDefinitions(table, kpiDefinitions)
                && sourceIsNotDimSource(datasourceByType, datasource)) {

                timesForLookBackAndLastProcessedTime.add(
                        getMaxTimeForLookBackAndLastProcessedTimeInUtcForCalculationPeriod(
                                table,
                                aggregationPeriodInMinutes,
                                datasourceTables,
                                kpiDefinitions
                        )
                );

                final Timestamp minimumTimestampForSource = dataSourceService.findMinUtcTimestamp(datasource.toDatabase(), table);
                if (isNotInitialTimestamp(minimumTimestampForSource)) {
                    utcTimeStampsForSource.add(minimumTimestampForSource);
                }
            }
        }));

        if (!timesForLookBackAndLastProcessedTime.isEmpty()) {
            startTimestampList.add(Collections.max(timesForLookBackAndLastProcessedTime));
        }
        if (!utcTimeStampsForSource.isEmpty()) {
            startTimestampList.add(Collections.min(utcTimeStampsForSource));
        }

        Timestamp maximumStartTimeStamp = initialTimeStamp();
        if (!startTimestampList.isEmpty()) {
            maximumStartTimeStamp = Collections.max(startTimestampList);
        }

        if (isInitialTimestamp(maximumStartTimeStamp)) {
            maximumStartTimeStamp = temporalHandler.getOffsetTimestamp();
        }
        return maximumStartTimeStamp;
    }

    private static boolean sourceIsNotDimSource(Map<DatasourceType, List<Datasource>> datasourceByType, Datasource datasource) {
        final List<Datasource> dimDatasources = datasourceByType.get(DatasourceType.DIM);
        if (dimDatasources == null) {
            return true;
        }
        return !dimDatasources.contains(datasource);
    }

    private static boolean isSourceAvailable(final Table table, final Set<Datasource> unavailableDatasources,
                            final DatasourceTables datasourceTables) {
        for (final Datasource unavailableDataSource : unavailableDatasources) {
            if (datasourceTables.containsTable(unavailableDataSource, table)) {
                return false;
            }
        }
        return true;
    }

    private boolean sourceRequiredForKpiDefinitions(final Table table, final Set<KpiDefinition> kpiDefinitions) {
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            for (final SourceTable kpiDefinitionSourceTable : kpiDefinitionHelper.getSourceTables(kpiDefinition)) {
                if (kpiDefinitionSourceTable.hasSameTable(table)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the maximum time of both the Max Look Back and Last Processed Time for source table for the aggregation period.
     *
     * @param table
     *         the source table
     * @param aggregationPeriodInMinutes
     *         the aggregation period of the current calculation
     * @param datasourceTables
     *         a {@link Map} of all the source tables to be used in the calculation keyed by datasource
     * @return start timestamp in utc
     */
    private Timestamp getMaxTimeForLookBackAndLastProcessedTimeInUtcForCalculationPeriod(final Table table,
                                                                                         final int aggregationPeriodInMinutes,
                                                                                         final DatasourceTables datasourceTables,
                                                                                         final Set<KpiDefinition> kpiDefinitions) {
        final Optional<LocalDateTime> lastTimeCollected = latestSourceDataRepository.findLastTimeCollected(
                table.getName(),
                aggregationPeriodInMinutes,
                sparkService.getExecutionGroup()
        );

        final Timestamp startTimeBasedOnLastProcessedTime = lastTimeCollected
                .map(Timestamp::valueOf)
                .map(timestamp -> new Timestamp(timestamp.getTime() + 10))
                .orElseGet(Timestamps::initialTimeStamp);

        final Timestamp maxLookBackStartTime = getStartTimeBasedOnMaxLookback(
                datasourceTables,
                kpiDefinitions
        );

        final List<Timestamp> startTimestampList = new ArrayList<>(3);
        startTimestampList.add(maxLookBackStartTime);
        startTimestampList.add(startTimeBasedOnLastProcessedTime);

        return Collections.max(startTimestampList);
    }

    /**
     * Get the end timestamp in utc.
     *
     * @param datasourceTables
     *            A {@link Map} of all source tables keyed by datasource
     * @return end timestamp in utc
     */
    public Timestamp getEndTimestampInUtc(final DatasourceTables datasourceTables,
                                          final Set<KpiDefinition> kpiDefinitions) {
        if (calculationProperties.isAllMaxLookBackPeriod()) {
            return getMostRecentlyAvailableSourceData(datasourceTables, kpiDefinitions);
        }
        return temporalHandler.getOffsetTimestamp();
    }

    private Timestamp getMostRecentlyAvailableSourceData(final DatasourceTables datasourceTables, final Set<KpiDefinition> kpiDefinitions) {
        final List<Timestamp> maxSourceData = new ArrayList<>();
        final Map<DatasourceType, List<Datasource>> datasourceByType = datasourceRegistryHandlerImpl.groupDatabaseExpressionTagsByType();
        final Set<Datasource> unavailableDataSources = sourceDataAvailability.getUnavailableDataSources(kpiDefinitions);

        //TODO iterate over the map, we already have the datasource, and then hardcode it lower down the stack
        datasourceTables.forEach((datasource, tables) -> tables.forEach(table -> {
            if (isSourceAvailable(table, unavailableDataSources, datasourceTables)
                && sourceRequiredForKpiDefinitions(table, kpiDefinitions)
                && sourceIsNotDimSource(datasourceByType, datasource)) {
                maxSourceData.add(dataSourceService.findMaxUtcTimestamp(datasource.toDatabase(), table));
            }
        }));

        Timestamp maxAvailableSourceData;
        final Timestamp offsetFromNow = temporalHandler.getOffsetTimestamp();
        if (maxSourceData.isEmpty()) {
            maxAvailableSourceData = offsetFromNow;
        } else {
            maxAvailableSourceData = Collections.max(maxSourceData);
            if (isInitialTimestamp(maxAvailableSourceData) || maxAvailableSourceData.after(offsetFromNow)) {
                maxAvailableSourceData = offsetFromNow;
            }
        }
        return maxAvailableSourceData;
    }

    private Timestamp getStartTimeBasedOnMaxLookback(final DatasourceTables datasourceTables,
                                                     final Set<KpiDefinition> kpiDefinitions) {
        if (calculationProperties.isAllMaxLookBackPeriod()) {
            return initialTimeStamp();
        }

        final Timestamp localStartTime = new Timestamp(
                getEndTimestampInUtc( datasourceTables, kpiDefinitions).toInstant()
                        .minus(calculationProperties.getMaxLookBackPeriod())
                        .plus(10, ChronoUnit.MILLIS).toEpochMilli());

        return Timestamp.valueOf(DATE_FORMATTER.format(localStartTime.toInstant()));
    }

}
