/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test;

import static com.ericsson.oss.air.pm.stats.test.KpiCalculationIntTests.COMPLEX_EXEC_GROUP_1_TRIGGERED;
import static com.ericsson.oss.air.pm.stats.test.KpiCalculationIntTests.SIMPLE_EXEC_GROUP_1;
import static com.ericsson.oss.air.pm.stats.test.util.RequestUtils.onFailure;
import static com.ericsson.oss.air.pm.stats.test.util.RequestUtils.onSuccess;
import static com.ericsson.oss.air.pm.stats.test.util.sql.DatabasePollUtils.pollDatabaseForExpectedValue;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.complexCalculation;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.complexReadinessLog;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.fetchCalculationReadinessLog;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.fetchReadinessLogResponseByCalculationId;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.forceFetchCalculationByExecutionGroup;
import static com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.validateTableDefinition;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDefinitionPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationStateResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.ReadinessLogResponse;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.test.dto.input.ErrorResponse;
import com.ericsson.oss.air.pm.stats.test.dto.input._assert.ErrorResponseAssertions;
import com.ericsson.oss.air.pm.stats.test.integration.IntegrationTest;
import com.ericsson.oss.air.pm.stats.test.util.KpiUtils;
import com.ericsson.oss.air.pm.stats.test.util.ServiceRestExecutor;
import com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier;
import com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier.CalculationReadinessLog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@IntegrationTest
class KpiApiIntTests {
    private static final ObjectMapper OBJECT_MAPPER = objectMapper();

    private static final String KPI_CALCULATION_REQUEST_NOT_IN_DB = "KpiCalculationRequest_NotInDB.json";
    private static final String TEST_SOURCE = "TEST";
    private static final String TEST_ON_DEMAND_KPIS_FILE_PATH = "TestOnDemandKpis.json";
    private static final int TEST_COMPLEX_KPI_TOTAL_COUNT = 14;
    private static final int TEST_ON_DEMAND_KPI_TOTAL_COUNT = 16;
    private static final int NOT_DELETED_SIMPLE_KPI_COUNT = 4;
    private static final String TEST_INVALID_SIMPLE_FILE_PATH = "TestInvalidSimpleKpis.json";
    private static final String TEST_INVALID_WHITELIST_SIMPLE_FILE_PATH = "TestInvalidWhitelistSimpleKpis.json";
    private static final String OTHER_COLLECTION_ID = "19a37fae-be3b-41f6-8474-086ac97db584";
    private static final String WRONG_COLLECTION_ID = "19a37fa-41f6-8474-086ac97db584";


