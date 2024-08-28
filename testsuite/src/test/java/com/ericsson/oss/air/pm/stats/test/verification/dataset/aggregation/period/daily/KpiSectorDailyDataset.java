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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.KpiSectorDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

/**
 * {@link AbstractDataset} implementation for the {@link KpiSectorDaily} domain.
 */
public final class KpiSectorDailyDataset extends AbstractDataset {
    public static final KpiSectorDailyDataset INSTANCE = new KpiSectorDailyDataset();

    public KpiSectorDailyDataset() {
        super("kpi_sector_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "max_integer_1440_kpidb");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiSectorDaily.builder()
                                       .aggColumn0(1)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(2)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(3)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(5)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(7)
                                       .maxInteger1440KpiDb(3)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(8)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(9)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(10)
                                       .maxInteger1440KpiDb(3)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(11)
                                       .maxInteger1440KpiDb(3)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(16)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(18)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(22)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(23)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(24)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(25)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(27)
                                       .maxInteger1440KpiDb(3)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(28)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(31)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(32)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(33)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(34)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(35)
                                       .maxInteger1440KpiDb(2)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(36)
                                       .maxInteger1440KpiDb(1)
                                       .build(),
                         KpiSectorDaily.builder()
                                       .aggColumn0(39)
                                       .maxInteger1440KpiDb(1)
                                       .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }

}
