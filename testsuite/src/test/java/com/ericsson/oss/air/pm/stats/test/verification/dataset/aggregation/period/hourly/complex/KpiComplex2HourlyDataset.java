/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.complex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.complex.KpiComplex2Hourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class KpiComplex2HourlyDataset extends AbstractDataset {

    public static final KpiComplex2HourlyDataset INSTANCE = new KpiComplex2HourlyDataset();

    public KpiComplex2HourlyDataset() {
        super("kpi_complex2_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "sum_integer_integer_arrayindex_complex");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiComplex2Hourly.builder()
                    .aggColumn0(2)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(3)
                    .sumIntegerIntegerArrayindexComplex(null)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(7)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(10)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(11)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(12)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(15)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(18)
                    .sumIntegerIntegerArrayindexComplex(null)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(22)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(25)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(26)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(27)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(32)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(38)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(42)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(43)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(44)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(45)
                    .build(),
                 KpiComplex2Hourly.builder()
                    .aggColumn0(46)
                    .sumIntegerIntegerArrayindexComplex(null)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(49)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(51)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(55)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(61)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(63)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(64)
                    .build(),
                KpiComplex2Hourly.builder()
                    .aggColumn0(71)
                    .build())
            .map(DatabaseRow::convertToRow)
            .collect(Collectors.toList());
    }
}
