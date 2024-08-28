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

import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.common.sqlparser.exception.InvalidSqlSyntaxException;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.catalyst.expressions.Expression;

@NoArgsConstructor(access = PRIVATE)
public final class FactoryUtils {

    public static <T> T deduceCalculatorVisit(
            @NonNull final Expression expression,
            @NonNull final Map<Class<?>, ? extends Function<Expression, T>> expressionMapper
    ) {
        final Class<? extends Expression> expressionClass = expression.getClass();
        if (expressionMapper.containsKey(expressionClass)) {
            return expressionMapper.get(expressionClass).apply(expression);
        }

        throw new InvalidSqlSyntaxException(String.format("'%s' is not deduced. Report to the developers", expressionClass));
    }

}
