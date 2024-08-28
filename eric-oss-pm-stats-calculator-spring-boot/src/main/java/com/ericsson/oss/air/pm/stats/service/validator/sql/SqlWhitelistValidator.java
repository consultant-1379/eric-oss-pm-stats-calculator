/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.prepare.SqlExpressionPrepares.prepareSyntax;
import static lombok.AccessLevel.PUBLIC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkTrees;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.NotAllowedSqlElementException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.SqlWhitelistValidationException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist.SqlValidatorImpl;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.definition.KpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.catalyst.trees.TreeNode;
import org.apache.spark.sql.execution.SparkSqlParser;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class SqlWhitelistValidator {
    @Inject
    private SparkSqlParser sparkSqlParser;
    @Inject
    private SqlValidatorImpl sqlValidator;

    public void validateSqlElements(final KpiDefinitionRequest kpiDefinitionRequest) {
        final Map<String, Set<String>> notAllowedElementsByKpiDefinitions = new HashMap<>();

        kpiDefinitionRequest.definitions()
                .forEach(kpiDefinition -> {
                    Set<String> notAllowedSqlElements = collectNotAllowedSqlElements(kpiDefinition);

                    if (!notAllowedSqlElements.isEmpty()) {
                        notAllowedElementsByKpiDefinitions.put(kpiDefinition.name().value(), notAllowedSqlElements);
                    }
                });

        if (!notAllowedElementsByKpiDefinitions.isEmpty()) {
            throw new SqlWhitelistValidationException(notAllowedElementsByKpiDefinitions);
        }
    }

    public void validateSqlElements(final KpiDefinitionEntity kpiDefinitionEntity) {
        Set<String> notAllowedSqlElements = collectNotAllowedSqlElements(kpiDefinitionEntity);
        if (!notAllowedSqlElements.isEmpty()) {
            throw new SqlWhitelistValidationException(Map.of(kpiDefinitionEntity.name(), notAllowedSqlElements));
        }
    }

    private Set<String> collectNotAllowedSqlElements(final KpiDefinition kpiDefinition) {
        final LogicalPlan logicalPlan = sparkSqlParser.parsePlan(prepareSyntax(kpiDefinition.expression()));

        final Set<String> notAllowedElements = new HashSet<>();

        treeNodes(logicalPlan).forEach(
                treeNode -> collectInvalidElement(treeNode).ifPresent(notAllowedElements::add)
        );

        return notAllowedElements;
    }

    private Set<String> collectNotAllowedSqlElements(final KpiDefinitionEntity kpiDefinitionEntity) {
        final LogicalPlan logicalPlan = sparkSqlParser.parsePlan(prepareSyntax(kpiDefinitionEntity.expression()));

        final Set<String> notAllowedElements = new HashSet<>();

        treeNodes(logicalPlan).forEach(
                treeNode -> collectInvalidElement(treeNode).ifPresent(notAllowedElements::add)
        );

        return notAllowedElements;
    }

    @SuppressWarnings("squid:S1166")
    private Optional<String> collectInvalidElement(final TreeNode<?> treeNode) {
        try {
            sqlValidator.validate(treeNode);
            return Optional.empty();
        } catch (final NotAllowedSqlElementException e) {
            return Optional.of(e.element());
        }
    }

    private static Stream<TreeNode<? extends TreeNode<?>>> treeNodes(final LogicalPlan logicalPlan) {
        return Stream.concat(
                Stream.of(logicalPlan),
                SparkCollections.asJavaStream(logicalPlan.expressions()).flatMap(SparkTrees::asJavaStreamWithChildren)
        );
    }
}