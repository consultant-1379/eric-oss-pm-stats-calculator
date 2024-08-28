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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.DefinitionNameErrorResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.Parameter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.util.DefinitionMapper;
import com.ericsson.oss.air.pm.stats.service.validator.FilterSqlKeywordValidator;
import com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Class used to apply KPI calculation requests.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class KpiCalculationRequestValidator {

    private static final String KPI_CALCULATION_REQUEST_VALID = "Requested KPIs are valid and calculation has been launched";
    private static final String KPI_DEFINITION_IS_SCHEDULED = "A KPI requested for calculation contains an execution group and " +
            "is calculated on a schedule";
    private static final String KPI_DEFINITION_HAS_UNSATISFIED_PARAMETERS = "The KPIs requested for calculation have unresolved parameters";
    private static final String PARAMETERIZED_KPIS_FAILED_SCHEMA_VERIFICATION_MESSAGE = "Failed schema verification. Ensure that parameters provided are correct " +
            "for requested KPIs";
    private static final String NO_KPI_DEFINITION_FOUND = "A KPI requested for calculation was not found in the database";
    private static final String VALIDATING_KPI_CALCULATION_REQUEST = "Validating KPI Calculation Request";

    private final KpiDefinitionService kpiDefinitionService;
    private final DefinitionMapper definitionMapper;

    /**
     * Validates a {@link KpiCalculationRequestPayload}.
     *
     * @param calculationId
     *            the ID for this calculation
     * @param kpiCalculationRequestPayload
     *            the KPI calculation request which is lists the names of requested KPIs and optional parameters
     * @return the {@link Response} associated with the request.
     */
    public ResponseEntity<?> validateKpiCalculationRequest(final UUID calculationId, final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        final List<KpiDefinition> definitionsInDatabase = definitionMapper.toKpiDefinitions(kpiDefinitionService.findAll());
        final ResponseEntity<?> failureResponse = validateKpiCalculationRequests(kpiCalculationRequestPayload,
                definitionsInDatabase);
        if (failureResponse.getBody() != null) {
            return failureResponse;
        }

        final List<KpiDefinition> kpisToCalculate = KpiCalculationRequestUtils.getRequestedKpiDefinitionsFromExistingKpiDefinitions(
                kpiCalculationRequestPayload, definitionsInDatabase);

        final Map<String, String> submittedKpiDefinitions = DefinitionResourceUtils
                .getKpisByTableName(definitionMapper.kpiDefinitionsToEntities(kpisToCalculate));

        return new ResponseEntity<>(KpiCalculationRequestResourceUtils
                .getKpiCalculationRequestSuccessResponse(KPI_CALCULATION_REQUEST_VALID, calculationId, submittedKpiDefinitions), HttpStatus.CREATED);
    }

    private static ResponseEntity<ErrorResponse> validateKpiCalculationRequests(final KpiCalculationRequestPayload kpiCalculationRequestPayload,
            final List<KpiDefinition> existingKpiDefinitions) {
        log.debug(VALIDATING_KPI_CALCULATION_REQUEST);

        final ResponseEntity<ErrorResponse> kpiNamesPresentInPayload = KpiCalculationRequestResourceUtils.findInvalidKpiNamesInPayload(kpiCalculationRequestPayload);
        if (!Objects.isNull(kpiNamesPresentInPayload)) {
            return kpiNamesPresentInPayload;
        }

        final ResponseEntity<ErrorResponse> checkRequestedKpisExistsResponse = checkThatRequestedKpisExistInDatabase(kpiCalculationRequestPayload.getKpiNames(),
                existingKpiDefinitions);
        if (!Objects.isNull(checkRequestedKpisExistsResponse)) {
            return checkRequestedKpisExistsResponse;
        }

        final List<KpiDefinition> requestedKpiDefinitions = KpiCalculationRequestUtils.getRequestedKpiDefinitionsFromExistingKpiDefinitions(
                kpiCalculationRequestPayload, existingKpiDefinitions);

        final ResponseEntity<ErrorResponse> checkRequestedKpisHaveEmptyExecutionGroup = checkThatRequestedKpisHaveEmptyExecutionGroup(requestedKpiDefinitions);
        if (!Objects.isNull(checkRequestedKpisHaveEmptyExecutionGroup)) {
            return checkRequestedKpisHaveEmptyExecutionGroup;
        }

        final List<Parameter> parameters = kpiCalculationRequestPayload.getParameters();
        final Map<String, String> parametersAsMap = parameters.stream().collect(Collectors.toMap(Parameter::getName, param -> param.getValue().toString()));

        final Set<Definition> definitionsWithParametersApplied = KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(
                parametersAsMap, requestedKpiDefinitions);

        final ResponseEntity<ErrorResponse> checkThatAllKpiParametersAreProvided = checkThatRequestedKpisHaveAllParametersProvided(definitionsWithParametersApplied);
        if (!Objects.isNull(checkThatAllKpiParametersAreProvided)) {
            return checkThatAllKpiParametersAreProvided;
        }

        final List<DefinitionNameErrorResponse> parameterizedKpiDefinitionErrors = FilterSqlKeywordValidator.validateFiltersForSqlKeywords(definitionsWithParametersApplied);

        if (!parameterizedKpiDefinitionErrors.isEmpty()) {
            final ErrorResponse errorResponse = RestErrorResponse.getErrorResponse(PARAMETERIZED_KPIS_FAILED_SCHEMA_VERIFICATION_MESSAGE,
                    Status.BAD_REQUEST, new ArrayList<>(parameterizedKpiDefinitionErrors));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<ErrorResponse> checkThatRequestedKpisHaveEmptyExecutionGroup(final List<KpiDefinition> requestedKpiDefinitions) {
        final Set<String> invalidExecutionGroupRequests = new HashSet<>();
        for (final KpiDefinition kpiDefinition : requestedKpiDefinitions) {
            if (kpiDefinition.getExecutionGroup() != null) {
                invalidExecutionGroupRequests.add(kpiDefinition.getName());
            }
        }

        return getResponseFromValidationObject(invalidExecutionGroupRequests, KPI_DEFINITION_IS_SCHEDULED);
    }

    private static ResponseEntity<ErrorResponse> checkThatRequestedKpisExistInDatabase(final Set<String> kpiNames,
            final List<KpiDefinition> existingKpiDefinitions) {
        final Set<String> requestsNotFoundInDatabase = KpiCalculationRequestUtils
                .checkThatKpisRequestedForCalculationExist(kpiNames, existingKpiDefinitions);

        return getResponseFromValidationObject(requestsNotFoundInDatabase, NO_KPI_DEFINITION_FOUND);
    }

    private static ResponseEntity<ErrorResponse> getResponseFromValidationObject(final Set<String> validationObject, final String message) {
        if (!validationObject.isEmpty()) {
            final ErrorResponse errorResponse = RestErrorResponse.getErrorResponse(message,
                    Status.BAD_REQUEST, new ArrayList<>(validationObject));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        return null;
    }

    private static ResponseEntity<ErrorResponse> checkThatRequestedKpisHaveAllParametersProvided(final Set<Definition> definitionsWithParametersApplied) {
        final Set<String> requestedKpiNamesMissingParameters = KpiCalculationRequestUtils
                .getNamesOfKpisWithUnresolvedParameters(definitionsWithParametersApplied);

        return getResponseFromValidationObject(requestedKpiNamesMissingParameters, KPI_DEFINITION_HAS_UNSATISFIED_PARAMETERS);
    }
}
