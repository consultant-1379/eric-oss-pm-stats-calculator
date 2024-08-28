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

import static com.ericsson.oss.air.pm.stats.test.util.WebFluxUtils.getCalculationResponseUntilFinished;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;
import com.ericsson.oss.air.pm.stats.test.integration.IntegrationTest;
import com.ericsson.oss.air.pm.stats.test.util.CalculationRequestHandler;
import com.ericsson.oss.air.pm.stats.test.util.ServiceRestExecutor;
import com.ericsson.oss.air.pm.stats.test.util.WebFluxUtils;
import com.ericsson.oss.air.pm.stats.test.verification.KpiDatabaseVerifier;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiCellDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiCellSectorDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiExecutionIdDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiOnDemandFdnAggDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiOnDemandFdnEdgeDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiRollingAggregationDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.KpiSectorDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.OnDemandKpiParameterTypesDailyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.KpiCellHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.KpiRelationHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.KpiSectorHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.OnDemandFdnHourlyDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.quarter.KpiLimitedOnDemandQuarterDataset;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@Slf4j
@IntegrationTest
public class KpiCalculatorOnDemandIntTestMethods {

    private static final String TEST_ON_DEMAND_FILE_PATH = "TestOnDemandKpis.json";
    private static final String KPI_CALCULATION_REQUEST_1_FILE_PATH = "KpiCalculationRequest_1.json";
    private static final String KPI_CALCULATION_REQUEST_2_FILE_PATH = "KpiCalculationRequest_2.json";
    private static final String KPI_CALCULATION_REQUEST_3_FILE_PATH = "KpiCalculationRequest_3.json";
    private static final String KPI_CALCULATION_REQUEST_FDN_FILE_PATH = "KpiCalculationRequest_fdn.json";
    private static final String KPI_CALCULATION_REQUEST_MISSING_PARAMETERS_FILE_PATH = "KpiCalculationRequest_MissingParameters.json";
    private static final Map<String, List<String>> ON_DEMAND_EXPECTED_TABLE_NAME_TO_COLUMNS = new HashMap<>();
    private static final int MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST_SECONDS = 20;
    private static final int RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS = 30;
    private static final CalculationRequestHandler KPI_CALCULATION_REQUEST_HANDLER = new CalculationRequestHandler(
            MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST_SECONDS,
            RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS);

    public static void setUpOnDemandTest() {
        populateExpectedOnDemandMaps(KpiCellDailyDataset.INSTANCE);
        populateExpectedOnDemandMaps(KpiCellHourlyDataset.INSTANCE);

        populateExpectedOnDemandMaps(KpiRelationHourlyDataset.INSTANCE);

        populateExpectedOnDemandMaps(KpiCellSectorDailyDataset.INSTANCE);

        populateExpectedOnDemandMaps(KpiSectorDailyDataset.INSTANCE);
        populateExpectedOnDemandMaps(KpiSectorHourlyDataset.INSTANCE);

        populateExpectedOnDemandMaps(KpiRollingAggregationDailyDataset.INSTANCE);

        populateExpectedOnDemandMaps(KpiExecutionIdDailyDataset.INSTANCE);

        populateExpectedOnDemandMaps(KpiOnDemandFdnEdgeDailyDataset.INSTANCE);
        populateExpectedOnDemandMaps(KpiOnDemandFdnAggDailyDataset.INSTANCE);

        populateExpectedOnDemandMaps(OnDemandKpiParameterTypesDailyDataset.INSTANCE);

        populateExpectedOnDemandMaps(OnDemandFdnHourlyDataset.INSTANCE);
    }

    public static void whenValidKpisArePassedToKpiServiceDefinitionEndpoint_thenStatus201IsReturnedWithCorrectMessage()
            throws Exception {
        final String validKpisPayload = ResourceLoaderUtils.getClasspathResourceAsString(TEST_ON_DEMAND_FILE_PATH);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postKpis(validKpisPayload);

        assertThat(response.getStatus())
                .as("Expected status code %s when validating KPIs", HttpStatus.SC_CREATED)
                .isEqualTo(HttpStatus.SC_CREATED);
    }

    public static void whenKpiDefinitionsArePersisted_thenRequestKpiCalculationsAndWaitUntilFinished() {
        Optional<UUID> calculationId1 = sendOnDemandCalculationRequest(KPI_CALCULATION_REQUEST_1_FILE_PATH);
        assertTrue(calculationId1.isPresent());
        waitForCalculationRequestToBeFinished(calculationId1.get());

        Optional<UUID> calculationId2 = sendOnDemandCalculationRequest(KPI_CALCULATION_REQUEST_2_FILE_PATH);
        assertTrue(calculationId2.isPresent());
        waitForCalculationRequestToBeFinished(calculationId2.get());

        Optional<UUID> calculationId3 = sendOnDemandCalculationRequest(KPI_CALCULATION_REQUEST_3_FILE_PATH);
        assertTrue(calculationId3.isPresent());
        waitForCalculationRequestToBeFinished(calculationId3.get());

        Optional<UUID> calculationIdFdn = sendOnDemandCalculationRequest(KPI_CALCULATION_REQUEST_FDN_FILE_PATH);
        assertTrue(calculationIdFdn.isPresent());
        waitForCalculationRequestToBeFinished(calculationIdFdn.get());
    }

