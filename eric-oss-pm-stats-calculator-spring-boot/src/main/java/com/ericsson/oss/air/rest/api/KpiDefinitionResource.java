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

import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_200_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_400;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_400_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_500;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_500_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.DELETE_DEFINITION_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_DEFINITION_200;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_DEFINITION_200_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_DEFINITION_500;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_DEFINITION_500_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_DEFINITION_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.GET_DEFINITION_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_200;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_200_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_400;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_400_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_500;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_500_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_REQUEST;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_REQUEST_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.PATCH_DEFINITION_REQUEST_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_201;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_201_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_400;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_400_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_409;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_409_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_500;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_500_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_DESCRIPTION;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_REQUEST;
import static com.ericsson.oss.air.rest.api.util.HttpExamples.POST_DEFINITION_SUMMARY;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.BAD_REQUEST;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.CONFLICT;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.CREATED;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.INTERNAL_SERVER_ERROR;
import static com.ericsson.oss.air.rest.api.util.HttpResponseCode.OK;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.KpiDefinitionPatchRequest;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.VerificationSuccessResponse;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kpi.model.KpiDefinitionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("model/v1/definitions")
public interface KpiDefinitionResource extends KpiResource {



    @PutMapping("/{kpiCollectionId}")
    @Operation(summary = "Add KPI definitions",
               description = "Add KPI Definition list to the database and submit simple KPIs",
               responses = {
                    @ApiResponse(responseCode = CREATED, description = "The proposed KPI Definitions are compatible with the expected schema and have been updated"),
                    @ApiResponse(responseCode = BAD_REQUEST, description = "Payload is not valid"),
                    @ApiResponse(responseCode = CONFLICT, description = "Conflicting KPI definitions exist"),
                    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "Failed to process the incoming KPI definitions")},
               tags = "kpi-definition",
               deprecated = true
    )
    ResponseEntity<VerificationSuccessResponse> updateKpiDefinitions(@PathVariable(name = "kpiCollectionId") UUID collectionId,
                                                                     @org.springframework.web.bind.annotation.RequestBody KpiDefinitionRequest kpiDefinition);


    @PatchMapping(value = {"/{name}","/{kpiCollectionId}/{name}"})
    @RequestBody(content = @Content(schema = @Schema(implementation = KpiDefinitionPatchRequest.class),
                 examples = @ExampleObject(value = PATCH_DEFINITION_REQUEST)))
    @Operation(summary = PATCH_DEFINITION_REQUEST_SUMMARY,
               description = PATCH_DEFINITION_REQUEST_DESCRIPTION,
               responses = {
                    @ApiResponse(responseCode = OK, description=PATCH_DEFINITION_200_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = KpiDefinitionUpdateResponse.class),
                                                    examples = @ExampleObject(value = PATCH_DEFINITION_200))),
                    @ApiResponse(responseCode = BAD_REQUEST, description = PATCH_DEFINITION_400_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = PATCH_DEFINITION_400))),
                    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = PATCH_DEFINITION_500_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = PATCH_DEFINITION_500)))},
               tags = "Modify a KPI Definition"
    )
    ResponseEntity<KpiDefinitionUpdateResponse> updateKpiDefinition(
            @org.springframework.web.bind.annotation.RequestBody KpiDefinitionPatchRequest kpiDefinitionPatchRequest,
            @PathVariable(name = "kpiCollectionId") Optional<UUID> collectionId,
            @PathVariable(name = "name") String name);


    @GetMapping(value = {"", "/{kpiCollectionId}"})
    @Operation(summary = GET_DEFINITION_SUMMARY,
               description = GET_DEFINITION_DESCRIPTION,
               responses = {
                    @ApiResponse(responseCode = OK, description = GET_DEFINITION_200_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = KpiDefinitionsResponse.class),
                                                    examples = @ExampleObject(value = GET_DEFINITION_200))),
                    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = GET_DEFINITION_500_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = GET_DEFINITION_500)))},
               tags = "List KPI Definitions"
    )
    ResponseEntity<KpiDefinitionsResponse> getKpiDefinitions(@PathVariable(name = "kpiCollectionId") Optional<UUID> collectionId,
                                                             @RequestParam(name = "showDeleted", defaultValue = "false") boolean showDeleted);


    @PostMapping(value = {"", "/{kpiCollectionId}"})
    @RequestBody(content = @Content(schema = @Schema(implementation = KpiDefinitionRequest.class),
                 examples = @ExampleObject(value = POST_DEFINITION_REQUEST)))
    @Operation(summary = POST_DEFINITION_SUMMARY,
               description = POST_DEFINITION_DESCRIPTION,
               responses = {
                    @ApiResponse(responseCode = CREATED,
                                description = POST_DEFINITION_201_DESCRIPTION,
                                content = @Content(schema = @Schema(implementation = VerificationSuccessResponse.class),
                                                   examples = @ExampleObject(value = POST_DEFINITION_201))),
                    @ApiResponse(responseCode = BAD_REQUEST,
                                description = POST_DEFINITION_400_DESCRIPTION,
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                   examples = @ExampleObject(value = POST_DEFINITION_400))),
                    @ApiResponse(responseCode = CONFLICT, description = POST_DEFINITION_409_DESCRIPTION,
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                   examples = @ExampleObject(value = POST_DEFINITION_409))),
                    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = POST_DEFINITION_500_DESCRIPTION,
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                   examples = @ExampleObject(value = POST_DEFINITION_500)))},
               tags = "Create KPI Definitions"
    )
    ResponseEntity<VerificationSuccessResponse> addKpiDefinitions(@PathVariable(name = "kpiCollectionId") Optional<UUID> collectionId,
                                                                  @org.springframework.web.bind.annotation.RequestBody KpiDefinitionRequest kpiDefinition);


    @DeleteMapping(value = {"", "/{kpiCollectionId}"})
    @RequestBody(content = @Content(array = @ArraySchema(schema = @Schema(type = "string")),examples = @ExampleObject(value = DELETE_DEFINITION)))
    @Operation(summary = DELETE_DEFINITION_SUMMARY,
               description = DELETE_DEFINITION_DESCRIPTION,
               responses = {
                    @ApiResponse(responseCode = OK, description = DELETE_DEFINITION_200_DESCRIPTION,
                                 content = @Content(array = @ArraySchema(schema = @Schema(type = "string")),
                                                    examples = @ExampleObject(value = DELETE_DEFINITION))),
                    @ApiResponse(responseCode = BAD_REQUEST, description = DELETE_DEFINITION_400_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = DELETE_DEFINITION_400))),
                    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR,
                                 description = DELETE_DEFINITION_500_DESCRIPTION,
                                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = DELETE_DEFINITION_500)))},
               tags = "Delete KPI Definitions"
    )
    ResponseEntity<List<String>> deleteKpiDefinitions(@PathVariable(name="kpiCollectionId") Optional<UUID> collectionId,
                                                      @org.springframework.web.bind.annotation.RequestBody List<String> kpiDefinitionNames);
}