    @Test
    @Order(25)
    @Disabled("JAX-RS is currently throwing 404 error. This should be 400 Bad Request. Once this is solved, the Ignore annotation can be removed.")
    void whenInvalidCalculationIdsAreRequested_thenStatus400IsReturnedWithCorrectMessages() {
        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();

        final RestResponse<String> response = serviceRestExecutor.getCalculationState("invalid_id");

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_BAD_REQUEST)
                .isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Order(50)
    void whenInvalidVerySimpleKpisArePassedToKpiServiceDefinitionPostEndpoint_thenStatus400IsReturnedWithCorrectMessage()
            throws Exception {
        final String invalidKpisPayload = ResourceLoaderUtils.getClasspathResourceAsString(TEST_INVALID_SIMPLE_FILE_PATH);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postKpis(invalidKpisPayload);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_BAD_REQUEST)
                .isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Order(60)
    void whenInvalidWhitelistSimpleKpisArePassedToKpiServiceDefinitionPostEndpoint_thenStatus400IsReturnedWithCorrectMessage()
            throws Exception {
        final String invalidKpisPayload = ResourceLoaderUtils.getClasspathResourceAsString(TEST_INVALID_WHITELIST_SIMPLE_FILE_PATH);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postKpis(invalidKpisPayload);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_BAD_REQUEST)
                .isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Order(100)
    @Disabled("Prepare test case for new KPI Definition")
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenInvalidKpisArePassedToKpiServiceDefinitionEndpoint_thenStatus400IsReturnedWithCorrectMessage() throws JsonProcessingException {
        final List<Map<String, Object>> invalidKpis = KpiUtils.getInvalidKpis();

        final KpiDefinitionPayload modelDefinition = new KpiDefinitionPayload(TEST_SOURCE, invalidKpis);
        final String invalidKpisPayload = OBJECT_MAPPER.writeValueAsString(modelDefinition);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(invalidKpisPayload);
        assertThat(response.getStatus())
                .as("Expected error code %s when validating KPIs", SC_BAD_REQUEST)
                .isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Order(150)
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenStringValueIsGivenForScalarTypeInAKpiPassedToKpiServiceDefinitionEndpoint_thenStatus400IsReturnedWithCorrectMessage() throws Exception{
        final String invalidIntegerKpiPayload =
                "{" +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"aggregation_period\": \"1440\"," +
                "        \"alias\": \"invalid_integer\"," +
                "        \"aggregation_elements\": [" +
                "          \"kpi_simple_60.agg_column_0\"," +
                "          \"kpi_simple_60.agg_column_1\"" +
                "        ]," +
                "        \"exportable\": true," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"sum_column_kpi_1440\"," +
                "            \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\"," +
                "            \"object_type\": \"INTEGER\"," +
                "            \"aggregation_type\": \"SUM\"," +
                "            \"execution_group\": \"COMPLEX1\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                "}";
        final String invalidBooleanKpiPayload =
                "{" +
                        "  \"scheduled_complex\": {" +
                        "    \"kpi_output_tables\": [{" +
                        "        \"aggregation_period\": 1440," +
                        "        \"alias\": \"invalid_boolean\"," +
                        "        \"aggregation_elements\": [" +
                        "          \"kpi_simple_60.agg_column_0\"," +
                        "          \"kpi_simple_60.agg_column_1\"" +
                        "        ]," +
                        "        \"exportable\": \"true\"," +
                        "        \"kpi_definitions\": [" +
                        "          {" +
                        "            \"name\": \"sum_column_kpi_1440\"," +
                        "            \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\"," +
                        "            \"object_type\": \"INTEGER\"," +
                        "            \"aggregation_type\": \"SUM\"," +
                        "            \"execution_group\": \"COMPLEX1\"" +
                        "          }" +
                        "        ]" +
                        "    }]" +
                        "  }" +
                        "}";

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        try (RestResponse<String> invalidIntegerResponse = serviceRestExecutor.postKpis(invalidIntegerKpiPayload)) {
            final ErrorResponse errorResponse = OBJECT_MAPPER.readValue(invalidIntegerResponse.getEntity(), ErrorResponse.class);
            Assertions.assertThat(errorResponse.status()).isEqualTo(SC_BAD_REQUEST);
            Assertions.assertThat(errorResponse.message()).isEqualTo("Cannot coerce String value (\"1440\") to `java.lang.Integer` value (but might if coercion "
                    + "using `CoercionConfig` was enabled) at [line: 1, column: 583] path [scheduled_complex -> kpi_output_tables[0] -> aggregation_period]");
        }

        try (RestResponse<String> invalidBooleanResponse = serviceRestExecutor.postKpis(invalidBooleanKpiPayload)) {
            final ErrorResponse errorResponse = OBJECT_MAPPER.readValue(invalidBooleanResponse.getEntity(), ErrorResponse.class);
            Assertions.assertThat(errorResponse.status()).isEqualTo(SC_BAD_REQUEST);
            Assertions.assertThat(errorResponse.message()).isEqualTo("Cannot coerce String value (\"true\") to `java.lang.Boolean` value (but might if coercion "
                    + "using `CoercionConfig` was enabled) at [line: 1, column: 583] path [scheduled_complex -> kpi_output_tables[0] -> exportable]");
        }
    }

