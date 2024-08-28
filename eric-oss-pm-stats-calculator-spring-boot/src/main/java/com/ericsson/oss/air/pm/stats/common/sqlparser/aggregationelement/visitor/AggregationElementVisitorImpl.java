/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers.parseReferences;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaList;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.api.AggregationElementVisitor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.implementation.AggregationElementAlias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.implementation.AggregationElementUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;

import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.Alias;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.expressions.Literal;

public class AggregationElementVisitorImpl implements AggregationElementVisitor {

    @Override
    public Set<Reference> visit(@NonNull final AggregationElementAlias aggregationElementAlias) {
        final Alias alias = (Alias) aggregationElementAlias.expression();

        final Expression child = alias.child();
        if (child instanceof UnresolvedAttribute) {
            final UnresolvedAttribute unresolvedAttribute = (UnresolvedAttribute) child;
            return parseReferences(asJavaList(unresolvedAttribute.nameParts()), alias.name());
        }

        if (child instanceof Literal) {
            final Literal literal = (Literal) child;
            final String value = literal.value().toString();
            final Column column = value.isEmpty() ? null : column('\'' + value + '\'');
            return Set.of(References.reference(null, null, column, alias(alias.name())));
        }

        if (child instanceof UnresolvedFunction) {
            // Since UnresolvedFunction in aggregation will always require an alias, it may only be visited as member of an AggregationElementAlias,
            // and not in the form of a separate overload.
            // AggregationElement validation guarantees the existence and validity of the specific name parts at this point.
            final UnresolvedFunction unresolvedFunction = (UnresolvedFunction) child;
            final List<String> functionNameParts = asJavaList(unresolvedFunction.nameParts());
            Validate.isTrue(!functionNameParts.isEmpty(), "Function name parts must not be empty.");
            java.util.Optional<Expression> attribute = asJavaList(unresolvedFunction.arguments())
                    .stream()
                    .filter(UnresolvedAttribute.class::isInstance)
                    .findFirst();
            Validate.isTrue(attribute.isPresent(), "The function's first attribute must be present.");
            return AttributeParsers.parseFunctionReferences(
                    ((UnresolvedAttribute)attribute.get()).nameParts(), alias.name(), unresolvedFunction.sql());
        }

        throw new IllegalArgumentException(String.format("'%s' is not parsable '%s'", AggregationElement.class.getSimpleName(), alias.sql()));
    }

    @Override
    public Set<Reference> visit(@NonNull final AggregationElementUnresolvedAttribute aggregationElementUnresolvedAttribute) {
        final UnresolvedAttribute unresolvedAttribute = (UnresolvedAttribute) aggregationElementUnresolvedAttribute.expression();
        return AttributeParsers.parseReferences(unresolvedAttribute.nameParts(), null);
    }
}
