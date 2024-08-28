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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.KpiOnDemandFdnAggDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiOnDemandFdnAggDaily} domain.
 */
public final class KpiOnDemandFdnAggDailyDataset extends AbstractDataset {
    public static final KpiOnDemandFdnAggDailyDataset INSTANCE = new KpiOnDemandFdnAggDailyDataset();

    private KpiOnDemandFdnAggDailyDataset() {
        super("kpi_ondemand_fdn_agg_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("subnet",
                       "fdn_sum_agg",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiOnDemandFdnAggDaily.builder()
                                               .subnet("SubNetwork=SN1")
                                               .fdnSumAgg(63504L)
                                               .build(),
                         KpiOnDemandFdnAggDaily.builder()
                                               .subnet("SubNetwork=SN2")
                                               .fdnSumAgg(168864L)
                                               .build(),
                         KpiOnDemandFdnAggDaily.builder()
                                               .subnet("SubNetwork=SN1")
                                               .fdnSumAgg(40015L)
                                               .aggregationBeginTime(TimeUtils.TODAY)
                                               .aggregationEndTime(TimeUtils.daysAgo(-1))
                                               .build(),
                         KpiOnDemandFdnAggDaily.builder()
                                               .subnet("SubNetwork=SN2")
                                               .fdnSumAgg(105605L)
                                               .aggregationBeginTime(TimeUtils.TODAY)
                                               .aggregationEndTime(TimeUtils.daysAgo(-1))
                                               .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
