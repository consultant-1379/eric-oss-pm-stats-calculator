/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINISHED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.IN_PROGRESS;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.NOTHING_CALCULATED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.STARTED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.ON_DEMAND;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_COMPLEX;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_SIMPLE;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationStateResponse;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation.CalculationBuilder;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.log.LoggerHandler;
import com.ericsson.oss.air.rest.util.KpiCalculationRequestHandler;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class KpiCalculationResourceImplTest {
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 7), LocalTime.NOON);

    @Mock KpiCalculationRequestHandler kpiCalculationRequestHandlerMock;
    @Mock CalculationService calculationServiceMock;
    @Mock LoggerHandler loggerHandlerMock;
    @Mock HttpServletRequest httpServletRequestMock;

    @InjectMocks
    KpiCalculationResourceImpl objectUnderTest;

    @BeforeEach
    void setUp() {
        when(httpServletRequestMock.getRequestURI()).thenReturn("/kpis");
    }

    @AfterEach
    void tearDown() {
        verify(httpServletRequestMock).getRequestURI();
    }

    @Nested
    class FindCalculationsStartedAfter {
        final UUID uuid1 = uuid("84503670-dee8-44df-907f-959b55accd57");
        final UUID uuid2 = uuid("a6a1a98a-7a4c-47a4-8280-996a2dc4656f");
        final UUID uuid3 = uuid("7740fa60-1db4-451f-8234-d24264971355");
        final UUID uuid4 = uuid("a12773fd-3301-4b93-8106-a8b46807e162");

        @Test
        void shouldFindCalculationsCompletedAfter_All() {
            mockingFindCalculationsCreatedWithin();

            final ResponseEntity<Collection<CalculationResponse>> actual = objectUnderTest.findCalculationsCreatedAfter(15, true);

            verify(calculationServiceMock).findCalculationsCreatedWithin(Duration.ofMinutes(15));
            verify(loggerHandlerMock).logAudit(any(), eq("GET request received at '/kpis'"));

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(200);
            Assertions.assertThat(actual.getBody()).asInstanceOf(list(CalculationResponse.class)).satisfiesExactly(
                    element1 -> assertCalculationResponse(element1, SCHEDULED_SIMPLE, NOTHING_CALCULATED, uuid1),
                    element2 -> assertCalculationResponse(element2, ON_DEMAND, FINISHED, uuid4),
                    element3 -> assertCalculationResponse(element3, SCHEDULED_COMPLEX, STARTED, uuid2),
                    element4 -> assertCalculationResponse(element4, ON_DEMAND, IN_PROGRESS, uuid3)
            );
        }

        @Test
        void shouldFindCalculationsCompletedAfter_WithoutNothingCalculated() {
            mockingFindCalculationsCreatedWithin();

            final ResponseEntity<Collection<CalculationResponse>> actual = objectUnderTest.findCalculationsCreatedAfter(15, false);

            verify(calculationServiceMock).findCalculationsCreatedWithin(Duration.ofMinutes(15));
            verify(loggerHandlerMock).logAudit(any(), eq("GET request received at '/kpis'"));
            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(200);
            Assertions.assertThat(actual.getBody()).asInstanceOf(list(CalculationResponse.class)).satisfiesExactly(
                    element1 -> assertCalculationResponse(element1, ON_DEMAND, FINISHED, uuid4),
                    element2 -> assertCalculationResponse(element2, SCHEDULED_COMPLEX, STARTED, uuid2),
                    element3 -> assertCalculationResponse(element3, ON_DEMAND, IN_PROGRESS, uuid3)
            );
        }

        private void assertCalculationResponse(
                final CalculationResponse actual, final KpiType kpiType, final KpiCalculationState state, final UUID id
        ) {
            Assertions.assertThat(actual.getExecutionGroup()).isEqualTo("execution_group");
            Assertions.assertThat(actual.getKpiType()).isEqualTo(kpiType);
            Assertions.assertThat(actual.getStatus()).isEqualTo(state);
            Assertions.assertThat(actual.getCalculationId()).isEqualTo(id);
        }

        void mockingFindCalculationsCreatedWithin() {
            when(calculationServiceMock.findCalculationsCreatedWithin(Duration.ofMinutes(15))).thenReturn(List.of(
                    calculation(uuid1, testTime(10), NOTHING_CALCULATED, SCHEDULED_SIMPLE),
                    calculation(uuid2, testTime(5), STARTED, SCHEDULED_COMPLEX),
                    calculation(uuid3, testTime(0), IN_PROGRESS, ON_DEMAND),
                    calculation(uuid4, testTime(10), FINISHED, ON_DEMAND)
            ));
        }

        Calculation calculation(
                final UUID uuid, final LocalDateTime timeCreated, final KpiCalculationState kpiCalculationState,
                final KpiType kpiType
        ) {
            final CalculationBuilder builder = Calculation.builder();
            builder.withCalculationId(uuid);
            builder.withTimeCreated(timeCreated);
            builder.withExecutionGroup("execution_group");
            builder.withKpiCalculationState(kpiCalculationState);
            builder.withKpiType(kpiType);
            return builder.build();
        }

        UUID uuid(final String name) {
            return UUID.fromString(name);
        }

        LocalDateTime testTime(final int minute) {
            return LocalDateTime.of(2_022, Month.DECEMBER, 5, 12, minute);
        }
    }

    @Test
    void shouldReturnResponse_whenKpiCalculationCreationSucceeds(@Mock final KpiCalculationRequestPayload kpiCalculationRequestPayloadMock,
                                                                 @Mock final ResponseEntity<?> responseEntityMock) {
        when(kpiCalculationRequestHandlerMock.handleKpiCalculationRequest(kpiCalculationRequestPayloadMock)).thenAnswer(invocation -> responseEntityMock);

        final ResponseEntity<?> actual = objectUnderTest.calculateKpis(kpiCalculationRequestPayloadMock);

        verify(kpiCalculationRequestHandlerMock).handleKpiCalculationRequest(kpiCalculationRequestPayloadMock);
        verify(loggerHandlerMock).logAudit(any(), eq("POST request received at '/kpis'"));

        Assertions.assertThat(actual).isEqualTo(responseEntityMock);
    }

    @Test
    void shouldReturnErrorResponse_whenKpiCalculationCreationFails(@Mock final KpiCalculationRequestPayload kpiCalculationRequestPayloadMock) {
        try (final MockedStatic<LocalDateTime> localDateTimeMockedStatic = mockStatic(LocalDateTime.class)) {
            final Set<String> kpiNames = Sets.newLinkedHashSet("kpi_1", "kpi_2");

            doThrow(KpiCalculatorException.class).when(kpiCalculationRequestHandlerMock).handleKpiCalculationRequest(kpiCalculationRequestPayloadMock);
            when(kpiCalculationRequestPayloadMock.getKpiNames()).thenReturn(kpiNames);
            localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(TEST_TIME);

            final ResponseEntity<?> actual = objectUnderTest.calculateKpis(kpiCalculationRequestPayloadMock);

            verify(kpiCalculationRequestHandlerMock).handleKpiCalculationRequest(kpiCalculationRequestPayloadMock);
            verify(loggerHandlerMock).logAudit(any(), eq("POST request received at '/kpis'"));
            verify(kpiCalculationRequestPayloadMock).getKpiNames();
            localDateTimeMockedStatic.verify(LocalDateTime::now);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            Assertions.assertThat(actual)
                    .extracting(ResponseEntity::getBody, type(ErrorResponse.class))
                    .satisfies(errorResponse -> {
                        Assertions.assertThat(errorResponse.status()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
                        Assertions.assertThat(errorResponse.error()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        Assertions.assertThat(errorResponse)
                                .extracting(ErrorResponse::timestamp, LOCAL_DATE_TIME).isEqualTo(TEST_TIME);
                    });
        }
    }

    @Test
    void shouldReturnResponse_whenKpiCalculationFetchingByIdSucceeds(@Mock final ResponseEntity<CalculationStateResponse> responseMock) {
        final UUID calculationId = UUID.fromString("490fd8a5-5a7d-423a-b8fd-c1b9dd056536");
        when(kpiCalculationRequestHandlerMock.getCalculationState(calculationId)).thenReturn(responseMock);

        final ResponseEntity<CalculationStateResponse> actual = objectUnderTest.getApplicationState(calculationId);

        verify(kpiCalculationRequestHandlerMock).getCalculationState(calculationId);
        verify(loggerHandlerMock).logAudit(any(), eq("GET request received at '/kpis'"));

        Assertions.assertThat(actual).isEqualTo(responseMock);
    }
}
