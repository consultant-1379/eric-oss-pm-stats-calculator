/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.sqlProcessorDelegator;
import static org.apache.spark.sql.functions.col;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static scala.collection.JavaConverters.asJavaCollection;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.KpiDefinitionBuilder;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.spark.SparkTimestampAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.apache.spark.sql.functions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.collection.Seq;
import za.co.absa.abris.config.FromAvroConfig;

@ExtendWith(MockitoExtension.class)
class SparkKafkaHelperTest {
    SparkService sparkServiceMock = mock(SparkService.class);
    KpiDefinitionHelperImpl kpiDefinitionHelperMock = mock(KpiDefinitionHelperImpl.class);
    SparkTimestampAdjuster sparkTimestampAdjusterMock = mock(SparkTimestampAdjuster.class);
    ReadinessLogRepository readinessLogRepositoryMock = mock(ReadinessLogRepository.class);
    CalculationProperties calculationPropertiesMock = mock(CalculationProperties.class);

    SparkKafkaHelper objectUnderTest = new SparkKafkaHelper(
            sparkServiceMock, sparkTimestampAdjusterMock, kpiDefinitionHelperMock, readinessLogRepositoryMock, sqlProcessorDelegator(new SparkSqlParser()), calculationPropertiesMock
    );

    @Nested
    class ProcessData {
        @Mock Dataset<Row> datasetMock;
        @Mock FromAvroConfig avroConfigMock;

        @Test
        void shouldProcessData() {
            try (final MockedStatic<functions> sparkFunctions = mockStatic(functions.class);
                 final MockedStatic<za.co.absa.abris.avro.functions> abrisFunctions = mockStatic(za.co.absa.abris.avro.functions.class)) {
                final Column valueColumnMock = mock(Column.class);
                final Column dataSplitColumnMock = mock(Column.class);
                final Column fromAvroColumnMock = mock(Column.class);
                final Column asDataColumnMock = mock(Column.class);

                sparkFunctions.when(() -> col("value")).thenReturn(valueColumnMock);
                abrisFunctions.when(() -> za.co.absa.abris.avro.functions.from_avro(valueColumnMock, avroConfigMock)).thenReturn(fromAvroColumnMock);
                when(fromAvroColumnMock.as("data")).thenReturn(asDataColumnMock);
                when(datasetMock.select(asDataColumnMock)).thenReturn(datasetMock);
                sparkFunctions.when(() -> col("data.*")).thenReturn(dataSplitColumnMock);
                when(datasetMock.select(dataSplitColumnMock)).thenReturn(datasetMock);

                objectUnderTest.processData(datasetMock, avroConfigMock);

                sparkFunctions.verify(() -> col("value"));
                abrisFunctions.verify(() -> za.co.absa.abris.avro.functions.from_avro(valueColumnMock, avroConfigMock));
                verify(fromAvroColumnMock).as("data");
                verify(datasetMock).select(asDataColumnMock);
                sparkFunctions.verify(() -> col("data.*"));
                verify(datasetMock).select(dataSplitColumnMock);
            }

        }
    }

    @Nested
    class ConvertRopToTimestamp {

        @Mock Dataset<Row> inputDatasetMock;
        @Mock Dataset<Row> adjustedDatasetMock;
        @Mock Dataset<Row> adjustedAgainDatasetMock;
        @Mock Column columnRopBeginTimeMock;
        @Mock Column columnRopEndTimeMock;

        @Test
        void shouldConvertIntoNewColumns() {
            when(sparkTimestampAdjusterMock.adjustStringToTimestampColumn("ropBeginTime")).thenReturn(columnRopBeginTimeMock);
            when(sparkTimestampAdjusterMock.adjustStringToTimestampColumn("ropEndTime")).thenReturn(columnRopEndTimeMock);
            when(inputDatasetMock.withColumn("ropBeginTime", columnRopBeginTimeMock)).thenReturn(adjustedDatasetMock);
            when(adjustedDatasetMock.withColumn("ropEndTime", columnRopEndTimeMock)).thenReturn(adjustedAgainDatasetMock);
            when(adjustedAgainDatasetMock.withColumnRenamed("ropBeginTime","aggregation_begin_time")).thenReturn(adjustedAgainDatasetMock);

            final Dataset<Row> actual = objectUnderTest.convertRopToTimestampColumns(inputDatasetMock);

            verify(sparkTimestampAdjusterMock).adjustStringToTimestampColumn("ropBeginTime");
            verify(sparkTimestampAdjusterMock).adjustStringToTimestampColumn("ropEndTime");
            verify(inputDatasetMock).withColumn("ropBeginTime", columnRopBeginTimeMock);
            verify(adjustedDatasetMock).withColumn("ropEndTime", columnRopEndTimeMock);

            assertThat(actual).isEqualTo(adjustedAgainDatasetMock);
        }
    }

