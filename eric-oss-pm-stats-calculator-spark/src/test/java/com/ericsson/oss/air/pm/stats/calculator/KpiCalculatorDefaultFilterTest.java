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
import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.sqlProcessorDelegator;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils._KpiDefinitions.getKpiDefinitionsForAggregationPeriod;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.HOURLY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.RELATION_GUID_SOURCE_GUID_TARGET_GUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiRetrievalException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties.KafkaProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.registry.DataLoaderRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.registry.OffsetPersistencyRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api.DatasetWriter;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry.api.DatasetWriterRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.DataSourceRepositoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestSourceDataRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.DataSourceServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.SparkServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DataSourceService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.CalculatorHandlerRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.CalculationExecutorFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.DatabasePropertiesProviderImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.DatasourceRegistryHandlerImp;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiCalculationPeriodUtils;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.ParameterParserImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.TemporalHandlerImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.UserDefinedFunctionRegisterImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerPostgres;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerPostgres.AggregationTimestampCache;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiDefinitionFileRetriever;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;
import com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KpiCalculatorSpark}.
 */
class KpiCalculatorDefaultFilterTest {
    static final String CELL_ALIAS = "cell_guid";
    static final String SIMPLE_CELL_ALIAS = "cell_guid_simple";
    static final String RELATION_ALIAS = "relation_guid_source_guid_target_guid";
    static final String SIMPLE_RELATION_ALIAS = "rel_guid_s_guid_t_guid_simple";
    static final String ROLLING_AGGREGATION_ALIAS = "rolling_aggregation";
    static final String EXECUTION_ID_ALIAS = "execution_id";
    static final String CELL_SECTOR_ALIAS = "cell_sector";
    static final Database DIM_DS_0 = new Database("dim_ds_0");
    static final Database FACT_DS_1 = new Database("fact_ds_1");
    static final Database FACT_DS_2 = new Database("fact_ds_2");
    static final String DIM_TABLE_0 = "dim_table_0";
    static final String DIM_TABLE_1 = "dim_table_1";
    static final String DIM_TABLE_2 = "dim_table_2";
    static final String FACT_TABLE_0 = "fact_table_0";
    static final String FACT_TABLE_1 = "fact_table_1";
    static final String FACT_TABLE_2 = "fact_table_2";
    static final String FACT_TABLE_3 = "fact_table_3";
    static final Database KPI = new Database("kpi");
    static final Database KPI_DB = new Database("kpi_db");
    static final String FACT = "FACT";
    static final String DIM = "DIM";
    static final String TYPE = "type";
    static final String EXPRESSION_TAG = "expressionTag";
    static final String JDBC_URL = "jdbcUrl";
    static final String CELL_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(CELL_ALIAS, 1440);
    static final String SIMPLE_CELL_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(SIMPLE_CELL_ALIAS, 1440);
    static final String RELATION_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(RELATION_ALIAS, 1440);
    static final String SIMPLE_RELATION_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(SIMPLE_RELATION_ALIAS, 1440);
    static final String CELL_SECTOR_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(CELL_SECTOR_ALIAS, 1440);
    static final String ROLLING_AGGREGATION_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(ROLLING_AGGREGATION_ALIAS, 1440);
    static final String EXECUTION_ID_1440_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(EXECUTION_ID_ALIAS, 1440);
    static final String CELL_60_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(CELL_ALIAS, 60);
    static final String RELATION_60_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(RELATION_ALIAS, 60);
    static final String CELL_SECTOR_60_OUTPUT_TABLE_NAME = KpiNameUtils.createOutputTableName(CELL_SECTOR_ALIAS, 60);
    static final List<String> IGNORED_KPIS = List.of("fdn_agg");

    static final Set<Datasource> UNAVAILABLE_DATASOURCES = new HashSet<>();

    static TableDatasets dailyTableDatasets;
    static TableDatasets hourlyTableDatasets;

    static SparkSession sparkSession;
    static KpiCalculatorDefaultFilter dailyAggregationPeriodKpiCalculator;

