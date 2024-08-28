/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.function.Predicate.isEqual;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
public final class References {

    public static void markLocation(@NonNull final Collection<Reference> references, final Location location) {
        references.forEach(tableColumn -> tableColumn.addLocation(location));
    }

    @SafeVarargs
    public static Set<Reference> mergeReferences(final Set<Reference>... references) {
        final Set<Reference> result = new HashSet<>();

        for (final Set<Reference> reference : references) {
            for (final Reference actualReference : reference) {
                result.stream().filter(isEqual(actualReference)).collect(toOptional()).ifPresentOrElse(
                        resultReference -> resultReference.addLocation(actualReference.locations()),
                        () -> result.add(actualReference));
            }
        }

        return result;
    }

    public static Relation relation(final Datasource datasource, final Table table, final Alias alias) {
        return Relation.of(datasource, table, alias);
    }

    public static Reference reference(final Datasource datasource, final Table table, final Column column, final Alias alias) {
        return Reference.of(datasource, table, column, alias);
    }

    public static Reference reference(final Datasource datasource, final Table table, final Column column, final Alias alias,
                                      final String parsedFunctionSql) {
        return Reference.of(datasource, table, column, alias, parsedFunctionSql);
    }

    public static Datasource datasource(final String datasource) {
        return Datasource.of(datasource);
    }

    public static Column column(final String column) {
        return Column.of(column);
    }

    public static Table table(final String table) {
        return Table.of(table);
    }

    public static Alias alias(final String alias) {
        return Alias.of(alias);
    }
}