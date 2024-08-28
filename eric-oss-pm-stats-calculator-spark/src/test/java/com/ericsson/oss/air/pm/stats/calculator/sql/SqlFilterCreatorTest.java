/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SqlFilterCreator}.
 */
class SqlFilterCreatorTest {
    private final SqlFilterCreator sqlFilterCreator = new SqlFilterCreator();
    private final Timestamp startTimestamp = Timestamp.valueOf("2019-05-09 12:00:00");
    private final Timestamp endTimestamp = Timestamp.valueOf("2019-05-09 16:00:00");

    @Test
    void whenSqlIsBuilt_ThenValidSqlReturned() {
        sqlFilterCreator.selectAll("kpi_cell_sector_1440");
        sqlFilterCreator.where();
        sqlFilterCreator.addFilter("kpi_db://kpi_cell_sector_1440.TO_DATE(aggregation_begin_time) = ''2019-05-09''");
        sqlFilterCreator.and();
        sqlFilterCreator.aggregationBeginTimeBetween(startTimestamp, endTimestamp);;

        assertThat(sqlFilterCreator.build()).isEqualTo("SELECT * FROM kpi_cell_sector_1440 "
                + "WHERE kpi_db://kpi_cell_sector_1440.TO_DATE(aggregation_begin_time) = ''2019-05-09'' "
                + "AND aggregation_begin_time BETWEEN TO_TIMESTAMP('2019-05-09 12:00:00.0') "
                + "AND TO_TIMESTAMP('2019-05-09 16:00:00.0')");

        assertThat(sqlFilterCreator.build()).isEqualTo("");
    }

}