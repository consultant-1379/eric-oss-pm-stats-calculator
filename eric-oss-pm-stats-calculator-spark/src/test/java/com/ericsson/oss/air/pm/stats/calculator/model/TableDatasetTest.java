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

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TableDatasetTest {

    @SuppressWarnings("unchecked") Dataset<Row> datasetMock = mock(Dataset.class);

    @Test
    void shouldConstruct() {
        final TableDataset actual = TableDataset.of(SingleTableColumns.of(Table.of("table"), null), datasetMock);

        Assertions.assertThat(actual.getTable()).isEqualTo(Table.of("table"));
        Assertions.assertThat(actual.getDataset()).isEqualTo(datasetMock);
    }
}