/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

class KpiCalculatorCustomFilterTest {
    SparkService sparkServiceMock = mock(SparkService.class);
    SparkSession sparkSessionMock = mock(SparkSession.class);
    SqlProcessorDelegator sqlProcessorDelegator = mock(SqlProcessorDelegator.class);
    KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);

    KpiCalculatorCustomFilter objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = spy(new KpiCalculatorCustomFilter(sparkServiceMock, sqlProcessorDelegator, 60, emptyList(), sparkSessionMock));
        objectUnderTest.setKpiDefinitionHelper(kpiDefinitionHelperMock);
    }

    @Test
    void shouldInitialize() {
        Assertions.assertThat(objectUnderTest).isNotNull();
    }


    @Nested
    @ExtendWith(MockitoExtension.class)
    class Calculate {
        @Mock Dataset<Row> datasetMock;

        @Test
        void shouldNotCalculate() {
            //  Very dummy test just to make SonarQube happy

            when(kpiDefinitionHelperMock.groupKpisByStage(emptyList())).thenReturn(emptyMap());
            when(kpiDefinitionHelperMock.extractAliases(emptyList())).thenReturn(emptySet());

            doReturn(TableDatasets.of()).when(objectUnderTest).calculateKpis(anySet(), anyMap(), anyMap(), any(TableDatasets.class));
            objectUnderTest.calculate();

            verify(kpiDefinitionHelperMock).groupKpisByStage(emptyList());
            verify(kpiDefinitionHelperMock).extractAliases(emptyList());
            verify(objectUnderTest).calculateKpis(anySet(), anyMap(), anyMap(), any(TableDatasets.class));
        }

        @Test
        void shouldCalculate() {
            //  Very dummy test just to make SonarQube happy

            when(kpiDefinitionHelperMock.groupKpisByStage(emptyList())).thenReturn(emptyMap());
            when(kpiDefinitionHelperMock.extractAliases(emptyList())).thenReturn(emptySet());

            final TableDatasets tableDatasets = TableDatasetsUtil.from(singletonMap("table", datasetMock));
            doReturn(tableDatasets).when(objectUnderTest).calculateKpis(anySet(), anyMap(), anyMap(), any(TableDatasets.class));
            doReturn(tableDatasets).when(objectUnderTest).writeResults(anySet(), anyMap(), any(TableDatasets.class));

            objectUnderTest.calculate();

            verify(kpiDefinitionHelperMock).groupKpisByStage(emptyList());
            verify(kpiDefinitionHelperMock).extractAliases(emptyList());
            verify(objectUnderTest).calculateKpis(anySet(), anyMap(), anyMap(), any(TableDatasets.class));
            verify(objectUnderTest).writeResults(anySet(), anyMap(), any(TableDatasets.class));
        }
    }
}