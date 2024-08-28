/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.factory;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.ExpressionVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedNamedLambdaVariable;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.FactoryUtils;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.expressions.Literal;
import org.apache.spark.sql.catalyst.expressions.UnresolvedNamedLambdaVariable;

@NoArgsConstructor(access = PRIVATE)
public final class ExpressionFactories {
    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Function<Expression, ExpressionVisit>> expressionMapper = new ConcurrentHashMap<>(ofEntries(
            entry(UnresolvedAttribute.class, ExpressionUnresolvedAttribute::new),
            entry(UnresolvedNamedLambdaVariable.class, ExpressionUnresolvedNamedLambdaVariable::new),
            entry(Literal.class, ExpressionLiteral::new)
    ));

    public static ExpressionVisit deduceCalculatorVisit(@NonNull final Expression expression) {
        return FactoryUtils.deduceCalculatorVisit(expression, expressionMapper);
    }

}
