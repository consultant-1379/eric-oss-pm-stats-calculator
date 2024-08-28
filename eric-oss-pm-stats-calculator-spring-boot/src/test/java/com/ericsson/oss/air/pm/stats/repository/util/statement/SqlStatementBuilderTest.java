/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.statement;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlStatementBuilderTest {
    @Test
    void shouldCreateSqlStatement() {
        final SqlStatement sqlStatement = SqlStatementBuilder.template("${before} very long test... ${template}")
                .replace("before", "after")
                .replace("template", "nothing to say")
                .create();

        Assertions.assertThat(sqlStatement).isEqualTo(SqlStatement.of("after very long test... nothing to say"));
    }
}