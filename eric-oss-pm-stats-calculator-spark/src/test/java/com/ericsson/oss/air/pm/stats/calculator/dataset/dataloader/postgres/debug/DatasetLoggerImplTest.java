/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.debug;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import scala.Tuple2;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class DatasetLoggerImplTest {
    @Mock SparkSession sparkSessionMock;

    @InjectMocks DatasetLoggerImpl objectUnderTest;

    @BeforeAll
    static void beforeAll() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(DatasetLoggerImpl.class).setLevel(Level.DEBUG);
    }

    @Nested
    @DisplayName("Testing logging filtered rows")
    class FilteredRows {
        @Mock Dataset<Row> datasetMock;

        @Captor ArgumentCaptor<ForeachFunction<Row>> functionArgumentCaptor;

        @Test
        void shouldLogFilteredRows(final CapturedOutput capturedOutput) throws Exception {
            when(sparkSessionMock.sql("SELECT * FROM table WHERE filter")).thenReturn(datasetMock);

            objectUnderTest.logFilteredRows(Table.of("table"), "filter");

            verify(sparkSessionMock).sql("SELECT * FROM table WHERE filter");
            verify(datasetMock).foreach(functionArgumentCaptor.capture());

            functionArgumentCaptor.getValue().call(Row.fromTuple(new Tuple2<>("hello", "everyone")));

            Assertions.assertThat(capturedOutput).contains("Filtering out row '[hello,everyone]'");
        }
    }
}