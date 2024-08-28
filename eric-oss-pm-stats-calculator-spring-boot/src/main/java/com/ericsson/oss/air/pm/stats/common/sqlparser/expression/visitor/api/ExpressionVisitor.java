/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.api;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation.ExpressionUnresolvedNamedLambdaVariable;

import lombok.NonNull;

public interface ExpressionVisitor<T> {
    Set<T> visit(@NonNull ExpressionLiteral expressionLiteral);
    Set<T> visit(@NonNull ExpressionUnresolvedAttribute expressionUnresolvedAttribute);
    Set<T> visit(@NonNull ExpressionUnresolvedNamedLambdaVariable expressionUnresolvedNamedLambdaVariable);
}
