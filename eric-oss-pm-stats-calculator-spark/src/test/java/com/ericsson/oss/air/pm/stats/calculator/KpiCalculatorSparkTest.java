/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.kpiDefinitionHelper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiRetrievalException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.registry.DataLoaderRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.DataSourceRepositoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.CalculatorHandlerRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.CalculationExecutorFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.DatasourceRegistryHandlerImp;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiCalculationPeriodUtils;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerPostgres;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiDefinitionFileRetriever;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;
import com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils;

import lombok.SneakyThrows;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

// TODO: this class needs to be refactored
@ExtendWith(MockitoExtension.class)
class KpiCalculatorSparkTest {

    static final String JDBC_URL = "jdbcUrl";
    static final Properties KPI_JDBC_PROPERTIES = new Properties();

    @Mock CalculatorHandlerRegistryFacadeImpl calculatorHandlerRegistryFacadeMock;
    @Mock KpiCalculationPeriodUtils kpiCalculationPeriodUtils;
    @Mock OffsetHandlerPostgres offsetHandlerPostgres;
    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock OffsetHandlerRegistryFacade offsetHandlerRegistryFacadeMock;
    @Mock DataLoaderRegistryFacadeImpl dataLoaderRegistryFacadeMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;

    KpiCalculatorSpark objectUnderTest;

    static final List<KpiDefinition> testKpis = new ArrayList<>();

    @BeforeAll
    static void setUp() throws KpiRetrievalException {
        populateTestKpis();
    }

    static void populateTestKpis() throws KpiRetrievalException {
        final KpiDefinitionFileRetriever kpiDefinitionRetriever = new KpiDefinitionFileRetriever();
        testKpis.addAll(kpiDefinitionRetriever.retrieveAllKpiDefinitions());

        final KpiDefinition newKpi1 = kpiDefinitionRetriever.retrieveNamedKpiDefinitions(newHashSet("rolling_sum_integer_1440"))
                .iterator().next();
        newKpi1.setName("datasource_not_available_default_filter_kpi");
        newKpi1.setExpression("SUM(fact_table_0.integerColumn0) FROM no_fact_ds_1://fact_table_0");
        testKpis.add(newKpi1);

        final KpiDefinition newKpi2 = kpiDefinitionRetriever.retrieveNamedKpiDefinitions(newHashSet("sum_integer_60"))
                .iterator().next();
        newKpi2.setName("sum_integer_60_from_yesterday_scheduled");
        newKpi2.setExecutionGroup("0 30 0/1 * * ? *");
        newKpi2.setExpression("FIRST(cell_guid.sum_integer_60) FROM kpi_db://kpi_cell_guid_60 JOIN kpi_db://kpi_cell_guid_60 as cell_guid " +
                "ON kpi_cell_guid_60.agg_column_0 = cell_guid.agg_column_0 AND cell_guid.local_timestamp = (kpi_cell_guid_60.local_timestamp - interval 1 day) " +
                "AND cell_guid.sum_integer_60 IS NOT NULL");
        newKpi2.getAggregationElements().clear();
        newKpi2.getAggregationElements().add("cell_guid.agg_column_0 as agg_column_0");
        newKpi2.setFilter(Collections.singletonList(new Filter("kpi_db://kpi_cell_guid_60.aggregation_begin_time BETWEEN " +
                "(date_trunc(''hour'', TO_TIMESTAMP(''${param.start_date_time}'')) - interval 1 day) and date_trunc(''hour'', TO_TIMESTAMP(''${param.end_date_time}''))")));
        testKpis.add(newKpi2);

        final KpiDefinition newKpi3 = kpiDefinitionRetriever.retrieveNamedKpiDefinitions(newHashSet("sum_integer_60"))
                .iterator().next();
        newKpi3.setName("sum_integer_60_scheduled");
        newKpi3.setExecutionGroup("0 30 0/1 * * ? *");
        testKpis.add(newKpi3);

        final KpiDefinition newKpi4 = kpiDefinitionRetriever.retrieveNamedKpiDefinitions(newHashSet("sum_integer_1440_simple"))
            .iterator().next();
        newKpi4.setName("sum_integer_1440_simple_filter");
        newKpi4.getAggregationElements().clear();
        newKpi4.getAggregationElements().add("SampleCellFDD_1.nodeFDN");
        newKpi4.setExpression("SUM(SampleCellFDD_1.pmCounters.exampleCounter3.counterValue)");
        newKpi4.setFilter(Collections.singletonList(new Filter("SampleCellFDD_1.nodeFDN = 'node1'")));
        testKpis.add(newKpi4);
    }

