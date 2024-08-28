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

import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TODAY;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.daysAgo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.complex.ComplexFdnHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class ComplexFdnHourlyDataset extends AbstractDataset {
    public static final ComplexFdnHourlyDataset INSTANCE = new ComplexFdnHourlyDataset();

    private ComplexFdnHourlyDataset() {
        super("kpi_complex_fdn_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("fdn_parse_mo",
                       "nodeFDN",
                       "cell_level_agg",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                         .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                         .cellLevelAgg(14448L)
                                         .aggregationBeginTime(daysAgo(1, 22))
                                         .aggregationEndTime(daysAgo(1, 23))
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                         .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                         .cellLevelAgg(14544L)
                                         .aggregationBeginTime(daysAgo(1, 23))
                                         .aggregationEndTime(TODAY)
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                         .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                         .cellLevelAgg(14640L)
                                         .aggregationBeginTime(TODAY)
                                         .aggregationEndTime(daysAgo(0, 1))
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                         .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                         .cellLevelAgg(17224L)
                                         .aggregationBeginTime(daysAgo(1, 22))
                                         .aggregationEndTime(daysAgo(1, 23))
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                         .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                         .cellLevelAgg(17288L)
                                         .aggregationBeginTime(daysAgo(1, 23))
                                         .aggregationEndTime(TODAY)
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                         .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                         .cellLevelAgg(17352L)
                                         .aggregationBeginTime(TODAY)
                                         .aggregationEndTime(daysAgo(0, 1))
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                         .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                         .cellLevelAgg(84416L)
                                         .aggregationBeginTime(daysAgo(1, 22))
                                         .aggregationEndTime(daysAgo(1, 23))
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                         .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                         .cellLevelAgg(84448L)
                                         .aggregationBeginTime(daysAgo(1, 23))
                                         .aggregationEndTime(TODAY)
                                         .build(),
                         ComplexFdnHourly.builder()
                                         .fdnParseMo("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                         .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                         .cellLevelAgg(84480L)
                                         .aggregationBeginTime(TODAY)
                                         .aggregationEndTime(daysAgo(0, 1))
                                         .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
