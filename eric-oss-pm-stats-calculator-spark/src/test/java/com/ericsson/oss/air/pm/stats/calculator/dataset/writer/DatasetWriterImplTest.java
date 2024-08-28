/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.writer;

import static com.ericsson.oss.air.pm.stats.common.spark.sink.JdbcUpsertSink.OPTION_KPI_NAMES_TO_CALCULATE;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.KpiDefinitionServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatasetWriterImplTest {
    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionServiceImpl kpiDefinitionServiceMock;

    @InjectMocks DatasetWriterImpl objectUnderTest;

    @Test
    void shouldRegisterJobDescription() {
        objectUnderTest.registerJobDescription("description");
        verify(sparkServiceMock).registerJobDescription("description");
    }

    @Test
    void shouldUnregisterJobDescription() {
        objectUnderTest.unregisterJobDescription();
        verify(sparkServiceMock).unregisterJobDescription();
    }

    @Nested
    class Support {
        @Test
        void shouldSupport() {
            doCallRealMethod().when(kpiDefinitionServiceMock).isDefaultAggregationPeriod(-1);

            final boolean actual = objectUnderTest.supports(-1);

            verify(kpiDefinitionServiceMock).isDefaultAggregationPeriod(-1);

            Assertions.assertThat(actual).isTrue();
        }

        @Test
        void shouldNotSupport() {
            doCallRealMethod().when(kpiDefinitionServiceMock).isDefaultAggregationPeriod(60);

            final boolean actual = objectUnderTest.supports(60);

            verify(kpiDefinitionServiceMock).isDefaultAggregationPeriod(60);

            Assertions.assertThat(actual).isFalse();
        }
    }

    @Nested
    @DisplayName("When write to target table")
    class WhenWriteToTargetTable {
        static final String DUMMY = "dummy";

        @Mock DataFrameWriter<Row> dataFrameWriterMock;
        @Mock Dataset<Row> datasetMock;

        Properties properties;

        @BeforeEach
        void setUp() {
            properties = new Properties();
            properties.setProperty("user", DUMMY);
            properties.setProperty("driver", DUMMY);
            properties.setProperty("password", DUMMY);
            properties.setProperty("type", DUMMY);
            properties.setProperty("expressionTag", DUMMY);
        }

        @Test
        void shouldWriteToTargetTable() {
            when(sparkServiceMock.getKpiJdbcConnection()).thenReturn("jdbcUrl");
            when(sparkServiceMock.getKpiJdbcProperties()).thenReturn(properties);
            when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(Sets.newLinkedHashSet(kpiDefinition("kpi3"), kpiDefinition("kpi4")));
            when(datasetMock.filter("pk1 is not null and pk2 is not null and pk3 is not null")).thenReturn(datasetMock);
            when(datasetMock.write()).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.format("jdbc-sink")).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("dbtable", "targetTable")).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("url", "jdbcUrl")).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("user", DUMMY)).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("password", DUMMY)).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("driver", DUMMY)).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("primaryKey", "pk1,pk2,pk3")).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option("batchSize", 10_000)).thenReturn(dataFrameWriterMock);
            when(dataFrameWriterMock.option(OPTION_KPI_NAMES_TO_CALCULATE, "kpi3,kpi4")).thenReturn(dataFrameWriterMock);

            objectUnderTest.writeToTargetTable(datasetMock, "targetTable", "pk1,pk2,pk3");

            final InOrder inOrder = inOrder(sparkServiceMock, datasetMock, dataFrameWriterMock);
            inOrder.verify(sparkServiceMock).getKpiJdbcConnection();
            inOrder.verify(sparkServiceMock).getKpiJdbcProperties();
            inOrder.verify(datasetMock).filter("pk1 is not null and pk2 is not null and pk3 is not null");
            inOrder.verify(datasetMock).write();
            inOrder.verify(dataFrameWriterMock).option("dbtable", "targetTable");
            inOrder.verify(dataFrameWriterMock).option("url", "jdbcUrl");
            inOrder.verify(dataFrameWriterMock).option("user", DUMMY);
            inOrder.verify(dataFrameWriterMock).option("password", DUMMY);
            inOrder.verify(dataFrameWriterMock).option("driver", DUMMY);
            inOrder.verify(dataFrameWriterMock).option("primaryKey", "pk1,pk2,pk3");
            inOrder.verify(dataFrameWriterMock).option(OPTION_KPI_NAMES_TO_CALCULATE, "kpi3,kpi4");
            inOrder.verify(dataFrameWriterMock).save();
        }
    }

    private static KpiDefinition kpiDefinition(String name) {
        return KpiDefinition.builder().withName(name).build();
    }
}