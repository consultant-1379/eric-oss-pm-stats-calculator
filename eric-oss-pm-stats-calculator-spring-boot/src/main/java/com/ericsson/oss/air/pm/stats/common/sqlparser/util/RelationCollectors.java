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

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers.parseRelation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.Casts.tryCast;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaCollection;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaList;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.catalyst.plans.logical.SubqueryAlias;

@NoArgsConstructor(access = PRIVATE)
public final class RelationCollectors {

    @SuppressWarnings("squid:S1602")
    public static Set<Relation> collect(@NonNull final LogicalPlan logicalPlan) {
        final Set<Relation> relations = new HashSet<>();

        asJavaCollection(logicalPlan.children()).forEach(child -> {
            tryCast(child, SubqueryAlias.class).ifPresentOrElse(subqueryAlias -> {
                //  Template looks like <datasource>.<table> AS <alias> where the
                //      <alias>              is the SubqueryAlias
                //      <datasource>.<table> is the SubqueryAlias.children as UnresolvedRelation
                asJavaList(child.children()).forEach(grandChild -> {
                    tryCast(grandChild, UnresolvedRelation.class).ifPresent(unresolvedRelation -> {
                        relations.add(parseRelation(asJavaList(unresolvedRelation.multipartIdentifier()), subqueryAlias.alias()));
                    });
                });
            }, () -> {
                tryCast(child, UnresolvedRelation.class).ifPresentOrElse(unresolvedRelation -> {
                    //  Template looks like <datasource>.<table> AS <null> where the
                    //      <datasource>.<table> is the UnresolvedRelation
                    relations.add(parseRelation(asJavaList(unresolvedRelation.multipartIdentifier()), null));
                }, () -> {
                    //  If it is not a SubqueryAlias or UnresolvedRelation then we have to go deeper recursively
                    relations.addAll(collect(child));
                });
            });
        });

        return relations;
    }
}