    void createKpiCalculatorSpark(final Set<String> kpiNames) {
        Set<KpiDefinition> kpiDefinitions = getKpis(kpiNames);

        final KpiDefinitionHierarchy kpiDefinitionHierarchy = new KpiDefinitionHierarchy(kpiDefinitions);

        final KpiDefinitionHelperImpl kpiDefinitionHelper = kpiDefinitionHelper(kpiDefinitionHierarchy);

        final SourceDataAvailability sourceDataAvailability = new SourceDataAvailability(
                new DataSourceRepositoryImpl(
                        DatasourceRegistry.getInstance(),
                        sparkServiceMock
                ),
                DatasourceRegistry.getInstance(),
                kpiDefinitionHelper,
                sparkServiceMock
        );

        final CalculationExecutorFacadeImpl calculationExecutor = new CalculationExecutorFacadeImpl(
                sourceDataAvailability,
                sparkServiceMock,
                kpiDefinitionServiceMock
        );

        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);

        lenient().when(offsetHandlerRegistryFacadeMock.offsetHandler()).thenReturn(offsetHandlerPostgres);

        //  TODO: Mock this later on once all responsibilities are distributed
        final DatasourceRegistryHandlerImp datasourceRegistryHandlerImp = new DatasourceRegistryHandlerImp(
                sparkServiceMock,
                DatasourceRegistry.getInstance(),
                kpiDefinitionHelperMock
        );

        objectUnderTest = Mockito.spy(
                new KpiCalculatorSpark(
                        kpiDefinitionServiceMock,
                        kpiDefinitionHierarchy,
                        datasourceRegistryHandlerImp,
                        kpiDefinitionHelper,
                        calculationExecutor,
                        calculatorHandlerRegistryFacadeMock,
                        offsetHandlerRegistryFacadeMock,
                        dataLoaderRegistryFacadeMock
                )
        );

