/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SqlConfigurationTest {
    final SqlConfiguration objectUnderTest = new SqlConfiguration();

    @Test
    void shouldVerifyLogicalPlanExecutor() {
        final LogicalPlanExtractor actual = objectUnderTest.logicalPlanExtractor();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void shouldVerifyLeafCollector() {
        final LeafCollector actual = objectUnderTest.leafCollector();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void shouldVerifyExpressionCollector() {
        final LeafCollector leafCollectorMock = Mockito.mock(LeafCollector.class);

        final ExpressionCollector actual = objectUnderTest.expressionCollector(leafCollectorMock);

        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void shouldVerifySparkSqlParser() {
        final SparkSession sparkSessionMock = Mockito.mock(SparkSession.class, RETURNS_DEEP_STUBS);
        final SparkSqlParser sqlParserMock = Mockito.mock(SparkSqlParser.class);

        when(sparkSessionMock.sessionState().sqlParser()).thenReturn(sqlParserMock);

        final SparkSqlParser actual = objectUnderTest.sparkSqlParser(sparkSessionMock);

        verify(sparkSessionMock.sessionState()).sqlParser();

        Assertions.assertThat(actual).isEqualTo(sqlParserMock);
    }

    @Test
    void shouldVerifySqlParser() {
        final SparkSqlParser sparkSqlParserMock = Mockito.mock(SparkSqlParser.class);
        final LogicalPlanExtractor logicalPlanExtractorMock = Mockito.mock(LogicalPlanExtractor.class);

        final SqlParserImpl actual = objectUnderTest.sqlParser(sparkSqlParserMock, logicalPlanExtractorMock);

        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void shouldVerifySqlProcessorService() {
        final SqlParserImpl sqlParserMock = Mockito.mock(SqlParserImpl.class);
        final ExpressionCollector expressionCollectorMock = Mockito.mock(ExpressionCollector.class);

        final SqlProcessorService actual = objectUnderTest.sqlProcessorService(sqlParserMock, expressionCollectorMock);

        Assertions.assertThat(actual).isNotNull();
    }
}