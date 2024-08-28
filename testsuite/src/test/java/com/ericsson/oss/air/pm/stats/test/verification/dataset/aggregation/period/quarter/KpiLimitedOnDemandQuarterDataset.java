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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.quarterHourly.KpiLimitedOnDemand;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiLimitedOnDemand} domain.
 */
public final class KpiLimitedOnDemandQuarterDataset extends AbstractDataset {
    public static final KpiLimitedOnDemandQuarterDataset INSTANCE = new KpiLimitedOnDemandQuarterDataset();

    public KpiLimitedOnDemandQuarterDataset() {
        super("kpi_limited_ondemand_15");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("ossID",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "aggregate_array_15");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiLimitedOnDemand.builder()
                                   .ossID(1)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregateArray15(105)
                                   .build(),
                         KpiLimitedOnDemand.builder()
                                   .ossID(2)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregateArray15(60)
                                   .build(),
                         KpiLimitedOnDemand.builder()
                                   .ossID(3)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregateArray15(90)
                                   .build(),
                         KpiLimitedOnDemand.builder()
                                   .ossID(1)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .aggregateArray15(90)
                                   .build(),
                         KpiLimitedOnDemand.builder()
                                   .ossID(2)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .build(),
                         KpiLimitedOnDemand.builder()
                                   .ossID(3)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}