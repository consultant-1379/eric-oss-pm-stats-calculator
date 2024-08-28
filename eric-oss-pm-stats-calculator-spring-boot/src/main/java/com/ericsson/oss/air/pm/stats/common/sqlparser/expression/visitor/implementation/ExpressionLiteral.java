/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.implementation;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.ExpressionVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.api.ExpressionVisitor;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.expressions.Expression;

public class ExpressionLiteral extends ExpressionVisit {
    public ExpressionLiteral(final Expression expression) {
        super(expression);
    }

    @Override
    public <T> Set<T> apply(final @NonNull ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