    @BeforeAll
    static void init() {
        UserGroupInformation.setLoginUser(UserGroupInformation.createUserForTesting("hadoopTestUser", new String[]{"testGroup"}));
        sparkSession = SparkSession.builder()
                                   .master("local[*]")
                                   .appName("kpi-calculator")
                                   .config("spark.executionGroup", "execution_group")
                                   .config("spark.sql.shuffle.partitions", "1")
                                   .config("spark.default.parallelism", "1")
                                   .config("spark.calculationId", "812f4d8a-6c8f-4dcd-b278-e4dfadbacc7f")
                                   .getOrCreate();

        final KpiDefinitionFileRetriever kpiDefinitionRetriever = new KpiDefinitionFileRetriever();

        Set<KpiDefinition> kpiDefinitionsWithoutParametersApplied;
        try {
            kpiDefinitionsWithoutParametersApplied = kpiDefinitionRetriever.retrieveAllKpiDefinitions();
        } catch (final KpiRetrievalException e) {
            kpiDefinitionsWithoutParametersApplied = Collections.emptySet();
        }

        final Set<KpiDefinition> kpiDefinitions = KpiDefinitionUtils.convertDefinitionsToKpiDefinitions(
                KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(getAllParameters(), kpiDefinitionsWithoutParametersApplied));

        // Prevent the following Scala error: Failed to execute user defined function
        kpiDefinitions.removeIf(kpi -> IGNORED_KPIS.contains(kpi.getAlias()));

        final KpiDefinitionHierarchy kpiDefinitionHierarchy = new KpiDefinitionHierarchy(kpiDefinitions);
        final KpiDefinitionHelperImpl kpiDefinitionHelper = kpiDefinitionHelper(kpiDefinitionHierarchy);

        final Set<KpiDefinition> customFilterKpis = kpiDefinitionHelper.getFilterAssociatedKpiDefinitions(kpiDefinitions);
        final Set<KpiDefinition> defaultFilterKpiDefinitions = new HashSet<>(kpiDefinitions);
        defaultFilterKpiDefinitions.removeAll(customFilterKpis);

        final Set<KpiDefinition> dailyAggregatedKpiDefinitions = getKpiDefinitionsForAggregationPeriod(defaultFilterKpiDefinitions, "1440");
        final Set<KpiDefinition> hourlyAggregatedKpiDefinitions = getKpiDefinitionsForAggregationPeriod(defaultFilterKpiDefinitions, "60");

        final DatabaseProperties dbProperties = getDatabaseProperties();

        createKpiCalculatorSpark(sparkSession, dbProperties);

        final UserDefinedFunctionRegisterImpl userDefinedFunctionRegister = new UserDefinedFunctionRegisterImpl();
        userDefinedFunctionRegister.register(sparkSession);

        final Map<String, Dataset<Row>> inputDatasets = loadInputDatasets();
        createTempViewsForDatasets(inputDatasets);
        final Map<String, Dataset<Row>> kpiDatasets = loadKpiDatasets();
        createTempViewsForDatasets(kpiDatasets);

        final CalculationRepository calculationRepository = mock(CalculationRepository.class);
        final SparkServiceImpl sparkService = new SparkServiceImpl(
                calculationRepository,
                new ParameterParserImpl(new ObjectMapper()),
                new DatabasePropertiesProviderImpl(),
                sparkSession
        );

        final SqlProcessorDelegator sqlProcessorDelegator = sqlProcessorDelegator(sparkSession);
        final OffsetPersistencyRegistryFacadeImpl offsetPersistencyRegistryFacade = mock(OffsetPersistencyRegistryFacadeImpl.class);
        dailyAggregationPeriodKpiCalculator = new KpiCalculatorDefaultFilter(
                sparkService,
                DAILY_AGGREGATION_PERIOD_IN_MINUTES,
                dailyAggregatedKpiDefinitions,
                sqlProcessorDelegator,
                sparkSession,
                offsetPersistencyRegistryFacade
        );

        final KpiCalculatorDefaultFilter hourlyAggregationPeriodKpiCalculator = new KpiCalculatorDefaultFilter(
                sparkService,
                HOURLY_AGGREGATION_PERIOD_IN_MINUTES,
                hourlyAggregatedKpiDefinitions,
                sqlProcessorDelegator,
                sparkSession,
                offsetPersistencyRegistryFacade
        );

        final DatasetWriterRegistryFacade datasetWriterRegistryFacadeMock = mock(DatasetWriterRegistryFacade.class);
        final DatasetWriter datasetWriterMock = mock(DatasetWriter.class);

        when(datasetWriterRegistryFacadeMock.datasetWriter(anyInt())).thenReturn(datasetWriterMock);

        dailyAggregationPeriodKpiCalculator.setKpiDefinitionHelper(kpiDefinitionHelper(new KpiDefinitionHierarchy(dailyAggregatedKpiDefinitions)));
        dailyAggregationPeriodKpiCalculator.setSparkService(sparkService);
        dailyAggregationPeriodKpiCalculator.setDatasetWriterRegistryFacade(datasetWriterRegistryFacadeMock);
        dailyAggregationPeriodKpiCalculator.setSqlProcessorDelegator(sqlProcessorDelegator);

        hourlyAggregationPeriodKpiCalculator.setKpiDefinitionHelper(kpiDefinitionHelper(new KpiDefinitionHierarchy(hourlyAggregatedKpiDefinitions)));
        hourlyAggregationPeriodKpiCalculator.setSparkService(sparkService);
        hourlyAggregationPeriodKpiCalculator.setDatasetWriterRegistryFacade(datasetWriterRegistryFacadeMock);
        hourlyAggregationPeriodKpiCalculator.setSqlProcessorDelegator(sqlProcessorDelegator);

        when(calculationRepository.forceFetchById(UUID.fromString("812f4d8a-6c8f-4dcd-b278-e4dfadbacc7f"))).thenReturn(Calculation.builder().kpiType(KpiType.SCHEDULED_SIMPLE).build());

        dailyTableDatasets = dailyAggregationPeriodKpiCalculator.calculate();
        hourlyTableDatasets = hourlyAggregationPeriodKpiCalculator.calculate();
    }

