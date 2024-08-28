/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist.helper.SqlValidatorHelper;

import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.CaseWhen;
import org.apache.spark.sql.catalyst.expressions.Cast;
import org.apache.spark.sql.catalyst.expressions.SortOrder;
import org.apache.spark.sql.catalyst.expressions.aggregate.AggregateFunction;
import org.apache.spark.sql.catalyst.plans.logical.Aggregate;
import org.apache.spark.sql.catalyst.trees.TreeNode;

// TODO entry level composite/visitor pattern, need to extend to support runtime addition of new validators
// TODO Composite pattern would call validation for each element, here we just call the one we need
// TODO hence composite/visitor hybrid
@ApplicationScoped
public class SqlValidatorImpl implements SqlValidator {

    private final Map<Class<?>, SqlValidator> validators = Map.of(
            AggregateFunction.class, SqlValidatorHelper::validateAggregateFunction,
            UnresolvedFunction.class, SqlValidatorHelper::validateUnresolvedFunction,
            Cast.class, SqlValidatorHelper::validateCast,
            SortOrder.class, SqlValidatorHelper::validateSortOrder,
            CaseWhen.class, SqlValidatorHelper::validateCaseWhen,
            Aggregate.class, SqlValidatorHelper::validateAggregate);

    public void validate(final TreeNode<?> treeNode) {
        if (validators.containsKey(treeNode.getClass())) {
            validators.get(treeNode.getClass()).validate(treeNode);
        }
    }
}