    @Nested
    class ComputeProjectionColumns {

        @Test
        void shouldComputeProjectionColumns() {
            final DataIdentifier inpDataIdentifier1 = DataIdentifier.of("dataspace", "category", "schema-1");
            final DataIdentifier inpDataIdentifier2 = DataIdentifier.of("dataspace", "category", "schema-2");

            final Seq<Column> actual = objectUnderTest.computeProjectionColumns(
                    inpDataIdentifier1, List.of(
                            kpiDefinition(
                                    "SUM(fact_table.pmCounters.integerColumn0)",
                                    inpDataIdentifier1, List.of("fact_table_0.agg_column_0")
                            ),
                            kpiDefinition(
                                    "SUM(fact_table.pmCounters.integerArrayColumn0[1] + fact_table.pmCounters.integerArrayColumn0[3])",
                                    inpDataIdentifier1, List.of("fact_table_0.agg_column_1 AS agg_column_2")
                            ),
                            kpiDefinition(
                                    "MAX(fact_table.pmCounters.integerColumn0)",
                                    inpDataIdentifier1, List.of("fact_table_0.agg_column_1 AS agg_column_2")
                            ),
                            kpiDefinition(
                                    "FDN_PARSE(fact_table.nodeFDN)",
                                    inpDataIdentifier1, List.of("fact_table_0.nodeFDN")
                            ),
                            kpiDefinition(
                                    "SUM(z_new_fact_table_1.pmCounters.floatColumn0.counterValue)",
                                    inpDataIdentifier2, List.of("fact_table_0.agg_column_3")
                            )
                    )
            );

            assertThat(asJavaCollection(actual)).map(Column::toString).containsExactlyInAnyOrder(
                    //  from expression
                    "pmCounters.integerColumn0 AS pmCounters_integerColumn0",
                    "pmCounters.integerArrayColumn0 AS pmCounters_integerArrayColumn0",

                    //  from aggregation_elements
                    "agg_column_0",
                    "agg_column_1 AS agg_column_2",
                    "aggregation_begin_time",

                    // from both
                    "nodeFDN"
            );

        }

        KpiDefinition kpiDefinition(final String expression, final DataIdentifier inpDataIdentifier, final List<String> aggregationElements) {
            final KpiDefinitionBuilder builder = KpiDefinition.builder();
            builder.withExpression(expression);
            builder.withInpDataIdentifier(inpDataIdentifier);
            builder.withAggregationElements(aggregationElements);
            return builder.build();
        }

    }

    @Nested
    class LoadFrom {
        @Mock DataFrameReader dataFrameReaderMock;
        @Mock Dataset<Row> datasetMock;

        @Test
        void shouldLoadFrom() {
            final SchemaDetail schemaDetail = SchemaDetail.builder().withTopic("topic").build();

            when(sparkServiceMock.fromKafka()).thenReturn(dataFrameReaderMock);
            when(calculationPropertiesMock.getKafkaBootstrapServers()).thenReturn("server");
            when(dataFrameReaderMock.option("kafka.bootstrap.servers", "server")).thenReturn(dataFrameReaderMock);
            when(dataFrameReaderMock.option("subscribe", "topic")).thenReturn(dataFrameReaderMock);
            when(dataFrameReaderMock.option("includeHeaders", true)).thenReturn(dataFrameReaderMock);
            when(dataFrameReaderMock.option("startingOffsets", "startingOffset")).thenReturn(dataFrameReaderMock);
            when(dataFrameReaderMock.option("endingOffsets", "endingOffset")).thenReturn(dataFrameReaderMock);
            when(dataFrameReaderMock.load()).thenReturn(datasetMock);

            final Dataset<Row> actual = objectUnderTest.loadDataFrom(schemaDetail, "startingOffset", "endingOffset");

            verify(sparkServiceMock).fromKafka();
            verify(calculationPropertiesMock).getKafkaBootstrapServers();
            verify(dataFrameReaderMock).option("kafka.bootstrap.servers", "server");
            verify(dataFrameReaderMock).option("subscribe", "topic");
            verify(dataFrameReaderMock).option("includeHeaders", true);
            verify(dataFrameReaderMock).option("startingOffsets", "startingOffset");
            verify(dataFrameReaderMock).option("endingOffsets", "endingOffset");
            verify(dataFrameReaderMock).load();

            assertThat(actual).isEqualTo(datasetMock);
        }
    }

