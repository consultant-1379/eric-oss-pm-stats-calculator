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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.util.DefinitionMapper;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class KpiCalculationRequestValidatorTest {
    @Mock DefinitionMapper definitionMapperMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @InjectMocks KpiCalculationRequestValidator objectUnderTest;

    final KpiDefinition kpi = KpiDefinition.builder().withName("kpiName").build();
    final UUID calculationId = UUID.fromString("19ccc30c-b6e8-47a9-b7c8-8f20d7dd7617");
    final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of("kpiName"));

    @Test
    void shouldValidateKpiCalculationRequest(){
        when(definitionMapperMock.toKpiDefinitions(kpiDefinitionServiceMock.findAll())).thenReturn(List.of(kpi));

        ResponseEntity<?> actual = objectUnderTest.validateKpiCalculationRequest(calculationId, kpiCalculationRequestPayload);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_CREATED);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody, type( CalculationRequestSuccessResponse.class))
                .satisfies(errorResponse -> {
                    Assertions.assertThat(errorResponse.getSuccessMessage()).startsWith("Requested KPIs are valid and calculation has been launched");
                });
    }

    @Test
    void shouldThrowErrorResponseWhenRequestedKpisDoesNotExistInDatabase() {
        when(definitionMapperMock.toKpiDefinitions(kpiDefinitionServiceMock.findAll())).thenReturn(new ArrayList<>());

        ResponseEntity<?> actual = objectUnderTest.validateKpiCalculationRequest(calculationId, kpiCalculationRequestPayload);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody, type(ErrorResponse.class))
                .satisfies(errorResponse -> {
                    Assertions.assertThat(errorResponse.message()).startsWith("A KPI requested for calculation was not found in the database");
                    Assertions.assertThat(errorResponse.status()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
                    Assertions.assertThat(errorResponse.error()).isEqualTo(Response.Status.BAD_REQUEST.getReasonPhrase());
                    Assertions.assertThat(errorResponse)
                            .extracting(ErrorResponse::timestamp, LOCAL_DATE_TIME);
                });
    }

    KpiCalculationRequestPayload requestBuilder(final Set<String> kpiNames) {
        final KpiCalculationRequestPayload.Parameter parameter = KpiCalculationRequestPayload.Parameter.builder().name("key").value("value").build();
        return KpiCalculationRequestPayload.builder().source("SOURCE").kpiNames(kpiNames).parameters(List.of(parameter)).tabularParameters(List.of(
                tabularParameter())).build();
    }

    KpiCalculationRequestPayload.TabularParameters tabularParameter() {
        return KpiCalculationRequestPayload.TabularParameters.builder().name("test").format(Format.CSV).value("12").build();
    }
}