/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINALIZING;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINISHED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.NOTHING_CALCULATED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.ON_DEMAND;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_COMPLEX;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_SIMPLE;
import static com.ericsson.oss.air.pm.stats.test.tools.KafkaUtils.increasePartition;
import static com.ericsson.oss.air.pm.stats.test.util.IntegrationTestUtils.sleep;
import static com.ericsson.oss.air.pm.stats.test.util.RequestUtils.onSuccess;
import static com.ericsson.oss.air.pm.stats.test.util.sql.DatabasePollUtils.pollDatabaseForExpectedValue;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.verifyCalculationDoesNotExistsForExecutionGroup;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.verifyCalculationExistForExecutionGroup;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.verifyLatestProcessedOffsetsTable;
import static java.util.Comparator.comparing;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse.CalculationResponseBuilder;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;
import com.ericsson.oss.air.pm.stats.test.dto.input.HealthCheckRestResponseInputDto;
import com.ericsson.oss.air.pm.stats.test.integration.IntegrationTest;
import com.ericsson.oss.air.pm.stats.test.loader.DataCatalogLoader;
import com.ericsson.oss.air.pm.stats.test.tools.consumer.ExecutionReportReader;
import com.ericsson.oss.air.pm.stats.test.tools.consumer.KpiExposureReader;
import com.ericsson.oss.air.pm.stats.test.tools.producer.KafkaWriter;
import com.ericsson.oss.air.pm.stats.test.tools.schema.registry.SchemaRegister;
import com.ericsson.oss.air.pm.stats.test.util.ServiceRestExecutor;
import com.ericsson.oss.air.pm.stats.test.util.sql.DatabaseCleaner;
import com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier;
import com.ericsson.oss.air.pm.stats.test.verification.KpiExecutionReportVerifier;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.ComplexExampleDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.complex.KpiComplexDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.simple.KpiCellSimpleDailyDateset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.simple.KpiRelGuidSimpleFilterDailyDateset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.ExampleTableDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.KpiSimpleSameDayDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.PredefinedSimpleHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.RelationSimpleHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.SimpleExampleHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.SimpleFdnCellHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.complex.ComplexFdnHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.complex.KpiComplex2HourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.complex.KpiComplexHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.complex.PredefinedComplexHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.none.KpiRelGuidSimpleNoneDateset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.none.complex.KpiComplexNoneDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.quarter.KpiLimitedComplexDependentQuarterDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.quarter.KpiLimitedComplexQuarterDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.quarter.KpiLimitedQuarterDataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@IntegrationTest
class KpiCalculationIntTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int TEST_KPI_TOTAL_COUNT = 88;

    static final String SIMPLE_EXEC_GROUP_1 = "dataSpace|category|fact_table";
    static final String SIMPLE_EXEC_GROUP_3 = "5G|category|fact_table";
    static final String VERY_SIMPLE_EXEC_GROUP = "dataSpace|category|a_new_very_simple_kpi";
    static final String SIMPLE_SAME_DAY_EXEC_GROUP = "dataSpace|category|simple_kpi_same_day";
    static final String SIMPLE_EXEC_GROUP_NO_DATA = "dataSpace|category|no_data";
    static final String SIMPLE_EXEC_GROUP_LIMITED_AGG = "dataSpace|category|limited_agg";
    static final String SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1 = "4G|PM_COUNTERS|SampleCellFDD_1";
    static final String SIMPLE_EXEC_GROUP_SAMPLE_RELATION_1 = "4G|PM_COUNTERS|SampleRelation_1";

    static final String COMPLEX_EXEC_GROUP_1_TRIGGERED = "COMPLEX1";
    static final String COMPLEX_EXEC_GROUP_2_TRIGGERED = "COMPLEX2";
    static final String COMPLEX_EXEC_GROUP_3_UNTRIGGERED = "COMPLEX3";
    static final String COMPLEX_EXEC_GROUP_4_TRIGGERED = "COMPLEX4";
    static final String COMPLEX_EXEC_GROUP_5_TRIGGERED = "COMPLEX5";

    private static final String TOPIC_0 = "topic0";
    private static final String TOPIC_1 = "topic1";
    private static final String TOPIC_2 = "topic2";
    private static final String TOPIC_3 = "topic3";
    private static final String TOPIC_4 = "topic4";
    private static final String TOPIC_5 = "topic5";
    private static final String TOPIC_6 = "topic6";
    private static final String TOPIC_SR = "topicsr";
    private static final int PARTITION_NUMBER_3 = 3;
    private static final int PARTITION_NUMBER_2 = 2;
    private static final int PARTITION_NUMBER_1 = 1;

    private static final String OTHER_COLLECTION_ID ="384cbeba-be7f-4280-af67-361142891e65";

    @BeforeAll
    static void setUpKafkaAndDataCatalogData() {
        //Register schema manually, due to no data
        SchemaRegister.register("kafka_files/no_data-schema");
        SchemaRegister.register("kafka_files/SampleCellTDD_1-schema.json");

        KafkaWriter.writeToKafka("kafka_files/fact_table-test_namespace.json", TOPIC_0, PARTITION_NUMBER_3, "kafka_files/fact_table-test_namespace-schema");
        KafkaWriter.writeToKafka("kafka_files/fact_table-kpi_service.json", TOPIC_1, PARTITION_NUMBER_3, "kafka_files/fact_table-kpi_service-schema");
        KafkaWriter.writeToKafka("kafka_files/new_very_simple_kpi_2022.json", TOPIC_2, PARTITION_NUMBER_2, "kafka_files/new_very_simple_kpi-schema");
        KafkaWriter.writeToKafka("kafka_files/simple_kpi_same_day.json", TOPIC_3, PARTITION_NUMBER_1, "kafka_files/simple_kpi_same_day-schema");
        KafkaWriter.writeToKafka("kafka_files/15_minutes.json", TOPIC_4, PARTITION_NUMBER_1, "kafka_files/15_minutes-schema.json");
        KafkaWriter.writeToKafka("kafka_files/SampleCellFDD.json", TOPIC_5, PARTITION_NUMBER_1, "kafka_files/SampleCellFDD_1-schema.json");
        KafkaWriter.writeToKafka("kafka_files/SampleRelation.json", TOPIC_SR, PARTITION_NUMBER_1, "kafka_files/SampleRelation_1-schema.json");

        //  Add empty partition to simulate a partition from which we cannot consume more data
        //  For testing start offset is not greater than end offset [IDUN-106233]
        increasePartition(TOPIC_0, partitions -> partitions + 1);

        //Register kafka instance
        final Long messageBusId = DataCatalogLoader.postMessageBus("eric-data-message-bus-kf:9092");

        //Register RAN parser like specRef
        DataCatalogLoader.postMessageSchema(TOPIC_0, DataIdentifier.of("dataSpace|category|fact_table"), "test-namespace.fact_table", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_1, DataIdentifier.of("5G|category|fact_table"), "kpi-service.fact_table", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_2, DataIdentifier.of("dataSpace|category|a_new_very_simple_kpi"), "kpi-service.a_new_very_simple_kpi", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_4, DataIdentifier.of(SIMPLE_EXEC_GROUP_LIMITED_AGG), "5G.limited_agg", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_4, DataIdentifier.of(SIMPLE_EXEC_GROUP_NO_DATA), "5G.no_data", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_5, DataIdentifier.of(SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1), "4G.RAN.PM_COUNTERS.SampleCellFDD_1", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_6, DataIdentifier.of("4G|PM_COUNTERS|SampleCellTDD_1"), "4G.RAN.PM_COUNTERS.SampleCellTDD_1", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_SR, DataIdentifier.of(SIMPLE_EXEC_GROUP_SAMPLE_RELATION_1), "4G.RAN.PM_COUNTERS.SampleRelation_1", messageBusId);

        //Register ESOA like specRef
        DataCatalogLoader.postMessageSchema(TOPIC_3, DataIdentifier.of("dataSpace|category|simple_kpi_same_day"), "kpi-service.simple_kpi_same_day/1", messageBusId);
        DataCatalogLoader.postMessageSchema(TOPIC_3, DataIdentifier.of("dataSpace|category|simple_kpi_same_day"), "kpi-service.simple_kpi_same_day/2", messageBusId);  //testing DataIdentifierValidator handles multiple schema response correctly
    }

    @BeforeAll
    static void setupOnDemandTests() {
        KpiCalculatorOnDemandIntTestMethods.setUpOnDemandTest();
    }

    @Test
    @Order(100)
    void setUpKpiCalculationData() {
        DatabaseCleaner.cleanKpiInputTables();
        DatabaseCleaner.cleanKpiOutputTables();
    }

    @Test
    @Order(200)
    void shouldVerifyHealthCheckEndpoint() throws Exception {
        try (final CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            final HttpUriRequest httpGet = new HttpGet(URI.create("http://eric-oss-pm-stats-calculator:8080/kpi-handling/calculator-service/health"));
            try (final CloseableHttpResponse response = closeableHttpClient.execute(httpGet)) {
                final String entity = EntityUtils.toString(response.getEntity());
                final HealthCheckRestResponseInputDto healthCheckRestResponseInputDto = OBJECT_MAPPER.readValue(entity, HealthCheckRestResponseInputDto.class);

                Assertions.assertThat(healthCheckRestResponseInputDto.getState()).isEqualTo("UP");
                Assertions.assertThat(healthCheckRestResponseInputDto.getAppName()).isEqualTo("eric-oss-pm-stats-calculator");

                final Map<String, Object> expected = new HashMap<>(2);
                expected.put("KPI_DATABASE", "HEALTHY");
                expected.put("SPARK_MASTER", "HEALTHY");
                expected.put("SCHEMA_REGISTRY", "HEALTHY");
                expected.put("KAFKA", "HEALTHY");
                Assertions.assertThat(healthCheckRestResponseInputDto.getAdditionalData())
                          .containsExactlyInAnyOrderEntriesOf(expected);
            }
        }
    }

    @Test
    @Order(299)
    void atStartupKpiExposureWasSent() {
        try (final KpiExposureReader kafkaReader = new KpiExposureReader()) {
            KpiExposureConfig kpiExposureMessage = kafkaReader.readKpiExposures(0L);

            assertThat(kpiExposureMessage).isNull(); //Tombstone message is null
        }
    }

    @Test
    @Order(300)
    void whenValidVerySimpleKpisArePassedToKpiServiceDefinitionEndpoint_thenStatus201IsReturnedWithCorrectMessage_andDefinitionsArePersisted()
        throws Exception {
        assertAcceptedKpiDefinitionRegister(
            ResourceLoaderUtils.getClasspathResourceAsString("TestVerySimpleKpis.json"));
    }

    @Test
    @Order(350)
    void whenVerySimpleKpisArePersisted_thenKpiExposureIsSentCorrectlyToKafka() {
        List<String> tableNames = List.of("kpi_simple_60", "kpi_rolling_aggregation_1440", "kpi_same_day_simple_60");
        List<KpiTableExposureConfig> expectedExposure = tableNames.stream().map(KpiTableExposureConfig::ofTable).collect(Collectors.toList());

        try (final KpiExposureReader kafkaReader = new KpiExposureReader()) {
            KpiExposureConfig kpiExposureMessage = kafkaReader.readKpiExposures(tableNames.size() - 2L);

            assertThat(kpiExposureMessage).isNotNull();
            assertThat(kpiExposureMessage.getExposure()).containsExactlyInAnyOrderElementsOf(expectedExposure);
        }
    }

    @Test
    @Order(400)
    void whenValidSimpleKpisArePassedToKpiServiceDefinitionEndpoint_thenStatus201IsReturnedWithCorrectMessage()
            throws Exception {
        assertAcceptedKpiDefinitionRegister(
            ResourceLoaderUtils.getClasspathResourceAsString("TestSimpleKpis.json"));
    }

    @Test
    @Order(401)
    void whenValidComplexKpisAdded_thenStatus201IsReturnedWithCorrectMessage()
            throws Exception {
        assertAcceptedKpiDefinitionRegister(
                ResourceLoaderUtils.getClasspathResourceAsString("TestComplexKpis.json"));
    }

    @Test
    @Order(402)
    void whenValidOnDemandKpisArePassedToKpiServiceDefinitionEndpoint_thenStatus201IsReturnedWithCorrectMessage() throws Exception {
        KpiCalculatorOnDemandIntTestMethods.whenValidKpisArePassedToKpiServiceDefinitionEndpoint_thenStatus201IsReturnedWithCorrectMessage();
    }

    @Test
    @Order(403)
    void whenValidSDKKpisAdded_thenStatus201IsReturnedWithCorrectMessage() throws Exception {
        assertAcceptedKpiDefinitionRegister(ResourceLoaderUtils.getClasspathResourceAsString("SDKKpis.json"));
    }

    @Test
    @Order(404)
    void whenValidSDKKpisNotCalculatedAdded_thenStatus201IsReturnedWithCorrectMessage() throws Exception {
        assertAcceptedKpiDefinitionRegister(ResourceLoaderUtils.getClasspathResourceAsString("SDKKpis_notCalculated.json"));
    }

    @Test
    @Order(405)
    void whenValidSimpleKpisArePersisted_thenNumberOfEntriesInKpiDefinitionTableEqualsTestKpiTotalCount() {
        KpiDatabaseVerifier.verifyKpiDefinitionTable(TEST_KPI_TOTAL_COUNT);
    }

    @Order(406)
    @MethodSource("provideForDeleteRequest")
    @ParameterizedTest(name = "[{index}] Trying to delete KPIS: ''{0}''")
    void whenDeleteRequestIsSent_thenStatus204IsReturned_andDefinitionsAreSoftDeleted(final List<String> payload) {
        onSuccess().deleteKpiDefinitions(payload, (statusCode, response) -> {
            assertThat(statusCode).isEqualTo(SC_OK);
            assertThat(response).containsExactlyInAnyOrderElementsOf(payload);
        });

        pollDatabaseForExpectedValue(configurationBuilder -> {
            configurationBuilder.pollTableName("kpi_definition");
            configurationBuilder.conditionalClause(String.format("WHERE time_deleted IS NOT NULL AND name = ANY('{%s}')", String.join(",", payload)));
            configurationBuilder.forExpectedCount(payload.size());
            return configurationBuilder.build();
        });
    }

    @Test
    @Order(450)
    void whenValidKpisPersistedWithCollectionId_thenStatus201IsReturned() throws IOException {
            assertAcceptedKpiDefinitionRegister(ResourceLoaderUtils.getClasspathResourceAsString("KpisForCollection_notCalculated.json"), OTHER_COLLECTION_ID);
    }

    @Test
    @Order(451)
    void whenPatchEndPointCalledWithCollectionId_thenCorrectKpiFormatReturned() throws IOException {
        onSuccess().updateKpiDefinition(
                "collection_id_test", Optional.of(OTHER_COLLECTION_ID), kpiDefinitionRequest -> {
                    kpiDefinitionRequest.setExportable(true);
                    return kpiDefinitionRequest;
                }, (statusCode, response) -> {
                    assertThat(statusCode).isEqualTo(SC_OK);
                    assertThat(response.getName()).isEqualTo("collection_id_test");
                    assertThat(response.getExportable()).isTrue();
                }
        );
    }

    @Test
    @Order(452)
    void whenGetEndPointCalledWithCollectionId_thenCorrectKpiFormatReturned() throws IOException {
        try (final CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            final HttpUriRequest httpGet = new HttpGet(URI.create("http://eric-oss-pm-stats-calculator:8080/kpi-handling/model/v1/definitions/"+OTHER_COLLECTION_ID));
            try (final CloseableHttpResponse response = closeableHttpClient.execute(httpGet)) {
                final String entity = EntityUtils.toString(response.getEntity());

                final JsonNode actual = OBJECT_MAPPER.readTree(entity);
                final JsonNode expected = OBJECT_MAPPER.readTree(ResourceLoaderUtils.getClasspathResourceAsString("KpisForCollection_getResponse.json"));
                assertThat(actual).isEqualTo(expected);
            }
        }
    }

    @Test
    @Order(453)
    void whenDeleteEndPointCalledWithCollectionId_thenKpisOdCollectionAreDeleted(){
        List<String> kpisToDelete = List.of("collection_id_test");
        onSuccess().deleteKpiDefinitions(kpisToDelete, OTHER_COLLECTION_ID, (statusCode, response) -> {
            assertThat(statusCode).isEqualTo(SC_OK);
            assertThat(response).containsExactlyInAnyOrderElementsOf(kpisToDelete);
        });

        pollDatabaseForExpectedValue(configurationBuilder -> {
            configurationBuilder.pollTableName("kpi_definition");
            configurationBuilder.conditionalClause(String.format("WHERE time_deleted IS NOT NULL AND name = ANY('{%s}') AND collection_id = %s",
                    String.join(",", kpisToDelete),
                    OTHER_COLLECTION_ID));
            configurationBuilder.forExpectedCount(kpisToDelete.size());
            return configurationBuilder.build();
        });
    }


    @Test
    @Order(510)
    void whenValidSimpleKpisArePersisted_thenKpiSimpleOutputColumnsMustMatch() {
        KpiDatabaseVerifier.verifySimpleKpiOutputTableColumns(); //TODO: make sure all table is validated
    }

    @Test
    @Order(600)
    void whenSimpleKpiCalculationFinished_thenSimpleKpiValuesMustMatchInDB() {
        verifyCalculationExistForExecutionGroup(VERY_SIMPLE_EXEC_GROUP, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(SIMPLE_EXEC_GROUP_1, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(SIMPLE_EXEC_GROUP_3, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(SIMPLE_SAME_DAY_EXEC_GROUP, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(SIMPLE_EXEC_GROUP_LIMITED_AGG, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(SIMPLE_EXEC_GROUP_SAMPLE_RELATION_1, FINALIZING, FINISHED);

        KpiDatabaseVerifier.assertTableContents(KpiCellSimpleDailyDateset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiRelGuidSimpleNoneDateset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiRelGuidSimpleFilterDailyDateset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiSimpleSameDayDailyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiLimitedQuarterDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(SimpleExampleHourlyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(RelationSimpleHourlyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(SimpleFdnCellHourlyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(PredefinedSimpleHourlyDataset.INSTANCE);

        List<String> simpleKpis = Arrays.asList("sum_Integer_1440_simple", "sum_integer_arrayindex_1440_simple",
                "sum_float_1440_simple_filter", "sum_integer_1440_simple_filter", "count_integer_1440_simple_filter", "sum_integer_simple",
                "integer_simple_same_day", "float_simple_same_day", "sum_integer_15", "count_integer_15", "transform_array_15", "not_calculated_simple_15");
        DatabaseCleaner.cleanKpiDefinitionTable(simpleKpis);
    }

    @Test
    @Order(601)
    void whenThereIsNoIncomingKafkaData_thenCalculationStateChangedToNothingToCalculated() {
        verifyCalculationExistForExecutionGroup(SIMPLE_EXEC_GROUP_NO_DATA, NOTHING_CALCULATED);
    }

    @Test
    @Order(602)
    void whenComplexCalculationStarted_thenNewDataArrive() {
        verifyCalculationExistForExecutionGroup(COMPLEX_EXEC_GROUP_1_TRIGGERED, KpiCalculationState.STARTED, KpiCalculationState.IN_PROGRESS);
        KafkaWriter.writeToKafka("kafka_files/new_very_simple_late_data.json", TOPIC_2, PARTITION_NUMBER_2, "kafka_files/new_very_simple_kpi-schema");
    }

    @Test
    @Order(610)
    void whenSimpleKpiCalculationFinished_thenSimpleKpiReadinessLogMustMatchInDB() {
        KpiDatabaseVerifier.waitForReadinessLogExistForLateData();
        KpiDatabaseVerifier.verifyReadinessLogExistForExecutionGroup();
        sleep(1, SECONDS);
    }

    @Test
    @Order(611)
    void whenOnDemandKpiDefinitionsArePersisted_thenRequestKpiCalculationsAndWaitUntilFinished() {
        KpiCalculatorOnDemandIntTestMethods.whenKpiDefinitionsArePersisted_thenRequestKpiCalculationsAndWaitUntilFinished();
    }

    @Test
    @Order(612)
    void whenKpiDefinitionsArePersisted_thenRequestKpiCalculationWithGzippedAndWaitUntilFinished() {
        KpiCalculatorOnDemandIntTestMethods.whenKpiDefinitionsArePersisted_thenRequestKpiCalculationWithGzippedAndWaitUntilFinished();
    }

    @Test
    @Order(620)
    void whenSimpleKpiCalculationFinished_thenSimpleKpiOffsetsMustMatchInDB() {
        verifyLatestProcessedOffsetsTable(TOPIC_0, SIMPLE_EXEC_GROUP_1, 126);
        verifyLatestProcessedOffsetsTable(TOPIC_1, SIMPLE_EXEC_GROUP_3, 125);
        verifyLatestProcessedOffsetsTable(TOPIC_2, VERY_SIMPLE_EXEC_GROUP, 44);
        verifyLatestProcessedOffsetsTable(TOPIC_3, SIMPLE_SAME_DAY_EXEC_GROUP, 2);
        verifyLatestProcessedOffsetsTable(TOPIC_4, SIMPLE_EXEC_GROUP_NO_DATA, 17);
        verifyLatestProcessedOffsetsTable(TOPIC_4, SIMPLE_EXEC_GROUP_LIMITED_AGG, 17);
        verifyLatestProcessedOffsetsTable(TOPIC_5, SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1, 116);
        verifyLatestProcessedOffsetsTable(TOPIC_SR, SIMPLE_EXEC_GROUP_SAMPLE_RELATION_1, 77);
    }

    @Test
    @Order(710)
    @SneakyThrows
    void whenGetEndpointCalled_thenCorrectJsonResponseIsReturned() {
        try (final CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            final HttpUriRequest httpGet = new HttpGet(URI.create("http://eric-oss-pm-stats-calculator:8080/kpi-handling/model/v1/definitions"));
            try (final CloseableHttpResponse response = closeableHttpClient.execute(httpGet)) {
                final String entity = EntityUtils.toString(response.getEntity());

                final JsonNode actual = OBJECT_MAPPER.readTree(entity);
                final JsonNode expected = OBJECT_MAPPER.readTree(ResourceLoaderUtils.getClasspathResourceAsString("KpiDefinitionsResponse.json"));
                assertThat(actual).isEqualTo(expected);
            }
        }
    }

    @Test
    @Order(900)
    void whenComplexKpiCalculationFinished_thenComplexKpiValues_andReadinessLogMustMatchInDB() {
        verifyCalculationExistForExecutionGroup(COMPLEX_EXEC_GROUP_1_TRIGGERED, 1, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(COMPLEX_EXEC_GROUP_2_TRIGGERED, 1, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(COMPLEX_EXEC_GROUP_4_TRIGGERED, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup(COMPLEX_EXEC_GROUP_5_TRIGGERED, FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup("LIMITED_COMPLEX", FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup("LIMITED_COMPLEX2", FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup("complex_execution_group1", FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup("complex_execution_group2", FINALIZING, FINISHED);
        verifyCalculationExistForExecutionGroup("sdk_complex_group3", FINALIZING, FINISHED);

        KpiDatabaseVerifier.assertTableContents(KpiComplexHourlyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiComplex2HourlyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiComplexDailyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiComplexNoneDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiLimitedComplexQuarterDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiLimitedComplexDependentQuarterDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(ExampleTableDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(ComplexExampleDailyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(PredefinedComplexHourlyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(ComplexFdnHourlyDataset.INSTANCE);

        KpiDatabaseVerifier.validateComplexReadinessLog(COMPLEX_EXEC_GROUP_1_TRIGGERED, 0,
                List.of(
                        "dataSpace|category|a_new_very_simple_kpi"
                )
        );

        KpiDatabaseVerifier.validateComplexReadinessLog(COMPLEX_EXEC_GROUP_1_TRIGGERED, 1,
                List.of(
                        "dataSpace|category|a_new_very_simple_kpi"
                )
        );

        KpiDatabaseVerifier.validateComplexReadinessLog(COMPLEX_EXEC_GROUP_2_TRIGGERED, 0,
                List.of(
                        "dataSpace|category|a_new_very_simple_kpi"
                )
        );

        KpiDatabaseVerifier.validateComplexReadinessLog(COMPLEX_EXEC_GROUP_2_TRIGGERED, 1,
                List.of(
                        "dataSpace|category|a_new_very_simple_kpi"
                )
        );

        KpiDatabaseVerifier.validateComplexReadinessLog(COMPLEX_EXEC_GROUP_4_TRIGGERED, 0,
                List.of(
                        "dataSpace|category|simple_kpi_same_day",
                        "dataSpace|category|fact_table",
                        "5G|category|fact_table"
                )
        );
        KpiDatabaseVerifier.validateComplexReadinessLog(COMPLEX_EXEC_GROUP_5_TRIGGERED, 0,
                List.of(
                        "dataSpace|category|simple_kpi_same_day"
                )
        );
        KpiDatabaseVerifier.validateComplexReadinessLog("LIMITED_COMPLEX", 0,
                List.of(
                        SIMPLE_EXEC_GROUP_LIMITED_AGG
                )
        );
        KpiDatabaseVerifier.validateComplexReadinessLog("LIMITED_COMPLEX2", 0,
                List.of(
                        SIMPLE_EXEC_GROUP_LIMITED_AGG
                )
        );
        KpiDatabaseVerifier.validateComplexReadinessLog("complex_execution_group1", 0,
                List.of(
                        SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1
                )
        );
        KpiDatabaseVerifier.validateComplexReadinessLog("complex_execution_group2", 0,
                List.of(
                        SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1
                )
        );
        KpiDatabaseVerifier.validateComplexReadinessLog("sdk_complex_group3", 0,
                List.of(
                        SIMPLE_EXEC_GROUP_SAMPLE_RELATION_1
                )
        );
    }
    @Test
    @Order(905)
    void whenComplexKpiCalculationFinished_thenNumberOfEntriesInCalculationReliabilityTableEqualsToNumberOfComplexKpis() {
        KpiDatabaseVerifier.verifyCalculationReliabilityTable(45); // 19 on demand + 5 on demand 2nd time + 17 complex + 4 complex calculated 2nd time
    }

    @Test
    @Order(910)
    void whenComplexKpiDefinitionsArePersisted_thenNoCalculationExistsForUntriggeredComplexGroup() {
        verifyCalculationDoesNotExistsForExecutionGroup(COMPLEX_EXEC_GROUP_3_UNTRIGGERED);
    }

    @Test
    @Order(1_060)
    void whenOnDemandKpiCalculationsAreFinished_thenAggregatedKpiValuesMustMatchInDatabase() {
        KpiCalculatorOnDemandIntTestMethods.whenKpiCalculationsAreFinished_thenAggregatedKpiValuesMustMatchInDatabase();
    }

    @Test
    @Order(1_070)
    void whenOnDemandKpiCalculationsAreFinished_thenKpiOutputTableColumnsMustBeChanged() {
        KpiCalculatorOnDemandIntTestMethods.whenKpiCalculationsAreFinished_thenKpiOutputTableColumnsMustBeChanged();
    }

    @Test
    @Order(1_080)
    void whenOnDemandParameterizedKpiIsRequestedForCalculation_andParametersAreNotSatisfied_then400IsReturnedWithCorrectMessage() throws Exception {
        KpiCalculatorOnDemandIntTestMethods.whenParameterizedKpiIsRequestedForCalculation_andParametersAreNotSatisfied_then400IsReturnedWithCorrectMessage();
    }

    @Test
    @Order(1_100)
    void whenCalculationsFinished_thenExecutionReportsShouldBeSentToKafka() {
        try (final ExecutionReportReader kafkaReader = new ExecutionReportReader()) {
            KpiExecutionReportVerifier.verifyExecutionReportsMatchExpected(kafkaReader.readExecutionReports());
        }
    }

    private static void assertAcceptedKpiDefinitionRegister(final String validKpisPayload) {
        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        try (RestResponse<String> response = serviceRestExecutor.postKpis(validKpisPayload)) {
            log.info("Received response: {}", response);
            assertThat(response.getStatus())
                    .as("Expected status code %s when validating KPIs", HttpStatus.SC_CREATED)
                    .isEqualTo(HttpStatus.SC_CREATED);
        }
    }

    private static void assertAcceptedKpiDefinitionRegister(final String validKpisPayload, final String collectionId) {
        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        try (RestResponse<String> response = serviceRestExecutor.postKpis(validKpisPayload, collectionId)) {
            log.info("Received response: {}", response);
            assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", HttpStatus.SC_CREATED)
                .isEqualTo(HttpStatus.SC_CREATED);
        }
    }

    @Nested
    class FindCalculationsWithinElapsedTime  {
        final ObjectMapper objectMapper = objectMapper();

        @Test
        @Order(1_201)
        void shouldVerifyCalculations() throws Exception {
            final int elapsedTime = 30;
            final List<CalculationResponse> calculationResponses = fetchCalculationIdsWithinElapsedTime(elapsedTime, false);

            log.info(
                    "Found calculation IDs - '{}' amount - within the '{}' elapsed minutes:{}{}",
                    calculationResponses.size(), elapsedTime, System.lineSeparator(), objectMapper.writeValueAsString(calculationResponses)
            );

            assertThat(calculationResponses).extracting(CalculationResponse::getStatus).doesNotContain(NOTHING_CALCULATED);

            final Comparator<CalculationResponse> calculationResponseComparator = (calculationResponse1, calculationResponse2) -> {
                /* Ignoring calculation ID as it is a runtime generated UUID */
                return comparing(CalculationResponse::getExecutionGroup)
                        .thenComparing(CalculationResponse::getKpiType)
                        .thenComparing(CalculationResponse::getStatus)
                        .compare(calculationResponse1, calculationResponse2);
            };

            assertThat(calculationResponses).usingElementComparator(calculationResponseComparator).containsAll(List.of(
                    /* ON_DEMAND */
                    calculationResponse("ON_DEMAND", ON_DEMAND, FINISHED, null),
                    calculationResponse("ON_DEMAND", ON_DEMAND, FINISHED, null),
                    calculationResponse("ON_DEMAND", ON_DEMAND, FINISHED, null),

                    /* SCHEDULED_SIMPLE */
                    calculationResponse("5G|category|fact_table", SCHEDULED_SIMPLE, FINISHED, null),
                    calculationResponse("dataSpace|category|a_new_very_simple_kpi", SCHEDULED_SIMPLE, FINISHED, null),
                    calculationResponse("dataSpace|category|fact_table", SCHEDULED_SIMPLE, FINISHED, null),
                    calculationResponse(SIMPLE_EXEC_GROUP_LIMITED_AGG, SCHEDULED_SIMPLE, FINISHED, null),
                    calculationResponse("dataSpace|category|simple_kpi_same_day", SCHEDULED_SIMPLE, FINISHED, null),
                    calculationResponse(SIMPLE_EXEC_GROUP_SAMPLE_CELL_FDD_1, SCHEDULED_SIMPLE, FINISHED, null),
                    calculationResponse(SIMPLE_EXEC_GROUP_SAMPLE_RELATION_1, SCHEDULED_SIMPLE, FINISHED, null),

                    /* SCHEDULED_COMPLEX */
                    calculationResponse("COMPLEX1", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("COMPLEX2", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("COMPLEX4", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("COMPLEX5", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("LIMITED_COMPLEX", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("LIMITED_COMPLEX2", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("complex_execution_group1", SCHEDULED_COMPLEX, FINISHED, null),
                    calculationResponse("complex_execution_group2", SCHEDULED_COMPLEX, FINISHED, null)
            ));
        }

        @Test
        @Order(1_202)
        void checkCalculatedAndNonCalculatedRelation() {
            final List<CalculationResponse> allCalculations = fetchCalculationIdsWithinElapsedTime(null, true);
            final List<CalculationResponse> calculatedCalculations = fetchCalculationIdsWithinElapsedTime(null, false);

            assertThat(allCalculations).containsAll(calculatedCalculations).hasSizeGreaterThanOrEqualTo(calculatedCalculations.size());
        }

        @SneakyThrows
        List<CalculationResponse> fetchCalculationIdsWithinElapsedTime(final Integer elapsedTime, final Boolean includeNothingCalculated) {
            try (final CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
                final URIBuilder uriBuilder = new URIBuilder(URI.create("http://eric-oss-pm-stats-calculator:8080/kpi-handling/calc/v1/calculations"));
                if (Objects.nonNull(elapsedTime)) {
                    uriBuilder.addParameter("elapsedMinutes", String.valueOf(elapsedTime));
                }
                if (includeNothingCalculated) {
                    uriBuilder.addParameter("includeNothingCalculated", "true");
                }
                final HttpUriRequest httpGet = new HttpGet(uriBuilder.build());

                try (final CloseableHttpResponse response = closeableHttpClient.execute(httpGet)) {
                    final String entity = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(entity, new TypeReference<>() {});
                }
            }
        }

        ObjectMapper objectMapper() {
            final DefaultPrettyPrinter defaultPrettyPrinter = new DefaultPrettyPrinter();
            defaultPrettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

            final ObjectMapper mapper = new ObjectMapper();
            mapper.setDefaultPrettyPrinter(defaultPrettyPrinter);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            return mapper;
        }

        CalculationResponse calculationResponse(
                final String executionGroup, final KpiType kpiType, final KpiCalculationState kpiCalculationState, final UUID calculationId
        ) {
            final CalculationResponseBuilder builder = CalculationResponse.builder();
            builder.executionGroup(executionGroup);
            builder.kpiType(kpiType);
            builder.status(kpiCalculationState);
            builder.calculationId(calculationId);
            return builder.build();
        }
    }
    static Stream<Arguments> provideForDeleteRequest() {
        final List<String> simpleHourly = List.of("sum_float_simple2_hourly", "sum_integer_simple1_hourly", "sum_integer_simple2_hourly", "sum_integer_simple3_hourly", "sum_integer_simple4_hourly",
                "sum_elements_array_simple", "array_max_simple", "array_size_simple", "boolean_example");
        final List<String> simpleDaily = List.of("sum_float_simple_daily", "max_float_simple_daily", "numerator", "denominator");
        final List<String> simpleFddTdd = List.of("sum_FDD_simple", "sum_TDD_simple");
        final List<String> complex = List.of("monitoring_kpi", "complex1", "complex2", "complex3", "complex4", "complex5", "fdd_or_tdd_kpi", "node_aggregated_kpi", "threshold_exceeded_time");
        final List<String> ondemand = List.of("threshold_exceeded", "agg_element_parameter_ondemand");

        return Stream.of(
                Arguments.of(ondemand),
                Arguments.of(complex),
                Arguments.of(simpleFddTdd),
                Arguments.of(simpleHourly),
                Arguments.of(simpleDaily)
        );
    }

}
