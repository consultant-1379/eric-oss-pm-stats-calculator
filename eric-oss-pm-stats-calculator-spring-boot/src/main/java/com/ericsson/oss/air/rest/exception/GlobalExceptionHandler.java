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

import static com.ericsson.oss.air.rest.exception.model.ErrorResponse.badRequest;
import static com.ericsson.oss.air.rest.exception.model.ErrorResponse.conflict;
import static com.ericsson.oss.air.rest.exception.model.ErrorResponse.gatewayTimeout;
import static com.ericsson.oss.air.rest.exception.model.ErrorResponse.internalServerError;
import static com.ericsson.oss.air.rest.exception.model.ErrorResponse.notFound;
import static com.ericsson.oss.air.rest.exception.util.ResponseHandlers.asResponse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaInvalidException;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryUnreachableException;
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
import com.ericsson.oss.air.rest.exception.model.ErrorResponse;
import com.ericsson.oss.air.rest.exception.util.JacksonPaths;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.spark.sql.AnalysisException;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    public static final String JACKSON_PATH_FORMAT = "%s at [line: %d, column: %d] path [%s]";

    @ExceptionHandler(AnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAnalysisException(final AnalysisException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(DataCatalogException.class)
    public ResponseEntity<ErrorResponse> handleDataCatalogException(final DataCatalogException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(DataCatalogUnreachableException.class)
    public ResponseEntity<ErrorResponse> handleDataCatalogUnreachableException(final DataCatalogUnreachableException exception) {
        return asResponse(gatewayTimeout(exception.getMessage()));
    }

    @ExceptionHandler(DatasourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDatasourceNotFoundException(final DatasourceNotFoundException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(DecompressedRequestSizeTooBigException.class)
    public ResponseEntity<ErrorResponse> handleDecompressedRequestSizeTooBigException(final DecompressedRequestSizeTooBigException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(final EntityNotFoundException exception) {
        return asResponse(notFound(exception.getMessage()));
    }

    @ExceptionHandler(ExpressionContainsOnlyTabularParametersException.class)
    public ResponseEntity<ErrorResponse> handleExpressionContainsOnlyTabularParametersException(final ExpressionContainsOnlyTabularParametersException exception) {
        final String messageTemplate = "'%s' definition has an invalid expression: '%s'! The tabular parameters:// data source can only be used with INNER JOIN, not alone.";
        if (Objects.isNull(exception.getName()) && Objects.isNull(exception.getExpression())) {
            return asResponse(badRequest(getExceptionMessageStream(exception)));
        } else {
            return asResponse(badRequest(String.format(messageTemplate, exception.getName(), exception.getExpression())));
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(final IllegalStateException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(InvalidDataSourceAliasException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataSourceAliasException(final InvalidDataSourceAliasException exception) {
        final Stream<String> messages = exception.invalidAliasedRelations().stream()
                                                 .map(relation -> String.format("Tabular Parameter table cannot be aliased '%s'", relation.table().getName()));
        return asResponse(badRequest(messages.toList()));
    }

    @ExceptionHandler(InvalidSqlReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSqlReferenceException(final InvalidSqlReferenceException exception) {
        final Stream<String> messages = exception.unresolvedResolutions().stream().map(resolution -> {
            final Set<RelationReferenceResolution> unresolvedReferences = resolution.unresolvedResolutions();
            final ArrayList<String> partialMessages = new ArrayList<>(unresolvedReferences.size());

            for (final RelationReferenceResolution relationReferenceResolution : unresolvedReferences) {
                partialMessages.add(String.format("KPI Definition '%s' has reference '%s'", resolution.kpiDefinitionName(), relationReferenceResolution));
            }

            return partialMessages;
        }).flatMap(List::stream);

        return asResponse(badRequest(messages.toList()));
    }

    @ExceptionHandler(KpiDefinitionValidationException.class)
    public ResponseEntity<ErrorResponse> handleKpiDefinitionValidationException(final KpiDefinitionValidationException exception) {
        return asResponse(exception.getStatusType() == Response.Status.CONFLICT ? conflict(exception.getMessage())
                                  : badRequest(exception.getMessage()));
    }

    @ExceptionHandler(KpiPersistenceException.class)
    public ResponseEntity<ErrorResponse> handleKpiPersistenceException(final KpiPersistenceException exception) {
        return asResponse(internalServerError(exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(final NoSuchElementException exception) {
        return asResponse(notFound(exception.getMessage()));
    }

    @ExceptionHandler(ParameterValidationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidationException(final ParameterValidationException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(RetentionPeriodValidationException.class)
    public ResponseEntity<ErrorResponse> handleRetentionPeriodValidationException(final RetentionPeriodValidationException exception) {
        return asResponse(exception.getStatusType() == Response.Status.CONFLICT ? conflict(exception.getMessage())
                                  : badRequest(exception.getMessage()));
    }

    @ExceptionHandler(SchemaInvalidException.class)
    public ResponseEntity<ErrorResponse> handleSchemaInvalidException(final SchemaInvalidException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(SchemaRegistryUnreachableException.class)
    public ResponseEntity<ErrorResponse> handleSchemaRegistryUnreachableException(final SchemaRegistryUnreachableException exception) {
        return asResponse(gatewayTimeout(exception.getMessage()));
    }

    @ExceptionHandler(SqlWhitelistValidationException.class)
    public ResponseEntity<ErrorResponse> handleSqlWhitelistValidationException(final SqlWhitelistValidationException exception) {
        final List<String> messages = exception.notAllowedSqlElementsByKpiDefinition().entrySet().stream()
                                               .map(entry ->
                                                            String.format("KPI Definition '%s' contains the following prohibited sql elements: '%s'", entry.getKey(),
                                                                          String.join(", ", entry.getValue())
                                                            ))
                                               .collect(toList());

        return asResponse(badRequest(messages));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchWithUuid(MethodArgumentTypeMismatchException ex) {
        if (isOptionalUUID(ex.getParameter())) {
            return asResponse(badRequest("Invalid UUID as kpiCollectionId"));
        }
        return asResponse(badRequest("Path parameter is not correct"));
    }


    @ExceptionHandler(TabularParameterValidationException.class)
    public ResponseEntity<ErrorResponse> handleTabularParameterValidationException(final TabularParameterValidationException exception) {
        return asResponse(badRequest(exception.getMessage()));
    }

    @ExceptionHandler(UnscopedComplexInMemoryResolutionException.class)
    public ResponseEntity<ErrorResponse> handleUnscopedComplexInMemoryResolutionException(final UnscopedComplexInMemoryResolutionException exception) {
        final List<String> errorMessages = new ArrayList<>();
        exception.unscopedResolutions().asMap().forEach((kpiDefinition, relationReferenceResolutions) -> {
            for (final RelationReferenceResolution relationReferenceResolution : relationReferenceResolutions) {
                errorMessages.add(String.format(
                        "KPI Definition '%s' with reference '%s' points outside the execution group of '%s' but uses in-memory datasource",
                        kpiDefinition.name().value(), relationReferenceResolution, kpiDefinition.executionGroup().value()
                ));
            }
        });

        return asResponse(badRequest(errorMessages));
    }

    // TODO fix unnecessary wrapping due to spring boot
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof ValueInstantiationException valueInstantiationException) {
            return handleValueInstantiationException(valueInstantiationException);
        }
        if (cause instanceof InvalidNullException invalidNullException) {
            return handleInvalidNullException(invalidNullException);
        }
        if (cause instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
            return handleUnrecognizedPropertyException(unrecognizedPropertyException);
        }
        if (cause instanceof MismatchedInputException mismatchedInputException) {
            return handleMismatchedInputException(mismatchedInputException);
        }
        return handleGenericException(exception);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Throwable throwable) {
        final Throwable rootCause = rootCause(throwable);
        log.warn("No exception mapper found for '{}'", rootCause.getClass());
        final String errorMessage = rootCause.getMessage() == null ? "Exception contained no error message" : rootCause.getMessage();
        return asResponse(internalServerError(errorMessage));
    }

    private ResponseEntity<ErrorResponse> handleUnrecognizedPropertyException(final UnrecognizedPropertyException exception) {
        return asResponse(badRequest(computeMessage(exception.getPropertyName())));
    }

    private ResponseEntity<ErrorResponse> handleValueInstantiationException(final ValueInstantiationException exception) {
        return asResponse(badRequest(String.format(
                JACKSON_PATH_FORMAT,
                ExceptionUtils.getRootCause(exception).getMessage(),
                exception.getLocation().getLineNr(),
                exception.getLocation().getColumnNr(),
                JacksonPaths.normalizePath(exception)
        )));
    }

    private ResponseEntity<ErrorResponse> handleInvalidNullException(final InvalidNullException exception) {
        return asResponse(badRequest(String.format(
                JACKSON_PATH_FORMAT,
                exception.getOriginalMessage(),
                exception.getLocation().getLineNr(),
                exception.getLocation().getColumnNr(),
                JacksonPaths.normalizePath(exception)
        )));
    }

    private ResponseEntity<ErrorResponse> handleMismatchedInputException(final MismatchedInputException exception) {
        final String message = String.format(
                JACKSON_PATH_FORMAT,
                exception.getOriginalMessage(),
                exception.getLocation().getLineNr(),
                exception.getLocation().getColumnNr(),
                JacksonPaths.normalizePath(exception)
        );
        return asResponse(badRequest(message));
    }

    private static List<String> getExceptionMessageStream(final ExpressionContainsOnlyTabularParametersException exception) {
        return exception.getKpiDefinitionsWithOnlyTabularParamRelations()
                        .stream()
                        .map(kpiDefinition -> String.format("'%s' definition has an invalid expression: '%s'! The tabular parameters:// data source can only be used with INNER JOIN, not alone.",
                                                            kpiDefinition.name().value(),
                                                            kpiDefinition.expression().value()))
                        .toList();
    }

    private static String computeMessage(final String propertyName) {
        final Set<String> unmodifiableProperties = Set.of(
                "name",
                "alias",
                "aggregation_period",
                "aggregation_type",
                "aggregation_elements",
                "execution_group",
                "data_reliability_offset",
                "inp_data_identifier"
        );
        if (unmodifiableProperties.contains(propertyName)) {
            return String.format("Property '%s' cannot be modified in a PATCH request", propertyName);
        }
        return String.format("'%s' is not a recognizable property name", propertyName);
    }

    private static Throwable rootCause(final Throwable throwable) {
        return ofNullable(getRootCause(throwable)).orElse(throwable);
    }

    private boolean isOptionalUUID(MethodParameter parameter) {
        if (parameter.getParameterType().equals(Optional.class)) {
            Type[] genericTypes = ((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments();
            return genericTypes.length == 1 && genericTypes[0].equals(UUID.class);
        }
        return false;
    }

}
