/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table;

import java.util.Arrays;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlAppenderTest {
    final StringBuilder stringBuilder = new StringBuilder();

    @InjectMocks
    SqlAppenderImpl objectUnderTest;

    @Test
    void shouldAppendPartition() {
        objectUnderTest.appendPartition(stringBuilder);

        Assertions.assertThat(stringBuilder).hasToString(" PARTITION BY RANGE ( aggregation_begin_time ) WITH ( OIDS = FALSE );");
    }

    @Test
    void shouldAppendTimestampColumns() {
        objectUnderTest.appendTimestampColumns(stringBuilder);

        Assertions.assertThat(stringBuilder)
                .hasToString("aggregation_begin_time TIMESTAMP WITHOUT TIME ZONE NOT NULL, aggregation_end_time TIMESTAMP WITHOUT TIME ZONE");
    }

    @Test
    void shouldAppendPrimaryKeys() {
        objectUnderTest.appendPrimaryKey(stringBuilder, Arrays.asList("column1", "column2"));

        Assertions.assertThat(stringBuilder).hasToString("PRIMARY KEY (\"column1\", \"column2\")");
    }

    @Test
    void shouldAppendColumnNameAndType() {
        objectUnderTest.appendColumnNameAndType(stringBuilder, "kpiDefinitionName", "INTEGER");
        Assertions.assertThat(stringBuilder).hasToString("\"kpiDefinitionName\" int4, ");
    }

    @Test
    void appendAggregationElementColumns() {
        objectUnderTest.appendAggregationElementColumns(stringBuilder, Arrays.asList("agg1", "agg2"),
                Map.of(
                        "agg1", KpiDataType.POSTGRES_LONG,
                        "agg2", KpiDataType.POSTGRES_LONG));

        Assertions.assertThat(stringBuilder).hasToString("\"agg1\" int8 NOT NULL, \"agg2\" int8 NOT NULL, ");
    }
}