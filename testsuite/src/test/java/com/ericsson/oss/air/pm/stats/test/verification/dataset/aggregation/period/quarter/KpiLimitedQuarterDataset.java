/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.quarter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.quarterHourly.simple.KpiLimited;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiLimited} domain.
 */
public final class KpiLimitedQuarterDataset extends AbstractDataset {
    public static final KpiLimitedQuarterDataset INSTANCE = new KpiLimitedQuarterDataset();

    public KpiLimitedQuarterDataset() {
        super("kpi_limited_15");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("ossID",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "sum_integer_15",
                             "count_integer_15",
                             "transform_array_15",
                             "not_calculated_simple_15");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiLimited.builder()
                                   .ossID(1)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .sumInteger15(7)
                                   .transformArray15(JsonArrayUtils.create(7, 14, 21, 28, 35))
                                   .build(),
                         KpiLimited.builder()
                                   .ossID(2)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .sumInteger15(5)
                                   .transformArray15(JsonArrayUtils.create(4, 8, 12, 16, 20))
                                   .build(),
                         KpiLimited.builder()
                                   .ossID(3)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .sumInteger15(4)
                                   .transformArray15(JsonArrayUtils.create(6, 12, 18, 24, 30))
                                   .build(),
                         KpiLimited.builder()
                                   .ossID(1)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .sumInteger15(8)
                                   .transformArray15(JsonArrayUtils.create(6, 12, 18, 24, 30))
                                   .build(),
                         KpiLimited.builder()
                                   .ossID(2)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .sumInteger15(7)
                                   .build(),
                         KpiLimited.builder()
                                   .ossID(3)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .sumInteger15(9)
                                   .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}