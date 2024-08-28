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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.TabularParameterFacade;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.Parameter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationStateResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.ReadinessLogResponse;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationExecutionController;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.registry.readiness.log.ReadinessLogRegistryFacade;
import com.ericsson.oss.air.rest.api.KpiCalculationValidator;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.mapper.EntityMapper;
import com.ericsson.oss.air.rest.metric.TabularParamMetrics;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class KpiCalculationRequestHandlerTest {
    @Mock KpiCalculationExecutionController kpiCalculationExecutionControllerMock;
    @Mock KpiCalculationRequestValidator kpiCalculationRequestValidatorMock;
    @Mock CalculationService calculationServiceMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock EntityMapper entityMapperMock;
    @Mock ReadinessLogRegistryFacade readinessLogRegistryFacadeMock;
    @Mock TabularParameterFacade tabularParameterFacadeMock;
    @Mock KpiCalculationValidator kpiCalculationValidatorMock;
    @Mock TabularParamMetrics tabularParamMetricsMock;
    @Mock CalculatorProperties calculatorPropertiesMock;
    @InjectMocks KpiCalculationRequestHandler objectUnderTest;

    @Nested
    @DisplayName("when handleKpiCalculationRequest")
    class WhenHandleKpiCalculationRequest {
        private static final String KPI_NAME_TEST_NAME = "testName";
        private static final String SOURCE = "SOURCE";

        @Test
        void andKpiNamesAreEmpty_thenReturnsBadRequest() {
            assertKpiNamesHandling(Collections.emptySet());
        }

        @Test
        void andMaxOnDemandCalculationExceedsLimit_thenReturnsTooManyRequests() {
            final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of(KPI_NAME_TEST_NAME));
            final ResponseEntity<?> responseMock = mock(ResponseEntity.class);

            when(kpiCalculationRequestValidatorMock.validateKpiCalculationRequest(any(UUID.class), eq(kpiCalculationRequestPayload))).thenAnswer(invocation -> responseMock);
            when(responseMock.getStatusCodeValue()).thenReturn(HttpStatus.SC_CREATED);
            when(calculatorPropertiesMock.getMaxNumberOfParallelOnDemandCalculations()).thenReturn(2);
            when(kpiCalculationExecutionControllerMock.getCurrentOnDemandCalculationCount()).thenReturn(2);

            final ResponseEntity<?> actual = objectUnderTest.handleKpiCalculationRequest(kpiCalculationRequestPayload);

            verify(kpiCalculationValidatorMock).validate(kpiCalculationRequestPayload);
            verify(kpiCalculationRequestValidatorMock).validateKpiCalculationRequest(any(UUID.class), eq(kpiCalculationRequestPayload));
            verify(responseMock).getStatusCodeValue();
            verify(calculatorPropertiesMock, times(2)).getMaxNumberOfParallelOnDemandCalculations();
            verify(kpiCalculationExecutionControllerMock).getCurrentOnDemandCalculationCount();
            verifyNoInteractions(tabularParameterFacadeMock);

            assertResponse(actual,
                    429, /* TOO_MANY_REQUESTS */
                    "PM Stats Calculator is currently handling the maximum number of calculations",
                    Collections.singletonList(KPI_NAME_TEST_NAME));
        }

        @Test
        void andMaxOnDemandCalculationNotExceedsLimit_thenSubmitsCalculationAndReturnsCreated() throws Exception {
            final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of(KPI_NAME_TEST_NAME));
            final ResponseEntity<?> responseMock = mock(ResponseEntity.class);

            when(kpiCalculationRequestValidatorMock.validateKpiCalculationRequest(any(UUID.class), eq(kpiCalculationRequestPayload))).thenAnswer(invocation -> responseMock);
            when(responseMock.getStatusCodeValue()).thenReturn(HttpStatus.SC_CREATED);
            when(calculatorPropertiesMock.getMaxNumberOfParallelOnDemandCalculations()).thenReturn(2);
            when(kpiCalculationExecutionControllerMock.getCurrentOnDemandCalculationCount()).thenReturn(1);

            objectUnderTest.handleKpiCalculationRequest(kpiCalculationRequestPayload);

            final ArgumentCaptor<Calculation> calculationArgumentCaptor = ArgumentCaptor.forClass(Calculation.class);
            final ArgumentCaptor<KpiCalculationJob> kpiCalculationJobArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationJob.class);

            verify(kpiCalculationValidatorMock).validate(kpiCalculationRequestPayload);
            verify(kpiCalculationRequestValidatorMock).validateKpiCalculationRequest(any(UUID.class), eq(kpiCalculationRequestPayload));
            verify(responseMock).getStatusCodeValue();
            verify(calculatorPropertiesMock).getMaxNumberOfParallelOnDemandCalculations();
            verify(kpiCalculationExecutionControllerMock).getCurrentOnDemandCalculationCount();
            verify(calculationServiceMock).save(calculationArgumentCaptor.capture());
            verify(kpiDefinitionServiceMock).saveOnDemandCalculationRelation(eq(kpiCalculationRequestPayload.getKpiNames()), any(UUID.class));
            verify(kpiCalculationExecutionControllerMock).scheduleCalculation(kpiCalculationJobArgumentCaptor.capture());
            verify(tabularParameterFacadeMock).saveTabularParameterTables(any(UUID.class), eq(List.of(tabularParameter())));
            verify(tabularParameterFacadeMock).saveTabularParameterDimensions(any(UUID.class), eq(List.of(tabularParameter())));
            verify(tabularParamMetricsMock, times(1)).increaseMetricCalculationPostWithTabularParam();
            verify(tabularParamMetricsMock, times(1)).increaseMetricTabularParamCsvFormat();
            verify(tabularParamMetricsMock, times(0)).increaseMetricTabularParamJsonFormat();

            Assertions.assertThat(calculationArgumentCaptor.getValue()).satisfies(calculation -> {
                Assertions.assertThat(calculation.getCalculationId()).isNotNull();
                Assertions.assertThat(calculation.getTimeCreated()).isNotNull();
                Assertions.assertThat(calculation.getKpiCalculationState()).isEqualTo(KpiCalculationState.STARTED);
                Assertions.assertThat(calculation.getExecutionGroup()).isEqualTo("ON_DEMAND");
                Assertions.assertThat(calculation.getParameters()).isEqualTo("{\"key\":\"value\"}");
            });

            Assertions.assertThat(kpiCalculationJobArgumentCaptor.getValue()).satisfies(kpiCalculationJob -> {
                Assertions.assertThat(kpiCalculationJob.getCalculationId()).isNotNull();
                Assertions.assertThat(kpiCalculationJob.getTimeCreated()).isNotNull();
                Assertions.assertThat(kpiCalculationJob.getExecutionGroup()).isEqualTo("ON_DEMAND");
                Assertions.assertThat(kpiCalculationJob.getJobType()).isEqualTo(KpiType.ON_DEMAND);
                Assertions.assertThat(kpiCalculationJob.getKpiDefinitionNames()).containsExactlyInAnyOrder(KPI_NAME_TEST_NAME);
            });
        }

        @Test
        void andPersistingCalculationThrowsSQLException_thenKpiPersistenceExceptionIsThrown() throws Exception {
            final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of(KPI_NAME_TEST_NAME));
            final ResponseEntity<?> responseMock = mock(ResponseEntity.class);

            when(kpiCalculationRequestValidatorMock.validateKpiCalculationRequest(any(UUID.class), eq(kpiCalculationRequestPayload))).thenAnswer(invocation -> responseMock);
            when(responseMock.getStatusCodeValue()).thenReturn(HttpStatus.SC_CREATED);
            when(calculatorPropertiesMock.getMaxNumberOfParallelOnDemandCalculations()).thenReturn(2);
            when(kpiCalculationExecutionControllerMock.getCurrentOnDemandCalculationCount()).thenReturn(1);
            doThrow(SQLException.class).when(calculationServiceMock).save(any(Calculation.class));

            Assertions.assertThatThrownBy(() -> objectUnderTest.handleKpiCalculationRequest(kpiCalculationRequestPayload))
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Unable to persist a state for requested KPI calculation");

            verify(kpiCalculationValidatorMock).validate(kpiCalculationRequestPayload);
            verify(kpiCalculationRequestValidatorMock).validateKpiCalculationRequest(any(UUID.class), eq(kpiCalculationRequestPayload));
            verify(responseMock).getStatusCodeValue();
            verify(calculatorPropertiesMock).getMaxNumberOfParallelOnDemandCalculations();
            verify(kpiCalculationExecutionControllerMock).getCurrentOnDemandCalculationCount();
            verify(calculationServiceMock).save(any(Calculation.class));
            verify(tabularParameterFacadeMock).saveTabularParameterTables(any(UUID.class), eq(List.of(tabularParameter())));
            verifyNoMoreInteractions(tabularParameterFacadeMock);
        }

        private void assertKpiNamesHandling(final Set<String> kpiDefinitionNames) {
            final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(kpiDefinitionNames);

            final ResponseEntity<?> actual = objectUnderTest.handleKpiCalculationRequest(kpiCalculationRequestPayload);

            assertResponse(actual,
                    HttpStatus.SC_BAD_REQUEST,
                    "The KPI calculation request payload must not be null or empty",
                    Collections.emptyList());
        }

        private void assertResponse(final ResponseEntity<?> actual,
                                    final int statusCode,
                                    final String message,
                                    final List<Object> errorData) {
            Assertions.assertThat(actual).satisfies(response -> {
                Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(statusCode);
                Assertions.assertThat(response.getBody())
                        .asInstanceOf(InstanceOfAssertFactories.type(ErrorResponse.class))
                        .satisfies(errorResponse -> {
                            Assertions.assertThat(errorResponse.message()).isEqualTo(message + (errorData.isEmpty() ? "" : (" - Error data: " + errorData)));
                            Assertions.assertThat(errorResponse.status()).isEqualTo(statusCode);
                        });
            });
        }

        KpiCalculationRequestPayload requestBuilder(final Set<String> kpiNames) {
            final Parameter parameter = Parameter.builder().name("key").value("value").build();
            return KpiCalculationRequestPayload.builder().source(SOURCE).kpiNames(kpiNames).parameters(List.of(parameter)).tabularParameters(List.of(
                    tabularParameter())).build();
        }

        TabularParameters tabularParameter() {
            return TabularParameters.builder().name("test").format(Format.CSV).value("12").build();
        }
    }

    @Nested
    @DisplayName("when getCalculationState")
    class WhenGetCalculationState {
        private UUID uuid;

        @BeforeEach
        void setUp() {
            uuid = UUID.fromString("39a9fc26-42cd-49eb-a599-15aa9c3002fb");
        }

        @Test
        void andNonOnDemandCalculationExists_thenAcceptedReturned(
                @Mock final List<ReadinessLog> readinessLogsMock,
                @Mock final Collection<ReadinessLogResponse> readinessLogResponsesMock) {
            when(calculationServiceMock.forceFindByCalculationId(uuid)).thenReturn(KpiCalculationState.IN_PROGRESS);
            when(calculationServiceMock.forceFetchExecutionGroupByCalculationId(uuid)).thenReturn("executionGroup");
            when(readinessLogRegistryFacadeMock.findByCalculationId(uuid)).thenReturn(readinessLogsMock);
            when(entityMapperMock.mapReadinessLogs(readinessLogsMock)).thenReturn(readinessLogResponsesMock);

            final ResponseEntity<CalculationStateResponse> actual = objectUnderTest.getCalculationState(uuid);

            verify(calculationServiceMock).forceFindByCalculationId(uuid);
            verify(calculationServiceMock).forceFetchExecutionGroupByCalculationId(uuid);
            verify(readinessLogRegistryFacadeMock).findByCalculationId(uuid);
            verify(entityMapperMock).mapReadinessLogs(readinessLogsMock);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
            Assertions.assertThat(actual.getBody())
                    .asInstanceOf(InstanceOfAssertFactories.type(CalculationStateResponse.class))
                    .satisfies(calculationStateResponse -> {
                        Assertions.assertThat(calculationStateResponse.getCalculationId()).isEqualTo(uuid);
                        Assertions.assertThat(calculationStateResponse.getStatus()).isEqualTo(String.valueOf(KpiCalculationState.IN_PROGRESS));
                        Assertions.assertThat(calculationStateResponse.getExecutionGroup()).isEqualTo("executionGroup");
                        Assertions.assertThat(calculationStateResponse.getReadinessLogs()).isEqualTo(readinessLogResponsesMock);
                    });
        }

        @Test
        void andOnDemandCalculationExists_thenAcceptedReturned() {
            when(calculationServiceMock.forceFindByCalculationId(uuid)).thenReturn(KpiCalculationState.IN_PROGRESS);
            when(calculationServiceMock.forceFetchExecutionGroupByCalculationId(uuid)).thenReturn("ON_DEMAND");

            final ResponseEntity<CalculationStateResponse> actual = objectUnderTest.getCalculationState(uuid);

            verify(calculationServiceMock).forceFindByCalculationId(uuid);
            verify(calculationServiceMock).forceFetchExecutionGroupByCalculationId(uuid);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
            Assertions.assertThat(actual.getBody())
                    .asInstanceOf(InstanceOfAssertFactories.type(CalculationStateResponse.class))
                    .satisfies(calculationStateResponse -> {
                        Assertions.assertThat(calculationStateResponse.getCalculationId()).isEqualTo(uuid);
                        Assertions.assertThat(calculationStateResponse.getStatus()).isEqualTo(String.valueOf(KpiCalculationState.IN_PROGRESS));
                        Assertions.assertThat(calculationStateResponse.getExecutionGroup()).isEqualTo("ON_DEMAND");
                        Assertions.assertThat(calculationStateResponse.getReadinessLogs()).isEmpty();
                    });
        }

    }
}