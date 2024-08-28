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

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.InvalidDataSourceAliasException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AliasValidatorTest {

    @InjectMocks
    AliasValidator objectUnderTest;

    @Test
    void shouldFindTabularAlias() {
        final KpiDefinition kpiDefinitionMock = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
        final List<ResolutionResult> resolutionResults = List.of(
                resolutionResult(kpiDefinitionMock, List.of(
                        resolution(
                                relation(TABULAR_PARAMETERS, table("tabular_table"), Alias.of("alias_name")),
                                reference(TABULAR_PARAMETERS, table("alias_name"), column("node_fdn"), null
                                ))
                )));

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateNoAliasedTabularParameterTable(resolutionResults))
                .isInstanceOf(InvalidDataSourceAliasException.class);

    }

    @Test
    void withoutTabularAlias() {
        final KpiDefinition kpiDefinitionMock = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
        final List<ResolutionResult> resolutionResults = List.of(
                resolutionResult(kpiDefinitionMock, List.of(
                        resolution(
                                relation(TABULAR_PARAMETERS, table("tabular_table"), null),
                                reference(TABULAR_PARAMETERS, table("tabular_table"), column("node_fdn"), null
                                ))
                )));

        objectUnderTest.validateNoAliasedTabularParameterTable(resolutionResults);
    }

    static RelationReferenceResolution resolution(final Relation relation, final Reference reference) {
        return new RelationReferenceResolution(relation, reference);
    }

    static ResolutionResult resolutionResult(final KpiDefinition kpiDefinition, final List<RelationReferenceResolution> resolutions) {
        final ResolutionResult resolutionResult = new ResolutionResult(kpiDefinition);

        resolutions.forEach(resolution -> {
            resolutionResult.addResolvedResolution(resolution.relation().orElse(null), resolution.reference());
        });


        return resolutionResult;
    }
}