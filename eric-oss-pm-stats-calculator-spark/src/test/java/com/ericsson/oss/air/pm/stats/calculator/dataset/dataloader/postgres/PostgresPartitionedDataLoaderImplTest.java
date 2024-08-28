/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.DatasetUtils;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresPartitionedDataLoaderImplTest {
    @Mock SparkService sparkServiceMock;
    @Mock CalculationProperties calculationPropertiesMock;
    @Mock SparkSession sparkSessionMock;

    @InjectMocks PostgresPartitionedDataLoaderImpl objectUnderTest;

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
    @DisplayName("Testing data loading")
    class LoadData {
        @Mock Dataset<Row> datasetMock;

        @Test
        void shouldLoadData() {
            try (final MockedStatic<DatasetUtils> datasetUtilsMockedStatic = mockStatic(DatasetUtils.class)) {
                final Properties properties = new Properties();

                final Verification verification = () -> DatasetUtils.loadDatasetWithSpecificColumnsAndPrimaryKeys(
                        sparkSessionMock,
                        "kpi_sector",
                        Collections.singleton("agg_column_0"),
                        "url",
                        properties,
                        4,
                        "indexedNumericPartitionColumns",
                        ReadingOptions.empty()
                );

                when(calculationPropertiesMock.getSparkParallelism()).thenReturn(4);
                when(calculationPropertiesMock.getIndexedNumericPartitionColumns()).thenReturn("indexedNumericPartitionColumns");
                datasetUtilsMockedStatic.when(verification).thenReturn(datasetMock);

                final ReadingOptions readingOptions = ReadingOptions.empty();
                final Dataset<Row> actual = objectUnderTest.loadData(
                        SingleTableColumns.of(Table.of("kpi_sector"), Collections.singleton(Column.of("agg_column_0"))),
                        JdbcDatasource.of("url", properties), readingOptions
                );

                verify(calculationPropertiesMock).getSparkParallelism();
                verify(calculationPropertiesMock).getIndexedNumericPartitionColumns();
                datasetUtilsMockedStatic.verify(verification);

                Assertions.assertThat(actual).isEqualTo(datasetMock);
            }
        }
    }
}