    @Test
    void verifyAggObject1_1440Dataset() {
        final Dataset<Row> actualResult = dailyTableDatasets.get(getKeyForDataset(CELL_ALIAS, 1440));
        final String[] columns = actualResult.columns();
        final Dataset<Row> expectedResult = OutputDatasetBuilder.buildKpiAggElement1_1440Dataset(sparkSession,
                                                                                                 TestData.getExpectedKpiAggObject1_1440Data());
        final Dataset<Row> reorderedExpectedResult = expectedResult.select(columns[0], Arrays.copyOfRange(columns, 1, columns.length));

        assertThat(actualResult.exceptAll(reorderedExpectedResult).isEmpty()).isTrue();
        assertThat(reorderedExpectedResult.exceptAll(actualResult).isEmpty()).isTrue();
    }

    @Test
    void verifySimpleAggObject_1440Dataset() {
        final Dataset<Row> actualResult = dailyTableDatasets.get(getKeyForDataset(SIMPLE_CELL_ALIAS, 1440));
        final String[] columns = actualResult.columns();
        final Dataset<Row> expectedResult = OutputDatasetBuilder.buildSimpleKpiAggElement_1440Dataset(sparkSession,
                                                                                                 TestData.getExpectedSimpleKpiAggObject1_1440Data());
        final Dataset<Row> reorderedExpectedResult = expectedResult.select(columns[0], Arrays.copyOfRange(columns, 1, columns.length));

        assertThat(actualResult.exceptAll(reorderedExpectedResult).isEmpty()).isTrue();
        assertThat(reorderedExpectedResult.exceptAll(actualResult).isEmpty()).isTrue();
    }

    @Test
    void verifyAggObject2_1440Dataset() {
        final Dataset<Row> actualResult = dailyTableDatasets.get(getKeyForDataset(RELATION_GUID_SOURCE_GUID_TARGET_GUID, 1440));
        final String[] columns = actualResult.columns();
        final Dataset<Row> expectedResult = OutputDatasetBuilder.buildKpiAggElement2_1440Dataset(sparkSession,
                                                                                                 TestData.getExpectedKpiAggObject2_1440Data());
        final Dataset<Row> reorderedExpectedResult = expectedResult.select(columns[0], Arrays.copyOfRange(columns, 1, columns.length));

        assertThat(actualResult.exceptAll(reorderedExpectedResult).isEmpty()).isTrue();
        assertThat(reorderedExpectedResult.exceptAll(actualResult).isEmpty()).isTrue();
    }

