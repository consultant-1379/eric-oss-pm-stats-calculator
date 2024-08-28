/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.complex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.complex.KpiComplexDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class KpiComplexDailyDataset extends AbstractDataset {

    public static final KpiComplexDailyDataset INSTANCE = new KpiComplexDailyDataset();

    public KpiComplexDailyDataset() {
        super("kpi_complex_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "agg_column_1",
                             "sum_float_1440_complex",
                             "sum_integer_1440_complex_non_triggered",
                             "aggregation_begin_time",
                             "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiComplexDaily.builder()
                    .aggColumn0(1)
                    .aggColumn1(18)
                    .build(),
                KpiComplexDaily.builder()
                    .aggColumn0(2)
                    .aggColumn1(25)
                    .build(),
                 KpiComplexDaily.builder()
                    .aggColumn0(3)
                    .aggColumn1(28)
                    .build())
            .map(DatabaseRow::convertToRow)
            .collect(Collectors.toList());
    }
}
