/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SqlDateUtilsTest {
    @Test
    void shouldThrowException_whenAggregationPeriodIsNotKnow() {
        final int aggregationPeriod = 75;
        Assertions.assertThatThrownBy(() -> SqlDateUtils.truncateToAggregationPeriod("table.aggregation_begin_time", aggregationPeriod))
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessage("Aggregation period '%d' is not supported", aggregationPeriod);
    }

    @CsvSource(delimiter = ';', value = {
            "table.aggregation_begin_time;   -1; CAST(date_format(table.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)",
            "table.aggregation_begin_time;   15; TRUNCATE_TO_FIFTEEN_MINUTE(table.aggregation_begin_time)",
            "table.aggregation_begin_time;   60; CAST(date_format(table.aggregation_begin_time, 'yyyy-MM-dd HH') AS TIMESTAMP)",
            "table.aggregation_begin_time; 1440; CAST(date_format(table.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)"
    })
    @ParameterizedTest
    void shouldTruncateToAggregationPeriod(final String column, final int aggregationPeriod, final String expected) {
        final String actual = SqlDateUtils.truncateToAggregationPeriod(column, aggregationPeriod);
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}