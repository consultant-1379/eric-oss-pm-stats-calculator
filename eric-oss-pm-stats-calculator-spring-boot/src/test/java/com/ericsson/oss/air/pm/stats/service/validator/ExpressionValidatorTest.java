/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static java.util.Collections.emptySet;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.ExpressionContainsOnlyTabularParametersException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpressionValidatorTest {

    @Mock
    SqlRelationExtractor sqlRelationExtractorMock;
    @InjectMocks
    ExpressionValidator objectUnderTest;

    @Test
    void shouldThrowException() {
        final KpiDefinition kpiDefinitionMock = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);

        final List<ResolutionResult> resolutionResults = List.of(
                resolutionResult(kpiDefinitionMock,
                        List.of(
                                resolution(
                                        relation(TABULAR_PARAMETERS, table("tabular_table1"), null),
                                        reference(TABULAR_PARAMETERS, table("tabular_table1"), column("node_fdn"), null)
                                ),
                                resolution(
                                        relation(TABULAR_PARAMETERS, table("tabular_table2"), null),
                                        reference(TABULAR_PARAMETERS, table("tabular_table2"), column("node_fdn"), null)
                                )
                        )));

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateExpressionNotContainsOnlyTabularParametersDatasource(resolutionResults))
                .isInstanceOf(ExpressionContainsOnlyTabularParametersException.class);

    }

    @Test
    void shouldNotThrowException() {
        final KpiDefinition kpiDefinitionMock = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);

        final List<ResolutionResult> resolutionResults = List.of(
                resolutionResult(kpiDefinitionMock,
                        List.of(
                                resolution(
                                        relation(TABULAR_PARAMETERS, table("tabular_table1"), null),
                                        reference(TABULAR_PARAMETERS, table("tabular_table1"), column("node_fdn"), null)
                                ),
                                resolution(
                                        relation(TABULAR_PARAMETERS, table("tabular_table2"), null),
                                        reference(TABULAR_PARAMETERS, table("tabular_table2"), column("node_fdn"), null)
                                ),
                                resolution(
                                        relation(KPI_DB, table("tabular_table2"), null),
                                        reference(KPI_DB, table("tabular_table2"), column("node_fdn"), null)
                                )
                        )));

        Assertions.assertThatCode(() -> objectUnderTest.validateExpressionNotContainsOnlyTabularParametersDatasource(resolutionResults))
                .doesNotThrowAnyException();

    }

    @Test
    void shouldThrowExceptionWithKpiDefinitionEntity(@Mock KpiDefinitionEntity kpiDefinitionEntityMock) {
        final Set<Relation> relations = Set.of(
                Relation.of(TABULAR_PARAMETERS, Table.of("KPI Table 1"), null)
        );

        when(sqlRelationExtractorMock.extractRelations(kpiDefinitionEntityMock)).thenReturn(relations);
        when(kpiDefinitionEntityMock.name()).thenReturn("Test KPI");
        when(kpiDefinitionEntityMock.expression()).thenReturn("SUM(dummy_data.tabular_table_1) FROM tabular_parameters://tabular_table_1");

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateExpressionNotContainsOnlyTabularParametersDatasource(kpiDefinitionEntityMock))
                .isInstanceOf(ExpressionContainsOnlyTabularParametersException.class);

        verify(sqlRelationExtractorMock).extractRelations(kpiDefinitionEntityMock);
        verify(kpiDefinitionEntityMock, times(2)).name();
        verify(kpiDefinitionEntityMock).expression();
    }

    @Test
    void shouldNotThrowExceptionWithKpiDefinitionEntity(@Mock KpiDefinitionEntity kpiDefinitionEntityMock) {
        final Set<Relation> relations = Set.of(
                Relation.of(TABULAR_PARAMETERS, Table.of("KPI Table 1"), null),
                Relation.of(KPI_DB, Table.of("KPI Table 2"), null)
        );

        when(sqlRelationExtractorMock.extractRelations(kpiDefinitionEntityMock)).thenReturn(relations);

        objectUnderTest.validateExpressionNotContainsOnlyTabularParametersDatasource(kpiDefinitionEntityMock);

        verify(sqlRelationExtractorMock).extractRelations(kpiDefinitionEntityMock);
    }

    @Test
    void shouldNotThrowExceptionWithKpiDefinitionEntityWhenRelationsAreEmpty(@Mock KpiDefinitionEntity kpiDefinitionEntityMock) {
        final Set<Relation> relations = emptySet();

        when(sqlRelationExtractorMock.extractRelations(kpiDefinitionEntityMock)).thenReturn(relations);

        objectUnderTest.validateExpressionNotContainsOnlyTabularParametersDatasource(kpiDefinitionEntityMock);

        verify(sqlRelationExtractorMock).extractRelations(kpiDefinitionEntityMock);
    }

    static RelationReferenceResolution resolution(final Relation relation, final Reference reference) {
        return new RelationReferenceResolution(relation, reference);
    }

    static ResolutionResult resolutionResult(final KpiDefinition kpiDefinition, final List<RelationReferenceResolution> resolutions) {
        final ResolutionResult resolutionResult = new ResolutionResult(kpiDefinition);
        resolutions.forEach(resolution -> resolutionResult.addResolvedResolution(resolution.relation().orElse(null), resolution.reference()));
        return resolutionResult;
    }

}