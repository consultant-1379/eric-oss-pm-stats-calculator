/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api;

import static org.mockito.Mockito.inOrder;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatasetWriterTest {
    @Spy DatasetWriter objectUnderTest;

    @Nested
    class WriteToTargetTable {
        @Mock Dataset<Row> datasetMock;

        @Test
        void shouldWriteToTargetTable() {
            objectUnderTest.writeToTargetTable("description", datasetMock, "targetTable", "primaryKeys");

            final InOrder inOrder = inOrder(objectUnderTest);

            inOrder.verify(objectUnderTest).registerJobDescription("description");
            inOrder.verify(objectUnderTest).writeToTargetTable(datasetMock, "targetTable", "primaryKeys");
            inOrder.verify(objectUnderTest).unregisterJobDescription();
        }
    }

    @Test
    void shouldWriteToTargetTableWithCache(@Mock final Dataset<Row> datasetMock) {
        objectUnderTest.writeToTargetTableWithCache("description", datasetMock, "targetTable", "primaryKeys");

        final InOrder inOrder = inOrder(objectUnderTest);

        inOrder.verify(objectUnderTest).registerJobDescription("description");
        inOrder.verify(objectUnderTest).writeToTargetTable(datasetMock, "targetTable", "primaryKeys");
        inOrder.verify(objectUnderTest).unregisterJobDescription();
        inOrder.verify(objectUnderTest).cacheOnDemandReliability(datasetMock);
    }
}