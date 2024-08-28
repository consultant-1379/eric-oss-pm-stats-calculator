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

import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;

import lombok.NonNull;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.apache.spark.sql.internal.SessionState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqlConfiguration {

    @Bean
    public LogicalPlanExtractor logicalPlanExtractor() {
        return new LogicalPlanExtractor();
    }

    @Bean
    public LeafCollector leafCollector() {
        return new LeafCollector();
    }

    @Bean
    public ExpressionCollector expressionCollector(final LeafCollector leafCollector) {
        return new ExpressionCollector(leafCollector);
    }

    @Bean
    public SparkSqlParser sparkSqlParser(@NonNull final SparkSession sparkSession) {
        final SessionState sessionState = sparkSession.sessionState();
        return (SparkSqlParser) sessionState.sqlParser();
    }

    @Bean
    public SqlParserImpl sqlParser(@NonNull final SparkSqlParser sparkSqlParser, final LogicalPlanExtractor logicalPlanExtractor) {
        return new SqlParserImpl(sparkSqlParser, logicalPlanExtractor);
    }

    @Bean
    public SqlProcessorService sqlProcessorService(final SqlParserImpl sqlParser, final ExpressionCollector expressionCollector) {
        return new SqlProcessorService(sqlParser, expressionCollector);
    }
}
