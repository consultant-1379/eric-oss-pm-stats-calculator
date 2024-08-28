/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.implementation;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.AggregationElementVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.api.AggregationElementVisitor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.expressions.Expression;

public class AggregationElementUnresolvedAttribute extends AggregationElementVisit {
    public AggregationElementUnresolvedAttribute(final Expression expression) {
        super(expression);
    }

    @Override
    public Set<Reference> apply(@NonNull final AggregationElementVisitor visitor) {
        return visitor.visit(this);
    }
}
