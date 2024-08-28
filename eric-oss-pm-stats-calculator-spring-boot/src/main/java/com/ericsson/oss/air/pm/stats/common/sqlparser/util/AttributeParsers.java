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

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.datasource;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaCollection;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaList;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import scala.collection.Iterable;

@NoArgsConstructor(access = PRIVATE)
public final class AttributeParsers {

    public static Column parseColumn(final UnresolvedAttribute unresolvedAttribute) {
        final List<String> parts = asJavaList(unresolvedAttribute.nameParts());

        if (isEmpty(parts) || parts.size() > 1) {
            throw new IllegalArgumentException(String.format("Expected a single column but found invalid parts '%s'", parts));
        }

        return column(parts.get(0));
    }

    public static Relation parseRelation(@NonNull final List<String> parts, @Nullable final String alias) {
        final Alias actualAlias = ofNullable(alias).map(References::alias).orElse(null);
        switch (parts.size()) {
            case 1:
                return relation(null, table(parts.get(0)), actualAlias);
            case 2:
                return relation(datasource(parts.get(0)), table(parts.get(1)), actualAlias);
            default:
                throw new IllegalArgumentException(String.format("'%s' contains invalid parts '%s'", Relation.class.getSimpleName(), parts));
        }
    }

    public static Set<JsonPath> parseJsonPath(final Iterable<String> path) {
        final List<String> parts = new ArrayList<>(asJavaCollection(path));
        return Set.of(JsonPath.of(parts));
    }

    public static Set<Reference> parseReferences(@NonNull final List<String> nameParts, final String alias) {
        final Alias actualAlias = ofNullable(alias).map(References::alias).orElse(null);
        switch (nameParts.size()) {
            case 1:
                return Set.of(reference(null, null, column(nameParts.get(0)), actualAlias));
            case 2:
                return Set.of(reference(null, table(nameParts.get(0)), column(nameParts.get(1)), actualAlias));
            case 3:
                return Set.of(reference(datasource(nameParts.get(0)), table(nameParts.get(1)), column(nameParts.get(2)), actualAlias));
            default:
                throw new IllegalArgumentException(String.format(
                        "'%s' can have at most three parts <column> or <table>.<column> or <datasource>.<table>.<column> but had '%d': '%s'",
                        UnresolvedAttribute.class.getSimpleName(), nameParts.size(), nameParts
                ));
        }
    }

    public static Set<Reference> parseReferences(final Iterable<String> nameParts, final String alias) {
        final List<String> parts = new ArrayList<>(asJavaCollection(nameParts));
        return parseReferences(parts, alias);
    }

    public static Set<Reference> parseFunctionReferences(final Iterable<String> nameParts, @NonNull final String alias,
                                                         @NonNull final String parsedFunctionSql) {
        final List<String> parts = new ArrayList<>(asJavaCollection(nameParts));
        final Alias actualAlias = Alias.of(alias);
        switch (nameParts.size()) {
            case 1:
                return Set.of(reference(null, null, column(parts.get(0)), actualAlias, parsedFunctionSql));
            case 2:
                return Set.of(reference(null, table(parts.get(0)), column(parts.get(1)), actualAlias, parsedFunctionSql));
            case 3:
                return Set.of(reference(datasource(parts.get(0)), table(parts.get(1)), column(parts.get(2)), actualAlias, parsedFunctionSql));
            default:
                throw new IllegalArgumentException(String.format(
                        "'%s' can have at most three parts <column> or <table>.<column> or <datasource>.<table>.<column> but had '%d': '%s'",
                        UnresolvedAttribute.class.getSimpleName(), nameParts.size(), nameParts
                ));
        }
    }

}
