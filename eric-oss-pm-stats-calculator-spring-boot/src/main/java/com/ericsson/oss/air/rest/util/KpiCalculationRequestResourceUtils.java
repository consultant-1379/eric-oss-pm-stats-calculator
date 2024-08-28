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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utils for KPI Calculation REST endpoint.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiCalculationRequestResourceUtils {

    static final String KPI_CALCULATION_REQUEST_IS_NULL = "The KPI calculation request payload must not be null";

    static ResponseEntity<ErrorResponse> findInvalidKpiNamesInPayload(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        if (KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(kpiCalculationRequestPayload)) {
            final ErrorResponse errorResponse = RestErrorResponse.getErrorResponse(KPI_CALCULATION_REQUEST_IS_NULL,
                    Response.Status.BAD_REQUEST,
                    Collections.emptyList());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    static CalculationRequestSuccessResponse getKpiCalculationRequestSuccessResponse(final String successMessage, final UUID calculationId,
                                                                                     final Map<String, String> proposals) {
        return DefinitionResourceUtils.getCalculationRequestSuccessResponse(successMessage, calculationId, proposals);
    }
}
