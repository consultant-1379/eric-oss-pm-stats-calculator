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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.KpiSectorHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiSectorHourly} domain.
 */
public final class KpiSectorHourlyDataset extends AbstractDataset {
    public static final KpiSectorHourlyDataset INSTANCE = new KpiSectorHourlyDataset();

    public KpiSectorHourlyDataset() {
        super("kpi_sector_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "sum_integer_60_join_kpidb");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiSectorHourly.builder()
                                        .aggColumn0(1)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(2)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(3)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(5)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(7)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(3)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(8)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(9)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(10)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(3)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(11)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(3)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(16)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(18)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(22)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(23)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(24)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(25)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(27)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(3)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(28)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(31)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(32)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(33)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(34)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(35)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(35)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(2)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(36)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60JoinKpiDb(1)
                                        .build(),
                         KpiSectorHourly.builder()
                                        .aggColumn0(39)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                        .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                        .sumInteger60JoinKpiDb(1)
                                        .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
