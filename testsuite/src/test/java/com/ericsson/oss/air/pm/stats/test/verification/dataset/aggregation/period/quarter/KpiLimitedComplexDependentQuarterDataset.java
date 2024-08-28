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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.quarterHourly.complex.KpiLimitedComplexDependent;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiLimitedComplexDependent} domain.
 */
public final class KpiLimitedComplexDependentQuarterDataset extends AbstractDataset {
    public static final KpiLimitedComplexDependentQuarterDataset INSTANCE = new KpiLimitedComplexDependentQuarterDataset();

    public KpiLimitedComplexDependentQuarterDataset() {
        super("kpi_limited_complex_dependent_15");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("ossID",
                "aggregation_begin_time",
                "aggregation_end_time",
                "transform_complex_15");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiLimitedComplexDependent.builder()
                                                   .ossID(1)
                                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                                   .transformComplex15(JsonArrayUtils.create(147, 294, 441, 588, 735))
                                                   .build(),
                         KpiLimitedComplexDependent.builder()
                                                   .ossID(2)
                                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                                   .transformComplex15(JsonArrayUtils.create(60, 120, 180, 240, 300))
                                                   .build(),
                         KpiLimitedComplexDependent.builder()
                                                   .ossID(3)
                                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 0))
                                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 15))
                                                   .transformComplex15(JsonArrayUtils.create(72, 144, 216, 288, 360))
                                                   .build(),
                         KpiLimitedComplexDependent.builder()
                                                   .ossID(1)
                                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                                   .transformComplex15(JsonArrayUtils.create(144, 288, 432, 576, 720))
                                                   .build(),
                         KpiLimitedComplexDependent.builder()
                                                   .ossID(2)
                                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                                   .transformComplex15(JsonArrayUtils.create(168, 336, 504, 672, 840))
                                                   .build(),
                         KpiLimitedComplexDependent.builder()
                                                   .ossID(3)
                                                   .aggregationBeginTime(TimeUtils.daysAgo(2, 22, 15))
                                                   .aggregationEndTime(TimeUtils.daysAgo(2, 22, 30))
                                                   .transformComplex15(JsonArrayUtils.create(216, 432, 648, 864, 1080))
                                                   .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}