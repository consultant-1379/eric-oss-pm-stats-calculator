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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.dataset.exception.DataSourceException;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TempViewCreatorImplTest {
    @Mock Supplier<Dataset<Row>> datasetSupplierMock;
    @Mock Dataset<Row> datasetMock;
    @Mock SparkService sparkServiceMock;

    @InjectMocks TempViewCreatorImpl objectUnderTest;

    @Test
    void shouldCreateOrReplaceTempView() {
        when(datasetSupplierMock.get()).thenReturn(datasetMock);
        when(datasetMock.columns()).thenReturn(new String[]{"column1", "column2"});

        objectUnderTest.createOrReplaceTempView(Table.of("table"), datasetSupplierMock);

        verify(datasetSupplierMock).get();
        verify(datasetMock).columns();
        verify(sparkServiceMock).cacheView("table", datasetMock);
    }

    @Test
    void shouldThrowException_whenDatasetIsEmpty_onCreateOrReplaceTempView() {
        when(datasetSupplierMock.get()).thenReturn(datasetMock);
        when(datasetMock.columns()).thenReturn(ArrayUtils.EMPTY_STRING_ARRAY);

        Assertions.assertThatThrownBy(() -> objectUnderTest.createOrReplaceTempView(Table.of("table"), datasetSupplierMock))
                  .isInstanceOf(DataSourceException.class)
                  .hasMessage("Error loading Dataset, table '%s' returned an empty Dataset", "table");

        verify(datasetSupplierMock).get();
        verify(datasetMock).columns();
        verify(sparkServiceMock, never()).cacheView("table", datasetMock);
    }
}