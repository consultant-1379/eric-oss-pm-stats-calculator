/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.service.SparkServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlCreator;

import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatasetExecutorTest {
    SqlCreator sqlCreatorMock = mock(SqlCreator.class);
    SparkSession sparkSessionMock = mock(SparkSession.class);
    SparkService sparkServiceMock = mock(SparkServiceImpl.class);
    SqlExpressionHelperImpl sqlExpressionHelperMock = mock(SqlExpressionHelperImpl.class);
    KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);

    int aggregationPeriodInMinutes = 60;

    DatasetExecutor objectUnderTest = objectUnderTest();

    @Test
    void shouldGetMaxUtcTimestamp() {
        when(sqlCreatorMock.createMaxTimestampSql("table")).thenReturn("SQL");

        objectUnderTest.getMaxUtcTimestamp("table");

        verify(sqlCreatorMock).createMaxTimestampSql("table");
        verify(sparkSessionMock).sql("SQL");
    }

    DatasetExecutor objectUnderTest() {
        return new DatasetExecutor(
                sparkServiceMock, sqlCreatorMock, sqlExpressionHelperMock, kpiDefinitionHelperMock, aggregationPeriodInMinutes, sparkSessionMock
        );
    }
}