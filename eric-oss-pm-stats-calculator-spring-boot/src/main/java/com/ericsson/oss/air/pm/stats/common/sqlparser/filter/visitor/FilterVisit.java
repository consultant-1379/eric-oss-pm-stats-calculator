/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.factory.FilterFactories.deduceCalculatorVisit;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaList;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.api.FilterVisitor;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.Expression;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public class FilterVisit {
    private final Expression expression;

    public Set<Reference> apply(@NonNull final FilterVisitor visitor) {
        final Set<Reference> references = new HashSet<>();

        for (final Expression child : asJavaList(expression.children())) {
            if (expression instanceof UnresolvedFunction && child instanceof UnresolvedAttribute) {
                // Function attributes collected by the Visitor implementation FilterVisitorImpl.visit(FilterUnresolvedFunction)
                continue;
            }

            final FilterVisit filterVisit = deduceCalculatorVisit(child);
            references.addAll(filterVisit.apply(visitor));
        }

        return references;
    }

}
