/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.mapper;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
public final class SourceMapper {

    public static Set<SourceColumn> toSourceColumns(@NonNull final Collection<Reference> references, final Collection<Relation> relations) {
        final Set<SourceColumn> sourceColumns = new HashSet<>(references.size());

        references.forEach(reference -> {
            final SourceColumn sourceColumn = SourceColumn.from(reference);

            deduceTable(sourceColumn, relations, reference);
            deduceDatasource(sourceColumn, relations);

            sourceColumns.add(sourceColumn);
        });

        return sourceColumns;
    }

    public static Set<SourceTable> toSourceTables(@NonNull final Collection<Relation> relations) {
        return relations.stream().map(SourceTable::from).collect(toSet());
    }

    private static void deduceDatasource(final SourceColumn sourceColumn, final Collection<Relation> relations) {
        if (sourceColumn.hasDatasource() || !sourceColumn.hasTable()) {
            return;
        }

        for (final Relation relation : relations) {
            relation.datasource().ifPresent(datasource -> {
                if (relation.hasTable(sourceColumn.getTable())) {
                    sourceColumn.setDatasource(datasource);
                }
            });
        }
    }

    private static void deduceTable(final SourceColumn sourceColumn, final Collection<Relation> relations, final Reference reference) {
        for (final Relation relation : relations) {
            if (isRelationAliased(relation, reference)) {
                sourceColumn.setTable(relation.table());
                return;
            }
        }
    }

    private static boolean isRelationAliased(final Relation relation, final Reference reference) {
        final Optional<Alias> alias = relation.alias();
        final Optional<Table> table = reference.table();
        return alias.isPresent() && table.isPresent() && table.get().getName().equals(alias.get().name());
    }
}
