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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.VirtualDatabaseResolver;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.VirtualDatabaseService;

import kpi.model.KpiDefinitionRequest;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.junit.jupiter.api.Test;

class SqlReferenceValidatorTest {
    final DatabaseService databaseServiceMock = mock(DatabaseService.class);
    final ParameterRepository parameterRepositoryMock = mock(ParameterRepository.class);

    final SqlReferenceValidator objectUnderTest = objectUnderTest(databaseServiceMock, parameterRepositoryMock);

    @Test
    void shouldValidateRequiredKpis() {
        when(databaseServiceMock.findAllOutputTablesWithoutPartition()).thenReturn(List.of("kpi_rolling_aggregation_1440"));
        when(databaseServiceMock.findColumnNamesForTable("kpi_rolling_aggregation_1440")).thenReturn(List.of(
                "agg_column_0", "aggregation_begin_time", "aggregation_end_time", "rolling_sum_integer_1440", "rolling_max_integer_1440"
        ));

        final KpiDefinitionRequest kpiDefinitionRequest = loadDefinition();
        objectUnderTest.validateReferences(kpiDefinitionRequest);

        verify(databaseServiceMock).findAllOutputTablesWithoutPartition();
        verify(databaseServiceMock).findColumnNamesForTable("kpi_rolling_aggregation_1440");
    }

    static SqlReferenceValidator objectUnderTest(
            final DatabaseService databaseServiceMock,
            final ParameterRepository parameterRepositoryMock
    ) {
        final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());
        final SqlProcessorService sqlProcessorService = new SqlProcessorService(sqlParser, new ExpressionCollector(new LeafCollector()));

        final VirtualDatabaseResolver virtualDatabaseResolver = new VirtualDatabaseResolver(
                new SqlExtractorService(sqlProcessorService),
                new SqlRelationExtractor(sqlParser)
        );

        return new SqlReferenceValidator(new VirtualDatabaseService(
                new SqlRelationExtractor(sqlParser),
                parameterRepositoryMock,
                sqlProcessorService,
                databaseServiceMock
        ), virtualDatabaseResolver);
    }

    static KpiDefinitionRequest loadDefinition() {
        final String jsonContent = JsonLoaders.load("NewRequiredKpis.json");
        return Serialization.deserialize(jsonContent, KpiDefinitionRequest.class);
    }
}