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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import com.ericsson.orchestration.common.utilities.auth.context.AccessToken;
import com.ericsson.orchestration.common.utilities.auth.context.AuthorizationContext;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.KpiDefinitionPatchRequest;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.VerificationSuccessResponse;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiTimer;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;
import com.ericsson.oss.air.pm.stats.service.api.TabularParameterService;
import com.ericsson.oss.air.rest.api.KpiDefinitionResource;
import com.ericsson.oss.air.rest.api.KpiValidator;
import com.ericsson.oss.air.rest.facade.api.TableFacade;
import com.ericsson.oss.air.rest.log.LoggerHandler;
import com.ericsson.oss.air.rest.mapper.EntityMapper;
import com.ericsson.oss.air.rest.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse;
import com.ericsson.oss.air.rest.resourcemanager.ResourceManagerWriter;
import com.ericsson.oss.air.rest.util.DefinitionResourceUtils;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandParameter.OnDemandParameterBuilder;
import kpi.model.ondemand.OnDemandTabularParameter;
import kpi.model.ondemand.OnDemandTabularParameter.OnDemandTabularParameterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * REST endpoint for receiving KPI Definition information.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class KpiDefinitionResourceImpl implements KpiDefinitionResource {

    private static final String ALL_KPIS_VALID_MESSAGE = "All KPIs proposed are validated and persisted to database";
    private static final String EO_CLIENT_ID = "eo";

    private final KpiDefinitionService kpiDefinitionService;
    private final ParameterService parameterService;

    private final TabularParameterService tabularParameterService;
    private final RetentionPeriodService retentionPeriodService;
    private final KpiValidator kpiValidator;
    private final TableFacade tableFacade;
    private final EntityMapper entityMapper;
    private final ApiMetricRegistry apiMetricRegistry;
    private final LoggerHandler loggerHandler;

    private final HttpServletRequest httpServletRequest;
    private final ResourceManagerWriter resourceManagerWriter;

    @Override
    public ResponseEntity<VerificationSuccessResponse> updateKpiDefinitions( final UUID collectionId, final KpiDefinitionRequest kpiDefinition) {
        loggerHandler.logAudit(log, String.format("PUT request received at '%s'", httpServletRequest.getRequestURI()));

        final Optional<AccessToken> accessToken = AuthorizationContext.getAccessToken();
        accessToken.ifPresent(token -> resourceManagerWriter.createOrUpdateOutputTopicKafkaUser(token.getIssuedFor().equals(EO_CLIENT_ID) ? token.getUserName() : token.getIssuedFor()));

        kpiValidator.validate(kpiDefinition, collectionId);
        kpiDefinitionService.upsert(kpiDefinition);
        return new ResponseEntity<>(manageEndpoint(kpiDefinition, Optional.of(collectionId)), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<VerificationSuccessResponse> addKpiDefinitions(final Optional<UUID> collectionId, final KpiDefinitionRequest kpiDefinition) {
        loggerHandler.logAudit(log, String.format("POST request received at '%s'", httpServletRequest.getRequestURI()));
        final UUID actualCollectionId = collectionId.orElse(DEFAULT_COLLECTION_ID);

        final Optional<AccessToken> accessToken = AuthorizationContext.getAccessToken();
        accessToken.ifPresent(token -> resourceManagerWriter.createOrUpdateOutputTopicKafkaUser(token.getIssuedFor().equals(EO_CLIENT_ID) ? token.getUserName() : token.getIssuedFor()));

        return apiMetricRegistry.timer(ApiTimer.DEFINITION_POST_SERVICE_TIME, () -> {
            kpiValidator.validate(kpiDefinition, actualCollectionId);
            kpiDefinitionService.insert(kpiDefinition, actualCollectionId);
            parameterService.insert(kpiDefinition, actualCollectionId);
            tabularParameterService.insert(kpiDefinition, actualCollectionId);
            retentionPeriodService.insertRetentionPeriod(kpiDefinition, actualCollectionId);
            return new ResponseEntity<>(manageEndpoint(kpiDefinition, collectionId), HttpStatus.CREATED);
        });
    }

    @Override
    public ResponseEntity<List<String>> deleteKpiDefinitions(final Optional<UUID> collectionId, final List<String> kpiDefinitionNames) {
        loggerHandler.logAudit(log, String.format("DELETE request received at '%s'", httpServletRequest.getRequestURI()));

        final UUID actualCollectionId = collectionId.orElse(DEFAULT_COLLECTION_ID);

        return apiMetricRegistry.timer(ApiTimer.DEFINITION_DELETE_SERVICE_TIME, () -> {
            kpiValidator.validate(kpiDefinitionNames, actualCollectionId);
            kpiDefinitionService.softDelete(kpiDefinitionNames, actualCollectionId);
            return new ResponseEntity<>(kpiDefinitionNames, HttpStatus.OK);
        });
    }

    @Override
    public ResponseEntity<KpiDefinitionUpdateResponse> updateKpiDefinition(final KpiDefinitionPatchRequest kpiDefinitionPatchRequest,
                                                                           final Optional<UUID> collectionId,
                                                                           final String name) {
        loggerHandler.logAudit(log, String.format("PATCH request received at '%s'", httpServletRequest.getRequestURI()));

        final UUID actualCollectionId = collectionId.orElse(DEFAULT_COLLECTION_ID);

        return apiMetricRegistry.timer(ApiTimer.DEFINITION_PATCH_SERVICE_TIME, () -> {
            final KpiDefinitionEntity kpiDefinitionEntity = kpiDefinitionService.forceFindByName(name, actualCollectionId);
            final boolean objectTypeChanged = objectTypeChanged(kpiDefinitionEntity, kpiDefinitionPatchRequest);
            kpiDefinitionEntity.update(kpiDefinitionPatchRequest);
            kpiValidator.validate(kpiDefinitionEntity);

            if (objectTypeChanged) {
                kpiDefinitionService.updateWithColumnTypeChange(kpiDefinitionEntity, actualCollectionId);
            } else {
                kpiDefinitionService.updateWithoutColumnTypeChange(kpiDefinitionEntity, actualCollectionId);
            }

            final KpiDefinitionUpdateResponse response = entityMapper.mapToKpiDefinitionUpdateResponse(kpiDefinitionEntity);

            return new ResponseEntity<>(response, HttpStatus.OK);
        });
    }

    @Override
    public ResponseEntity<KpiDefinitionsResponse> getKpiDefinitions(final Optional<UUID> collectionId, final boolean showDeleted) {
        loggerHandler.logAudit(log, String.format("GET request received at '%s'", httpServletRequest.getRequestURI()));

        final UUID actualCollectionId = collectionId.orElse(DEFAULT_COLLECTION_ID);

        return apiMetricRegistry.timer(ApiTimer.DEFINITION_GET_SERVICE_TIME, () -> {
            final List<KpiDefinitionEntity> kpiDefinitionEntities = showDeleted
                    ? kpiDefinitionService.findAllIncludingSoftDeleted(actualCollectionId)
                    : kpiDefinitionService.findAll(actualCollectionId);

            final List<Parameter> parameters = parameterService.findAllParameters(actualCollectionId);
            final List<OnDemandParameter> onDemandParameters = new ArrayList<>();
            final List<OnDemandTabularParameter> onDemandTabularParameters = new ArrayList<>();

            for (final Parameter parameter : parameters) {
                if (parameter.tabularParameter() == null) {
                    onDemandParameters.add(onDemandParameter(parameter));
                } else {
                    final String tabularParameterName = parameter.tabularParameter().name();
                    final OnDemandParameter onDemandParameter = onDemandParameter(parameter);
                    findByName(onDemandTabularParameters, tabularParameterName).ifPresentOrElse(tabularParameter -> {
                        final List<OnDemandParameter> columns = tabularParameter.columns();
                        columns.add(onDemandParameter);
                    }, () -> {
                        final List<OnDemandParameter> columns = new ArrayList<>(List.of(onDemandParameter));
                        onDemandTabularParameters.add(onDemandTabularParameter(tabularParameterName, columns));
                    });
                }
            }

            final KpiDefinitionsResponse response = entityMapper.mapToKpiDefinitionResponse(kpiDefinitionEntities, onDemandTabularParameters, onDemandParameters);
            return new ResponseEntity<>(response, HttpStatus.OK);
        });
    }

    private VerificationSuccessResponse manageEndpoint(final KpiDefinitionRequest kpiDefinition, final Optional<UUID> collectionId) {
        tableFacade.createOrUpdateOutputTable(kpiDefinition, collectionId);
        final Map<String, String> submittedKpiDefinitions = DefinitionResourceUtils.getKpisByTableName(kpiDefinition);
        return DefinitionResourceUtils.getVerificationSuccessResponse(ALL_KPIS_VALID_MESSAGE, submittedKpiDefinitions);
    }

    private boolean objectTypeChanged(final KpiDefinitionEntity kpiDefinitionEntity, final KpiDefinitionPatchRequest kpiDefinitionPatchRequest) {
        return kpiDefinitionPatchRequest.getObjectType() != null && !kpiDefinitionEntity.objectType().equals(kpiDefinitionPatchRequest.getObjectType());
    }

    private static OnDemandTabularParameter onDemandTabularParameter(final String tabularParameterName, final List<OnDemandParameter> columns) {
        final OnDemandTabularParameterBuilder builder = OnDemandTabularParameter.builder();
        builder.name(tabularParameterName);
        builder.columns(columns);
        return builder.build();
    }

    private static OnDemandParameter onDemandParameter(final Parameter parameter) {
        final OnDemandParameterBuilder builder = OnDemandParameter.builder();
        builder.name(parameter.name());
        builder.type(parameter.type());
        return builder.build();
    }

    private static Optional<OnDemandTabularParameter> findByName(final List<OnDemandTabularParameter> onDemandTabularParameters, final String tabularParameterName) {
        return onDemandTabularParameters.stream().filter(tabularParameter -> tabularParameter.name().equals(tabularParameterName)).findFirst();
    }
}
