/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.KpiCellSectorDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

/**
 * {@link AbstractDataset} implementation for the {@link KpiCellSectorDaily} domain.
 */
public final class KpiCellSectorDailyDataset extends AbstractDataset {
    public static final KpiCellSectorDailyDataset INSTANCE = new KpiCellSectorDailyDataset();

    public KpiCellSectorDailyDataset() {
        super("kpi_cell_sector_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("moFdn",
                             "nodeFDN",
                             "execution_id",
                             "first_float_dim_enrich_1440",
                             "first_integer_dim_enrich_1440",
                             "aggregation_begin_time",
                             "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiCellSectorDaily.builder()
                                           .moFdn(1)
                                           .nodeFDN(1)
                                           .firstIntegerDimEnrich1440(1)
                                           .build(),
                         KpiCellSectorDaily.builder()
                                           .moFdn(2)
                                           .nodeFDN(2)
                                           .firstIntegerDimEnrich1440(1)
                                           .firstFloatDimEnrich1440(0.5F)
                                           .build(),
                         KpiCellSectorDaily.builder()
                                           .moFdn(1)
                                           .nodeFDN(14)
                                           .firstFloatDimEnrich1440(2F)
                                           .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
