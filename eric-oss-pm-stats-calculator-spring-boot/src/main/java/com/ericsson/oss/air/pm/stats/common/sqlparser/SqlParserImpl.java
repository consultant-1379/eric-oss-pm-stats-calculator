/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.prepare.SqlExpressionPrepares.prepareSyntax;
import static java.lang.String.format;
import static java.lang.String.join;
import static lombok.AccessLevel.PUBLIC;

import java.util.Set;
import java.util.function.Supplier;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.exception.InvalidSqlSyntaxException;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.RelationCollectors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.plans.logical.Join;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.SparkSqlParser;

@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class SqlParserImpl {
    @Inject private SparkSqlParser sparkSqlParser;
    @Inject private LogicalPlanExtractor logicalPlanExtractor;

    public LogicalPlan parsePlan(@NonNull final ExpressionAttribute expressionAttribute) {
        final LogicalPlan logicalPlan = handleException(() -> sparkSqlParser.parsePlan(prepareSyntax(expressionAttribute)));
        logicalPlanExtractor.consumeType(logicalPlan, Join.class, joins -> {
            Set<Relation> relations = RelationCollectors.collect(joins);
            if (joins.condition().isEmpty()) {
                throw new InvalidSqlSyntaxException(format(
                        "In expression %s 'JOIN' with the following tables: %s does not contain 'ON' condition",
                        expressionAttribute.value(),
                        join(", ", relations.stream().map(Object::toString).toList())));
            }

            final boolean condition = relations.stream().anyMatch(relation -> relation.datasource().isEmpty());
            if (condition) {
                throw new InvalidSqlSyntaxException(format(
                        "In expression %s there is missing datasource for the following table(s): %s",
                        expressionAttribute.value(),
                        join(", ", relations.stream()
                                                    .filter(relation -> relation.datasource().isEmpty())
                                                    .map(Object::toString)
                                                    .toList())
                ));
            }
        });
        return logicalPlan;
    }

    public Expression parseExpression(@NonNull final FilterElement filterElement) {
        return handleException(() -> sparkSqlParser.parseExpression(prepareSyntax(filterElement)));
    }

    public Expression parseExpression(@NonNull final AggregationElement aggregationElement) {
        return handleException(() -> sparkSqlParser.parseExpression(aggregationElement.value()));
    }

    public LogicalPlan parsePlan(@NonNull String sqlExpression){
        return handleException(()->sparkSqlParser.parsePlan(sqlExpression));
    }

    private <O> O handleException(final Supplier<O> outputSupplier) {
        try {
            return outputSupplier.get();
        } catch (final Exception e) {
            /* org.apache.spark.sql.catalyst.parser.ParseException is not caught with RuntimeException */
            throw new InvalidSqlSyntaxException(e);
        }
    }
}
