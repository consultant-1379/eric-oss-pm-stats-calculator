/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.reference;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
public class RelationReferenceResolution {
    private final Relation relation;
    private final Reference reference;

    private boolean isResolved;

    @SuppressWarnings("nousage")
    public RelationReferenceResolution(@Nullable final Relation relation, @NonNull final Reference reference) {
        validateState(relation, reference);
        this.relation = relation;
        this.reference = reference;
    }

    public boolean isUnresolved() {
        return !isResolved;
    }

    public void resolve() {
        isResolved = true;
    }

    public Optional<Relation> relation() {
        return ofNullable(relation);
    }

    private static void validateState(final Relation relation, final Reference reference) {
        validateDatasource(relation, reference);
        validateTable(relation, reference);
        validateAlias(relation, reference);
    }

    @SuppressWarnings("squid:S1602")
    private static void validateAlias(final Relation relation, final Reference reference) {
        ofNullable(relation).flatMap(Relation::alias).ifPresent(relationAlias -> {
            reference.table().ifPresent(referenceTable -> {
                checkArgument(relationAlias.name().equals(referenceTable.getName()), String.format(
                        "'%s' '%s' '%s' is not equal to '%s' '%s' '%s'",
                        Relation.class.getSimpleName(), Alias.class.getSimpleName(), relationAlias.name(),
                        Reference.class.getSimpleName(), Table.class.getSimpleName(), referenceTable.getName()
                ));
            });
        });
    }

    @SuppressWarnings("squid:S1602")
    private static void validateTable(final Relation relation, final Reference reference) {
        ofNullable(relation).ifPresent(actualRelation -> {
            if (actualRelation.alias().isEmpty()) {
                //  In case of alias we check the reference table against the alias in that validation
                final Table relationTable = actualRelation.table();
                reference.table().ifPresent(referenceTable -> {
                    checkArgument(relationTable.equals(referenceTable), String.format(
                            "'%s' '%s' '%s' is not equal to '%s' '%s' '%s'",
                            Relation.class.getSimpleName(), Table.class.getSimpleName(), relationTable.getName(),
                            Reference.class.getSimpleName(), Table.class.getSimpleName(), referenceTable.getName()
                    ));
                });
            }
        });
    }

    @SuppressWarnings("squid:S1602")
    private static void validateDatasource(final Relation relation, final Reference reference) {
        ofNullable(relation).flatMap(Relation::datasource).ifPresent(relationDatasource -> {
            reference.datasource().ifPresent(referenceDatasource -> {
                checkArgument(relationDatasource.equals(referenceDatasource), String.format(
                        "'%s' '%s' '%s' is not equal to '%s' '%s' '%s'",
                        Relation.class.getSimpleName(), Datasource.class.getSimpleName(), relationDatasource,
                        Reference.class.getSimpleName(), Datasource.class.getSimpleName(), referenceDatasource
                ));
            });
        });
    }

    @Override
    public String toString() {
        return String.join(
                " ",
                isResolved ? "RESOLVED" : "UNRESOLVED",
                relation().map(Object::toString).orElse("<relation>"),
                reference.toString()
        );
    }
}
