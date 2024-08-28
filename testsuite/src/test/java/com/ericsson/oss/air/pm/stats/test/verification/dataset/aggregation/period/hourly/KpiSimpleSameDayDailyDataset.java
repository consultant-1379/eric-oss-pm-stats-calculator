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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.simple.KpiSimpleSameDayDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

public final class KpiSimpleSameDayDailyDataset extends AbstractDataset {

    public static final KpiSimpleSameDayDailyDataset INSTANCE = new KpiSimpleSameDayDailyDataset();

    public KpiSimpleSameDayDailyDataset() {
        super("kpi_same_day_simple_60");
    }

    @Override public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "agg_column_1",
                             "float_simple_same_day",
                             "integer_simple_same_day",
                             "aggregation_begin_time",
                             "aggregation_end_time");
    }

    @Override public List<String> getDataset() {
        return Stream.of(KpiSimpleSameDayDaily.builder()
                                              .aggColumn0(1)
                                              .aggColumn1(18)
                                              .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                              .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                              .build(),
                         KpiSimpleSameDayDaily.builder()
                                              .aggColumn0(2)
                                              .aggColumn1(25)
                                              .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                              .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                              .build(),
                         KpiSimpleSameDayDaily.builder()
                                              .aggColumn0(3)
                                              .aggColumn1(28)
                                              .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                              .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                              .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