        populateDatasourceRegistry();
        mockKpiCalculatorDefaultFilterCreation();
        mockKpiCalculatorCustomFilterCreation();
    }

    void populateDatasourceRegistry() {
        final JdbcDatasource datasource1 = new JdbcDatasource("jdbc://fact_ds_1", getProperties("username", "password", DatasourceType.FACT));
        DatasourceRegistry.getInstance().addDatasource(Datasource.of("fact_ds_1"), datasource1);
        final JdbcDatasource datasource2 = new JdbcDatasource("jdbc://fact_ds_2", getProperties("username", "password", DatasourceType.FACT));
        DatasourceRegistry.getInstance().addDatasource(Datasource.of("fact_ds_2"), datasource2);
        final JdbcDatasource datasource5 = new JdbcDatasource("jdbc://dim_ds_0", getProperties("username", "password", DatasourceType.DIM));
        DatasourceRegistry.getInstance().addDatasource(Datasource.of("dim_ds_0"), datasource5);

        //  TODO: Mock this later to no pollute the registry
        DatasourceRegistry.getInstance().addDatasource(
                Datasource.KPI_DB,
                new JdbcDatasource(
                        JDBC_URL,
                        KPI_JDBC_PROPERTIES
                )
        );
    }

    @SneakyThrows
    void mockKpiCalculatorCustomFilterCreation() {
        final KpiCalculator kpiCalculatorCustomFilterMock = mock(KpiCalculatorCustomFilter.class);
        final TableDatasets tableDatasetsMock = mock(TableDatasets.class);
        lenient().when(calculatorHandlerRegistryFacadeMock.customCalculator(any())).thenReturn(kpiCalculatorCustomFilterMock);
        lenient().when(kpiCalculatorCustomFilterMock.calculate()).thenReturn(tableDatasetsMock);
    }

    @SneakyThrows
    void mockKpiCalculatorDefaultFilterCreation() {
        final KpiCalculator kpiCalculatorDefaultFilterMock = mock(KpiCalculatorDefaultFilter.class);
        final TableDatasets tableDatasetsMock = mock(TableDatasets.class);
        lenient().when(calculatorHandlerRegistryFacadeMock.defaultCalculator(any())).thenReturn(kpiCalculatorDefaultFilterMock);
        lenient().when(kpiCalculatorDefaultFilterMock.calculate()).thenReturn(tableDatasetsMock);
    }

    @Test
    void whenNoKpiNamesPassedInCalculatorCreation_thenNoDataLoaded_andNoKpiCalculated() {
        final Set<String> kpiNames = newHashSet();

        createKpiCalculatorSpark(kpiNames);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(0);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(0);
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(0);
    }

    @Test
    void whenTwoDefaultFilterKpis_andOneKpiDatasourceIsNotRegistered_thenNoDataLoaded_andNoKpiCalculated() {
        final Set<String> kpiNames = newHashSet("rolling_sum_integer_1440", "datasource_not_available_default_filter_kpi");

        when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(getKpis(kpiNames))).thenReturn(
                Sets.newLinkedHashSet(Datasource.of("no_fact_ds_1")));

        createKpiCalculatorSpark(kpiNames);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(0);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(0);
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(0);
    }

    @Test
    void whenTwoCustomFilterKpis_andOneKpiDatasourceIsNotRegistered_thenNoDataLoaded_andNoKpiCalculated() {
        final Set<String> kpiNames = newHashSet( "datasource_not_available_custom_filter_kpi");

        createKpiCalculatorSpark(kpiNames);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(0);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(0);
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(0);
    }

    @Test
    void whenOneDefaultFilterKpi_andOneTimeSlot_thenDataLoadedOnce_andKpiIsCalculatedOnce() {
        final Set<String> kpi1 = newHashSet("rolling_sum_integer_1440");

        createKpiCalculatorSpark(kpi1);

        //Mock
        final Iterator<TableDatasets> iterator = List.of(TableDatasets.of()).iterator();
        when(dataLoaderRegistryFacadeMock.defaultFilterIterator(any())).thenReturn(iterator);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verify(dataLoaderRegistryFacadeMock).defaultFilterIterator(getDefinitionVerifier(kpi1));
        verify(calculatorHandlerRegistryFacadeMock).defaultCalculator(getDefinitionVerifier(kpi1));

        //verify max calls
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(1);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(1);
    }

    @Test
    void whenOneDefaultFilterKpi_andTwoTimeSlots_thenDataLoadedTwice_andKpiIsCalculatedTwice() {
        final Set<String> kpiNames = newHashSet("rolling_sum_integer_1440");

        createKpiCalculatorSpark(kpiNames);

        //Mock
        final Iterator<TableDatasets> iterator = List.of(TableDatasets.of()).iterator();
        when(dataLoaderRegistryFacadeMock.defaultFilterIterator(any())).thenReturn(iterator);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verify(dataLoaderRegistryFacadeMock).defaultFilterIterator(getDefinitionVerifier(kpiNames));
        verify(calculatorHandlerRegistryFacadeMock).defaultCalculator(getDefinitionVerifier(kpiNames));

        //verify max calls
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(1);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(1);
    }

    @Test
    void whenTwoDefaultFilterKpis_andDifferentAlias_andSameAggregationPeriod_thenDataLoadedForBothKpisTogether_andBothKpisCalculatedTogether() {
        final Set<String> kpiNames = newHashSet("rolling_sum_integer_1440", "executionid_sum_integer_1440");

        createKpiCalculatorSpark(kpiNames);

        //Mock
        final Iterator<TableDatasets> iterator = List.of(TableDatasets.of()).iterator();
        when(dataLoaderRegistryFacadeMock.defaultFilterIterator(any())).thenReturn(iterator);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verify(dataLoaderRegistryFacadeMock).defaultFilterIterator(getDefinitionVerifier(kpiNames));
        verify(calculatorHandlerRegistryFacadeMock).defaultCalculator(getDefinitionVerifier(kpiNames));

        //verify max calls
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(1);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(1);
    }

    @Test
    void whenTwoDefaultFilterKpis_andDifferentAggregationPeriod_thenDataLoadedOnceForEachAggregationPeriodKpisSeparately_andKpisCalculatedSeparately() {
        final Set<String> kpi1 = newHashSet("rolling_sum_integer_1440");
        final Set<String> kpi2 = newHashSet("sum_integer_60");

        createKpiCalculatorSpark(newHashSet(kpi1, kpi2));

        //Mock
        final Iterator<TableDatasets> iterator = List.of(TableDatasets.of()).iterator();
        when(dataLoaderRegistryFacadeMock.defaultFilterIterator(any())).thenReturn(iterator);


        //invoke calculation
        objectUnderTest.calculate();

        //verify
        //first aggregation period
        verify(dataLoaderRegistryFacadeMock).defaultFilterIterator(getDefinitionVerifier(kpi1));
        verify(calculatorHandlerRegistryFacadeMock).defaultCalculator(getDefinitionVerifier(kpi1));

        //second aggregation period
        verify(dataLoaderRegistryFacadeMock).defaultFilterIterator(getDefinitionVerifier(kpi2));
        verify(calculatorHandlerRegistryFacadeMock).defaultCalculator(getDefinitionVerifier(kpi2));

        //verify max calls
        verifyMaximumTimesDataLoaderCreatedForDefaultFilter(2);
        verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(2);
    }

    @Test
    void whenOneCustomFilterKpis_thenDataLoadedOnlyOnceWithCorrectFilters_andKpiIsCalculatedOnce() {
        final Set<String> kpiNames = newHashSet("first_integer_1440_join_kpidb_filter");

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("param.date_for_filter", "1970-01-01 01:30:30.250");

        createKpiCalculatorSpark(kpiNames);

        //Mock
        when(offsetHandlerPostgres.getKpisForAggregationPeriodWithTimestampParameters( anyInt(), any())).thenReturn(new ArrayList<>(getKpis(kpiNames)));
        when(dataLoaderRegistryFacadeMock.customFilterDatasets(any(), anySet())).thenReturn(TableDatasets.of());

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verify(dataLoaderRegistryFacadeMock).customFilterDatasets(any(), anySet());
        verify(calculatorHandlerRegistryFacadeMock).customCalculator(getDefinitionVerifierSet(kpiNames));

        //verify max calls
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(1);
    }

    @Test
    void whenOneSimpleScheduledCustomFilterKpis_thenDataLoadedOnlyOnceWithCorrectFilters_andKpiIsCalculatedOnce() {
        final Set<String> kpiNames = newHashSet("sum_integer_1440_simple_filter");

        createKpiCalculatorSpark(kpiNames);

        //Mock
        when(dataLoaderRegistryFacadeMock.customFilterDatasets(any(), anySet())).thenReturn(TableDatasets.of());
        when(offsetHandlerPostgres.getKpisForAggregationPeriodWithTimestampParameters( anyInt(), any())).thenReturn(new ArrayList<>(getKpis(kpiNames)));
        when(kpiDefinitionServiceMock.areScheduledSimple(any())).thenReturn(true);

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        verify(calculatorHandlerRegistryFacadeMock).customCalculator(getDefinitionVerifierSet(kpiNames));

        //verify max calls
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(1);
    }

    @Test
    void whenTwoCustomFilterKpis_andBothWithDifferentAggregationPeriods_thenDataLoadedOnceForEveryAggregationPeriodWith_andBothKpisCalculatedSeparately() {
        final Set<String> kpi1 = newHashSet("first_integer_1440_join_kpidb_filter");
        final Set<String> kpi2 = newHashSet("sum_integer_60_join_kpidb");

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("param.date_for_filter", "1970-01-01 01:30:30.250");

        createKpiCalculatorSpark(newHashSet(kpi1, kpi2));

        Set<KpiDefinition> appliedKpis1 = KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(
                KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(parameters, getKpis(kpi1)));
        Set<KpiDefinition> appliedKpis2 = KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(
                KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(parameters, getKpis(kpi2)));
        //Mock
        when(dataLoaderRegistryFacadeMock.customFilterDatasets(any(), anySet())).thenReturn(TableDatasets.of());
        when(offsetHandlerPostgres.getKpisForAggregationPeriodWithTimestampParameters( anyInt(), any()))
                .thenReturn(new ArrayList<>(appliedKpis1), new ArrayList<>(appliedKpis2));

        //invoke calculation
        objectUnderTest.calculate();

        //first aggregation period
        verify(calculatorHandlerRegistryFacadeMock).customCalculator(getDefinitionVerifierSet(new HashSet<>(kpi1)));

        //it is equal what definitions we add to KpiDefinitionHierarchy, as we will not use the related methods in KpiDefinitionHelper
        KpiDefinitionHelperImpl kpiDefinitionHelper = TestObjectFactory.kpiDefinitionHelper(new KpiDefinitionHierarchy(appliedKpis1));
        verify(kpiCalculationPeriodUtils, never()).getKpiCalculationStartTimeStampInUtc(
                eq(1440),
                eq(kpiDefinitionHelper.extractNonInMemoryDatasourceTables(getKpis(kpi1))),
                anySet()
        );

        //second aggregation period
        verify(calculatorHandlerRegistryFacadeMock).customCalculator(getDefinitionVerifierSet(new HashSet<>(kpi2)));
        verify(kpiCalculationPeriodUtils, never()).getKpiCalculationStartTimeStampInUtc(
                eq(60),
                eq(kpiDefinitionHelper.extractNonInMemoryDatasourceTables(getKpis(kpi2))),
                anySet()
        );

        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(2);
    }

    @Test
    void whenTwoCustomFilterKpisWithSameFilter_andSameAggregationPeriod_thenDataForBothKpisLoadedAtTheSameTime_thenKpisAreCalculatedTogether() {
        final Set<String> kpiNames = newHashSet("first_integer_1440_join_kpidb_filter", "first_integer_1440_join_kpidb_filter");

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("param.date_for_filter", "1970-01-01 01:30:30.250");

        createKpiCalculatorSpark(kpiNames);

        Set<KpiDefinition> appliedKpis = KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(
                KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(parameters, getKpis(kpiNames)));
        //Mock
        when(dataLoaderRegistryFacadeMock.customFilterDatasets(any(), anySet())).thenReturn(TableDatasets.of());
        when(offsetHandlerPostgres.getKpisForAggregationPeriodWithTimestampParameters( anyInt(), any())).thenReturn(new ArrayList<>(appliedKpis));

        //invoke calculation
        objectUnderTest.calculate();

        //verify
        final Set<Filter> filterExpected = newHashSet(new Filter("kpi_db://kpi_cell_guid_1440.TO_DATE(local_timestamp) = '1970-01-01 01:30:30.250'"));

        verify(calculatorHandlerRegistryFacadeMock).customCalculator(getDefinitionVerifierSet(kpiNames));

        //Verify max calls
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(1);
    }

    @Test
    void whenOneCustomFilterScheduledKpi_andOneTimeSlot_thenDataNotLoaded_andNothingIsCalculated() {
        final Set<String> kpi = newHashSet("sum_integer_60_from_yesterday_scheduled");

        createKpiCalculatorSpark(kpi);

        //Mock
        when(offsetHandlerPostgres.getKpisForAggregationPeriodWithTimestampParameters(anyInt(), any())).thenReturn(new ArrayList<>());

        //invoke calculation
        objectUnderTest.calculate();

        // verify
        verify(calculatorHandlerRegistryFacadeMock, never()).customCalculator(getDefinitionVerifierSet(kpi));

        // verify max calls
        verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(0);
    }

    @Test
    void whenOneCustomAndOneDefaultFilterScheduledKpi_andOneTimeSlot_thenDataLoadedTwice_andBothAreCalculated() {
        final Set<String> kpi1 = newHashSet("sum_integer_60_scheduled");
        final Set<String> kpi2 = newHashSet("sum_integer_60_from_yesterday_scheduled");

        createKpiCalculatorSpark(newHashSet(kpi1, kpi2));

        //Mock
        final Iterator<TableDatasets> iterator = List.of(TableDatasets.of()).iterator();
        when(dataLoaderRegistryFacadeMock.defaultFilterIterator(any())).thenReturn(iterator);
        when(dataLoaderRegistryFacadeMock.customFilterDatasets(any(), anySet())).thenReturn(TableDatasets.of());
        when(offsetHandlerPostgres.getKpisForAggregationPeriodWithTimestampParameters(anyInt(), any())).thenReturn(new ArrayList<>(getKpis(kpi2)));
        //invoke calculation
        objectUnderTest.calculate();

        // verify
        final Set<String> aliasExpected = newHashSet("cell_guid");

        // Default filter
        verify(calculatorHandlerRegistryFacadeMock).defaultCalculator(getDefinitionVerifierCollection(kpi1));

        // Custom filter
        verify(calculatorHandlerRegistryFacadeMock).customCalculator(getDefinitionVerifierCollection(kpi2));

        // verify max calls
        verify(dataLoaderRegistryFacadeMock).defaultFilterIterator(any());
    }

    List<KpiDefinition> getDefinitionVerifier(final Set<String> kpiNames) {
        return argThat((List<KpiDefinition> kpiDefinitions) -> kpiNames
                .containsAll(kpiDefinitions.stream().map(KpiDefinition::getName).collect(Collectors.toSet())));
    }

    Collection<KpiDefinition> getDefinitionVerifierCollection(final Set<String> kpiNames) {
        return argThat((Collection<KpiDefinition> kpiDefinitions) -> kpiNames
                .containsAll(kpiDefinitions.stream().map(KpiDefinition::getName).collect(Collectors.toSet())));
    }

    Set<KpiDefinition> getDefinitionVerifierSet(final Set<String> kpiNames) {
        return argThat((Set<KpiDefinition> kpiDefinitions) -> kpiNames
                .containsAll(kpiDefinitions.stream().map(KpiDefinition::getName).collect(Collectors.toSet())));
    }

    void verifyMaximumTimesDataLoaderCreatedForDefaultFilter(final int count) {
        verify(dataLoaderRegistryFacadeMock, times(count)).defaultFilterIterator(any());
    }

    void verifyMaximumTimesKpiCalculatorCreatedForDefaultFilter(final int count) {
        verify(calculatorHandlerRegistryFacadeMock, times(count)).defaultCalculator(any());
    }

    void verifyMaximumTimesKpiCalculatorCreatedForCustomFilter(final int count) {
        verify(calculatorHandlerRegistryFacadeMock, times(count)).customCalculator(any());
    }

    Set<KpiDefinition> getKpis(final Set<String> kpiNames) {
        return testKpis.stream().filter(kpiDefinition -> kpiNames.contains(kpiDefinition.getName()))
                .collect(Collectors.toSet());
    }

    Properties getProperties(final String username, final String password, final DatasourceType type) {
        final Properties properties = new Properties();
        properties.put("username", username);
        properties.put("password", password);
        properties.put("type", type.name());
        return properties;
    }

    @SafeVarargs
    static <T> Set<T> newHashSet(final T... objs) {
        final Set<T> set = new HashSet<>();
        Collections.addAll(set, objs);
        return set;
    }

    Set<String> newHashSet(final Set<String> kpi1, final Set<String> kpi2) {
        final Set<String> kpi = new HashSet<>(kpi1);
        kpi.addAll(kpi2);
        return kpi;
    }
}