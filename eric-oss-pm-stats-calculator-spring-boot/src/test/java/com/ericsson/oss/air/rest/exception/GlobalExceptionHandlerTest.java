/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.exception;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_POST_AGGREGATION;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaInvalidException;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryUnreachableException;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogException;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogUnreachableException;
import com.ericsson.oss.air.pm.stats.model.exception.DecompressedRequestSizeTooBigException;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;
import com.ericsson.oss.air.pm.stats.model.exception.ParameterValidationException;
import com.ericsson.oss.air.pm.stats.model.exception.RetentionPeriodValidationException;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;
import com.ericsson.oss.air.pm.stats.repository.exception.DatasourceNotFoundException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.ExpressionContainsOnlyTabularParametersException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.InvalidDataSourceAliasException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.InvalidSqlReferenceException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.SqlWhitelistValidationException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.UnscopedComplexInMemoryResolutionException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;
import com.ericsson.oss.air.rest.KpiDefinitionResourceImpl;
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.exception.model._assert.ErrorResponseAssertions;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.simple.table.definition.required.SimpleDefinitionExpression;
import kpi.model.simple.table.definition.required.SimpleDefinitionName;
import lombok.SneakyThrows;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.spark.sql.AnalysisException;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @InjectMocks GlobalExceptionHandler objectUnderTest;

    @Test
    void shouldHandleAnalysisException() {
        final AnalysisException analysisExceptionMock = mock(AnalysisException.class);
        when(analysisExceptionMock.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleAnalysisException(analysisExceptionMock);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleDataCatalogException() {
        final DataCatalogException dataCatalogException = mock(DataCatalogException.class);
        when(dataCatalogException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleDataCatalogException(dataCatalogException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleDataCatalogUnreachableException() {
        final DataCatalogUnreachableException dataCatalogUnreachableException = mock(DataCatalogUnreachableException.class);
        when(dataCatalogUnreachableException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleDataCatalogUnreachableException(dataCatalogUnreachableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.GATEWAY_TIMEOUT.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isGatewayTimeoutErrorWithMessage("test exception message"));
    }

    @Test
    void shouldHandleDatasourceNotFoundException() {
        final DatasourceNotFoundException datasourceNotFoundException = mock(DatasourceNotFoundException.class);
        when(datasourceNotFoundException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleDatasourceNotFoundException(datasourceNotFoundException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleDecompressedRequestSizeTooBigException() {
        final DecompressedRequestSizeTooBigException decompressedRequestSizeTooBigException = mock(DecompressedRequestSizeTooBigException.class);
        when(decompressedRequestSizeTooBigException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleDecompressedRequestSizeTooBigException(decompressedRequestSizeTooBigException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleEntityNotFoundException() {
        final EntityNotFoundException entityNotFoundException = mock(EntityNotFoundException.class);
        when(entityNotFoundException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleEntityNotFoundException(entityNotFoundException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isNotFoundWithMessage("test exception message"));
    }

    @Test
    void shouldHandleExpressionContainsOnlyTabularParametersException_whenNameAndExpressionIsNull() {
        final List<KpiDefinition> kpiDefinitionList = List.of(
                SimpleKpiDefinitions.SimpleKpiDefinition.builder()
                        .name(SimpleDefinitionName.of("simpleKpi"))
                        .expression(SimpleDefinitionExpression.of("simpleExpression"))
                        .build());
        final ExpressionContainsOnlyTabularParametersException expressionContainsOnlyTabularParametersException = new ExpressionContainsOnlyTabularParametersException(kpiDefinitionList);
        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleExpressionContainsOnlyTabularParametersException(expressionContainsOnlyTabularParametersException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessages(
                        List.of("'simpleKpi' definition has an invalid expression: 'simpleExpression'! " +
                                "The tabular parameters:// data source can only be used with INNER JOIN, not alone.")));
    }

    @Test
    void shouldHandleExpressionContainsOnlyTabularParametersException_whenNameAndExpressionIsNotNull() {
        final ExpressionContainsOnlyTabularParametersException expressionContainsOnlyTabularParametersException = mock(ExpressionContainsOnlyTabularParametersException.class);
        when(expressionContainsOnlyTabularParametersException.getName()).thenReturn("exceptionName");
        when(expressionContainsOnlyTabularParametersException.getExpression()).thenReturn("exceptionExpression");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleExpressionContainsOnlyTabularParametersException(expressionContainsOnlyTabularParametersException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "'exceptionName' definition has an invalid expression: 'exceptionExpression'! The tabular " +
                                "parameters:// data source can only be used with INNER JOIN, not alone."));
    }

    @Test
    void shouldHandleIllegalStateException() {
        final IllegalStateException illegalStateException = mock(IllegalStateException.class);
        when(illegalStateException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleIllegalStateException(illegalStateException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleInvalidDataSourceAliasException() {
        final Set<Relation> relationSet = Set.of(Relation.of(null, Table.of("tabular_table"), null));
        final InvalidDataSourceAliasException invalidDataSourceAliasException = new InvalidDataSourceAliasException(relationSet);

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleInvalidDataSourceAliasException(invalidDataSourceAliasException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessages(
                        List.of("Tabular Parameter table cannot be aliased 'tabular_table'")));
    }

    @Test
    void shouldHandleInvalidSqlReferenceException() {
        final ResolutionResult resolutionResult = resolutionResult(
                List.of(),
                List.of(new RelationReferenceResolution(
                        References.relation(KPI_DB, table("kpi_simple_60"), null),
                        reference(null, table("kpi_simple_60"), column("agg_column_0"), null))));
        final InvalidSqlReferenceException invalidSqlReferenceException = new InvalidSqlReferenceException(List.of(resolutionResult));

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleInvalidSqlReferenceException(invalidSqlReferenceException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessages(
                        List.of("KPI Definition 'null' has reference 'UNRESOLVED kpi_db.kpi_simple_60 kpi_simple_60.agg_column_0[]'")));

    }

    @Test
    void shouldHandleKpiDefinitionValidationException_whenStatusIsConflict() {
        final KpiDefinitionValidationException kpiDefinitionValidationException = mock(KpiDefinitionValidationException.class);
        when(kpiDefinitionValidationException.getStatusType()).thenReturn(Response.Status.CONFLICT);
        when(kpiDefinitionValidationException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleKpiDefinitionValidationException(kpiDefinitionValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isConflictWithMessage("test exception message"));
    }

    @Test
    void shouldHandleKpiDefinitionValidationException_whenStatusIsNotConflict() {
        final KpiDefinitionValidationException kpiDefinitionValidationException = mock(KpiDefinitionValidationException.class);
        when(kpiDefinitionValidationException.getStatusType()).thenReturn(Response.Status.BAD_REQUEST);
        when(kpiDefinitionValidationException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleKpiDefinitionValidationException(kpiDefinitionValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleKpiPersistenceException() {
        final KpiPersistenceException kpiPersistenceException = mock(KpiPersistenceException.class);
        when(kpiPersistenceException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleKpiPersistenceException(kpiPersistenceException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isInternalServerErrorWithMessage("test exception message"));
    }

    @Test
    void shouldHandleNoSuchElementException() {
        final NoSuchElementException noSuchElementException = mock(NoSuchElementException.class);
        when(noSuchElementException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleNoSuchElementException(noSuchElementException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isNotFoundWithMessage("test exception message"));
    }

    @Test
    void shouldHandleParameterValidationException() {
        final ParameterValidationException parameterValidationException = mock(ParameterValidationException.class);
        when(parameterValidationException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleParameterValidationException(parameterValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleRetentionPeriodValidationException_whenStatusIsConflict() {
        final RetentionPeriodValidationException retentionPeriodValidationException = mock(RetentionPeriodValidationException.class);
        when(retentionPeriodValidationException.getStatusType()).thenReturn(Response.Status.CONFLICT);
        when(retentionPeriodValidationException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleRetentionPeriodValidationException(retentionPeriodValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isConflictWithMessage("test exception message"));
    }

    @Test
    void shouldHandleRetentionPeriodValidationException_whenStatusIsNotConflict() {
        final RetentionPeriodValidationException retentionPeriodValidationException = mock(RetentionPeriodValidationException.class);
        when(retentionPeriodValidationException.getStatusType()).thenReturn(Response.Status.BAD_REQUEST);
        when(retentionPeriodValidationException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleRetentionPeriodValidationException(retentionPeriodValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleSchemaInvalidException() {
        final SchemaInvalidException schemaInvalidException = mock(SchemaInvalidException.class);
        when(schemaInvalidException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleSchemaInvalidException(schemaInvalidException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleSchemaRegistryUnreachableException() {
        final SchemaRegistryUnreachableException schemaRegistryUnreachableException = mock(SchemaRegistryUnreachableException.class);
        when(schemaRegistryUnreachableException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleSchemaRegistryUnreachableException(schemaRegistryUnreachableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.GATEWAY_TIMEOUT.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isGatewayTimeoutErrorWithMessage("test exception message"));
    }

    @Test
    void shouldHandleSqlWhitelistValidationException() {
        final Map<String, Set<String>> notAllowedSqlElementsByKpiDefinitions = new LinkedHashMap<>();
        notAllowedSqlElementsByKpiDefinitions.put("kpi_name1", Sets.newLinkedHashSet("element1", "element2", "element3"));
        final SqlWhitelistValidationException sqlWhitelistValidationException = new SqlWhitelistValidationException(notAllowedSqlElementsByKpiDefinitions);

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleSqlWhitelistValidationException(sqlWhitelistValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessages(
                        List.of("KPI Definition 'kpi_name1' contains the following prohibited sql elements: 'element1, element2, element3'")));
    }

    @Test
    void shouldHandleTabularParameterValidationException() {
        final TabularParameterValidationException tabularParameterValidationException = mock(TabularParameterValidationException.class);
        when(tabularParameterValidationException.getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleTabularParameterValidationException(tabularParameterValidationException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage("test exception message"));
    }

    @Test
    void shouldHandleUnscopedComplexInMemoryResolutionException() {
        final KpiDefinition kpiDefinitionMock = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
        when(kpiDefinitionMock.isScheduledComplex()).thenReturn(true);

        final HashSetValuedHashMap<KpiDefinition, RelationReferenceResolution> unscopedResolutions = new HashSetValuedHashMap<>();
        final RelationReferenceResolution resolution = new RelationReferenceResolution(
                relation(KPI_POST_AGGREGATION, table("table"), null),
                reference(KPI_POST_AGGREGATION, table("table"), column("column"), null)
        );

        unscopedResolutions.put(kpiDefinitionMock, resolution);

        final UnscopedComplexInMemoryResolutionException unscopedComplexInMemoryResolutionException = new UnscopedComplexInMemoryResolutionException(unscopedResolutions);

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleUnscopedComplexInMemoryResolutionException(unscopedComplexInMemoryResolutionException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessages(
                        List.of("KPI Definition 'null' with reference 'UNRESOLVED kpi_post_agg.table kpi_post_agg.table.column[]'" +
                                " points outside the execution group of 'null' but uses in-memory datasource")));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenValueInstantiationException() {
        final HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        final ValueInstantiationException valueInstantiationException = mock(ValueInstantiationException.class);
        final JsonLocation jsonLocationMock = mock(JsonLocation.class);

        when(httpMessageNotReadableException.getCause()).thenReturn(valueInstantiationException);
        when(valueInstantiationException.getLocation()).thenReturn(jsonLocationMock);
        when(ExceptionUtils.getRootCause(valueInstantiationException).getMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "test exception message at [line: 0, column: 0] path []"));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenInvalidNullException() {
        final HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        final InvalidNullException invalidNullException = mock(InvalidNullException.class);
        final JsonLocation jsonLocationMock = mock(JsonLocation.class);

        when(httpMessageNotReadableException.getCause()).thenReturn(invalidNullException);
        when(invalidNullException.getLocation()).thenReturn(jsonLocationMock);
        when(invalidNullException.getOriginalMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "test exception message at [line: 0, column: 0] path []"));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenUnrecognizedPropertyException_andPropertyNameIsNotRecognizable() {
        final HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        final UnrecognizedPropertyException unrecognizedPropertyException = mock(UnrecognizedPropertyException.class);

        when(httpMessageNotReadableException.getCause()).thenReturn(unrecognizedPropertyException);
        when(unrecognizedPropertyException.getPropertyName()).thenReturn("propertyName");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "'propertyName' is not a recognizable property name"));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenUnrecognizedPropertyException_andPropertyNameIsRecognizable() {
        final HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        final UnrecognizedPropertyException unrecognizedPropertyException = mock(UnrecognizedPropertyException.class);

        when(httpMessageNotReadableException.getCause()).thenReturn(unrecognizedPropertyException);
        when(unrecognizedPropertyException.getPropertyName()).thenReturn("alias");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "Property 'alias' cannot be modified in a PATCH request"));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenMismatchedInputException() {
        final HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        final MismatchedInputException mismatchedInputException = mock(MismatchedInputException.class);
        final JsonLocation jsonLocationMock = mock(JsonLocation.class);

        when(httpMessageNotReadableException.getCause()).thenReturn(mismatchedInputException);
        when(mismatchedInputException.getLocation()).thenReturn(jsonLocationMock);
        when(mismatchedInputException.getOriginalMessage()).thenReturn("test exception message");

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "test exception message at [line: 0, column: 0] path []"));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenGenericException() {
        final HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        final IgnoredPropertyException ignoredPropertyException = mock(IgnoredPropertyException.class);

        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isInternalServerErrorWithMessage(
                        "Exception contained no error message"));
    }

    @Test
    @SneakyThrows
    void shouldHandleMethodArgumentTypeMismatch_whenNotCorrectUuidFormat(){

        MethodParameter methodParameter = createMethodParameter(UUID.class, Optional.class, "addKpiDefinitions", Optional.class, KpiDefinitionRequest.class);

        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("invalid", Optional.class, "kpiCollectionId", methodParameter, new IllegalArgumentException());


        final ResponseEntity<ErrorResponse> actual = objectUnderTest.handleMethodArgumentTypeMismatchWithUuid(exception);

        assertThat(actual.getStatusCodeValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(actual.getBody()).asInstanceOf(type(ErrorResponse.class))
                .satisfies(errorResponse -> ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                        "Invalid UUID as kpiCollectionId"));
    }

    private MethodParameter createMethodParameter(Class<?> genericType, Class<?> rawType, String methodName, Class<?>... methodParameters) throws NoSuchMethodException {
        Method method = KpiDefinitionResourceImpl.class.getMethod(methodName, methodParameters);
        MethodParameter methodParameter = new MethodParameter(method, 0){
            @Override
            public Type getGenericParameterType() {
                return new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{genericType};
                    }

                    @Override
                    public Type getRawType() {
                        return rawType;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };
            }
        };
        return methodParameter;
    }


    private static ResolutionResult resolutionResult(
            final List<? extends RelationReferenceResolution> resolved,
            final List<? extends RelationReferenceResolution> unresolved
    ) {
        final ResolutionResult resolutionResult = new ResolutionResult(mock(KpiDefinition.class, RETURNS_DEEP_STUBS));

        resolved.forEach(resolution -> {
            resolutionResult.addResolvedResolution(resolution.relation().orElse(null), resolution.reference());
        });

        unresolved.forEach(resolution -> {
            resolutionResult.addUnresolvedResolution(resolution.relation().orElse(null), resolution.reference());
        });

        return resolutionResult;
    }
}