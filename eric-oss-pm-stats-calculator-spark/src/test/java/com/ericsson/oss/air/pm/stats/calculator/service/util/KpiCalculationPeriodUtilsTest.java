/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.kpiDefinitionHelper;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils._KpiDefinitions.getKpiDefinitionsForAggregationPeriod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties.KafkaProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestSourceDataRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DataSourceService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.test_utils.KpiDefinitionFileRetriever;
import com.ericsson.oss.air.pm.stats.calculator.test_utils.KpiDefinitionRetrievalTestUtils;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KpiCalculationPeriodUtils}.
 */
class KpiCalculationPeriodUtilsTest {

    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S", Locale.UK).withZone(ZoneOffset.UTC);
    static final Set<KpiDefinition> KPI_DEFINITIONS = KpiDefinitionRetrievalTestUtils.retrieveKpiDefinitions(new KpiDefinitionFileRetriever());
    static KpiCalculationPeriodUtils objectUnderTest;
    static final Set<Datasource> unavailableSources = new HashSet<>(0);
    static final DatasourceTables allSourceTablesByDatasource = DatasourceTables.newInstance(2);
    static Map<DatasourceType, List<Datasource>> datasourceByType;
    static final Datasource DIM_DS_0 = Datasource.of("dim_ds_0");
    static final Datasource FACT_DS_1 = Datasource.of("fact_ds_1");
    static final Datasource FACT_DS_2 = Datasource.of("fact_ds_2");
    static final Table FACT_TABLE_0 = Table.of("fact_table_0");
    static final Table FACT_TABLE_1 = Table.of("fact_table_1");
    static final Table FACT_TABLE_2 = Table.of("fact_table_2");
    static final Table FACT_TABLE_3 = Table.of("fact_table_3");
    static final String EXECUTION_GROUP = "0 */5 * ? * *";

    SparkService sparkServiceMock = mock(SparkService.class);
    DatasourceRegistryHandlerImp datasourceRegistryHandlerImpMock = mock(DatasourceRegistryHandlerImp.class);
    SourceDataAvailability sourceDataAvailabilityMock = mock(SourceDataAvailability.class);
    LatestSourceDataRepository latestSourceDataRepositoryMock = mock(LatestSourceDataRepository.class);
    DataSourceService dataSourceServiceMock = mock(DataSourceService.class);

    @BeforeAll
    static void setUp() {
        final Set<Table> factDs1 = new HashSet<>(2);
        final Set<Table> factDs2 = new HashSet<>(2);
        factDs1.add(FACT_TABLE_0);
        factDs1.add(FACT_TABLE_1);
        factDs2.add(FACT_TABLE_2);
        factDs2.add(FACT_TABLE_3);
        allSourceTablesByDatasource.put(FACT_DS_1, factDs1);
        allSourceTablesByDatasource.put(FACT_DS_2, factDs2);
        datasourceByType = new HashMap<>();
        datasourceByType.put(DatasourceType.FACT,  Arrays.asList(FACT_DS_1, FACT_DS_2));
        datasourceByType.put(DatasourceType.DIM, Collections.singletonList(DIM_DS_0));
    }

    @BeforeEach
    void init() {
        final CalculationProperties calculationProperties = new CalculationProperties(
                Duration.ofMinutes(15),
                Duration.ofMinutes(30),
                Duration.ofDays(1),
                StringUtils.EMPTY,
                false,
                4,
                StringUtils.EMPTY,
                new KafkaProperties(50, "server")
        );

        objectUnderTest = new KpiCalculationPeriodUtils(
                datasourceRegistryHandlerImpMock,
                new TemporalHandlerImpl(calculationProperties),
                sourceDataAvailabilityMock,
                dataSourceServiceMock,
                sparkServiceMock,
                calculationProperties,
                latestSourceDataRepositoryMock,
                kpiDefinitionHelper(new KpiDefinitionHierarchy(KPI_DEFINITIONS))
        );
    }

