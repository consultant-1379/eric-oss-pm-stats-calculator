/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.KpiCellDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiCellDaily} domain.
 */
public final class KpiCellDailyDataset extends AbstractDataset {
    public static final KpiCellDailyDataset INSTANCE = new KpiCellDailyDataset();

    public KpiCellDailyDataset() {
        super("kpi_cell_guid_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "first_integer_aggregate_slice_1440");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiCellDaily.builder()
                                     .aggColumn0(1)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(1)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(2)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(2)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(3)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(3)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(4)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(5)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(6)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(7)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(8)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(8)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(9)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(9)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(10)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(10)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(11)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(12)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(13)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(14)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(15)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(16)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(16)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(17)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(18)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(18)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(19)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(20)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(21)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(22)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(22)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(23)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(24)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(25)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(26)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(27)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(27)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(28)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(29)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(30)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(31)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(32)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(33)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(33)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(34)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(34)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(35)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(35)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(36)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(37)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(38)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(39)
                                     .build(),
                         KpiCellDaily.builder()
                                     .aggColumn0(40)
                                     .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                     .aggregationEndTime(TimeUtils.TODAY)
                                     .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }

}
