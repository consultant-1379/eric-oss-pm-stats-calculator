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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION;
import static com.ericsson.oss.air.rest.util.KpiCalculationRequestResourceUtils.KPI_CALCULATION_REQUEST_IS_NULL;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ericsson.oss.air.pm.stats.calculation.TabularParameterFacade;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Class used to limit the number of KPI calculation requests that can be run concurrently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KpiCalculationRequestHandler {

    private static final String KPI_CALCULATOR_BUSY_MESSAGE = "PM Stats Calculator is currently handling the maximum number of calculations";
    private static final String KPI_CALCULATION_REQUEST_IS_NULL_MESSAGE = "The KPI calculation request payload must not be null or empty";

    private final CalculationService calculationService;
    private final EntityMapper entityMapper;
    private final KpiCalculationExecutionController kpiCalculationExecutionController;
    private final KpiCalculationRequestValidator kpiCalculationRequestValidator;
    private final KpiDefinitionService kpiDefinitionService;
    private final ReadinessLogRegistryFacade readinessLogRegistryFacade;
    private final TabularParameterFacade tabularParameterFacade;
    private final KpiCalculationValidator kpiCalculationValidator;
    private final TabularParamMetrics tabularParamMetrics;
    private final CalculatorProperties calculatorProperties;

    /**
     * Submit a {@link KpiCalculationRequestPayload}s for validation and calculation.
     *
     * @param kpiCalculationRequestPayload
     *            the KPI calculation request which is lists the names of requested KPIs and optional parameters
     * @return the {@link Response} associated with the request. PM Stats Calculator will accept two concurrent requests in current configuration.
     */
    public ResponseEntity<?> handleKpiCalculationRequest(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        if (Objects.isNull(kpiCalculationRequestPayload.getKpiNames()) || kpiCalculationRequestPayload.getKpiNames().isEmpty()) {
            log.error(KPI_CALCULATION_REQUEST_IS_NULL);
            final ErrorResponse errorResponse = RestErrorResponse.getErrorResponse(
                    KPI_CALCULATION_REQUEST_IS_NULL_MESSAGE, Status.BAD_REQUEST, new ArrayList<>());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        setTabularParamMetrics(kpiCalculationRequestPayload);

        final UUID calculationId = UUID.randomUUID();
        kpiCalculationValidator.validate(kpiCalculationRequestPayload);
        final ResponseEntity<?> response = kpiCalculationRequestValidator.validateKpiCalculationRequest(calculationId,
                kpiCalculationRequestPayload);

        final long calculationStartTime = System.currentTimeMillis();
        if (Status.CREATED.getStatusCode() == response.getStatusCodeValue()) {

            final Timestamp timeCreated = new Timestamp(calculationStartTime);
            final int maxOnDemandCalculations =calculatorProperties.getMaxNumberOfParallelOnDemandCalculations();
            if (kpiCalculationExecutionController.getCurrentOnDemandCalculationCount() >= maxOnDemandCalculations) {
                return getMaxOnDemandCalculationsReachedErrorResponse(kpiCalculationRequestPayload.getKpiNames());
            }

            tabularParameterFacade.saveTabularParameterTables(calculationId, kpiCalculationRequestPayload.getTabularParameters());
            saveCalculation(calculationId, timeCreated, kpiCalculationRequestPayload.parameterString());
            tabularParameterFacade.saveTabularParameterDimensions(calculationId, kpiCalculationRequestPayload.getTabularParameters());
            submitKpisForCalculation(calculationId, timeCreated, kpiCalculationRequestPayload.getKpiNames());
        }
        return response;
    }

    /**
     * Check the status of a KPI calculation, its execution_group and the readiness_log.
     *
     * @param calculationId
     *         the ID of the calculation to check
     * @return the {@link Response} associated with the request. The status of the calculation, execution_group and the readiness_log is included.
     */
    public ResponseEntity<CalculationStateResponse> getCalculationState(final UUID calculationId) {
        final KpiCalculationState status = calculationService.forceFindByCalculationId(calculationId);

        final String executionGroup = calculationService.forceFetchExecutionGroupByCalculationId(calculationId);
        final CalculationStateResponse calculationStateResponse = new CalculationStateResponse();
        calculationStateResponse.setCalculationId(calculationId);
        calculationStateResponse.setStatus(status.name());
        calculationStateResponse.setExecutionGroup(executionGroup);
        calculationStateResponse.setReadinessLogs(EXECUTION_GROUP_ON_DEMAND_CALCULATION.equals(executionGroup)
                                                          ? Collections.emptyList()
                                                          : getReadinessLogResponse(calculationId));

        return ResponseEntity.ok().body(calculationStateResponse);
    }

    private void saveCalculation(final UUID calculationId, final Timestamp timeCreated, final String parameters) {
        final Calculation calculation = Calculation.builder()
                                                   .withCalculationId(calculationId)
                                                   .withTimeCreated(timeCreated.toLocalDateTime())
                                                   .withKpiCalculationState(KpiCalculationState.STARTED)
                                                   .withExecutionGroup(EXECUTION_GROUP_ON_DEMAND_CALCULATION)
                                                   .withParameters(parameters)
                                                   .withKpiType(KpiType.ON_DEMAND)
                                                   .build();
        try {
            calculationService.save(calculation);
        } catch (SQLException e) {
            log.error("Unable to persist state '{}' for calculation ID '{}'", KpiCalculationState.STARTED.name(), calculationId);
            throw new KpiPersistenceException("Unable to persist a state for requested KPI calculation", e);
        }
    }

    private void submitKpisForCalculation(final UUID calculationId, final Timestamp timeCreated, final Set<String> kpiNames) {
        kpiDefinitionService.saveOnDemandCalculationRelation(kpiNames, calculationId);

        kpiCalculationExecutionController.scheduleCalculation(
                KpiCalculationJob.builder()
                                 .withCalculationId(calculationId)
                                 .withTimeCreated(timeCreated)
                                 .withExecutionGroup(EXECUTION_GROUP_ON_DEMAND_CALCULATION)
                                 .withKpiDefinitionNames(kpiNames)
                                 .withJobType(KpiType.ON_DEMAND)
                                 .build());
    }

    private ResponseEntity<ErrorResponse> getMaxOnDemandCalculationsReachedErrorResponse(final Set<String> kpis) {
        log.error("PM Stats Calculator currently handling maximum number of requests {}", calculatorProperties.getMaxNumberOfParallelOnDemandCalculations());
        final ErrorResponse errorResponse = RestErrorResponse.getErrorResponse(
                KPI_CALCULATOR_BUSY_MESSAGE, Status.TOO_MANY_REQUESTS, Collections.singletonList(kpis));
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    private Collection<ReadinessLogResponse> getReadinessLogResponse(final UUID calculationId) {
        final List<ReadinessLog> readinessLogs = readinessLogRegistryFacade.findByCalculationId(calculationId);
        return entityMapper.mapReadinessLogs(readinessLogs);
    }

    private void setTabularParamMetrics(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        if (!kpiCalculationRequestPayload.getTabularParameters().isEmpty()) {
            tabularParamMetrics.increaseMetricCalculationPostWithTabularParam();
            for (final KpiCalculationRequestPayload.TabularParameters tabularParameter : kpiCalculationRequestPayload.getTabularParameters()) {
                if (tabularParameter.getFormat() == Format.CSV) {
                    tabularParamMetrics.increaseMetricTabularParamCsvFormat();
                } else {
                    tabularParamMetrics.increaseMetricTabularParamJsonFormat();
                }
            }
        }
    }

}
