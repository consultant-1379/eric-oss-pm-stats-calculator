/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser;

import static lombok.AccessLevel.PUBLIC;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.AggregationElementVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.AggregationElementVisitorImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor.ExpressionExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.FilterVisit;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.FilterVisitorImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.catalyst.analysis.CustomAnalyzer;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import scala.collection.Iterator;
import scala.collection.Seq;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class SqlProcessorService {
    @Inject private SqlParserImpl sqlParser;
    @Inject private ExpressionCollector expressionCollector;

    public LogicalPlan parsePlan(@NonNull final ExpressionAttribute expressionAttribute) {
        return CustomAnalyzer.execute(sqlParser.parsePlan(expressionAttribute));
    }

    public <T> Set<T> extractTableColumns(final ExpressionAttribute expressionAttribute, final ExpressionExtractor<T> expressionExtractor) {
        return collectAttributes(parsePlan(expressionAttribute), expressionExtractor, 0);
    }

    public Set<Reference> extractFilterTableColumns(final Iterable<? extends FilterElement> filterElements) {
        final Set<Reference> references = new LinkedHashSet<>();

        filterElements.forEach(filterElement -> {
            final Expression expression = sqlParser.parseExpression(filterElement);
            final FilterVisit filterVisit = new FilterVisit(expression);
            references.addAll(filterVisit.apply(new FilterVisitorImpl()));
        });

        return references;
    }

    public Set<Reference> extractAggregationElementTableColumns(final Iterable<? extends AggregationElement> aggregationElements) {
        final Set<Reference> references = new LinkedHashSet<>();

        aggregationElements.forEach(aggregationElement -> {
            final Expression expression = sqlParser.parseExpression(aggregationElement);
            final AggregationElementVisit aggregationElementVisit = new AggregationElementVisit(expression);
            references.addAll(aggregationElementVisit.apply(new AggregationElementVisitorImpl()));
        });

        return references;
    }

    private <T> Set<T> collectAttributes(@NonNull final LogicalPlan logicalPlan, final ExpressionExtractor<T> expressionExtractor, final int depth) {
        final Set<T> result = new HashSet<>(expressionCollector.collect(logicalPlan.expressions(), expressionExtractor, depth));

        final Seq<LogicalPlan> children = logicalPlan.children();
        final Iterator<LogicalPlan> iterator = children.iterator();

        while (iterator.hasNext()) {
            final LogicalPlan child = iterator.next();
            result.addAll(collectAttributes(child, expressionExtractor, depth + 1));
        }

        return result;
    }

}
