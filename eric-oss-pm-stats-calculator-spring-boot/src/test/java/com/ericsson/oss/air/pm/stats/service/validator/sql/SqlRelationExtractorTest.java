/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.exception.InvalidSqlSyntaxException;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlRelationExtractorTest {
    final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());

    final SqlRelationExtractor objectUnderTest = new SqlRelationExtractor(sqlParser);

    @Test
    void whenExtractColumns_shouldEnrichInvalidSqlSyntaxException() {
        final KpiDefinition kpiDefinitionMock = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);

        when(kpiDefinitionMock.expression()).thenReturn(OnDemandDefinitionExpression.of(
                "FIRST(TRANSFORM(kpi_simple_60.integer_array_simple , x -> x * kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60"
        ));
        when(kpiDefinitionMock.name().value()).thenReturn("test-kpi-name");

        Assertions.assertThatThrownBy(() -> objectUnderTest.extractColumns(kpiDefinitionMock))
                .isExactlyInstanceOf(InvalidSqlSyntaxException.class)
                .hasMessage(
                        "Syntax error in the expression of the following definition: test-kpi-name. \n" +
                                "Syntax error at or near 'FROM'(line 1, pos 99)\n" +
                                "\n" +
                                "== SQL ==" +
                                "\n" +
                                "SELECT FIRST(TRANSFORM(kpi_simple_60.integer_array_simple , x -> x * kpi_simple_60.integer_simple) FROM kpi_db.kpi_simple_60" +
                                "\n" +
                                "---------------------------------------------------------------------------------------------------^^^" +
                                "\n"
                );

        verify(kpiDefinitionMock).expression();
        verify(kpiDefinitionMock.name()).value();
    }

    @Test
    void whenExtractRelations_shouldEnrichInvalidSqlSyntaxException() {
        final KpiDefinitionEntity kpiDefinitionMock = mock(KpiDefinitionEntity.class, RETURNS_DEEP_STUBS);

        when(kpiDefinitionMock.expression()).thenReturn(
                "FIRST(TRANSFORM(kpi_simple_60.integer_array_simple , x -> x * kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60");
        when(kpiDefinitionMock.name()).thenReturn("test-kpi-name");

        Assertions.assertThatThrownBy(() -> objectUnderTest.extractRelations(kpiDefinitionMock))
                .isExactlyInstanceOf(InvalidSqlSyntaxException.class)
                .hasMessage(
                        "Syntax error in the expression of the following definition: test-kpi-name. \n" +
                                "Syntax error at or near 'FROM'(line 1, pos 99)\n" +
                                "\n" +
                                "== SQL ==" +
                                "\n" +
                                "SELECT FIRST(TRANSFORM(kpi_simple_60.integer_array_simple , x -> x * kpi_simple_60.integer_simple) FROM kpi_db.kpi_simple_60" +
                                "\n" +
                                "---------------------------------------------------------------------------------------------------^^^" +
                                "\n"
                );

        verify(kpiDefinitionMock).expression();
        verify(kpiDefinitionMock).name();
    }
}