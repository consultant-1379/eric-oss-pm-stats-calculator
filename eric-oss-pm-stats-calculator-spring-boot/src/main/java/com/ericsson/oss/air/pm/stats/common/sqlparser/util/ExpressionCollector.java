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


import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.Indents.indent;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor.ExpressionExtractor;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.expressions.LambdaFunction;
import org.apache.spark.sql.catalyst.expressions.NamedExpression;
import scala.collection.Iterable;
import scala.collection.Iterator;
import scala.collection.Seq;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor /* Internal testing only */
public class ExpressionCollector {
    @Inject private LeafCollector leafCollector;

    public <T> Set<T> collect(@NonNull final Iterable<Expression> expressions, final ExpressionExtractor<T> expressionExtractor, final int depth) {
        final Set<T> result = new HashSet<>();

        final Iterator<Expression> iterator = expressions.iterator();
        while (iterator.hasNext()) {
            final Expression expression = iterator.next();
            if (log.isDebugEnabled()) {
                log.debug("{}({})  {}", indent(depth), expression.getClass().getSimpleName(), expression.sql());
            }

            final Seq<Expression> children = expression.children();
            if (children.isEmpty()) {
                result.addAll(leafCollector.collect(expression.collectLeaves(), expressionExtractor));
                continue;
            }

            result.addAll(collect(children, expressionExtractor, depth + 1));

            lambdaParameters(expressionExtractor, expression).forEach(lambdaParameter -> {
                if (log.isDebugEnabled()) {
                    log.debug("{}({})   {} removed", indent(depth), expression.getClass().getSimpleName(), lambdaParameter);
                }
                result.remove(lambdaParameter);
            });
        }

        return result;
    }

    private static <T> Set<T> lambdaParameters(final ExpressionExtractor<T> expressionExtractor, final Expression expression) {
        final Set<T> toBeRemoved = new HashSet<>();

        if (expression instanceof LambdaFunction) {
            final LambdaFunction lambdaFunction = (LambdaFunction) expression;
            final Seq<NamedExpression> arguments = lambdaFunction.arguments();
            final Iterator<NamedExpression> iterator = arguments.iterator();
            while (iterator.hasNext()) {
                final NamedExpression namedExpression = iterator.next();
                toBeRemoved.addAll(expressionExtractor.extract((Expression) namedExpression));
            }
        }

        return toBeRemoved;
    }


}
