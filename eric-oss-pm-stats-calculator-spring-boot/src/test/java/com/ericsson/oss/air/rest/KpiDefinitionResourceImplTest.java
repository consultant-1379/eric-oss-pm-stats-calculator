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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.KpiDefinitionPatchRequest;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.VerificationSuccessResponse;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter.ParameterBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter.TabularParameterBuilder;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;
import com.ericsson.oss.air.pm.stats.service.api.TabularParameterService;
import com.ericsson.oss.air.rest.facade.api.TableFacade;
import com.ericsson.oss.air.rest.log.LoggerHandler;
import com.ericsson.oss.air.rest.mapper.EntityMapper;
import com.ericsson.oss.air.rest.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse;
import com.ericsson.oss.air.rest.resourcemanager.ResourceManagerWriter;
import com.ericsson.oss.air.rest.util.DefinitionResourceUtils;
import com.ericsson.oss.air.rest.validator.KpiValidatorImpl;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandParameter.OnDemandParameterBuilder;
import kpi.model.ondemand.OnDemandTabularParameter;
import kpi.model.ondemand.OnDemandTabularParameter.OnDemandTabularParameterBuilder;
import kpi.model.ondemand.ParameterType;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionResourceImplTest {

    final UUID DEFAULT_COLLECTION_ID = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
    Set<KpiDefinitionEntity> definitions;
    KpiDefinitionRequest kpiDefinitionPayloadMock;

    @Mock KpiValidatorImpl kpiValidatorMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock ParameterService parameterServiceMock;
    @Mock TabularParameterService tabularParameterServiceMock;
    @Mock TableFacade tableFacadeMock;
    @Mock EntityMapper entityMapperMock;
    @Mock LoggerHandler loggerHandlerMock;
    @Mock HttpServletRequest httpServletRequestMock;
    @Mock RetentionPeriodService retentionPeriodServiceMock;
    @Mock ResourceManagerWriter resourceManagerWriterMock;
    @Spy ApiMetricRegistry apiMetricRegistry;
    @InjectMocks KpiDefinitionResourceImpl objectUnderTest;

    @BeforeEach
    void setUp() {
        definitions = Set.of();
        kpiDefinitionPayloadMock = Mockito.mock(KpiDefinitionRequest.class);

        when(httpServletRequestMock.getRequestURI()).thenReturn("model/v1/definitions");
    }

    @Test
    @SneakyThrows
    void shouldReturnCreatedResponse_whenPutRequestSucceeds() {
        try (final MockedStatic<DefinitionResourceUtils> definitionResourceUtilsMockedStatic = mockStatic(DefinitionResourceUtils.class)) {
            final String successMessage = "All KPIs proposed are validated and persisted to database";

            final VerificationSuccessResponse verificationSuccessResponse = new VerificationSuccessResponse();
            verificationSuccessResponse.setSuccessMessage(successMessage);

            final Verification verificationGetVerificationSuccessResponse = () -> DefinitionResourceUtils.getVerificationSuccessResponse(eq(successMessage), anyMap());

            definitionResourceUtilsMockedStatic.when(verificationGetVerificationSuccessResponse).thenReturn(verificationSuccessResponse);

            final ResponseEntity<VerificationSuccessResponse> actual = objectUnderTest.updateKpiDefinitions(DEFAULT_COLLECTION_ID, kpiDefinitionPayloadMock);

            verify(loggerHandlerMock).logAudit(any(), eq("PUT request received at 'model/v1/definitions'"));
            verify(kpiValidatorMock).validate(kpiDefinitionPayloadMock, DEFAULT_COLLECTION_ID);
            verify(kpiDefinitionServiceMock).upsert(any());
            verify(tableFacadeMock).createOrUpdateOutputTable(kpiDefinitionPayloadMock, Optional.of(DEFAULT_COLLECTION_ID));
            definitionResourceUtilsMockedStatic.verify(verificationGetVerificationSuccessResponse);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_CREATED);
            Assertions.assertThat(actual)
                    .extracting(ResponseEntity::getBody, InstanceOfAssertFactories.type(VerificationSuccessResponse.class))
                    .isEqualTo(verificationSuccessResponse);
        }
    }

    @Test
    @SneakyThrows
    void shouldPatchReturnOkResponse_whenRequestSucceedsAndObjectTypeIsNotUpdated_asItRemainsNull(
            @Mock final KpiDefinitionPatchRequest kpiDefinitionPatchRequestMock,
            @Mock final KpiDefinitionEntity kpiDefinitionEntityMock,
            @Mock final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponseMock
    ) {
        when(kpiDefinitionServiceMock.forceFindByName("name", DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntityMock);

        when(kpiDefinitionPatchRequestMock.getObjectType()).thenReturn(null);
        when(entityMapperMock.mapToKpiDefinitionUpdateResponse(kpiDefinitionEntityMock)).thenReturn(kpiDefinitionUpdateResponseMock);
        final ResponseEntity<KpiDefinitionUpdateResponse> actual = objectUnderTest.updateKpiDefinition(kpiDefinitionPatchRequestMock, Optional.empty(), "name");

        verify(loggerHandlerMock).logAudit(any(), eq("PATCH request received at 'model/v1/definitions'"));
        verify(kpiValidatorMock).validate(kpiDefinitionEntityMock);
        verify(kpiDefinitionEntityMock).update(kpiDefinitionPatchRequestMock);
        verify(kpiDefinitionServiceMock).updateWithoutColumnTypeChange(kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isEqualTo(kpiDefinitionUpdateResponseMock);
    }

    @Test
    @SneakyThrows
    void shouldPatchReturnOkResponse_whenRequestSucceedsAndObjectTypeIsNotUpdated(
            @Mock final KpiDefinitionPatchRequest kpiDefinitionPatchRequestMock,
            @Mock final KpiDefinitionEntity kpiDefinitionEntityMock,
            @Mock final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponseMock
    ) {
        when(kpiDefinitionServiceMock.forceFindByName("name", DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntityMock);

        when(kpiDefinitionEntityMock.objectType()).thenReturn("INTEGER");
        when(kpiDefinitionPatchRequestMock.getObjectType()).thenReturn("INTEGER");
        when(entityMapperMock.mapToKpiDefinitionUpdateResponse(kpiDefinitionEntityMock)).thenReturn(kpiDefinitionUpdateResponseMock);
        final ResponseEntity<KpiDefinitionUpdateResponse> actual = objectUnderTest.updateKpiDefinition(kpiDefinitionPatchRequestMock, Optional.empty(),"name");

        verify(loggerHandlerMock).logAudit(any(), eq("PATCH request received at 'model/v1/definitions'"));
        verify(kpiValidatorMock).validate(kpiDefinitionEntityMock);
        verify(kpiDefinitionEntityMock).update(kpiDefinitionPatchRequestMock);
        verify(kpiDefinitionServiceMock).updateWithoutColumnTypeChange(kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isEqualTo(kpiDefinitionUpdateResponseMock);
    }

    @Test
    @SneakyThrows
    void shouldPatchReturnOkResponse_whenRequestSucceedsAndObjectTypeIsUpdated(
            @Mock final KpiDefinitionPatchRequest kpiDefinitionPatchRequestMock,
            @Mock final KpiDefinitionEntity kpiDefinitionEntityMock,
            @Mock final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponseMock
    ) {
        when(kpiDefinitionServiceMock.forceFindByName("name", DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntityMock);

        when(kpiDefinitionEntityMock.objectType()).thenReturn("INTEGER");
        when(kpiDefinitionPatchRequestMock.getObjectType()).thenReturn("FLOAT");
        when(entityMapperMock.mapToKpiDefinitionUpdateResponse(kpiDefinitionEntityMock)).thenReturn(kpiDefinitionUpdateResponseMock);
        final ResponseEntity<KpiDefinitionUpdateResponse> actual = objectUnderTest.updateKpiDefinition(kpiDefinitionPatchRequestMock, Optional.empty(), "name");

        verify(loggerHandlerMock).logAudit(any(), eq("PATCH request received at 'model/v1/definitions'"));
        verify(kpiValidatorMock).validate(kpiDefinitionEntityMock);
        verify(kpiDefinitionEntityMock).update(kpiDefinitionPatchRequestMock);
        verify(kpiDefinitionServiceMock).updateWithColumnTypeChange(kpiDefinitionEntityMock, DEFAULT_COLLECTION_ID);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isEqualTo(kpiDefinitionUpdateResponseMock);
    }

    @Test
    @SneakyThrows
    void shouldPatchReturnOkResponse_whenRequestSucceedsAndObjectTypeIsUpdatedWithCollectionId(
            @Mock final KpiDefinitionPatchRequest kpiDefinitionPatchRequestMock,
            @Mock final KpiDefinitionEntity kpiDefinitionEntityMock,
            @Mock final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponseMock
    ) {

        final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");

        when(kpiDefinitionServiceMock.forceFindByName("name", collectionId)).thenReturn(kpiDefinitionEntityMock);

        when(kpiDefinitionEntityMock.objectType()).thenReturn("INTEGER");
        when(kpiDefinitionPatchRequestMock.getObjectType()).thenReturn("FLOAT");
        when(entityMapperMock.mapToKpiDefinitionUpdateResponse(kpiDefinitionEntityMock)).thenReturn(kpiDefinitionUpdateResponseMock);
        final ResponseEntity<KpiDefinitionUpdateResponse> actual = objectUnderTest.updateKpiDefinition(kpiDefinitionPatchRequestMock, Optional.of(collectionId), "name");

        verify(loggerHandlerMock).logAudit(any(), eq("PATCH request received at 'model/v1/definitions'"));
        verify(kpiValidatorMock).validate(kpiDefinitionEntityMock);
        verify(kpiDefinitionEntityMock).update(kpiDefinitionPatchRequestMock);
        verify(kpiDefinitionServiceMock).updateWithColumnTypeChange(kpiDefinitionEntityMock, collectionId);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isEqualTo(kpiDefinitionUpdateResponseMock);
    }

    @Test
    @SneakyThrows
    void shouldReturnAcceptedResponse_whenDeleteRequestSucceeds(@Mock final List<String> kpiDefinitionNames) {
        final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");
        final ResponseEntity<List<String>> actual = objectUnderTest.deleteKpiDefinitions(Optional.of(collectionId), kpiDefinitionNames);

        verify(loggerHandlerMock).logAudit(any(), eq("DELETE request received at 'model/v1/definitions'"));
        verify(kpiValidatorMock).validate(kpiDefinitionNames, collectionId);
        verify(kpiDefinitionServiceMock).softDelete(kpiDefinitionNames, collectionId);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody, InstanceOfAssertFactories.type(List.class))
                .isEqualTo(kpiDefinitionNames);
    }

    @Test
    @SneakyThrows
    void shouldReturnAcceptedResponse_whenDeleteWithCollectionIdRequestSucceeds(@Mock final List<String> kpiDefinitionNames) {
        final ResponseEntity<List<String>> actual = objectUnderTest.deleteKpiDefinitions(Optional.empty(), kpiDefinitionNames);

        verify(loggerHandlerMock).logAudit(any(), eq("DELETE request received at 'model/v1/definitions'"));
        verify(kpiValidatorMock).validate(kpiDefinitionNames, DEFAULT_COLLECTION_ID);
        verify(kpiDefinitionServiceMock).softDelete(kpiDefinitionNames, DEFAULT_COLLECTION_ID);

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody, InstanceOfAssertFactories.type(List.class))
                .isEqualTo(kpiDefinitionNames);
    }

    @Test
    void shouldGetReturnOkResponse_whenRequestSucceedsAndParametersEmpty(
            @Mock final List<KpiDefinitionEntity> kpiDefinitionEntitiesMock,
            @Mock final KpiDefinitionsResponse kpiDefinitionsResponseMock
    ) {
        when(kpiDefinitionServiceMock.findAll(DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntitiesMock);
        when(entityMapperMock.mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList())).thenReturn(kpiDefinitionsResponseMock);

        final ResponseEntity<KpiDefinitionsResponse> actual = objectUnderTest.getKpiDefinitions(Optional.empty(),false);

        verify(loggerHandlerMock).logAudit(any(), eq("GET request received at 'model/v1/definitions'"));
        verify(parameterServiceMock).findAllParameters(DEFAULT_COLLECTION_ID);
        verify(kpiDefinitionServiceMock).findAll(DEFAULT_COLLECTION_ID);
        verify(entityMapperMock).mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList());

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isInstanceOf(KpiDefinitionsResponse.class);
    }

    @Test
    void shouldGetReturnOkResponse_whenCollectionIdPresentRequestSucceedsAndParametersEmpty(
            @Mock final List<KpiDefinitionEntity> kpiDefinitionEntitiesMock,
            @Mock final KpiDefinitionsResponse kpiDefinitionsResponseMock
    ) {
        final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");

        when(kpiDefinitionServiceMock.findAll(collectionId)).thenReturn(kpiDefinitionEntitiesMock);
        when(entityMapperMock.mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList())).thenReturn(kpiDefinitionsResponseMock);

        final ResponseEntity<KpiDefinitionsResponse> actual = objectUnderTest.getKpiDefinitions(Optional.of(collectionId), false);

        verify(loggerHandlerMock).logAudit(any(), eq("GET request received at 'model/v1/definitions'"));
        verify(parameterServiceMock).findAllParameters(collectionId);
        verify(kpiDefinitionServiceMock).findAll(collectionId);
        verify(entityMapperMock).mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList());

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isInstanceOf(KpiDefinitionsResponse.class);
    }

    @Test
    void shouldGetReturnOkResponseAndAllKpiDefinitionsIncludingSoftDeleted_whenCollectionIdPresentRequestSucceedsAndParametersEmpty(
            @Mock final List<KpiDefinitionEntity> kpiDefinitionEntitiesMock,
            @Mock final KpiDefinitionsResponse kpiDefinitionsResponseMock
    ) {
        final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");
        when(kpiDefinitionServiceMock.findAllIncludingSoftDeleted(collectionId)).thenReturn(kpiDefinitionEntitiesMock);
        when(entityMapperMock.mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList())).thenReturn(kpiDefinitionsResponseMock);

        final ResponseEntity<KpiDefinitionsResponse> actual = objectUnderTest.getKpiDefinitions(Optional.of(collectionId), true);

        verify(loggerHandlerMock).logAudit(any(), eq("GET request received at 'model/v1/definitions'"));
        verify(parameterServiceMock).findAllParameters(collectionId);
        verify(kpiDefinitionServiceMock).findAllIncludingSoftDeleted(collectionId);
        verify(entityMapperMock).mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList());

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isInstanceOf(KpiDefinitionsResponse.class);
    }

    @Test
    void shouldGetReturnOkResponseAndAllKpiDefinitionsIncludingSoftDeleted_whenRequestSucceedsAndParametersEmpty(
            @Mock final List<KpiDefinitionEntity> kpiDefinitionEntitiesMock,
            @Mock final KpiDefinitionsResponse kpiDefinitionsResponseMock
    ) {
        when(kpiDefinitionServiceMock.findAllIncludingSoftDeleted(DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntitiesMock);
        when(entityMapperMock.mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList())).thenReturn(kpiDefinitionsResponseMock);

        final ResponseEntity<KpiDefinitionsResponse> actual = objectUnderTest.getKpiDefinitions(Optional.empty(), true);

        verify(loggerHandlerMock).logAudit(any(), eq("GET request received at 'model/v1/definitions'"));
        verify(parameterServiceMock).findAllParameters(DEFAULT_COLLECTION_ID);
        verify(kpiDefinitionServiceMock).findAllIncludingSoftDeleted(DEFAULT_COLLECTION_ID);
        verify(entityMapperMock).mapToKpiDefinitionResponse(kpiDefinitionEntitiesMock, Collections.emptyList(), Collections.emptyList());

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isInstanceOf(KpiDefinitionsResponse.class);
    }

    @Test
    void shouldGetReturnOkResponse_whenRequestSucceedsAndParametersNotEmpty(
            @Mock final List<KpiDefinitionEntity> kpiDefinitionEntitiesMock,
            @Mock final KpiDefinitionsResponse kpiDefinitionsResponseMock
    ) {
        final TabularParameter tabularParameter = tabularParameter();
        final Parameter parameter1 = parameter("Parameter1", tabularParameter, ParameterType.INTEGER);
        final Parameter parameter2 = parameter("Parameter2", null, ParameterType.STRING);
        final OnDemandParameter expectedOnDemandParameter1 = onDemandParameter(parameter1);
        final OnDemandParameter expectedOnDemandParameter2 = onDemandParameter(parameter2);
        final OnDemandTabularParameter expectedOnDemandTabularParameter = onDemandTabularParameter(List.of(expectedOnDemandParameter1));

        when(parameterServiceMock.findAllParameters(DEFAULT_COLLECTION_ID)).thenReturn(List.of(parameter2, parameter1));
        when(kpiDefinitionServiceMock.findAllIncludingSoftDeleted(DEFAULT_COLLECTION_ID)).thenReturn(kpiDefinitionEntitiesMock);
        when(entityMapperMock.mapToKpiDefinitionResponse(
                kpiDefinitionEntitiesMock,
                List.of(expectedOnDemandTabularParameter),
                List.of(expectedOnDemandParameter2)
        )).thenReturn(kpiDefinitionsResponseMock);

        final ResponseEntity<KpiDefinitionsResponse> actual = objectUnderTest.getKpiDefinitions(Optional.empty(),true);

        verify(loggerHandlerMock).logAudit(any(), eq("GET request received at 'model/v1/definitions'"));
        verify(parameterServiceMock).findAllParameters(DEFAULT_COLLECTION_ID);
        verify(kpiDefinitionServiceMock).findAllIncludingSoftDeleted(DEFAULT_COLLECTION_ID);
        verify(entityMapperMock).mapToKpiDefinitionResponse(
                kpiDefinitionEntitiesMock,
                List.of(expectedOnDemandTabularParameter),
                List.of(expectedOnDemandParameter2)
        );

        Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertThat(actual)
                .extracting(ResponseEntity::getBody)
                .isInstanceOf(KpiDefinitionsResponse.class);
    }

    @Test
    @SneakyThrows
    void shouldReturnCreatedResponse_whenPostRequestWithoutCollectionIdSucceeds() {
        try (final MockedStatic<DefinitionResourceUtils> definitionResourceUtilsMockedStatic = mockStatic(DefinitionResourceUtils.class)) {
            final String successMessage = "All KPIs proposed are validated and persisted to database";

            final VerificationSuccessResponse verificationSuccessResponse = new VerificationSuccessResponse();
            verificationSuccessResponse.setSuccessMessage(successMessage);

            final Verification verificationGetVerificationSuccessResponse = () -> DefinitionResourceUtils.getVerificationSuccessResponse(eq(successMessage), anyMap());

            definitionResourceUtilsMockedStatic.when(verificationGetVerificationSuccessResponse).thenReturn(verificationSuccessResponse);

            final ResponseEntity<VerificationSuccessResponse> actual = objectUnderTest.addKpiDefinitions(Optional.empty(), kpiDefinitionPayloadMock);

            verify(loggerHandlerMock).logAudit(any(), eq("POST request received at 'model/v1/definitions'"));
            verify(kpiValidatorMock).validate(kpiDefinitionPayloadMock, DEFAULT_COLLECTION_ID);
            verify(kpiDefinitionServiceMock).insert(any(), any());
            verify(parameterServiceMock).insert(any(), any());
            verify(tabularParameterServiceMock).insert(any(), any());
            verify(tableFacadeMock).createOrUpdateOutputTable(kpiDefinitionPayloadMock, Optional.empty());
            definitionResourceUtilsMockedStatic.verify(verificationGetVerificationSuccessResponse);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_CREATED);
            Assertions.assertThat(actual)
                    .extracting(ResponseEntity::getBody, InstanceOfAssertFactories.type(VerificationSuccessResponse.class))
                    .isEqualTo(verificationSuccessResponse);
        }
    }

    @Test
    @SneakyThrows
    void shouldReturnCreatedResponse_whenPostRequestWithCollectionIdSucceeds() {
        try (final MockedStatic<DefinitionResourceUtils> definitionResourceUtilsMockedStatic = mockStatic(DefinitionResourceUtils.class)) {
            final String successMessage = "All KPIs proposed are validated and persisted to database";
            final UUID collectionId = UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");
            final VerificationSuccessResponse verificationSuccessResponse = new VerificationSuccessResponse();
            verificationSuccessResponse.setSuccessMessage(successMessage);

            final Verification verificationGetVerificationSuccessResponse = () -> DefinitionResourceUtils.getVerificationSuccessResponse(eq(successMessage), anyMap());

            definitionResourceUtilsMockedStatic.when(verificationGetVerificationSuccessResponse).thenReturn(verificationSuccessResponse);

            final ResponseEntity<VerificationSuccessResponse> actual = objectUnderTest.addKpiDefinitions(Optional.of(collectionId), kpiDefinitionPayloadMock);

            verify(loggerHandlerMock).logAudit(any(), eq("POST request received at 'model/v1/definitions'"));
            verify(kpiValidatorMock).validate(kpiDefinitionPayloadMock, collectionId);
            verify(kpiDefinitionServiceMock).insert(kpiDefinitionPayloadMock, collectionId);
            verify(parameterServiceMock).insert(any(), eq(collectionId));
            verify(tabularParameterServiceMock).insert(any(), eq(collectionId));
            verify(tableFacadeMock).createOrUpdateOutputTable(kpiDefinitionPayloadMock, Optional.of(collectionId));
            verify(retentionPeriodServiceMock).insertRetentionPeriod(kpiDefinitionPayloadMock, collectionId);
            definitionResourceUtilsMockedStatic.verify(verificationGetVerificationSuccessResponse);

            Assertions.assertThat(actual.getStatusCodeValue()).isEqualTo(HttpStatus.SC_CREATED);
            Assertions.assertThat(actual)
                    .extracting(ResponseEntity::getBody, InstanceOfAssertFactories.type(VerificationSuccessResponse.class))
                    .isEqualTo(verificationSuccessResponse);
        }
    }



    private static OnDemandTabularParameter onDemandTabularParameter(final List<OnDemandParameter> parameters) {
        final OnDemandTabularParameterBuilder builder = OnDemandTabularParameter.builder();
        builder.name("TabularParameter1");
        builder.columns(parameters);
        return builder.build();
    }

    private TabularParameter tabularParameter() {
        final TabularParameterBuilder builder = TabularParameter.builder();
        builder.withId(1);
        builder.withName("TabularParameter1");
        return builder.build();
    }

    private Parameter parameter(final String name, final TabularParameter tabularParameter, final ParameterType type) {
        final ParameterBuilder builder = Parameter.builder();
        builder.withId(1);
        builder.withName(name);
        builder.withTabularParameter(tabularParameter);
        builder.withType(type);
        return builder.build();
    }

    private static OnDemandParameter onDemandParameter(final Parameter parameter) {
        final OnDemandParameterBuilder builder = OnDemandParameter.builder();
        builder.name(parameter.name());
        builder.type(parameter.type());
        return builder.build();
    }
}