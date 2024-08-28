/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.ExpressionVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.api.ExpressionVisitor;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.catalyst.expressions.Expression;

@RequiredArgsConstructor
public class ExpressionExtractor<T> {
    private final ExpressionVisitor<T> expressionVisitor;

    public Set<T> extract(final Expression expression) {
        final ExpressionVisit expressionVisit = new ExpressionVisit(expression);
        return expressionVisit.apply(expressionVisitor);
    }

}
