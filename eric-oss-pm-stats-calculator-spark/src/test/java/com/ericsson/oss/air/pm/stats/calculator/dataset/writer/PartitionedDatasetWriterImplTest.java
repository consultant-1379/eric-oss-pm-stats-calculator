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
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.KpiDefinitionServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.reliability.OnDemandReliabilityRegister;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartitionedDatasetWriterImplTest {
    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionServiceImpl kpiDefinitionServiceMock;
    @Mock OnDemandReliabilityRegister onDemandReliabilityRegisterMock;

    @InjectMocks PartitionedDatasetWriterImpl objectUnderTest;

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
    class CacheCalculationReliability {

        @Test
        void shouldCacheForOnDemand(@Mock Dataset<Row> datasetMock) {
            when(sparkServiceMock.isOnDemand()).thenReturn(true);

            objectUnderTest.cacheOnDemandReliability(datasetMock);

            verify(sparkServiceMock).isOnDemand();
            verify(onDemandReliabilityRegisterMock).addReliability(datasetMock);
        }

        @Test
        void shouldNotCacheForScheduled(@Mock Dataset<Row> datasetMock) {
            when(sparkServiceMock.isOnDemand()).thenReturn(false);

            objectUnderTest.cacheOnDemandReliability(datasetMock);

            verify(sparkServiceMock).isOnDemand();
            verifyNoInteractions(onDemandReliabilityRegisterMock);
        }
    }

    @Nested
    class Support {
        @Test
        void shouldSupport() {
            doCallRealMethod().when(kpiDefinitionServiceMock).isDefaultAggregationPeriod(60);

            final boolean actual = objectUnderTest.supports(60);

            verify(kpiDefinitionServiceMock).isDefaultAggregationPeriod(60);

            Assertions.assertThat(actual).isTrue();
        }

        @Test
        void shouldNotSupport() {
            doCallRealMethod().when(kpiDefinitionServiceMock).isDefaultAggregationPeriod(-1);

            final boolean actual = objectUnderTest.supports(-1);

            verify(kpiDefinitionServiceMock).isDefaultAggregationPeriod(-1);

            Assertions.assertThat(actual).isFalse();
        }
    }

    @Nested
    @DisplayName("When write to partition target table")
    class WhenWriteToPartitionTargetTable {

        @Test
        @SuppressWarnings("unchecked")
        void shouldWriteToPartitionTargetTable() {
            final Properties properties = new Properties();
            properties.setProperty("user", "dummy");
            properties.setProperty("driver", "dummy");
            properties.setProperty("password", "dummy");
            properties.setProperty("type", "dummy");
            properties.setProperty("expressionTag", "dummy");

            final Dataset<? extends Row> datasetDeepMock = mock(Dataset.class, RETURNS_DEEP_STUBS);

            when(sparkServiceMock.getKpiJdbcConnection()).thenReturn("jdbcUrl");
            when(sparkServiceMock.getKpiJdbcProperties()).thenReturn(properties);
            when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(Sets.newLinkedHashSet(kpiDefinition("kpi1"), kpiDefinition("kpi2")));

            objectUnderTest.writeToTargetTable(datasetDeepMock, "targetTable", "pk1,pk2,pk3");

            verify(sparkServiceMock).getKpiJdbcConnection();
            verify(sparkServiceMock).getKpiJdbcProperties();
            verify(datasetDeepMock.filter("pk1 is not null and pk2 is not null and pk3 is not null")
                    .write()
                    .format("jdbc-sink")
                    .option("dbtable", "targetTable")
                    .option("url", "jdbcUrl")
                    .option("user", properties.getProperty("user"))
                    .option("password", properties.getProperty("password"))
                    .option("driver", properties.getProperty("driver"))
                    .option("primaryKey", "pk1,pk2,pk3")
                    .option("batchSize", 10_000)
                    .option(OPTION_KPI_NAMES_TO_CALCULATE, "kpi1,kpi2")
            ).save();
        }
    }

    static KpiDefinition kpiDefinition(String name) {
        return KpiDefinition.builder().withName(name).build();
    }
}