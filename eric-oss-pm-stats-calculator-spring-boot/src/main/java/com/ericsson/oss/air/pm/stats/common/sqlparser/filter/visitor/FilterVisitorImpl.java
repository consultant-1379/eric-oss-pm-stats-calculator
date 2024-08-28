/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers.parseColumn;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.datasource;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections.asJavaList;
import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.api.FilterVisitor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterCast;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterEqualTo;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterGreaterThenOrEqual;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterLessThenOrEqual;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterSubtract;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterUnresolvedFunction;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.AttributeParsers;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;

public class FilterVisitorImpl implements FilterVisitor {

    @Override
    public Set<Reference> visit(@NonNull final FilterEqualTo filterEqualTo) {
        /* We know EqualTo but cannot deduce Reference datasource it */
        return emptySet();
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterGreaterThenOrEqual filterGreaterThenOrEqual) {
        /* We know GreaterThenOrEqual but cannot deduce Reference datasource it */
        return emptySet();
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterLessThenOrEqual filterLessThenOrEqual) {
        /* We know LessThenOrEqual but cannot deduce Reference datasource it */
        return emptySet();
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterSubtract filterSubtract) {
        /* We know Subtract but cannot deduce Reference datasource it */
        return emptySet();
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterLiteral filterLiteral) {
        /* We know Literal but cannot deduce Reference datasource it */
        return emptySet();
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterCast filterCast) {
        /* We know Cast but cannot deduce Reference datasource it */
        return emptySet();
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterUnresolvedFunction filterUnresolvedFunction) {
        final UnresolvedFunction unresolvedFunction = (UnresolvedFunction) filterUnresolvedFunction.expression();

        final List<String> parts = asJavaList(unresolvedFunction.nameParts());

        if (parts.size() == 1) {
            //  Outer function - contains only the name of the function
            return emptySet();
        }

        final Datasource datasource = datasource(parts.get(0));
        final Table table = References.table(parts.get(1));

        //  final String function = parts.get(2); With the current syntax function

        final Set<Reference> references = new HashSet<>();
        asJavaList(unresolvedFunction.arguments()).forEach(expression -> {
            if (expression instanceof UnresolvedAttribute) {
                final Column column = parseColumn((UnresolvedAttribute) expression);
                references.add(References.reference(datasource, table, column, null));
            }
        });

        return references;
    }

    @Override
    public Set<Reference> visit(@NonNull final FilterUnresolvedAttribute filterUnresolvedAttribute) {
        final UnresolvedAttribute unresolvedAttribute = (UnresolvedAttribute) filterUnresolvedAttribute.expression();
        return AttributeParsers.parseReferences(unresolvedAttribute.nameParts(), null);
    }
}
