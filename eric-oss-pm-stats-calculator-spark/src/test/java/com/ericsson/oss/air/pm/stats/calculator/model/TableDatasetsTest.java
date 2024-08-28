/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.ericsson.oss.air.pm.stats.calculator._test.TableDatasetsUtil;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class TableDatasetsTest {
    TableDatasets objectUnderTest = TableDatasets.of();

    @Test
    void shouldVerifyNonEmptiness(@Mock final Dataset<Row> datasetMock) {
        final TableDatasets tableDatasets = TableDatasetsUtil.from(Collections.singletonMap("table_1", datasetMock));
        Assertions.assertThat(tableDatasets.isNotEmpty()).isTrue();
        Assertions.assertThat(tableDatasets.isEmpty()).isFalse();
    }

    @Test
    void shouldVerifyEmptiness() {
        final TableDatasets tableDatasets = TableDatasets.of();
        Assertions.assertThat(tableDatasets.isEmpty()).isTrue();
        Assertions.assertThat(tableDatasets.isNotEmpty()).isFalse();
    }

    @Test
    void shouldUnPersistDatasets(@Mock final Dataset<Row> datasetMock1, @Mock final Dataset<Row> datasetMock2) {
        objectUnderTest.put(TableDataset.of(Table.of("table_1"), datasetMock1));
        objectUnderTest.put(TableDataset.of(Table.of("table_2"), datasetMock2));

        objectUnderTest.unPersistDatasets();

        verify(datasetMock1).unpersist();
        verify(datasetMock2).unpersist();
    }

    @Test
    void shouldCacheDatasets(@Mock final Dataset<Row> datasetMock1, @Mock final Dataset<Row> datasetMock2) {
        objectUnderTest.put(TableDataset.of(Table.of("table_1"), datasetMock1));
        objectUnderTest.put(TableDataset.of(Table.of("table_2"), datasetMock2));

        objectUnderTest.cacheDatasets();

        verify(datasetMock1).cache();
        verify(datasetMock2).cache();
    }

    @Test
    void shouldPut(@Mock final Dataset<Row> datasetMock) {
        objectUnderTest.put(TableDataset.of(Table.of("table"), datasetMock));


        Assertions.assertThat(objectUnderTest).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                "table", datasetMock
        )));
    }

    @Test
    void shouldPutAll(@Mock final Dataset<Row> datasetMock) {
        final TableDatasets tableDatasets = TableDatasets.of();
        tableDatasets.put(TableDataset.of(Table.of("table1"), datasetMock));
        tableDatasets.put(TableDataset.of(Table.of("table2"), datasetMock));

        objectUnderTest.put(TableDataset.of(Table.of("table1"), datasetMock));
        objectUnderTest.put(TableDataset.of(Table.of("table3"), datasetMock));
        objectUnderTest.putAll(tableDatasets);

        Assertions.assertThat(objectUnderTest).isEqualTo(TableDatasetsUtil.from(ImmutableMap.of(
                "table1", datasetMock,
                "table2", datasetMock,
                "table3", datasetMock
        )));
    }

    @Test
    void shouldMerge(@Mock final Dataset<Row> datasetMock) {
        final TableDatasets left = TableDatasets.of();
        final TableDatasets right = TableDatasets.of();

        final Table table1 = Table.of("table1");
        final Table table2 = Table.of("table2");
        final Table table3 = Table.of("table3");

        left.put(TableDataset.of(table1, datasetMock));
        left.put(TableDataset.of(table2, datasetMock));

        right.put(TableDataset.of(table2, datasetMock));
        right.put(TableDataset.of(table3, datasetMock));

        final TableDatasets actual = TableDatasets.merge(left, right);

        final TableDatasets expected = TableDatasets.of();
        expected.put(TableDataset.of(table1, datasetMock));
        expected.put(TableDataset.of(table2, datasetMock));
        expected.put(TableDataset.of(table3, datasetMock));

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldInstantiateFrom(@Mock final Dataset<Row> datasetMock) {
        final TableDatasets actual = TableDatasetsUtil.from(Collections.singletonMap("table", datasetMock));

        final TableDatasets expected = TableDatasets.of();
        expected.put(Table.of("table"), datasetMock);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGiveBackAggregationPeriod(@Mock final Dataset<Row> datasetMock) {
        final TableDatasets tableDatasets = TableDatasets.of();
        tableDatasets.put(TableDataset.of(Table.of("kpi_table_60"), datasetMock));

        final Integer actual = tableDatasets.getAggregationPeriodOfTable();

        Assertions.assertThat(actual).isEqualTo(60);
    }


    @CsvSource({
            "true, false",
            "false, true"
    })
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ParameterizedTest(name = "[{index}] Is dataset empty ''{0}'' ==>  ''{1}''")
    void testHasNoEmptyValue(final boolean isDatasetEmpty, final boolean expected) {
        final Dataset datasetMock = mock(Dataset.class);

        objectUnderTest.put(TableDataset.of(Table.of("test_table"), datasetMock));

        when(datasetMock.isEmpty()).thenReturn(isDatasetEmpty);

        final boolean actual = objectUnderTest.hasNoEmptyValue();

        verify(datasetMock).isEmpty();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testHasNoValues_onHasNoEmptyValue() {
        final boolean actual = objectUnderTest.hasNoEmptyValue();

        Assertions.assertThat(actual).isTrue();
    }
}