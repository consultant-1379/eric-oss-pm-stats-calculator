/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.exception;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InvalidSqlReferenceExceptionTest {

    @Test
    void shouldGetImmutableUnresolvedResolutions() {
        final ResolutionResult resolutionResult = resolutionResult(
                resolvedResolutions(),
                unResolvedResolutions(resolution(relation(), reference(null, table("kpi_simple_60"), column("agg_column_0"), null)))
        );

        final InvalidSqlReferenceException invalidSqlReferenceException = new InvalidSqlReferenceException(List.of(resolutionResult));
        final List<ResolutionResult> actual = invalidSqlReferenceException.unresolvedResolutions();
        Assertions.assertThat(actual).isUnmodifiable().containsExactly(resolutionResult);
    }

    @Nested
    class Instantiation {
        @Test
        void shouldFailInstantiation_whenResolutionsAreEmpty() {
            Assertions.assertThatThrownBy(() -> new InvalidSqlReferenceException(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("'unresolvedResolutions' is empty");
        }

        @Test
        void shouldFailInstantiation_whenAnyResolutionIsResolved() {
            final ThrowingCallable throwingCallable = () -> {
                new InvalidSqlReferenceException(List.of(
                        resolutionResult(
                                resolvedResolutions(resolution(relation(), reference(null, null, null, alias("execution_id")))),
                                unResolvedResolutions(resolution(relation(), reference(null, table("kpi_simple_60"), column("agg_column_0"), null)))
                        ),
                        resolutionResult(
                                resolvedResolutions(resolution(relation(), reference(null, null, null, alias("execution_id")))),
                                unResolvedResolutions()
                        )
                ));
            };

            Assertions.assertThatThrownBy(throwingCallable)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("'unresolvedResolutions' contain RESOLVED");
        }

    }

    static Relation relation() {
        return References.relation(KPI_DB, table("kpi_simple_60"), null);
    }

    static List<RelationReferenceResolution> resolvedResolutions(final RelationReferenceResolution... resolutions) {
        return List.of(resolutions);
    }

    static List<RelationReferenceResolution> unResolvedResolutions(final RelationReferenceResolution... resolutions) {
        return List.of(resolutions);
    }


    static RelationReferenceResolution resolution(final Relation relation, final Reference reference) {
        return new RelationReferenceResolution(relation, reference);
    }

    static ResolutionResult resolutionResult(
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