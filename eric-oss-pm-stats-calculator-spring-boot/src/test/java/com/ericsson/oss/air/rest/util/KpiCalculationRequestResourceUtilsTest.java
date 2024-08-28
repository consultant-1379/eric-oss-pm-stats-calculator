/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.util;

import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.springframework.http.ResponseEntity;

class KpiCalculationResourceUtilsTest {
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 14), LocalTime.NOON);
    private KpiCalculationRequestPayload kpiCalculationRequestPayloadMock;

    @BeforeEach
    void setUp() {
        kpiCalculationRequestPayloadMock = mock(KpiCalculationRequestPayload.class);
    }

    @Test
    void shouldReturnNull_whenNoInvalidKpiNamesFoundInThePayload() {
        try (final MockedStatic<KpiCalculationRequestUtils> requestUtilsMockedStatic = mockStatic(KpiCalculationRequestUtils.class)) {
            final Verification verification = () -> KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(kpiCalculationRequestPayloadMock);
            requestUtilsMockedStatic.when(verification).thenReturn(false);

            final ResponseEntity<ErrorResponse> actual = KpiCalculationRequestResourceUtils.findInvalidKpiNamesInPayload(kpiCalculationRequestPayloadMock);

            requestUtilsMockedStatic.verify(verification);

            Assertions.assertThat(actual).isNull();
        }
    }

    @Test
    void shouldReturnErrorResponse_whenInvalidKpiNamesFoundInThePayload() {
        try (final MockedStatic<KpiCalculationRequestUtils> requestUtilsMockedStatic = mockStatic(KpiCalculationRequestUtils.class);
             final MockedStatic<LocalDateTime> localDateTimeMockedStatic = mockStatic(LocalDateTime.class)) {
            final Verification verification = () -> KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(kpiCalculationRequestPayloadMock);
            requestUtilsMockedStatic.when(verification).thenReturn(true);
            localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(TEST_TIME);

            final ResponseEntity<ErrorResponse> actual = KpiCalculationRequestResourceUtils.findInvalidKpiNamesInPayload(kpiCalculationRequestPayloadMock);

            requestUtilsMockedStatic.verify(verification);
            localDateTimeMockedStatic.verify(LocalDateTime::now);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
            Assertions.assertThat(actual)
                    .extracting(ResponseEntity::getBody, type(ErrorResponse.class))
                    .satisfies(errorResponse -> {
                        Assertions.assertThat(errorResponse.message()).startsWith("The KPI calculation request payload must not be null");
                        Assertions.assertThat(errorResponse.status()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
                        Assertions.assertThat(errorResponse.error()).isEqualTo(Status.BAD_REQUEST.getReasonPhrase());
                        Assertions.assertThat(errorResponse)
                                .extracting(ErrorResponse::timestamp, LOCAL_DATE_TIME);
                    });
        }
    }

    @Test
    void shouldReturnKpiCalculationRequestSuccessResponse() {
        try (final MockedStatic<DefinitionResourceUtils> definitionResourceUtilsMockedStatic = mockStatic(DefinitionResourceUtils.class)) {
            final CalculationRequestSuccessResponse calculationRequestSuccessResponseMock = mock(CalculationRequestSuccessResponse.class);

            final String successMessage = "successMessage";
            final UUID calculationId = UUID.fromString("19ccc30c-b6e8-47a9-b7c8-8f20d7dd7617");
            final Map<String, String> kpiOutputLocations = Collections.emptyMap();

            final Verification verification = () -> DefinitionResourceUtils.getCalculationRequestSuccessResponse(successMessage, calculationId, kpiOutputLocations);

            definitionResourceUtilsMockedStatic.when(verification).thenReturn(calculationRequestSuccessResponseMock);

            final CalculationRequestSuccessResponse actual = KpiCalculationRequestResourceUtils.getKpiCalculationRequestSuccessResponse(successMessage, calculationId, kpiOutputLocations);

            definitionResourceUtilsMockedStatic.verify(verification);

            Assertions.assertThat(actual).isEqualTo(calculationRequestSuccessResponseMock);
        }
    }
}