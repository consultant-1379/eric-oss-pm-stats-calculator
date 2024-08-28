/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.simple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.simple.KpiSumFloatSimpleDailyFilter;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

public class KpiRelGuidSimpleFilterDailyDateset extends AbstractDataset {
    public static final KpiRelGuidSimpleFilterDailyDateset INSTANCE = new KpiRelGuidSimpleFilterDailyDateset();

    public KpiRelGuidSimpleFilterDailyDateset() {
        super("kpi_rel_guid_s_guid_t_guid_simple_filter_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList(
                "nodeFDN",
                "aggregation_begin_time",
                "aggregation_end_time",
                "sum_integer_1440_simple_filter"
        );
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiSumFloatSimpleDailyFilter.builder()
                                .sumInteger1440SimpleFilter(32)
                                .build(),
                        KpiSumFloatSimpleDailyFilter.builder()
                                .aggregationBeginTime(TimeUtils.A_DAY_AGO)
                                .aggregationEndTime(TimeUtils.TODAY)
                                .sumInteger1440SimpleFilter(21)
                                .build()
                )
                .map(DatabaseRow::convertToRow)
                .collect(Collectors.toList());
    }
}