    public static void whenKpiDefinitionsArePersisted_thenRequestKpiCalculationWithGzippedAndWaitUntilFinished() {
        final Mono<ResponseEntity<CalculationRequestSuccessResponse>> calculationResponse = WebFluxUtils.postCalculationRequest(KPI_CALCULATION_REQUEST_2_FILE_PATH, true);
        calculationResponse.subscribe(
                (onNext) -> {
                    log.info("GZIP RESPONSE ARRIVED!");
                    final CalculationRequestSuccessResponse response = onNext.getBody();
                    assertTrue(response.getCalculationId() != null);
                    waitForCalculationRequestToBeFinished(response.getCalculationId());
                },
                (onError) -> {
                    log.info("ERROR IN WEBCLIENT:" + onError.getMessage());
                },
                () -> log.info("GZIP TEST COMPLETED!")
        );

    }

    public static void whenKpiCalculationsAreFinished_thenKpiOutputTableColumnsMustBeChanged() {
        for (final String tableName : ON_DEMAND_EXPECTED_TABLE_NAME_TO_COLUMNS.keySet()) {
            assertThat(KpiDatabaseVerifier.verifyTableColumns(tableName, ON_DEMAND_EXPECTED_TABLE_NAME_TO_COLUMNS.get(tableName))).isTrue();
        }
    }

    public static void whenKpiCalculationsAreFinished_thenAggregatedKpiValuesMustMatchInDatabase() {

        // cell tables
        KpiDatabaseVerifier.assertTableContents(KpiCellDailyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiCellHourlyDataset.INSTANCE);

        // relation table
        KpiDatabaseVerifier.assertTableContents(KpiRelationHourlyDataset.INSTANCE);

        // cell sector tables
        KpiDatabaseVerifier.assertTableContents(KpiCellSectorDailyDataset.INSTANCE);

        // sector tables
        KpiDatabaseVerifier.assertTableContents(KpiSectorDailyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiSectorHourlyDataset.INSTANCE);

        // rolling aggregation table
        KpiDatabaseVerifier.assertTableContents(KpiRollingAggregationDailyDataset.INSTANCE);

        // execution id table
        KpiDatabaseVerifier.assertTableContents(KpiExecutionIdDailyDataset.INSTANCE);

        //15 min table
        KpiDatabaseVerifier.assertTableContents(KpiLimitedOnDemandQuarterDataset.INSTANCE);

        // FDN tables
        KpiDatabaseVerifier.assertTableContents(KpiOnDemandFdnEdgeDailyDataset.INSTANCE);
        KpiDatabaseVerifier.assertTableContents(KpiOnDemandFdnAggDailyDataset.INSTANCE);

        //Parameter type casting check
        KpiDatabaseVerifier.assertTableContents(OnDemandKpiParameterTypesDailyDataset.INSTANCE);

        // SDK FDN with filter
        KpiDatabaseVerifier.assertTableContents(OnDemandFdnHourlyDataset.INSTANCE);
    }

    public static void whenParameterizedKpiIsRequestedForCalculation_andParametersAreNotSatisfied_then400IsReturnedWithCorrectMessage() throws Exception {
        final String invalidKpiCalculationRequest = ResourceLoaderUtils.getClasspathResourceAsString(KPI_CALCULATION_REQUEST_MISSING_PARAMETERS_FILE_PATH);

        final ServiceRestExecutor serviceRestExecutor = new ServiceRestExecutor.Builder().build();
        final RestResponse<String> response = serviceRestExecutor.postCalculationRequest(invalidKpiCalculationRequest);

        assertThat(response.getStatus())
                .as("Expected status code %s when submitting invalid KPI calculation request", HttpStatus.SC_BAD_REQUEST)
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity())
                .contains("The following tabular parameters are not present for the triggered KPIs: '[[first_integer_dim_enrich_1440: [tabular_parameters.cell_configuration_test]]]'");
    }

    private static Optional<UUID> sendOnDemandCalculationRequest(final String fileName) {

        final String kpiCalculationRequestPayload;
        try {
            kpiCalculationRequestPayload = ResourceLoaderUtils.getClasspathResourceAsString(fileName);
        } catch (IOException e) {
            log.error("Calculation request json file {} is invalid", fileName);
            return Optional.empty();
        }

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK).withZone(ZoneOffset.UTC);
        final String twoDaysAgo = dateTimeFormatter.format(LocalDate.now(ZoneOffset.UTC).minusDays(2));
        final String kpiCalculationRequestPayloadReplaced = kpiCalculationRequestPayload.replace("<DATE_FOR_FILTER>", String.format("\"%s\"", twoDaysAgo));
        final RestResponse<String> response = KPI_CALCULATION_REQUEST_HANDLER.sendKpiCalculationRequest(kpiCalculationRequestPayloadReplaced);

        if (response.getStatus() == HttpStatus.SC_CREATED) {
            final CalculationRequestSuccessResponse calculationRequestSuccessResponse = new Gson().fromJson(response.getEntity(),
                    CalculationRequestSuccessResponse.class);
            return Optional.of(calculationRequestSuccessResponse.getCalculationId());
        }

        return Optional.empty();
    }

    private static void waitForCalculationRequestToBeFinished(UUID calculationId) {
        getCalculationResponseUntilFinished(calculationId);
    }

    private static void populateExpectedOnDemandMaps(final AbstractDataset expectedDataset) {
        ON_DEMAND_EXPECTED_TABLE_NAME_TO_COLUMNS.put(expectedDataset.getTableName(), expectedDataset.getExpectedColumns());
    }
}
