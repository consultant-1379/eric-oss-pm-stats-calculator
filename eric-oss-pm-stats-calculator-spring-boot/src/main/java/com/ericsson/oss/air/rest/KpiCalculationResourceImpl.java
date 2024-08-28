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

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.NOTHING_CALCULATED;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse.CalculationResponseBuilder;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationStateResponse;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.rest.api.KpiCalculationResource;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.log.LoggerHandler;
import com.ericsson.oss.air.rest.util.KpiCalculationRequestHandler;
import com.ericsson.oss.air.rest.util.RestErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * REST endpoint for receiving KPI calculation requests.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class KpiCalculationResourceImpl implements KpiCalculationResource {

    private static final String KPI_CALCULATION_FAILED_MESSAGE = "Unable to calculate the requested KPIs";

    private final KpiCalculationRequestHandler kpiCalculationRequestHandler;
    private final CalculationService calculationService;
    private final LoggerHandler loggerHandler;

    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<?> calculateKpis(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        loggerHandler.logAudit(log, String.format("POST request received at '%s'", httpServletRequest.getRequestURI()));
        try {
            return kpiCalculationRequestHandler.handleKpiCalculationRequest(kpiCalculationRequestPayload);
        } catch (final KpiCalculatorException e) {
            log.error(e.getMessage(), e);
            return getKpiCalculationFailedErrorResponse(kpiCalculationRequestPayload);
        }
    }

    @Override
    public ResponseEntity<CalculationStateResponse> getApplicationState(final UUID calculationId) {
        loggerHandler.logAudit(log, String.format("GET request received at '%s'", httpServletRequest.getRequestURI()));
        return kpiCalculationRequestHandler.getCalculationState(calculationId);
    }

    @Override
    public ResponseEntity<Collection<CalculationResponse>> findCalculationsCreatedAfter(final int elapsedMinutes, final boolean includeNothingCalculated) {
        loggerHandler.logAudit(log, String.format("GET request received at '%s'", httpServletRequest.getRequestURI()));

        final Collection<CalculationResponse> calculations = calculationService
                .findCalculationsCreatedWithin(Duration.ofMinutes(elapsedMinutes))
                .stream()
                .filter(calculation -> includeNothingCalculated || calculation.getKpiCalculationState() != NOTHING_CALCULATED)
                .sorted(comparing(Calculation::getTimeCreated).reversed())
                .map(KpiCalculationResourceImpl::calculationResponse)
                .collect(toList());

        return ResponseEntity.ok(calculations);
    }

    private static CalculationResponse calculationResponse(final Calculation calculation) {
        final CalculationResponseBuilder builder = CalculationResponse.builder();
        builder.executionGroup(calculation.getExecutionGroup());
        builder.kpiType(calculation.getKpiType());
        builder.status(calculation.getKpiCalculationState());
        builder.calculationId(calculation.getCalculationId());
        return builder.build();
    }

    private static ResponseEntity<ErrorResponse> getKpiCalculationFailedErrorResponse(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        log.error("KPI calculation has failed");
        final ErrorResponse errorResponse = RestErrorResponse.getErrorResponse(
                KPI_CALCULATION_FAILED_MESSAGE, Status.INTERNAL_SERVER_ERROR,
                new ArrayList<>(kpiCalculationRequestPayload.getKpiNames()));
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