    @Test
    void verifySimpleAggObject2_1440Dataset() {
        final Dataset<Row> actualResult = dailyTableDatasets.get(getKeyForDataset(SIMPLE_RELATION_ALIAS, 1440));
        final String[] columns = actualResult.columns();
        final Dataset<Row> expectedResult = OutputDatasetBuilder.buildSimpleKpiAggElement2_1440Dataset(sparkSession,
                TestData.getExpectedSimpleKpiAggObject2_1440Data());
        final Dataset<Row> reorderedExpectedResult = expectedResult.select(columns[0], Arrays.copyOfRange(columns, 1, columns.length));

        assertThat(actualResult.exceptAll(reorderedExpectedResult).isEmpty()).isTrue();
        assertThat(reorderedExpectedResult.exceptAll(actualResult).isEmpty()).isTrue();
    }

    @Test
    void verifyAggObject1_60Dataset() {
        final Dataset<Row> actualResult = hourlyTableDatasets.get(getKeyForDataset(CELL_ALIAS, 60));
        final String[] columns = actualResult.columns();
        final Dataset<Row> expectedResult = OutputDatasetBuilder.buildKpiAggElement1_60Dataset(sparkSession,
                                                                                               TestData.getExpectedKpiAggObject1_60Data());
        final Dataset<Row> reorderedExpectedResult = expectedResult.select(columns[0], Arrays.copyOfRange(columns, 1, columns.length));

        assertThat(actualResult.exceptAll(reorderedExpectedResult).isEmpty()).isTrue();
        assertThat(reorderedExpectedResult.exceptAll(actualResult).isEmpty()).isTrue();
    }

    @Test
    void verifyAggObject2_60Dataset() {
        final Dataset<Row> actualResult = hourlyTableDatasets.get(getKeyForDataset(RELATION_GUID_SOURCE_GUID_TARGET_GUID, 60));
        final String[] columns = actualResult.columns();
        final Dataset<Row> expectedResult = OutputDatasetBuilder.buildKpiAggElement2_60Dataset(sparkSession,
                                                                                               TestData.getExpectedKpiAggObject2_60Data());
        final Dataset<Row> reorderedExpectedResult = expectedResult.select(columns[0], Arrays.copyOfRange(columns, 1, columns.length));

        assertThat(actualResult.exceptAll(reorderedExpectedResult).isEmpty()).isTrue();
        assertThat(reorderedExpectedResult.exceptAll(actualResult).isEmpty()).isTrue();
    }

    static DatabaseProperties getDatabaseProperties() {
        final DatabaseProperties dbProperties = DatabaseProperties.newInstance();
        dbProperties.put(DIM_DS_0, new Properties());
        dbProperties.get(DIM_DS_0).setProperty(EXPRESSION_TAG, DIM_DS_0.getName());
        dbProperties.get(DIM_DS_0).setProperty(JDBC_URL, StringUtils.EMPTY);
        dbProperties.get(DIM_DS_0).setProperty(TYPE, DIM);

        dbProperties.put(FACT_DS_1, new Properties());
        dbProperties.get(FACT_DS_1).setProperty(EXPRESSION_TAG, FACT_DS_1.getName());
        dbProperties.get(FACT_DS_1).setProperty(JDBC_URL, StringUtils.EMPTY);
        dbProperties.get(FACT_DS_1).setProperty(TYPE, FACT);

        dbProperties.put(FACT_DS_2, new Properties());
        dbProperties.get(FACT_DS_2).setProperty(EXPRESSION_TAG, FACT_DS_2.getName());
        dbProperties.get(FACT_DS_2).setProperty(JDBC_URL, StringUtils.EMPTY);
        dbProperties.get(FACT_DS_2).setProperty(TYPE, FACT);

        dbProperties.put(KPI, new Properties());
        dbProperties.get(KPI).setProperty(EXPRESSION_TAG, KPI.getName());
        dbProperties.get(KPI).setProperty(JDBC_URL, StringUtils.EMPTY);
        dbProperties.get(KPI).setProperty(TYPE, FACT);

        dbProperties.put(KPI_DB, new Properties());
        dbProperties.get(KPI_DB).setProperty(EXPRESSION_TAG, KPI_DB.getName());
        dbProperties.get(KPI_DB).setProperty(JDBC_URL, StringUtils.EMPTY);
        dbProperties.get(KPI_DB).setProperty(TYPE, FACT);

        return dbProperties;
    }

