/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.sql;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.proxy.AggregationElementProxy;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.proxy.FilterElementProxy;
import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor.ExpressionExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.NonSimpleExpressionVisitorImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.SimpleExpressionVisitorImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.RelationCollectors;

import kpi.model.simple.table.definition.required.SimpleDefinitionExpression;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqlProcessorDelegator {
    private final SqlProcessorService sqlProcessorService;

    public LogicalPlan parsePlan(@NonNull final ExpressionAttribute expressionAttribute) {
        return sqlProcessorService.parsePlan(expressionAttribute);
    }

    public Set<Reference> aggregationElements(@NonNull final Collection<String> elements) {
        final List<AggregationElementProxy> elementProxies = elements.stream().map(AggregationElementProxy::new).collect(toList());
        return sqlProcessorService.extractAggregationElementTableColumns(elementProxies);
    }

    public Set<Reference> filters(@NonNull final Collection<Filter> elements) {
        final List<FilterElementProxy> filterProxies = elements.stream().map(Filter::getName).map(FilterElementProxy::new).collect(toList());
        return sqlProcessorService.extractFilterTableColumns(filterProxies);
    }

    public Set<JsonPath> jsonPaths(final String expression) {
        final ExpressionExtractor<JsonPath> expressionExtractor = new ExpressionExtractor<>(new SimpleExpressionVisitorImpl());
        return sqlProcessorService.extractTableColumns(SimpleDefinitionExpression.of(expression), expressionExtractor);
    }

    /**
     * Collects {@link Relation} set from the expression using Sql Parser. Works for complex and ondemand expressions.
     *
     * @param expression complex and ondemand expressions
     * @return relations
     */
    public Set<Relation> getRelations(final ExpressionAttribute expression) {
        return RelationCollectors.collect(sqlProcessorService.parsePlan(expression));
    }

    /**
     * Collects {@link Reference} set from the expression using Sql Parser. Works for complex and ondemand expressions.
     *
     * @param expression complex and ondemand expressions
     * @return references
     */
    public Set<Reference> getReferences(final ExpressionAttribute expression) {
        final ExpressionExtractor<Reference> expressionExtractor = new ExpressionExtractor<>(new NonSimpleExpressionVisitorImpl());
        return sqlProcessorService.extractTableColumns(expression, expressionExtractor);
    }
}
