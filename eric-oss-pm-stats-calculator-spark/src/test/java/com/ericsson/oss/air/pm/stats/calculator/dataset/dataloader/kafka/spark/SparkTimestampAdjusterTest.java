/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.spark;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.avro.Schema.Field;
import org.apache.spark.sql.Column;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparkTimestampAdjusterTest {

    @InjectMocks SparkTimestampAdjuster objectUnderTest;

    @Nested
    class AdjustTimestampColumns {
        @Mock Field fieldMock;

        @Test
        void shouldAdjustTimestampColumns() {
            when(fieldMock.name()).thenReturn("field");

            final Column actual = objectUnderTest.adjustTimestampColumn(fieldMock);

            verify(fieldMock).name();

            Assertions.assertThat(actual.expr().sql()).isEqualTo(
                    "CASE WHEN (%s IS NULL) THEN field ELSE from_unixtime((field / 1000), 'yyyy-MM-dd HH:mm:ss') END", "field"
            );
        }
    }

    @Nested
    class AdjustStringToTimestampColumns {
        @Test
        void shouldAdjustStringToTimestampColumn() {

            final Column actual = objectUnderTest.adjustStringToTimestampColumn("fieldName");

            Assertions.assertThat(actual.expr().sql()).isEqualTo(
                    "to_timestamp(fieldName)", "fieldName"
            );
        }
    }
}