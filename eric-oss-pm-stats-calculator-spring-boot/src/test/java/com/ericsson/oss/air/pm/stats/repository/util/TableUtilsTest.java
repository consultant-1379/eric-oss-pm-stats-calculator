/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import java.time.LocalDate;
import java.time.Month;

import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

class TableUtilsTest {

    @Test
    void whenGetCreateUniqueIndexSqlForPartitionIsCalled_thenExpectedSqlIsReturned() {
        final Partition partition = new Partition(
                "kpi_table_1440_p_2021_01_08",
                "kpi_table_1440_p_2021_01_08",
                LocalDate.of(2_021, Month.JANUARY, 8),
                LocalDate.of(2_021, Month.JANUARY, 9),
                Sets.newLinkedHashSet("agg_col_2", "agg_col_1")
        );

        final String actual = TableUtils.createUniqueIndexSql(partition);
        Assertions.assertThat(actual).isEqualTo(
                "CREATE UNIQUE INDEX IF NOT EXISTS kpi_table_1440_p_2021_01_08_ui " +
                        "ON kpi_table_1440_p_2021_01_08 ( \"agg_col_1\",  \"agg_col_2\" );"
        );
    }

}