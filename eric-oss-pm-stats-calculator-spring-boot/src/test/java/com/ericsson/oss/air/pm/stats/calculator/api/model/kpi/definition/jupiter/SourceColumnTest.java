/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.jupiter;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SourceColumnTest {
    @MethodSource("provideIsNonInMemoryDatasourceData")
    @ParameterizedTest(name = "[{index}] SQL expression: ''{0}'' is non in memory datasource: ''{1}''")
    void shouldVerifyIsNonInMemoryDatasource(final String sqlExpression, final boolean expected) {
        final SourceColumn sourceColumn = new SourceColumn(sqlExpression);

        Assertions.assertThat(sourceColumn.isNonInMemoryDataSource()).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsNonInMemoryDatasourceData() {
        return Stream.of(
                Arguments.of("pm_stats://table.column", true),
                Arguments.of("table.column", true),
                Arguments.of("kpi_inmemory://table.column", false)
        );
    }
}