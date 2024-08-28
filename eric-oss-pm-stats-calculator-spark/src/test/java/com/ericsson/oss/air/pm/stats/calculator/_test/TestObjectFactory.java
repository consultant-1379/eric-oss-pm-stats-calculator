/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator._test;

import static lombok.AccessLevel.PRIVATE;

import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.apache.spark.sql.internal.SessionState;

@NoArgsConstructor(access = PRIVATE)
public final class TestObjectFactory {

    public static SqlProcessorDelegator sqlProcessorDelegator(@NonNull final SparkSession sparkSession) {
        final SessionState sessionState = sparkSession.sessionState();
        final SparkSqlParser sqlParser = ((SparkSqlParser) sessionState.sqlParser());
        return sqlProcessorDelegator(sqlParser);
    }

    public static SqlProcessorDelegator sqlProcessorDelegator(final SparkSqlParser sparkSqlParser) {
        final SqlParserImpl sqlParser = new SqlParserImpl(sparkSqlParser, new LogicalPlanExtractor());
        final ExpressionCollector expressionCollector = new ExpressionCollector(new LeafCollector());
        return new SqlProcessorDelegator(new SqlProcessorService(sqlParser, expressionCollector));
    }

    public static KpiDefinitionHelperImpl kpiDefinitionHelper(final KpiDefinitionHierarchy kpiDefinitionHierarchy) {
        return new KpiDefinitionHelperImpl(kpiDefinitionHierarchy, sqlProcessorDelegator(new SparkSqlParser()), sqlExpressionHelper());
    }

    public static SqlExpressionHelperImpl sqlExpressionHelper() {
        return new SqlExpressionHelperImpl(sqlProcessorDelegator(new SparkSqlParser()));
    }
}
