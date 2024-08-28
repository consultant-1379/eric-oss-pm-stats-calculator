/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.api;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderTest {
    @Mock JdbcDatasource jdbcDatasourceMock;
    @Mock Dataset<Row> datasetMock;

    @Spy PostgresDataLoader objectUnderTest;

    @Test
    void loadDataWithJobDescription() {
        final ReadingOptions readingOptions = ReadingOptions.empty();
        final SingleTableColumns tableColumns = SingleTableColumns.of(Table.of("table"), emptySet());

        when(objectUnderTest.loadData(tableColumns, jdbcDatasourceMock, readingOptions)).thenReturn(datasetMock);

        final Dataset<Row> actual = objectUnderTest.loadDataWithJobDescription("description", tableColumns, jdbcDatasourceMock, readingOptions);

        final InOrder inOrder = inOrder(objectUnderTest);
        inOrder.verify(objectUnderTest).registerJobDescription("description");
        inOrder.verify(objectUnderTest).loadData(tableColumns, jdbcDatasourceMock, readingOptions);
        inOrder.verify(objectUnderTest).unregisterJobDescription();

        Assertions.assertThat(actual).isEqualTo(datasetMock);
    }
}