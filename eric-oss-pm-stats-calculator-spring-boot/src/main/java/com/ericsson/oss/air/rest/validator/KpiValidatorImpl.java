/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.validator.AliasValidator;
import com.ericsson.oss.air.pm.stats.service.validator.DataIdentifierValidator;
import com.ericsson.oss.air.pm.stats.service.validator.DependencyValidator;
import com.ericsson.oss.air.pm.stats.service.validator.ExpressionValidator;
import com.ericsson.oss.air.pm.stats.service.validator.KpiDefinitionRequestValidator;
import com.ericsson.oss.air.pm.stats.service.validator.LoopValidator;
import com.ericsson.oss.air.pm.stats.service.validator.OnDemandParameterValidator;
import com.ericsson.oss.air.pm.stats.service.validator.SchemaExistenceValidator;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodValidator;
import com.ericsson.oss.air.pm.stats.service.validator.schema.SchemaFieldValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlDatasourceValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlReferenceValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlWhitelistValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;
import com.ericsson.oss.air.rest.api.KpiValidator;

import kpi.model.KpiDefinitionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KpiValidatorImpl implements KpiValidator {

    private final DataIdentifierValidator dataIdentifierValidator;
    private final DatabaseValidatorImpl databaseValidator;
    private final LoopValidator loopValidator;
    private final SchemaFieldValidator schemaFieldValidator;
    private final SchemaExistenceValidator schemaExistenceValidator;
    private final KpiDefinitionRequestValidator kpiDefinitionRequestValidator;
    private final DependencyValidator dependencyValidator;
    private final SqlReferenceValidator sqlReferenceValidator;
    private final SqlDatasourceValidator sqlDatasourceValidator;
    private final SqlWhitelistValidator sqlWhitelistValidator;
    private final OnDemandParameterValidator onDemandParameterValidator;
    private final AliasValidator aliasValidator;
    private final RetentionPeriodValidator retentionPeriodValidator;
    private final ExpressionValidator expressionValidator;

    @Override
    public void validate(final KpiDefinitionRequest kpiDefinition, UUID collectionId) {
        databaseValidator.validateKpiAliasAggregationPeriodNotConflictingWithDb(kpiDefinition);
        databaseValidator.validateNameIsUnique(kpiDefinition, collectionId);
        databaseValidator.validateExecutionGroup(kpiDefinition);
        loopValidator.validateNoCircle(kpiDefinition);
        dataIdentifierValidator.validateDataIdentifiers(kpiDefinition.scheduledSimple());
        schemaExistenceValidator.validate(kpiDefinition);
        schemaFieldValidator.validateReferencedSchema(kpiDefinition.scheduledSimple());
        onDemandParameterValidator.validateParametersResolved(kpiDefinition);

        final List<ResolutionResult> resolutionResults = sqlReferenceValidator.validateReferences(kpiDefinition);
        sqlDatasourceValidator.validateDatasources(resolutionResults);
        aliasValidator.validateNoAliasedTabularParameterTable(resolutionResults);
        expressionValidator.validateExpressionNotContainsOnlyTabularParametersDatasource(resolutionResults);

        sqlWhitelistValidator.validateSqlElements(kpiDefinition);

        retentionPeriodValidator.validateRetentionPeriod(kpiDefinition, collectionId);
    }

    @Override
    public void validate(final KpiDefinitionEntity kpiDefinitionEntity) {
        loopValidator.validateNoCircle(kpiDefinitionEntity);
        kpiDefinitionRequestValidator.validateRequestAttributes(kpiDefinitionEntity);
        sqlWhitelistValidator.validateSqlElements(kpiDefinitionEntity);
        expressionValidator.validateExpressionNotContainsOnlyTabularParametersDatasource(kpiDefinitionEntity);
    }

    @Override
    public void validate(final Collection<String> kpiDefinitionNames, final UUID collectionId) {
        dependencyValidator.validateNoDanglingDependenciesAfterDeletingDefinitions(kpiDefinitionNames, collectionId);
    }
}
