/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.none.complex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.none.complex.KpiComplexNone;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

/**
 * {@link AbstractDataset} implementation for the {@link KpiComplexNone} domain.
 */

public class KpiComplexNoneDataset extends AbstractDataset {
    public static final KpiComplexNoneDataset INSTANCE = new KpiComplexNoneDataset();

    public KpiComplexNoneDataset() {
        super("kpi_complex_");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                "agg_column_1",
                "sum_integer_complex");
    }

    public List<String> getDataset() {
        return Stream.of(KpiComplexNone.builder()
                                .aggColumn0(1)
                                .aggColumn1(18)
                                .build(),
                        KpiComplexNone.builder()
                                .aggColumn0(2)
                                .aggColumn1(25)
                                .build(),
                        KpiComplexNone.builder()
                                .aggColumn0(3)
                                .aggColumn1(28)
                                .build())
                .map(DatabaseRow::convertToRow)
                .collect(Collectors.toList());
    }
}
