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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import kpi.model.KpiDefinitionRequest;
import kpi.model.ScheduledSimple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiValidatorImplTest {
    @Mock DatabaseValidatorImpl databaseValidatorMock;
    @Mock DataIdentifierValidator dataIdentifierValidatorMock;
    @Mock LoopValidator loopValidatorMock;
    @Mock SchemaFieldValidator schemaFieldValidatorMock;
    @Mock SchemaExistenceValidator schemaExistenceValidatorMock;
    @Mock KpiDefinitionRequestValidator kpiDefinitionRequestValidatorMock;
    @Mock DependencyValidator dependencyValidatorMock;
    @Mock SqlReferenceValidator sqlReferenceValidatorMock;
    @Mock SqlDatasourceValidator sqlDatasourceValidatorMock;
    @Mock SqlWhitelistValidator sqlWhitelistValidatorMock;
    @Mock OnDemandParameterValidator onDemandParameterValidatorMock;
    @Mock AliasValidator aliasValidatorMock;

    @Mock RetentionPeriodValidator retentionPeriodValidator;
    @Mock ExpressionValidator expressionValidatorMock;
    @InjectMocks KpiValidatorImpl objectUnderTest;

    @Test
    void shouldValidate(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock final ScheduledSimple scheduledSimpleMock) {
        when(kpiDefinitionMock.scheduledSimple()).thenReturn(scheduledSimpleMock);
        final UUID collectionId = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
        objectUnderTest.validate(kpiDefinitionMock, collectionId);

        verify(databaseValidatorMock).validateKpiAliasAggregationPeriodNotConflictingWithDb(kpiDefinitionMock);
        verify(databaseValidatorMock).validateNameIsUnique(kpiDefinitionMock, collectionId);
        verify(databaseValidatorMock).validateExecutionGroup(kpiDefinitionMock);
        verify(loopValidatorMock).validateNoCircle(kpiDefinitionMock);
        verify(kpiDefinitionMock, times(2)).scheduledSimple();
        verify(dataIdentifierValidatorMock).validateDataIdentifiers(scheduledSimpleMock);
        verify(schemaExistenceValidatorMock).validate(kpiDefinitionMock);
        verify(schemaFieldValidatorMock).validateReferencedSchema(scheduledSimpleMock);
        verify(sqlReferenceValidatorMock).validateReferences(kpiDefinitionMock);
        verify(onDemandParameterValidatorMock).validateParametersResolved(kpiDefinitionMock);
        verify(sqlDatasourceValidatorMock).validateDatasources(anyList());
        verify(aliasValidatorMock).validateNoAliasedTabularParameterTable(anyList());
        verify(expressionValidatorMock).validateExpressionNotContainsOnlyTabularParametersDatasource(anyList());
        verify(sqlWhitelistValidatorMock).validateSqlElements(kpiDefinitionMock);
        verify(retentionPeriodValidator).validateRetentionPeriod(kpiDefinitionMock, collectionId);
    }

    @Test
    void shouldValidateSingleDefinition(@Mock final KpiDefinitionEntity kpiDefinitionEntity) {
        objectUnderTest.validate(kpiDefinitionEntity);

        verify(loopValidatorMock).validateNoCircle(kpiDefinitionEntity);
        verify(kpiDefinitionRequestValidatorMock).validateRequestAttributes(kpiDefinitionEntity);
        verify(sqlWhitelistValidatorMock).validateSqlElements(kpiDefinitionEntity);
        verify(expressionValidatorMock).validateExpressionNotContainsOnlyTabularParametersDatasource(kpiDefinitionEntity);
    }

    @Test
    void shouldValidateListOfDefinitionNames() {
        final List<String> kpiList = List.of("kpiA", "kpiB");
        final UUID collectionId = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
        objectUnderTest.validate(kpiList, collectionId);

        verify(dependencyValidatorMock).validateNoDanglingDependenciesAfterDeletingDefinitions(kpiList, collectionId);
    }
}