    static void createTempViewsForDatasets(final Map<String, Dataset<Row>> datasetsByType) {
        for (final Entry<String, Dataset<Row>> datasetEntry : datasetsByType.entrySet()) {
            datasetEntry.getValue().createOrReplaceTempView(datasetEntry.getKey());
        }
    }

    static KpiCalculatorSpark createKpiCalculatorSpark(final SparkSession sparkSession, final DatabaseProperties dbProperties) {
        final HashSet<KpiDefinition> kpiDefinitions = new HashSet<>();

        final SparkService sparkServiceMock = mock(SparkService.class);
        final KpiDefinitionService kpiDefinitionServiceMock = mock(KpiDefinitionService.class);
        final OffsetHandlerRegistryFacade offsetHandlerRegistryFacadeMock = mock(OffsetHandlerRegistryFacade.class);
        final DataLoaderRegistryFacadeImpl dataLoaderRegistryFacadeMock = mock(DataLoaderRegistryFacadeImpl.class);
        final DataSourceRepositoryImpl dataSourceRepository = new DataSourceRepositoryImpl(
                DatasourceRegistry.getInstance(),
                sparkServiceMock
        );

        final DataSourceService dataSourceService = new DataSourceServiceImpl(dataSourceRepository);

        final KpiDefinitionHierarchy kpiDefinitionHierarchy = new KpiDefinitionHierarchy(kpiDefinitions);

        final KpiDefinitionHelperImpl kpiDefinitionHelper =   kpiDefinitionHelper(kpiDefinitionHierarchy);

        final SourceDataAvailability sourceDataAvailability = new SourceDataAvailability(
                dataSourceRepository,
                DatasourceRegistry.getInstance(),
                kpiDefinitionHelper,
                sparkServiceMock
        );

        final CalculationExecutorFacadeImpl calculationExecutor = new CalculationExecutorFacadeImpl(
                sourceDataAvailability,
                sparkServiceMock,
                kpiDefinitionServiceMock
        );

        final LatestSourceDataRepository latestSourceDataRepositoryMock = mock(LatestSourceDataRepository.class);

        final CalculationProperties calculationProperties = new CalculationProperties(
                Duration.ofMinutes(15),
                Duration.ofMinutes(30),
                Duration.ofDays(1),
                StringUtils.EMPTY,
                false,
                4,
                StringUtils.EMPTY,
                new KafkaProperties(50, "Server")
        );

        final OffsetHandler offsetHandlerPostgres = new OffsetHandlerPostgres(
                kpiDefinitionServiceMock,
                new KpiCalculationPeriodUtils(
                        new DatasourceRegistryHandlerImp(
                                sparkServiceMock,
                                DatasourceRegistry.getInstance(),
                                kpiDefinitionHelper
                        ),
                        new TemporalHandlerImpl(calculationProperties),
                        sourceDataAvailability, dataSourceService, sparkServiceMock,
                        calculationProperties,
                        latestSourceDataRepositoryMock,
                        kpiDefinitionHelper
                ),
                kpiDefinitionHelper,
                sparkServiceMock,
                AggregationTimestampCache.of(),
                AggregationTimestampCache.of()
        );

        when(sparkServiceMock.getKpiJdbcConnection()).thenReturn(null);
        when(sparkServiceMock.getKpiJdbcProperties()).thenReturn(new Properties());
        when(sparkServiceMock.getDatabaseProperties()).thenReturn(dbProperties);

        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);