    @Nested
    class DataLookbackLimit {
        final KpiDefinition kpiDefinition = kpiDefinition("identifier", "group1");
        final Collection<KpiDefinition> kpiDefinitions = Collections.singletonList(kpiDefinition);

        @Test
        void shouldNotApplyTimeLimit(@Mock final Dataset<Row> datasetMock) {
            when(kpiDefinitionHelperMock.extractDataLookbackLimit(kpiDefinitions)).thenReturn(60);
            when(readinessLogRepositoryMock.findLatestCollectedTimeByDataSourceAndExecutionGroup(kpiDefinition.datasource(), kpiDefinition.getExecutionGroup())).thenReturn(Optional.empty());

            final Dataset<Row> actual = objectUnderTest.applyDataLookBackFilter(datasetMock, kpiDefinitions);

            verify(readinessLogRepositoryMock).findLatestCollectedTimeByDataSourceAndExecutionGroup(kpiDefinition.datasource(),kpiDefinition.getExecutionGroup());
            verify(kpiDefinitionHelperMock).extractDataLookbackLimit(kpiDefinitions);
            assertThat(actual).isEqualTo(datasetMock);
        }

        @Test
        void shouldApplyTimeLimit(@Mock final Dataset<Row> datasetMock, @Mock final Dataset<Row> filteredDatasetMock) {
            when(kpiDefinitionHelperMock.extractDataLookbackLimit(kpiDefinitions)).thenReturn(60);
            when(readinessLogRepositoryMock.findLatestCollectedTimeByDataSourceAndExecutionGroup(kpiDefinition.datasource(), kpiDefinition.getExecutionGroup())).thenReturn(Optional.of(testTime(12)));
            when(datasetMock.filter(col("aggregation_begin_time").geq(Timestamp.valueOf(testTime(11))))).thenReturn(filteredDatasetMock);

            final Dataset<Row> actual = objectUnderTest.applyDataLookBackFilter(datasetMock, kpiDefinitions);

            verify(readinessLogRepositoryMock).findLatestCollectedTimeByDataSourceAndExecutionGroup(kpiDefinition.datasource() ,kpiDefinition.getExecutionGroup());
            verify(kpiDefinitionHelperMock).extractDataLookbackLimit(kpiDefinitions);
            verify(datasetMock).filter(col("aggregation_begin_time").geq(Timestamp.valueOf(testTime(11))));

            assertThat(actual).isEqualTo(filteredDatasetMock);
        }

        KpiDefinition kpiDefinition(final String identifier, final String executionGroup) {
            return KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of(identifier)).withExecutionGroup(executionGroup).build();
        }

        LocalDateTime testTime(final int hour) {
            return LocalDateTime.of(2022, Month.OCTOBER, 15, hour, 0);
        }
    }

    @Nested
    class FindEarliestBeginTime{
        @Test
        @SuppressWarnings("unchecked")
        void testFindWhenDatasetIsEmpty(){
            final Dataset<Row> dataset = mock(Dataset.class, RETURNS_DEEP_STUBS);
            final TableDataset tableDataset = TableDataset.of(Table.of("kpi_table"), dataset);

            when(dataset.agg(any(Column.class)).head().get(0)).thenReturn(null);

            final Optional<LocalDateTime> actual = objectUnderTest.findEarliestTruncatedDateLoadedFromKafka(tableDataset, 1_440);

            assertThat(actual).isEmpty();
        }


        @Test
        void testFindWhenDatasetIsNotEmpty(){
            final TableDataset tableDataset = mock(TableDataset.class, RETURNS_DEEP_STUBS);
            when(tableDataset.getDataset().agg(any(Column.class)).head().get(0)).thenReturn(Timestamp.valueOf("2023-11-09 12:30:00"));

            final Optional<LocalDateTime> actual = objectUnderTest.findEarliestTruncatedDateLoadedFromKafka(tableDataset, 1_440);

            assertThat(actual).hasValue(LocalDateTime.parse("2023-11-09T00:00"));
        }



    }
}