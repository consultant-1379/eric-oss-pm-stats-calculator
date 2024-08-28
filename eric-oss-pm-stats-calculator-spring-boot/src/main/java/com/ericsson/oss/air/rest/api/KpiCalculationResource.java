/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.api;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.CALCULATION_ID;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.CALCULATION_ID_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.ELAPSED_MINUTES_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_200;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_200_RESPONSE;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_200;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_200_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_400;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_400_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_404;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_404_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_500;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_500_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_ID_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_CALCULATION_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.INCLUDE_NOTHING_CALCULATED_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_201;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_201_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_400;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_400_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_429;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_429_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_500;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_500_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_REQUEST;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_CALCULATION_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.BAD_REQUEST;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.CREATED;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.INTERNAL_SERVER_ERROR;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.NOT_FOUND;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.OK;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.TOO_MANY_REQUESTS;

import java.util.Collection;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationStateResponse;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public interface KpiCalculationResource extends KpiResource {


    @PostMapping("calc/v1/calculations")
    @RequestBody(content = @Content(schema = @Schema(implementation = KpiCalculationRequestPayload.class),
                                    examples = @ExampleObject(POST_CALCULATION_REQUEST)))
    @Operation(
        summary = POST_CALCULATION_SUMMARY,
        description = POST_CALCULATION_DESCRIPTION,
        responses = {
            @ApiResponse(responseCode = CREATED,
                         description = POST_CALCULATION_201_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = CalculationRequestSuccessResponse.class),
                                            examples = @ExampleObject(POST_CALCULATION_201))),
            @ApiResponse(responseCode = BAD_REQUEST,
                         description = POST_CALCULATION_400_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                            examples = @ExampleObject(POST_CALCULATION_400))),
            @ApiResponse(responseCode = TOO_MANY_REQUESTS,
                         description = POST_CALCULATION_429_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                            examples = @ExampleObject(POST_CALCULATION_429))),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR,
                         description = POST_CALCULATION_500_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                            examples = @ExampleObject(POST_CALCULATION_500)))},
        tags = "Trigger On-Demand KPI calculation(s)"
    )
    ResponseEntity<?> calculateKpis(@org.springframework.web.bind.annotation.RequestBody KpiCalculationRequestPayload kpiCalculationRequestPayload);

    @GetMapping("calc/v1/calculations/{calculation_id}")
    @Operation(
        summary = GET_CALCULATION_ID_SUMMARY,
        description = GET_CALCULATION_ID_DESCRIPTION,
        responses = {
            @ApiResponse(responseCode = OK,
                         description = GET_CALCULATION_ID_200_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = CalculationStateResponse.class),
                                            examples = @ExampleObject(GET_CALCULATION_ID_200))),
            @ApiResponse(responseCode = BAD_REQUEST,
                         description = GET_CALCULATION_ID_400_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                            examples = @ExampleObject(GET_CALCULATION_ID_400))),
            @ApiResponse(responseCode = NOT_FOUND,
                         description = GET_CALCULATION_ID_404_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                            examples = @ExampleObject(GET_CALCULATION_ID_404))),
            @ApiResponse(responseCode = INTERNAL_SERVER_ERROR,
                         description = GET_CALCULATION_ID_500_DESCRIPTION,
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                            examples = @ExampleObject(GET_CALCULATION_ID_500)))},
        tags = "Monitor KPI calculations"
    )
    ResponseEntity<CalculationStateResponse> getApplicationState(@PathVariable(CALCULATION_ID)
                                 @Parameter(description = CALCULATION_ID_DESCRIPTION) UUID calculationId);

    @GetMapping("calc/v1/calculations")
    @Operation(
        summary = GET_CALCULATION_SUMMARY,
        description = GET_CALCULATION_DESCRIPTION,
        responses =
            @ApiResponse(responseCode = OK,
                         description = GET_CALCULATION_200_RESPONSE,
                         content = @Content(schema = @Schema(implementation = CalculationResponse.class),
                                            examples = {@ExampleObject(value = GET_CALCULATION_200)})),
        tags = "Monitor KPI calculations"
    )
    ResponseEntity<Collection<CalculationResponse>> findCalculationsCreatedAfter(
        @RequestParam(name = "elapsedMinutes", defaultValue = "60")
        @Parameter(description = ELAPSED_MINUTES_DESCRIPTION) int elapsedMinutes,

        @RequestParam(name = "includeNothingCalculated", defaultValue = "false")
        @Parameter(description = INCLUDE_NOTHING_CALCULATED_DESCRIPTION) boolean includeNothingCalculated
    );
}
