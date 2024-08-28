/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist.helper;

import java.util.Locale;

import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlWhitelist;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.NotAllowedSqlElementException;

import lombok.NoArgsConstructor;
import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.CaseWhen;
import org.apache.spark.sql.catalyst.expressions.Cast;
import org.apache.spark.sql.catalyst.expressions.SortOrder;
import org.apache.spark.sql.catalyst.expressions.aggregate.AggregateFunction;
import org.apache.spark.sql.catalyst.plans.logical.Aggregate;
import org.apache.spark.sql.catalyst.trees.TreeNode;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class SqlValidatorHelper {

    public static void validateAgainstWhitelist(final @NotNull String sqlElement) {
        if (!SqlWhitelist.ELEMENTS.contains(sqlElement.toLowerCase(Locale.ENGLISH))) {
            throw new NotAllowedSqlElementException(sqlElement);
        }
    }

    public static void validateAggregateFunction(final TreeNode<?> aggregateFunction) {
        if (aggregateFunction instanceof AggregateFunction) {
            validateAgainstWhitelist(((AggregateFunction) aggregateFunction).prettyName());
        }
    }

    public static void validateUnresolvedFunction(final TreeNode<?> unresolvedFunction) {
        if (unresolvedFunction instanceof UnresolvedFunction) {
            validateAgainstWhitelist(((UnresolvedFunction) unresolvedFunction).prettyName().toLowerCase(Locale.ENGLISH));
        }
    }

    public static void validateCast(final TreeNode<?> cast) {
        if (cast instanceof Cast) {
            validateAgainstWhitelist(((Cast) cast).prettyName().toLowerCase(Locale.ENGLISH));
        }
    }

    public static void validateSortOrder(final TreeNode<?> sortOrder) {
        // todo look into the treenode to see if i can get the group by or sort by out and use that in the error
        if (sortOrder instanceof SortOrder) {
            validateAgainstWhitelist(((SortOrder) sortOrder).prettyName().toLowerCase(Locale.ENGLISH));
        }
    }

    public static void validateAggregate(final TreeNode<?> grouping) {
        if (grouping instanceof Aggregate) {
            validateAgainstWhitelist("group by");
        }
    }

    public static void validateCaseWhen(final TreeNode<?> caseWhen) {
        if (caseWhen instanceof CaseWhen) {
            validateAgainstWhitelist("case when");
        }
    }
}