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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.NotAllowedSqlElementException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist.SqlValidator;

import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.CaseWhen;
import org.apache.spark.sql.catalyst.expressions.Cast;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.expressions.SortOrder;
import org.apache.spark.sql.catalyst.expressions.aggregate.AggregateFunction;
import org.apache.spark.sql.catalyst.plans.logical.Aggregate;
import org.apache.spark.sql.catalyst.trees.TreeNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;

class SqlValidatorHelperTest {

    @ParameterizedTest
    @ValueSource(classes = {
            CaseWhen.class, AggregateFunction.class, UnresolvedFunction.class, Cast.class, SortOrder.class, Aggregate.class
    })
    void shouldTestThrowingExceptionWhenNotOnWhitelist(final Class<? extends TreeNode<?>> target) {
        final TreeNode<?> treeNodeMock = mock(target, RETURNS_DEEP_STUBS);
        final SqlValidator validator = requireNonNull(validator(target));

        try (final MockedStatic<SqlValidatorHelper> mockedSqlValidatorHelper = mockStatic(SqlValidatorHelper.class, CALLS_REAL_METHODS)) {
            final Verification verification = () -> SqlValidatorHelper.validateAgainstWhitelist(anyString());
            mockedSqlValidatorHelper.when(verification).thenThrow(NotAllowedSqlElementException.class);

            if (treeNodeMock instanceof Expression) {
                when(((Expression) treeNodeMock).prettyName()).thenReturn("test");
            }

            assertThatThrownBy(() -> validator.validate(treeNodeMock)).isInstanceOf(NotAllowedSqlElementException.class);

            mockedSqlValidatorHelper.verify(verification);
        }
    }

    private static SqlValidator validator(final Class<?> target) {
        final Map<Class<? extends TreeNode<?>>, SqlValidator> validators = Map.of(
                CaseWhen.class, SqlValidatorHelper::validateCaseWhen,
                AggregateFunction.class, SqlValidatorHelper::validateAggregateFunction,
                UnresolvedFunction.class, SqlValidatorHelper::validateUnresolvedFunction,
                Cast.class, SqlValidatorHelper::validateCast,
                SortOrder.class, SqlValidatorHelper::validateSortOrder,
                Aggregate.class, SqlValidatorHelper::validateAggregate
        );
        return validators.get(target);
    }

}



