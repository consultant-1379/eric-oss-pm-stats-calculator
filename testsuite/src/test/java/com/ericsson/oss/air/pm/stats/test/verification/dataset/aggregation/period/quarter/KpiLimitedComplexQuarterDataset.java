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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.quarterHourly.complex.KpiLimitedComplex;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiLimitedComplex} domain.
 */
public final class KpiLimitedComplexQuarterDataset extends AbstractDataset {
    public static final KpiLimitedComplexQuarterDataset INSTANCE = new KpiLimitedComplexQuarterDataset();

    public KpiLimitedComplexQuarterDataset() {
        super("kpi_limited_complex_15");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("ossID",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "first_integer_complex_15",
                             "copy_array_15");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiLimitedComplex.builder()
                                   .ossID(1)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .firstIntegerComplex15(21)
                                   .copyArray15(JsonArrayUtils.create(7, 14, 21, 28, 35))
                                   .build(),
                         KpiLimitedComplex.builder()
                                   .ossID(2)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .firstIntegerComplex15(15)
                                   .copyArray15(JsonArrayUtils.create(4, 8, 12, 16, 20))
                                   .build(),
                         KpiLimitedComplex.builder()
                                   .ossID(3)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                   .firstIntegerComplex15(12)
                                   .copyArray15(JsonArrayUtils.create(6, 12, 18, 24, 30))
                                   .build(),
                         KpiLimitedComplex.builder()
                                   .ossID(1)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .firstIntegerComplex15(24)
                                   .copyArray15(JsonArrayUtils.create(6, 12, 18, 24, 30))
                                   .build(),
                         KpiLimitedComplex.builder()
                                   .ossID(2)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .firstIntegerComplex15(21)
                                   .build(),
                         KpiLimitedComplex.builder()
                                   .ossID(3)
                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                   .firstIntegerComplex15(27)
                                   .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}