    @Test
    void whenMaxLookbackIsMostRecent_andMinForSourceDataIsLeastRecent_thenMaxLookbackIsReturnedAsStartTime() {
        when(dataSourceServiceMock.findMinUtcTimestamp(any(Database.class), any(Table.class))).thenReturn(new Timestamp(1_000_000_000));
        when(latestSourceDataRepositoryMock.findLastTimeCollected(anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(new Timestamp(1_100_000_000).toLocalDateTime()));

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "1440");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                1_440,
                allSourceTablesByDatasource,
                kpiDefinitions
        );

        final Instant nowTruncatedToMinutes = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        final long incrementToMillis = nowTruncatedToMinutes.toEpochMilli() % TimeUnit.MINUTES.toMillis(15);
        final Timestamp expectedStartTimestamp = new Timestamp(nowTruncatedToMinutes.minus(30, ChronoUnit.MINUTES)
                .minusMillis(incrementToMillis).minus(1, ChronoUnit.DAYS).toEpochMilli());

        final String expectedStartTime = DATE_FORMATTER.format(expectedStartTimestamp.toInstant());

        assertEquals(expectedStartTime, startTime.toString());
    }

    @Test
    void whenMaxLookbackIsMostRecent_andLastProcessedTimeIsLeastRecent_thenMaxLookbackIsReturnedAsStartTime() {
        when(dataSourceServiceMock.findMinUtcTimestamp(any(Database.class), any(Table.class))).thenReturn(new Timestamp(1_100_000_000));
        when(latestSourceDataRepositoryMock.findLastTimeCollected(anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(new Timestamp(1_000_000_000).toLocalDateTime()));

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "1440");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                1_440,
                allSourceTablesByDatasource,
                kpiDefinitions
        );

        final Instant nowTruncatedToMinutes = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        final long incrementToMillis = nowTruncatedToMinutes.toEpochMilli() % TimeUnit.MINUTES.toMillis(15);
        final Timestamp expectedStartTimestamp = new Timestamp(nowTruncatedToMinutes.minus(30, ChronoUnit.MINUTES)
                .minusMillis(incrementToMillis).minus(1, ChronoUnit.DAYS).toEpochMilli());

        final String expectedStartTime = DATE_FORMATTER.format(expectedStartTimestamp.toInstant());

        assertEquals(expectedStartTime, startTime.toString());
    }

    @Test
    void whenMinForSourceDataIsMostRecent_andMaxLookbackIsLeastRecent_thenMinForSourceDataIsReturnedAsStartTime() {
        when(dataSourceServiceMock.findMinUtcTimestamp(any(Database.class), any(Table.class)))
                .thenReturn(new Timestamp(Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).toEpochMilli()));
        when(latestSourceDataRepositoryMock.findLastTimeCollected(anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(
                        new Timestamp(
                                Instant.now().truncatedTo(ChronoUnit.HOURS).minus(6, ChronoUnit.HOURS).toEpochMilli()
                        ).toLocalDateTime()));

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "1440");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                1_440,
                allSourceTablesByDatasource,
                kpiDefinitions
        );
        final Timestamp expectedStartTime = new Timestamp(
                Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).toEpochMilli());

        assertEquals(expectedStartTime, startTime);
    }

    @Test
    void whenMinForSourceDataIsMostRecent_andLastProcessedTimeIsLeastRecent_thenMinForSourceDataIsReturnedAsStartTime() {
        when(dataSourceServiceMock.findMinUtcTimestamp(any(Database.class), any(Table.class)))
                .thenReturn(new Timestamp(Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).toEpochMilli()));
        when(latestSourceDataRepositoryMock.findLastTimeCollected(anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(new Timestamp(1_000_000_000).toLocalDateTime()));

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "1440");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                1_440,
                allSourceTablesByDatasource,
                kpiDefinitions
        );

        final Timestamp expectedStartTime = new Timestamp(
                Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).toEpochMilli());

        assertEquals(expectedStartTime, startTime);
    }

    @Test
    void whenLastProcessedTimeIsMostRecent_andMaxLookbackIsLeastRecent_thenLastProcessedTimeIsReturnedAsStartTime() {
        when(dataSourceServiceMock.findMinUtcTimestamp(any(Database.class), any(Table.class)))
                .thenReturn(new Timestamp(Instant.now().truncatedTo(ChronoUnit.HOURS).minus(6, ChronoUnit.HOURS).toEpochMilli()));
        when(latestSourceDataRepositoryMock.findLastTimeCollected(anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(
                        new Timestamp(
                                Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).toEpochMilli()
                        ).toLocalDateTime()));

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "1440");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                1_440,
                allSourceTablesByDatasource,
                kpiDefinitions
        );
        final Timestamp expectedStartTime = new Timestamp(
                (Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).plus(10, ChronoUnit.MILLIS).toEpochMilli()));

        assertEquals(expectedStartTime, startTime);
    }

    @Test
    void whenLastProcessedTimeIsMostRecent_andMinForSourceDataIsLeastRecent_thenLastProcessedTimeIsReturnedAsStartTime() {
        when(dataSourceServiceMock.findMinUtcTimestamp(any(Database.class), any(Table.class))).thenReturn(new Timestamp(1_000_000_000));
        when(latestSourceDataRepositoryMock.findLastTimeCollected(anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(
                        new Timestamp(
                                Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).toEpochMilli()
                        ).toLocalDateTime()
                ));

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "1440");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                1_440,
                allSourceTablesByDatasource,
                kpiDefinitions
        );
        final Timestamp expectedStartTime = new Timestamp(
                (Instant.now().truncatedTo(ChronoUnit.HOURS).minus(2, ChronoUnit.HOURS).plus(10, ChronoUnit.MILLIS).toEpochMilli()));

        assertEquals(expectedStartTime, startTime);
    }

    @Test
    void whenAllSourcesNotApplicableForAggregationPeriod_thenMostRecentValidSourceIsUsedAsStartTime() {
        final CalculationProperties calculationProperties = new CalculationProperties(
                Duration.ofMinutes(15),
                Duration.ofMinutes(30),
                Duration.ofDays(1),
                StringUtils.EMPTY,
                false,
                4,
                StringUtils.EMPTY,
                new KafkaProperties(50, "server")
        );

        objectUnderTest = new KpiCalculationPeriodUtils(
                datasourceRegistryHandlerImpMock,
                new TemporalHandlerImpl(calculationProperties),
                sourceDataAvailabilityMock,
                dataSourceServiceMock,
                sparkServiceMock,
                calculationProperties,
                latestSourceDataRepositoryMock,
                kpiDefinitionHelper(new KpiDefinitionHierarchy(KPI_DEFINITIONS))
        );

        final Timestamp factDs1StartTime = new Timestamp(Instant.now().truncatedTo(ChronoUnit.HOURS).minus(10, ChronoUnit.HOURS).toEpochMilli());
        final Timestamp factDs2StartTime = new Timestamp(Instant.now().truncatedTo(ChronoUnit.HOURS).minus(5, ChronoUnit.HOURS).toEpochMilli());

        Database factds1 = new Database("factds1");
        Database factds2 = new Database("factds2");

        when(dataSourceServiceMock.findMinUtcTimestamp(factds1, FACT_TABLE_0)).thenReturn(factDs1StartTime);
        when(dataSourceServiceMock.findMinUtcTimestamp(factds1, FACT_TABLE_1)).thenReturn(factDs1StartTime);
        when(dataSourceServiceMock.findMinUtcTimestamp(factds2, FACT_TABLE_2)).thenReturn(factDs2StartTime);
        when(dataSourceServiceMock.findMinUtcTimestamp(factds2, FACT_TABLE_3)).thenReturn(factDs2StartTime);

        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitionsForAggregationPeriod(KPI_DEFINITIONS, "60");

        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(DatabaseProperties.newInstance());
        when(datasourceRegistryHandlerImpMock.groupDatabaseExpressionTagsByType()).thenReturn(datasourceByType);
        when(sourceDataAvailabilityMock.getUnavailableDataSources(kpiDefinitions)).thenReturn(unavailableSources);

        final Timestamp startTime = objectUnderTest.getKpiCalculationStartTimeStampInUtc(
                60,
                allSourceTablesByDatasource,
                kpiDefinitions
        );

        assertEquals(factDs1StartTime, startTime);
    }
}
