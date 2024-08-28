/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.filter;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = PRIVATE)
public final class RelationFilterer {
    private final Collection<Relation> relations;

    public static RelationFilterer relationFilterer(final Collection<Relation> relations) {
        return new RelationFilterer(relations);
    }

    public Set<Relation> collect() {
        return new HashSet<>(relations);
    }

    public void ifPresentOrElse(final Consumer<? super Relation> action, final Runnable emptyAction) {
        stream().collect(MoreCollectors.toOptional()).ifPresentOrElse(action, emptyAction);
    }

    public RelationFilterer onDatasources(final Datasource datasource) {
        return relationFilterer(stream().filter(relation -> relation.hasDatasource(datasource)));
    }

    public RelationFilterer onTables(final Table table) {
        return relationFilterer(stream().filter(relation -> relation.hasTable(table)));
    }

    public RelationFilterer onAliases(final Alias alias) {
        return relationFilterer(stream().filter(relation -> relation.hasAlias(alias)));
    }

    public RelationFilterer onTableOrAlias(final Table table, final Alias alias) {
        return relationFilterer(Sets.union(onTables(table).collect(), onAliases(alias).collect()));
    }

    private static RelationFilterer relationFilterer(@NonNull final Stream<Relation> relationStream) {
        return relationFilterer(relationStream.collect(toList()));
    }

    private Stream<Relation> stream() {
        return relations.stream();
    }
}
