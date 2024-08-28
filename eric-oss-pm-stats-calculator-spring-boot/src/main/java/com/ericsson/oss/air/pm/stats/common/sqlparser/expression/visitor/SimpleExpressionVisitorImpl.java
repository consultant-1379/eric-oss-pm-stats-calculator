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

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers.parseJsonPath;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.api.ExpressionVisitor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedNamedLambdaVariable;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.expressions.UnresolvedNamedLambdaVariable;

public class SimpleExpressionVisitorImpl implements ExpressionVisitor<JsonPath> {
    Set<String> arguments = new HashSet<>();

    @Override
    public Set<JsonPath> visit(@NonNull final ExpressionLiteral expressionLiteral) {
        /* We know Literal but cannot deduce JSON path from it */
        return Set.of();
    }

    @Override
    public Set<JsonPath> visit(@NonNull final ExpressionUnresolvedAttribute expressionUnresolvedAttribute) {
        final UnresolvedAttribute unresolvedAttribute = (UnresolvedAttribute) expressionUnresolvedAttribute.expression();
        return parseJsonPath(unresolvedAttribute.nameParts());
    }

    @Override
    public Set<JsonPath> visit(@NonNull final ExpressionUnresolvedNamedLambdaVariable expressionUnresolvedNamedLambdaVariable) {
        final UnresolvedNamedLambdaVariable unresolvedAttribute = (UnresolvedNamedLambdaVariable) expressionUnresolvedNamedLambdaVariable.expression();
        return parseJsonPath(unresolvedAttribute.nameParts());
    }
}