    @Test
    @Order(200)
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenConflictingKpisArePassedToKpiServiceDefinitionsPostEndpoint_thenStatus409IsReturnedWithCorrectMessage() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"aggregation_period\": 1440," +
                "        \"alias\": \"conflict\"," +
                "        \"aggregation_elements\": [" +
                "          \"kpi_simple_60.agg_column_0\"," +
                "          \"kpi_simple_60.agg_column_1\"" +
                "        ]," +
                "        \"exportable\": true," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"sum_column_kpi_1440\"," +
                "            \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\"," +
                "            \"object_type\": \"INTEGER\"," +
                "            \"aggregation_type\": \"SUM\"," +
                "            \"execution_group\": \"COMPLEX1\"" +
                "          }," +
                "          {" +
                "            \"name\": \"sum_column_kpi_1440\"," +
                "            \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\"," +
                "            \"object_type\": \"FLOAT\"," +
                "            \"aggregation_type\": \"SUM\"," +
                "            \"execution_group\": \"COMPLEX1\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postKpis(json);
        //  "KPIs have conflicting definitions in the JSON received"
        assertThat(response.getStatus())
                .as("Expected error code %s when validating KPIs", SC_CONFLICT)
                .isEqualTo(SC_CONFLICT);
    }

    @Test
    @Order(201)
    void whenKpiDefinitionWithSameNamDifferentCollectionIdPassed_then201StatusIsReturned(){
        final String json =
                "{\n" +
                        "  \"scheduled_simple\": {\n" +
                        "    \"kpi_output_tables\": [\n" +
                        "      {\n" +
                        "        \"aggregation_period\": 1440,\n" +
                        "        \"alias\": \"cell_guid_simple\",\n" +
                        "        \"aggregation_elements\": [\n" +
                        "          \"fact_table.nodeFDN\"\n" +
                        "        ],\n" +
                        "        \"exportable\": true,\n" +
                        "        \"data_reliability_offset\": 0,\n" +
                        "        \"inp_data_identifier\": \"dataSpace|category|fact_table\",\n" +
                        "        \"kpi_definitions\": [\n" +
                        "          {\n" +
                        "            \"name\": \"sum_Integer_1440_simple\",\n" +
                        "            \"expression\": \"SUM(fact_table.pmCounters.integerColumn0)\",\n" +
                        "            \"object_type\": \"INTEGER\",\n" +
                        "            \"aggregation_type\": \"SUM\"\n" +
                        "          }]}]}}";

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postKpis(json, OTHER_COLLECTION_ID);
        assertThat(response.getStatus()).isEqualTo(SC_CREATED);
    }

    @Test
    @Order(202)
    void whenKpiDefinitionPostWithWrongUUID_then400StatusIsReturned(){
        final String json ="";

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postKpis(json, WRONG_COLLECTION_ID);
        assertThat(response.getStatus()).isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Disabled("Test is disabled because test tries to send in the same payload twice. which is not possible now, as name should be unique")
    @Order(300)
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenValidKpisArePassedToKpiServiceMultipleTimes_thenStatus201IsReturnedWithCorrectMessageForEachExecution_andDefinitionsAreNotChanged()
            throws Exception {
        final String validKpisPayload = ResourceLoaderUtils.getClasspathResourceAsString(TEST_ON_DEMAND_KPIS_FILE_PATH);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> firstExecution = serviceRestExecutor.putKpis(validKpisPayload);
        final RestResponse<String> secondExecution = serviceRestExecutor.putKpis(validKpisPayload);

        assertThat(firstExecution.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);
        assertThat(secondExecution.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        KpiDatabaseVerifier.verifyKpiDefinitionTable(TEST_COMPLEX_KPI_TOTAL_COUNT + TEST_ON_DEMAND_KPI_TOTAL_COUNT + NOT_DELETED_SIMPLE_KPI_COUNT);
    }

    @Test
    @Order(400)
    @Timeout(10)
    void whenAnUpdateToAKpiIsPassedToKpiService_thenStatus200IsReturned_andDefinitionsIsUpdated() {
        onSuccess().updateKpiDefinition(
                "integer_simple", Optional.empty(), kpiDefinitionRequest -> {
                    kpiDefinitionRequest.setExportable(false);
                    return kpiDefinitionRequest;
                }, (statusCode, response) -> {
                    assertThat(statusCode).isEqualTo(SC_OK);
                    assertThat(response.getName()).isEqualTo("integer_simple");
                    assertThat(response.getExportable()).isFalse();
                }
        );

        pollDatabaseForExpectedValue(configurationBuilder -> {
            configurationBuilder.pollTableName("kpi_definition");
            configurationBuilder.conditionalClause("WHERE exportable = 'false' AND name = 'integer_simple'");
            configurationBuilder.forExpectedCount(1);
            return configurationBuilder.build();
        });
    }

    @Order(410)
    @MethodSource("provideForInvalidPatchRequest")
    @ParameterizedTest(name = "[{index}] Trying to update ''{0}'' with request ''{1}'' expecting status code ''{2}''")
    void whenInvalidKpiDefinitionPatchRequestIsSentToDefinitionEndpoint(
            final String kpiName,
            final String json,
            final Integer expectedStatusCode,
            final String expectedErrorMessage
    ) {
        onFailure().updateKpiDefinition(kpiName, Optional.empty(), json, (statusCode, errorResponse) -> {
            log.info("status code: '{}', error response: '{}'", statusCode, errorResponse);

            assertThat(statusCode).isEqualTo(expectedStatusCode);
            assertThat(errorResponse.status()).isEqualTo(expectedStatusCode);
            assertThat(errorResponse.message()).isEqualTo(expectedErrorMessage);
        });
    }

    @Order(411)
    @Test
    void whenInvalidWhitelistKpiDefinitionPatchRequestIsSentToDefinitionEndpoint() {
        final String kpiName = "integer_simple";
        final String json = "{ \"expression\": \"ACOS(fact_table.pmCounters.integerColumn0)\" }";
        final Integer expectedStatusCode = SC_BAD_REQUEST;
        final String expectedErrorMessage = "KPI Definition 'integer_simple' contains the following prohibited sql elements: 'acos'";

        onFailure().updateKpiDefinition(kpiName, Optional.empty(), json, (statusCode, errorResponse) -> {
            log.info("status code: '{}', error response: '{}'", statusCode, errorResponse);

            assertThat(statusCode).isEqualTo(expectedStatusCode);
            assertThat(errorResponse.status()).isEqualTo(expectedStatusCode);
            assertThat(errorResponse.messages()).contains(expectedErrorMessage);
        });
    }

    @Test
    @Order(500)
    void whenInvalidKpiCalculationRequestIsSentToCalculationEndpoint_then400IsReturnedWithCorrectMessage() throws Exception {
        final String invalidKpiCalculationRequest = ResourceLoaderUtils.getClasspathResourceAsString(KPI_CALCULATION_REQUEST_NOT_IN_DB);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postCalculationRequest(invalidKpiCalculationRequest);

        assertThat(response.getStatus())
                .as("Expected status code %s when submitting invalid KPI calculation request", SC_BAD_REQUEST)
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity())
                .contains("A KPI requested for calculation was not found in the database");
    }

    @Test
    @Disabled("Not compatible with dynamic aggregation elements. Test case will be updated.")
    @Order(600)
    void whenNewKpiWithNoAggregationPeriodIsPassedToKpiService_thenTableIsCreated() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"alias\": \"test_cell_guid\"," +
                "        \"aggregation_elements\": [" +
                "          \"relation.id as guid\"," +
                "          \"relation.source_guid\"," +
                "          \"relation.target_guid\"" +
                "        ]," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"test_intra_site\"," +
                "            \"expression\": \"FIRST(relation.intraSite, true) FROM cm://relation\"," +
                "            \"object_type\": \"BOOLEAN\"," +
                "            \"aggregation_type\": \"FIRST\"," +
                "            \"execution_group\": \"test_group\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(json);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        final String tableName = "kpi_test_cell_guid";
        final List<String> expectedPrimaryKeys = Arrays.asList("guid", "source_guid", "target_guid");
        final Map<String, KpiDataType> expectedColumnTypes = new HashMap<>();
        expectedColumnTypes.put("guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("source_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("target_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("test_intra_site", KpiDataType.POSTGRES_BOOLEAN);

        KpiDatabaseVerifier.validateTableDefinition(tableName, expectedPrimaryKeys, expectedColumnTypes);
    }

    @Test
    @Disabled("Not compatible with dynamic aggregation elements. Test case will be updated.")
    @Order(700)
    void whenKpiWithWithNoAggregationPeriodNewPrimaryKeyIsPassedToKpiService_thenTableIsUpdated() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"alias\": \"test_cell_guid\"," +
                "        \"aggregation_elements\": [" +
                "          \"relation.id as guid\"," +
                "          \"relation.source_guid\"," +
                "          \"relation.target_guid\"," +
                "          \"cell.fdn as test_cell_fdn_daily\"" +   //  new aggregation_elements
                "        ]," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"test_intra_site\"," +
                "            \"expression\": \"cell.fdn FROM cm://cell\"," +
                "            \"object_type\": \"BOOLEAN\"," +
                "            \"aggregation_type\": \"FIRST\"," +
                "            \"execution_group\": \"test_group\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(json);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        final String tableName = "kpi_test_cell_guid";
        final List<String> expectedPrimaryKeys = Arrays.asList("guid", "source_guid", "target_guid", "test_cell_fdn_daily");
        final Map<String, KpiDataType> expectedColumnTypes = new HashMap<>();
        expectedColumnTypes.put("guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("source_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("target_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("test_intra_site", KpiDataType.POSTGRES_BOOLEAN);
        expectedColumnTypes.put("test_cell_fdn_daily", KpiDataType.POSTGRES_LONG);

        validateTableDefinition(tableName, expectedPrimaryKeys, expectedColumnTypes);
    }

    @Test
    @Disabled("Not compatible with dynamic aggregation elements. Test case will be updated.")
    @Order(800)
    void whenKpiWithNoAggregationPeriodWithModifiedDataTypeIsPassedToKpiService_thenTableIsUpdated() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"alias\": \"test_cell_guid\"," +
                "        \"aggregation_elements\": [" +
                "          \"relation.id as guid\"," +
                "          \"relation.source_guid\"," +
                "          \"relation.target_guid\"," +
                "          \"cell.fdn as test_cell_fdn_daily\"" +
                "        ]," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"test_intra_site\"," +
                "            \"expression\": \"FIRST(relation.intraSite, 0) FROM cm://relation\"," +
                "            \"object_type\": \"STRING\"," +    //  new object_type
                "            \"aggregation_type\": \"FIRST\"," +
                "            \"execution_group\": \"test_group\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(json);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        final String tableName = "kpi_test_cell_guid";
        final List<String> expectedPrimaryKeys = Arrays.asList("guid", "source_guid", "target_guid", "test_cell_fdn_daily");
        final Map<String, KpiDataType> expectedColumnTypes = new HashMap<>();
        expectedColumnTypes.put("guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("source_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("target_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("test_intra_site", KpiDataType.POSTGRES_STRING);
        expectedColumnTypes.put("test_cell_fdn_daily", KpiDataType.POSTGRES_LONG);

        validateTableDefinition(tableName, expectedPrimaryKeys, expectedColumnTypes);
    }

    @Test
    @Disabled("Not compatible with dynamic aggregation elements. Test case will be updated.")
    @Order(1_000)
    void whenNewKpiWithAggregationPeriodIsPassedToKpiService_thenTableIsCreated() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"aggregation_period\": 1440," +
                "        \"alias\": \"test_cell_guid\"," +
                "        \"aggregation_elements\": [" +
                "          \"relation.id as guid\"," +
                "          \"relation.source_guid\"," +
                "          \"relation.target_guid\"" +
                "        ]," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"test_intra_site\"," +
                "            \"expression\": \"FIRST(relation.intraSite, true) FROM cm://relation\"," +
                "            \"object_type\": \"BOOLEAN\"," +
                "            \"aggregation_type\": \"FIRST\"," +
                "            \"execution_group\": \"test_group\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(json);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        final String tableName = "kpi_test_cell_guid_1440";
        final List<Column> expectedUniqueKeyColumns = Arrays.asList(Column.of("guid"),
                Column.of("source_guid"),
                Column.of("target_guid"),
                Column.of("aggregation_begin_time"));
        expectedUniqueKeyColumns.sort(Comparator.comparing(Column::getName));
        final Map<String, KpiDataType> expectedColumnTypes = new HashMap<>();
        expectedColumnTypes.put("guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("source_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("target_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("test_intra_site", KpiDataType.POSTGRES_BOOLEAN);
        expectedColumnTypes.put("aggregation_begin_time", KpiDataType.POSTGRES_TIMESTAMP);
        expectedColumnTypes.put("aggregation_end_time", KpiDataType.POSTGRES_TIMESTAMP);

        KpiDatabaseVerifier.validatePartitionedTableDefinition(tableName, expectedUniqueKeyColumns, expectedColumnTypes);
    }

    @Test
    @Disabled("Not compatible with dynamic aggregation elements. Test case will be updated.")
    @Order(1_100)
    void whenExistingKpiWithNewAggregationPeriodIsPassedToKpiService_thenTableIsUpdated() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"aggregation_period\": 1440," +
                "        \"alias\": \"test_cell_guid\"," +
                "        \"aggregation_elements\": [" +
                "          \"relation.id as guid\"," +
                "          \"relation.source_guid\"," +
                "          \"relation.target_guid\"," +
                "          \"relation.aggregation_1\"" +    //  new aggregation_elements
                "        ]," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"test_intra_site\"," +
                "            \"expression\": \"FIRST(relation.intraSite, true) FROM cm://relation\"," +
                "            \"object_type\": \"BOOLEAN\"," +
                "            \"aggregation_type\": \"FIRST\"," +
                "            \"execution_group\": \"test_group\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(json);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        final String tableName = "kpi_test_cell_guid_1440";
        final List<Column> expectedUniqueKeyColumns = Arrays.asList(Column.of("guid"), Column.of("source_guid"), Column.of("target_guid"), Column.of("aggregation_1"), Column.of("aggregation_begin_time"));
        expectedUniqueKeyColumns.sort(Comparator.comparing(Column::getName));
        final Map<String, KpiDataType> expectedColumnTypes = new HashMap<>();
        expectedColumnTypes.put("guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("source_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("target_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("test_intra_site", KpiDataType.POSTGRES_BOOLEAN);
        expectedColumnTypes.put("aggregation_begin_time", KpiDataType.POSTGRES_TIMESTAMP);
        expectedColumnTypes.put("aggregation_end_time", KpiDataType.POSTGRES_TIMESTAMP);
        expectedColumnTypes.put("aggregation_1", KpiDataType.POSTGRES_LONG);

        KpiDatabaseVerifier.validatePartitionedTableDefinition(tableName, expectedUniqueKeyColumns, expectedColumnTypes);
    }

    @Test
    @Disabled("Not compatible with dynamic aggregation elements. Test case will be updated.")
    @Order(1_200)
    void whenExistingKpiWithUpdatedDatatypeIsPassedToKpiService_thenTableIsUpdated() {
        final String json =
                '{' +
                "  \"scheduled_complex\": {" +
                "    \"kpi_output_tables\": [{" +
                "        \"aggregation_period\": 1440," +
                "        \"alias\": \"test_cell_guid\"," +
                "        \"aggregation_elements\": [" +
                "          \"relation.id as guid\"," +
                "          \"relation.source_guid\"," +
                "          \"relation.target_guid\"," +
                "          \"relation.aggregation_1\"" +
                "        ]," +
                "        \"kpi_definitions\": [" +
                "          {" +
                "            \"name\": \"test_intra_site\"," +
                "            \"expression\": \"FIRST(relation.intraSite, true) FROM cm://relation\"," +
                "            \"object_type\": \"INTEGER\"," + // new object_type
                "            \"aggregation_type\": \"FIRST\"," +
                "            \"execution_group\": \"test_group\"" +
                "          }" +
                "        ]" +
                "    }]" +
                "  }" +
                '}';

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.putKpis(json);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", SC_CREATED)
                .isEqualTo(SC_CREATED);

        final String tableName = "kpi_test_cell_guid_1440";
        final List<Column> expectedUniqueKeyColumns = Arrays.asList(Column.of("guid"), Column.of("source_guid"), Column.of("target_guid"), Column.of("aggregation_1"), Column.of("aggregation_begin_time"));
        expectedUniqueKeyColumns.sort(Comparator.comparing(Column::getName));
        final Map<String, KpiDataType> expectedColumnTypes = new HashMap<>();
        expectedColumnTypes.put("guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("source_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("target_guid", KpiDataType.POSTGRES_LONG);
        expectedColumnTypes.put("test_intra_site", KpiDataType.POSTGRES_INTEGER);
        expectedColumnTypes.put("aggregation_begin_time", KpiDataType.POSTGRES_TIMESTAMP);
        expectedColumnTypes.put("aggregation_end_time", KpiDataType.POSTGRES_TIMESTAMP);
        expectedColumnTypes.put("aggregation_1", KpiDataType.POSTGRES_LONG);

        KpiDatabaseVerifier.validatePartitionedTableDefinition(tableName, expectedUniqueKeyColumns, expectedColumnTypes);
    }

    @Test
    @Order(1_250)
    void whenValidCalculationIdsAreRequested_thenCalculationStateResponseIsReturned() throws Exception {
        verifySimpleCalculationStateResponse();
        verifyComplexCalculationStateResponse();
        verifyOnDemandCalculationStateResponse();
    }

    @Test
    @Order(1_350)
    void shouldReturnNotFound_whenCalculationStateIsNotFoundById() {
        final UUID unknownCalculationId = UUID.fromString("9c7b119c-06af-48b8-8fb7-a8165ac4ab58");

        final ErrorResponse errorResponse = fetchCalculationStateById(unknownCalculationId, ErrorResponse.class);

        ErrorResponseAssertions.assertThat(errorResponse).isNotFoundWithMessage(
                String.format("Calculation state with id '%s' is not found", unknownCalculationId)
        );
    }

    @Order(1_350)
    @MethodSource("provideForInvalidDeleteRequest")
    @ParameterizedTest(name = "[{index}] Trying to delete KPIs: ''{0}''")
    void whenInValidDeleteRequestIsSentToDefinitionEndpoint(final String payload, final String errorMessageExpected) {
        onFailure().deleteKpiDefinitions(payload, (statusCode, response) -> {
            assertThat(statusCode).isEqualTo(SC_BAD_REQUEST);
            assertThat(response.status()).isEqualTo(SC_BAD_REQUEST);
            assertThat(response.message()).isEqualTo(errorMessageExpected);
        });
    }

    @Order(1_360)
    @MethodSource("provideForValidDeleteRequest")
    @ParameterizedTest(name = "[{index}] Trying to delete KPIS: ''{0}''")
    void whenValidDeleteRequestIsSentToDefinitionEndpoint_thenStatus204IsReturned_andDefinitionsAreSoftDeleted(final List<String> payload) {
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

    static Stream<Arguments> provideForInvalidPatchRequest() {
        final String validJson = "{ \"exportable\": false }";
        final String jsonUnknownField = "{ \"unknown\": \"unknown\" }";
        final String jsonUnmodifiableField = "{ \"aggregation_type\": \"SUM\" }";
        final String jsonUnknownObjectType = "{ \"object_type\": \"unknown\" }";
        final String jsonInvalidObjectTypeChange = "{ \"object_type\": \"BOOLEAN\" }";

        return Stream.of(
                Arguments.of("non_existent", validJson, SC_NOT_FOUND, "KPI was not found by name 'non_existent'"),
                Arguments.of("integer_simple", jsonUnknownField, SC_BAD_REQUEST, "'unknown' is not a recognizable property name"),
                Arguments.of("integer_simple", jsonUnmodifiableField, SC_BAD_REQUEST, "Property 'aggregation_type' cannot be modified in a PATCH request"),
                Arguments.of("integer_simple", jsonUnknownObjectType, SC_BAD_REQUEST, "'unknown' is not a valid ObjectType"),
                Arguments.of("integer_simple", jsonInvalidObjectTypeChange, SC_INTERNAL_SERVER_ERROR, "Column data type change from 'POSTGRES_INTEGER' to 'POSTGRES_BOOLEAN' for table: 'kpi_simple_60' is invalid")
        );
    }

    static Stream <Arguments> provideForInvalidDeleteRequest() {
        final String errorMessageFormat = "The Following KPIs have dependencies that would be deleted: %s";

        final String simpleThatHasDependencies = "[\"integer_array_simple\", \"first_integer_aggregate_slice_1440\", \"first_integer_1440_join_kpidb_filter\"]";
        final String simpleError = "[integer_array_complex: [integer_array_simple], udf_param: [integer_array_simple], udf_tabular_param: [integer_array_simple]]";
        final String complexThatHasDependencies = "[\"copy_array_15\", \"first_integer_complex_15\"]";
        final String complexError = "[transform_complex_15: [copy_array_15, first_integer_complex_15]]";
        final String onDemandThatHasDependencies = "[\"first_integer_operator_60_stage2\", \"first_integer_operator_60_stage3\"]";
        final String onDemandError = "[first_integer_operator_60_stage4: [first_integer_operator_60_stage2, first_integer_operator_60_stage3]]";

        return Stream.of(
            Arguments.of(simpleThatHasDependencies, String.format(errorMessageFormat, simpleError)),
            Arguments.of(complexThatHasDependencies, String.format(errorMessageFormat, complexError)),
            Arguments.of(onDemandThatHasDependencies, String.format(errorMessageFormat, onDemandError))
        );
    }


    static Stream <Arguments> provideForValidDeleteRequest() {
        final List<String> validSimple = List.of("integer_array_simple", "integer_array_complex", "first_integer_aggregate_slice_1440", "first_integer_1440_join_kpidb_filter", "udf_param", "udf_tabular_param");
        final List<String> validComplex = List.of("copy_array_15", "first_integer_complex_15", "transform_complex_15");
        final List<String> validOnDemand = List.of("first_integer_operator_60_stage2", "first_integer_operator_60_stage3", "first_integer_operator_60_stage4");

        return Stream.of(
            Arguments.of(validSimple),
            Arguments.of(validComplex),
            Arguments.of(validOnDemand)
        );
    }

    private static void verifyOnDemandCalculationStateResponse() {
        /* ON_DEMAND calculations have no readiness logs */
        final Calculation onDemandCalculation = forceFetchCalculationByExecutionGroup("ON_DEMAND");
        final CalculationStateResponse actual = fetchCalculationStateById(onDemandCalculation.getCalculationId(), CalculationStateResponse.class);
        assertThat(actual.getCalculationId()).isEqualTo(onDemandCalculation.getCalculationId());
        assertThat(actual.getExecutionGroup()).isEqualTo(onDemandCalculation.getExecutionGroup());
        assertThat(actual.getStatus()).isEqualTo(onDemandCalculation.getKpiCalculationState().name());
        assertThat(actual.getReadinessLogs()).isEmpty();
    }

    private static void verifyComplexCalculationStateResponse() throws SQLException {
        final List<CalculationReadinessLog> calculationReadinessLogs = fetchCalculationReadinessLog(COMPLEX_EXEC_GROUP_1_TRIGGERED, 0);
        final Calculation complexCalculation = complexCalculation(calculationReadinessLogs);
        final List<ReadinessLog> complexReadinessLogs = complexReadinessLog(calculationReadinessLogs);
        final CalculationStateResponse actual = fetchCalculationStateById(complexCalculation.getCalculationId(), CalculationStateResponse.class);
        assertThat(actual.getCalculationId()).isEqualTo(complexCalculation.getCalculationId());
        assertThat(actual.getExecutionGroup()).isEqualTo(complexCalculation.getExecutionGroup());
        assertThat(actual.getStatus()).isEqualTo(complexCalculation.getKpiCalculationState().name());
        assertThat(actual.getReadinessLogs()).containsExactlyInAnyOrderElementsOf(
                complexReadinessLogs.stream().map(KpiApiIntTests::toReadinessLogResponse).collect(Collectors.toList())
        );
    }

    private static void verifySimpleCalculationStateResponse() {
        final Calculation simpleCalculation = forceFetchCalculationByExecutionGroup(SIMPLE_EXEC_GROUP_1);
        final CalculationStateResponse actual = fetchCalculationStateById(simpleCalculation.getCalculationId(), CalculationStateResponse.class);

        assertThat(actual.getCalculationId()).isEqualTo(simpleCalculation.getCalculationId());
        assertThat(actual.getExecutionGroup()).isEqualTo(simpleCalculation.getExecutionGroup());
        assertThat(actual.getStatus()).isEqualTo(simpleCalculation.getKpiCalculationState().name());
        assertThat(actual.getReadinessLogs()).isEqualTo(fetchReadinessLogResponseByCalculationId(simpleCalculation.getCalculationId()));
    }

    private static ReadinessLogResponse toReadinessLogResponse(@NonNull final ReadinessLog readinessLog) {
        return ReadinessLogResponse.builder()
                                   .withDatasource(readinessLog.getDatasource())
                                   .withCollectedRowsCount(readinessLog.getCollectedRowsCount())
                                   .withEarliestCollectedData(readinessLog.getEarliestCollectedData())
                                   .withLatestCollectedData(readinessLog.getLatestCollectedData())
                                   .build();
    }

    @SneakyThrows
    private static <T> T fetchCalculationStateById(final UUID calculationId, final Class<T> valueType) {
        try (final CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            final HttpUriRequest httpGet = new HttpGet(URI.create(String.format(
                    "http://eric-oss-pm-stats-calculator:8080/kpi-handling/calc/v1/calculations/%s", calculationId
            )));

            try (final CloseableHttpResponse response = closeableHttpClient.execute(httpGet)) {
                final String entity = EntityUtils.toString(response.getEntity());

                return OBJECT_MAPPER.readValue(entity, valueType);
            }
        }
    }

    private static JsonMapper objectMapper() {
        final Builder builder = JsonMapper.builder();
        builder.addModule(new JavaTimeModule());
        return builder.build();
    }
}