        when(offsetHandlerRegistryFacadeMock.offsetHandler()).thenReturn(offsetHandlerPostgres);

        final DatasourceRegistryHandlerImp datasourceRegistryHandlerImp = new DatasourceRegistryHandlerImp(
                sparkServiceMock,
                DatasourceRegistry.getInstance(),
                kpiDefinitionHelper
        );
        datasourceRegistryHandlerImp.populateDatasourceRegistry();


        return new KpiCalculatorSpark(
                kpiDefinitionServiceMock,
                kpiDefinitionHierarchy,
                datasourceRegistryHandlerImp,
                kpiDefinitionHelper,
                calculationExecutor,
                mock(CalculatorHandlerRegistryFacadeImpl.class),
                offsetHandlerRegistryFacadeMock,
                dataLoaderRegistryFacadeMock
        );
    }

    static Map<String, Dataset<Row>> loadInputDatasets() {
        final Map<String, Dataset<Row>> inputDataSets = new HashMap<>(7);

        inputDataSets.put(DIM_TABLE_0, InputDatasetBuilder.buildDimTable0Dataset(sparkSession, TestData.getDimTable0Data()));
        inputDataSets.put(DIM_TABLE_1, InputDatasetBuilder.buildDimTable1Dataset(sparkSession, TestData.getDimTable1Data()));
        inputDataSets.put(DIM_TABLE_2, InputDatasetBuilder.buildDimTable2Dataset(sparkSession, TestData.getDimTable2Data()));

        inputDataSets.put(FACT_TABLE_0, InputDatasetBuilder.buildFactTable0Dataset(sparkSession, TestData.getFactTable0Data()));
        inputDataSets.put(FACT_TABLE_1, InputDatasetBuilder.buildFactTable1Dataset(sparkSession, TestData.getFactTable1Data()));

        inputDataSets.put(FACT_TABLE_2, InputDatasetBuilder.buildFactTable2Dataset(sparkSession, TestData.getFactTable2Data()));
        inputDataSets.put(FACT_TABLE_3, InputDatasetBuilder.buildFactTable3Dataset(sparkSession, TestData.getFactTable3Data()));

        return inputDataSets;
    }

    static Map<String, Dataset<Row>> loadKpiDatasets() {
        final Map<String, Dataset<Row>> datasets = new HashMap<>(6);
        datasets.put(CELL_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement1_1440Dataset(sparkSession, TestData.getAggObject1_1440InputData()));
        datasets.put(SIMPLE_CELL_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildSimpleKpiAggElement_1440Dataset(sparkSession, TestData.getSimpleAggObject_1440InputData()));
        datasets.put(RELATION_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement2_1440Dataset(sparkSession, TestData.getAggObject2_1440InputData()));
        datasets.put(SIMPLE_RELATION_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildSimpleKpiAggElement2_1440Dataset(sparkSession, TestData.getSimpleAggObject2_1440InputData()));
        datasets.put(CELL_SECTOR_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement3_1440Dataset(sparkSession, TestData.getAggObject3_1440InputData()));
        datasets.put(ROLLING_AGGREGATION_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement1RollingAggregate_1440Dataset(sparkSession, new Object[][] {}));
        datasets.put(EXECUTION_ID_1440_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement4ExecutionId_1440Dataset(sparkSession, new Object[][] {}));

        datasets.put(CELL_60_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement1_60Dataset(sparkSession, TestData.getAggObject1_60InputData()));
        datasets.put(RELATION_60_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement2_60Dataset(sparkSession, TestData.getAggObject2_60InputData()));
        datasets.put(CELL_SECTOR_60_OUTPUT_TABLE_NAME,
                InputDatasetBuilder.buildKpiAggElement3_60Dataset(sparkSession, new Object[][] {}));

        return datasets;
    }

    static Map<String, String> getAllParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("param.date_for_filter", "2019-05-09");
        parameters.put("param.execution_id", "TEST_1");
        return parameters;
    }

    static Table getKeyForDataset(final String alias, final int aggregationPeriod) {
        return Table.of(String.format("kpi_%s_%s", alias, aggregationPeriod));
    }
}