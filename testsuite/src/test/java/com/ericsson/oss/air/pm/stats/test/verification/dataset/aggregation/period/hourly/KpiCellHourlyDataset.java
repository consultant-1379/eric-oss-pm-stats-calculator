/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.KpiCellHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiCellHourly} domain.
 */
public final class KpiCellHourlyDataset extends AbstractDataset {
    public static final KpiCellHourlyDataset INSTANCE = new KpiCellHourlyDataset();

    public KpiCellHourlyDataset() {
        super("kpi_cell_guid_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "first_integer_operator_60_stage2",
                             "first_integer_operator_60_stage3",
                             "first_integer_operator_60_stage4");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiCellHourly.builder()
                                      .aggColumn0(1)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(1)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(2)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(2)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(3)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(3)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(4)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(5)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(6)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(7)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(15f)
                                      .firstIntegerOperator60Stage3(1.5f)
                                      .firstIntegerOperator60Stage4(13.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(8)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(8)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(5f)
                                      .firstIntegerOperator60Stage3(0.5f)
                                      .firstIntegerOperator60Stage4(4.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(9)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(9)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1.0f)
                                      .firstIntegerOperator60Stage4(9.0f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(10)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .firstIntegerOperator60Stage2(15f)
                                      .firstIntegerOperator60Stage3(1.5f)
                                      .firstIntegerOperator60Stage4(13.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(10)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(15f)
                                      .firstIntegerOperator60Stage3(1.5f)
                                      .firstIntegerOperator60Stage4(13.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(11)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(15f)
                                      .firstIntegerOperator60Stage3(1.5f)
                                      .firstIntegerOperator60Stage4(13.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(12)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(13)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(14)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(15)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(16)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(16)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(20f)
                                      .firstIntegerOperator60Stage3(2f)
                                      .firstIntegerOperator60Stage4(18f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(17)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(18)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(18)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(25f)
                                      .firstIntegerOperator60Stage3(2.5f)
                                      .firstIntegerOperator60Stage4(22.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(19)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(20)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(21)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .firstIntegerOperator60Stage2(15f)
                                      .firstIntegerOperator60Stage3(1.5f)
                                      .firstIntegerOperator60Stage4(13.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(22)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(22)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(30f)
                                      .firstIntegerOperator60Stage3(3f)
                                      .firstIntegerOperator60Stage4(27f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(23)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(24)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(25)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(26)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(27)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(15f)
                                      .firstIntegerOperator60Stage3(1.5f)
                                      .firstIntegerOperator60Stage4(13.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(27)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(35f)
                                      .firstIntegerOperator60Stage3(3.5f)
                                      .firstIntegerOperator60Stage4(31.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(28)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(29)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(30)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(31)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(5.0F)
                                      .firstIntegerOperator60Stage3(0.5F)
                                      .firstIntegerOperator60Stage4(4.5F)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(32)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(33)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(33)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(40f)
                                      .firstIntegerOperator60Stage3(4f)
                                      .firstIntegerOperator60Stage4(36f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(34)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(34)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(45f)
                                      .firstIntegerOperator60Stage3(4.5f)
                                      .firstIntegerOperator60Stage4(40.5f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(35)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(35)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(35)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(50f)
                                      .firstIntegerOperator60Stage3(5f)
                                      .firstIntegerOperator60Stage4(45f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(36)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                      .firstIntegerOperator60Stage2(5.0F)
                                      .firstIntegerOperator60Stage3(0.5F)
                                      .firstIntegerOperator60Stage4(4.5F)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(37)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .firstIntegerOperator60Stage2(10f)
                                      .firstIntegerOperator60Stage3(1f)
                                      .firstIntegerOperator60Stage4(9f)
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(38)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(39)
                                      .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                      .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                      .build(),
                         KpiCellHourly.builder()
                                      .aggColumn0(40)
                                      .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                      .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                      .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
