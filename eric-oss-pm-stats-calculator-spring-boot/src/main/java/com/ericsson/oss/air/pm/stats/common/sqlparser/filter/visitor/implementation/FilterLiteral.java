/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.union;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.FilterVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.api.FilterVisitor;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.expressions.Expression;

public class FilterLiteral extends FilterVisit {
    public FilterLiteral(final Expression expression) {
        super(expression);
    }

    @Override
    public Set<Reference> apply(@NonNull final FilterVisitor visitor) {
        return union(visitor.visit(this), super.apply(visitor));
    }
}
