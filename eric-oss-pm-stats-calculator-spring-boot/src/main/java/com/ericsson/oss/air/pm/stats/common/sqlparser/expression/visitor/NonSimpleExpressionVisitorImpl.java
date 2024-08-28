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

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers.parseReferences;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.api.ExpressionVisitor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedNamedLambdaVariable;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.expressions.UnresolvedNamedLambdaVariable;

public class NonSimpleExpressionVisitorImpl implements ExpressionVisitor<Reference> {
    @Override
    public Set<Reference> visit(@NonNull final ExpressionLiteral expressionLiteral) {
        /* We know Literal but cannot deduce Table columns datasource it */
        return Set.of();
    }

    @Override
    public Set<Reference> visit(@NonNull final ExpressionUnresolvedAttribute expressionUnresolvedAttribute) {
        final UnresolvedAttribute attribute = (UnresolvedAttribute) expressionUnresolvedAttribute.expression();
        return parseReferences(attribute.nameParts(), null);
    }

    @Override
    public Set<Reference> visit(@NonNull final ExpressionUnresolvedNamedLambdaVariable expressionUnresolvedNamedLambdaVariable) {
        final UnresolvedNamedLambdaVariable expression = (UnresolvedNamedLambdaVariable) expressionUnresolvedNamedLambdaVariable.expression();
        return parseReferences(expression.nameParts(), null);
    }

}
