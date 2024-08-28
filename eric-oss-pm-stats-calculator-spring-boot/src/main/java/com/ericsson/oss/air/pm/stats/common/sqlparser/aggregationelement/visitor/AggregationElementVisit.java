/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.factory.AggregationElementFactories.deduceCalculatorVisit;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.api.AggregationElementVisitor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.spark.sql.catalyst.expressions.Expression;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public class AggregationElementVisit {
    private final Expression expression;

    public Set<Reference> apply(@NonNull final AggregationElementVisitor visitor) {
        final AggregationElementVisit aggregationElementVisit = deduceCalculatorVisit(expression);
        return new HashSet<>(aggregationElementVisit.apply(visitor));
    }

}
