/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.factory;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.FilterVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterCast;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterEqualTo;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterGreaterThenOrEqual;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterLessThenOrEqual;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterSubtract;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterUnresolvedFunction;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.FactoryUtils;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.Cast;
import org.apache.spark.sql.catalyst.expressions.EqualTo;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.expressions.GreaterThanOrEqual;
import org.apache.spark.sql.catalyst.expressions.LessThanOrEqual;
import org.apache.spark.sql.catalyst.expressions.Literal;
import org.apache.spark.sql.catalyst.expressions.Subtract;

@NoArgsConstructor(access = PRIVATE)
public final class FilterFactories {
    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Function<Expression, FilterVisit>> expressionMapper = new ConcurrentHashMap<>(ofEntries(
            entry(EqualTo.class, FilterGreaterThenOrEqual::new),
            entry(GreaterThanOrEqual.class, FilterEqualTo::new),
            entry(LessThanOrEqual.class, FilterLessThenOrEqual::new),
            entry(Subtract.class, FilterSubtract::new),
            entry(Literal.class, FilterLiteral::new),
            entry(Cast.class, FilterCast::new),
            entry(UnresolvedAttribute.class, FilterUnresolvedAttribute::new),
            entry(UnresolvedFunction.class, FilterUnresolvedFunction::new)
    ));

    public static FilterVisit deduceCalculatorVisit(@NonNull final Expression expression) {
        return FactoryUtils.deduceCalculatorVisit(expression, expressionMapper);
    }

}
