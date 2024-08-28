/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor;


import static com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.factory.ExpressionFactories.deduceCalculatorVisit;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.api.ExpressionVisitor;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.spark.sql.catalyst.expressions.Expression;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public class ExpressionVisit {
    private final Expression expression;

    public <T> Set<T> apply(@NonNull final ExpressionVisitor<T> visitor) {
        final ExpressionVisit expressionVisit = deduceCalculatorVisit(expression);
        return expressionVisit.apply(visitor);
    }
}
