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

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.kpiDefinitionHelper;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.api.PostgresDataLoader;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderImplTest {
    @Mock TableColumnsDefinerImpl tableColumnsDefinerMock;
    @Mock TempViewCreatorImpl tempViewCreatorMock;
    @Mock PostgresDataLoader postgresPartitionedDataLoaderMock;
    @Mock PostgresDataLoader postgresUnPartitionedDataLoaderMock;
    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionHierarchy kpiDefinitionHierarchyMock;

    PostgresDataLoaderImpl objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new PostgresDataLoaderImpl(
                //  TODO: Refactor this to use mock and refactor tests as well to work with mocking alias extraction
                kpiDefinitionHelper(kpiDefinitionHierarchyMock),
                tableColumnsDefinerMock,
                tempViewCreatorMock,
                sparkServiceMock,
                postgresPartitionedDataLoaderMock,
                postgresUnPartitionedDataLoaderMock
        );
    }

    @Nested
    @DisplayName("Test load un partitioned data")
    class LoadUnPartitionedData {
        @Mock Dataset<Row> datasetMock;
        @Mock JdbcDatasource jdbcDatasourceMock;

        @Captor ArgumentCaptor<Supplier<Dataset<Row>>> datasetSupplierCaptor;

        final Table table = Table.of("table");
        final Set<Column> columns = singleton(Column.of("column"));
        final SingleTableColumns singleTableColumns = SingleTableColumns.of(table, columns);
        final Set<KpiDefinition> kpiDefinitions = singleton(KpiDefinition.builder().withAlias("alias").build());

        @Test
        void shouldLoadUnPartitionedData() {
            when(tableColumnsDefinerMock.defineTableColumns(kpiDefinitions, 60, "alias")).thenReturn(singleTableColumns);
            when(sparkServiceMock.getKpiJdbcDatasource()).thenReturn(jdbcDatasourceMock);
            when(tempViewCreatorMock.createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture())).thenReturn(datasetMock);

            final ReadingOptions readingOptions = ReadingOptions.empty();
            final String jobDescription = "GET_PRIMARY_KEYS : table : 60";

            final TableDatasets actual = objectUnderTest.loadUnPartitionedDataset(kpiDefinitions, 60, readingOptions);

            verify(tableColumnsDefinerMock).defineTableColumns(kpiDefinitions, 60, "alias");
            verify(tempViewCreatorMock).createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture());
            verify(sparkServiceMock).getKpiJdbcDatasource();

            when(postgresUnPartitionedDataLoaderMock.loadDataWithJobDescription(jobDescription, singleTableColumns, jdbcDatasourceMock, readingOptions)).thenReturn(datasetMock);

            datasetSupplierCaptor.getValue().get();

            verify(postgresUnPartitionedDataLoaderMock).loadDataWithJobDescription(jobDescription, singleTableColumns, jdbcDatasourceMock, readingOptions);

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                    "alias", datasetMock
            )));
        }

        @Test
        void shouldLoadUnPartitionedDataset() {
            when(tempViewCreatorMock.createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture())).thenReturn(datasetMock);

            final ReadingOptions readingOptions = ReadingOptions.empty();
            final String jobDescription = "GET_PRIMARY_KEYS : table : 60";

            objectUnderTest.loadUnPartitionedDataset(jdbcDatasourceMock, singleTableColumns, 60, readingOptions);

            verify(tempViewCreatorMock).createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture());

            when(postgresUnPartitionedDataLoaderMock.loadDataWithJobDescription(jobDescription, singleTableColumns, jdbcDatasourceMock, readingOptions)).thenReturn(datasetMock);

            datasetSupplierCaptor.getValue().get();

            verify(postgresUnPartitionedDataLoaderMock).loadDataWithJobDescription(jobDescription, singleTableColumns, jdbcDatasourceMock, readingOptions);
        }
    }

    @Nested
    @DisplayName("Test load partitioned data")
    class LoadPartitionedData {
        @Mock Dataset<Row> datasetMock;
        @Mock JdbcDatasource jdbcDatasourceMock;

        @Captor ArgumentCaptor<Supplier<Dataset<Row>>> datasetSupplierCaptor;

        final Table table = Table.of("table");
        final Set<Column> columns = singleton(Column.of("column"));
        final SingleTableColumns singleTableColumns = SingleTableColumns.of(table, columns);
        final Set<KpiDefinition> kpiDefinitions = singleton(KpiDefinition.builder().withAlias("alias").build());

        @Test
        void shouldLoadPartitionedData() {
            when(tableColumnsDefinerMock.defineTableColumns(kpiDefinitions, 60, "alias")).thenReturn(singleTableColumns);
            when(sparkServiceMock.getKpiJdbcDatasource()).thenReturn(jdbcDatasourceMock);
            when(tempViewCreatorMock.createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture())).thenReturn(datasetMock);

            final ReadingOptions readingOptions = ReadingOptions.empty();
            final TableDatasets actual = objectUnderTest.loadPartitionedDataset(kpiDefinitions, 60, readingOptions);

            verify(tableColumnsDefinerMock).defineTableColumns(kpiDefinitions, 60, "alias");
            verify(sparkServiceMock).getKpiJdbcDatasource();
            verify(tempViewCreatorMock).createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture());

            when(postgresPartitionedDataLoaderMock.loadDataWithJobDescription("GET_PARTITION_LIMITS : table : 60", singleTableColumns, jdbcDatasourceMock,
                                                                              readingOptions
            )).thenReturn(datasetMock);

            datasetSupplierCaptor.getValue().get();

            verify(postgresPartitionedDataLoaderMock).loadDataWithJobDescription("GET_PARTITION_LIMITS : table : 60", singleTableColumns, jdbcDatasourceMock,
                                                                                 readingOptions
            );

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                "alias", datasetMock
            )));
        }

        @Test
        void shouldLoadUnPartitionedData_whenExceptionHappens() {
            when(tableColumnsDefinerMock.defineTableColumns(kpiDefinitions, 60, "alias")).thenReturn(singleTableColumns);
            when(sparkServiceMock.getKpiJdbcDatasource()).thenReturn(jdbcDatasourceMock);
            when(tempViewCreatorMock.createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture()))
                    .thenThrow(IllegalArgumentException.class)
                    .thenReturn(datasetMock);

            final ReadingOptions readingOptions = ReadingOptions.empty();
            final String jobDescription = "GET_PRIMARY_KEYS : table : 60";

            final TableDatasets actual = objectUnderTest.loadPartitionedDataset(kpiDefinitions, 60, readingOptions);

            verify(tableColumnsDefinerMock).defineTableColumns(kpiDefinitions, 60, "alias");
            verify(sparkServiceMock).getKpiJdbcDatasource();
            verify(tempViewCreatorMock, times(2)).createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture());

            when(postgresUnPartitionedDataLoaderMock.loadDataWithJobDescription(jobDescription, singleTableColumns, jdbcDatasourceMock, readingOptions)).thenReturn(datasetMock);

            datasetSupplierCaptor.getValue().get();

            verify(postgresUnPartitionedDataLoaderMock).loadDataWithJobDescription(jobDescription, singleTableColumns, jdbcDatasourceMock, readingOptions);

            Assertions.assertThat(actual).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                "alias", datasetMock
            )));
        }

        @Test
        void shouldLoadPartitionedDataset() {
            when(tempViewCreatorMock.createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture())).thenReturn(datasetMock);

            final ReadingOptions readingOptions = ReadingOptions.empty();
            objectUnderTest.loadPartitionedDataset(jdbcDatasourceMock, singleTableColumns, 60, readingOptions);

            verify(tempViewCreatorMock).createOrReplaceTempView(eq(table), datasetSupplierCaptor.capture());

            when(postgresPartitionedDataLoaderMock.loadDataWithJobDescription("GET_PARTITION_LIMITS : table : 60", singleTableColumns, jdbcDatasourceMock,
                                                                              readingOptions
            )).thenReturn(datasetMock);

            datasetSupplierCaptor.getValue().get();

            verify(postgresPartitionedDataLoaderMock).loadDataWithJobDescription("GET_PARTITION_LIMITS : table : 60", singleTableColumns, jdbcDatasourceMock,
                                                                                 readingOptions
            );
        }
    }
}