/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.calculation.util;

import static com.ericsson.oss.air.pm.stats.service.validator.sql.calculation.util.SqlUtils.simplifyColumnExpression;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlUtilsTest {

    @Test
    void whenSimplifyingColumnExpressionAndPreferringAlias_shouldReturnAliasWhenPresent() {
        final List<String> actual = simplifyColumnExpression(
                List.of(
                        "column1",
                        "column2 as columnSneaky",
                        "table.column3",
                        "name4",
                        "FDN_PARSE(NbIotCell.nodeFDN, 'Equipment') AS equipment",
                        "SUBSTRING(agg_column0, 1, 5)"),
                true,
                true);

        final List<String> expected = List.of(
                "column1",
                "columnSneaky",
                "column3",
                "name4",
                "equipment",
                "SUBSTRING(agg_column0, 1, 5)"
        );

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void whenSimplifyingColumnExpressionAndNotPreferringAlias_shouldReturnNamePart() {
        final List<String> actual = simplifyColumnExpression(
                List.of(
                        "column1",
                        "column2 as columnSneaky",
                        "table.column3",
                        "name4",
                        "FDN_PARSE(NbIotCell.nodeFDN, 'Equipment') AS equipment",
                        "SUBSTRING(agg_column0, 1, 5)"),
                false,
                false);

        final List<String> expected = List.of(
                "column1",
                "column2",
                "table.column3",
                "name4",
                "FDN_PARSE(NbIotCell.nodeFDN, 'Equipment')",
                "SUBSTRING(agg_column0, 1, 5)"
        );

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

}
