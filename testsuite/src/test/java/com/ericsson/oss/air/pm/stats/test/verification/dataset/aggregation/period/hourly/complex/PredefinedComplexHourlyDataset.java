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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.complex.PredefinedComplexHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class PredefinedComplexHourlyDataset extends AbstractDataset {
    public static final PredefinedComplexHourlyDataset INSTANCE = new PredefinedComplexHourlyDataset();

    private PredefinedComplexHourlyDataset() {
        super("kpi_predefined_complex_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("moFdn",
                       "nodeFDN",
                       "median_complex_hourly",
                       "weighted_average_complex_hourly",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(14D)
                                                .weightedAverageComplexHourly(23.8125)
                                                .aggregationBeginTime(daysAgo(1, 22))
                                                .aggregationEndTime(daysAgo(1, 23))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(16D)
                                                .weightedAverageComplexHourly(24.5)
                                                .aggregationBeginTime(daysAgo(1, 23))
                                                .aggregationEndTime(TODAY)
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(24D)
                                                .weightedAverageComplexHourly(29D)
                                                .aggregationBeginTime(TODAY)
                                                .aggregationEndTime(daysAgo(0, 1))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(16D)
                                                .weightedAverageComplexHourly(24.5625)
                                                .aggregationBeginTime(daysAgo(1, 22))
                                                .aggregationEndTime(daysAgo(1, 23))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(18D)
                                                .weightedAverageComplexHourly(25D)
                                                .aggregationBeginTime(daysAgo(1, 23))
                                                .aggregationEndTime(TODAY)
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(28D)
                                                .weightedAverageComplexHourly(30D)
                                                .aggregationBeginTime(TODAY)
                                                .aggregationEndTime(daysAgo(0, 1))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(18D)
                                                .weightedAverageComplexHourly(25.6875)
                                                .aggregationBeginTime(daysAgo(1, 22))
                                                .aggregationEndTime(daysAgo(1, 23))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(20D)
                                                .weightedAverageComplexHourly(25.5)
                                                .aggregationBeginTime(daysAgo(1, 23))
                                                .aggregationEndTime(TODAY)
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(32D)
                                                .weightedAverageComplexHourly(31D)
                                                .aggregationBeginTime(TODAY)
                                                .aggregationEndTime(daysAgo(0, 1))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(20D)
                                                .weightedAverageComplexHourly(26.4375)
                                                .aggregationBeginTime(daysAgo(1, 22))
                                                .aggregationEndTime(daysAgo(1, 23))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(22D)
                                                .weightedAverageComplexHourly(26D)
                                                .aggregationBeginTime(daysAgo(1, 23))
                                                .aggregationEndTime(TODAY)
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(36D)
                                                .weightedAverageComplexHourly(32D)
                                                .aggregationBeginTime(TODAY)
                                                .aggregationEndTime(daysAgo(0, 1))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(22D)
                                                .weightedAverageComplexHourly(27.5625)
                                                .aggregationBeginTime(daysAgo(1, 22))
                                                .aggregationEndTime(daysAgo(1, 23))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(24D)
                                                .weightedAverageComplexHourly(26.5)
                                                .aggregationBeginTime(daysAgo(1, 23))
                                                .aggregationEndTime(TODAY)
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .medianComplexHourly(40D)
                                                .weightedAverageComplexHourly(33D)
                                                .aggregationBeginTime(TODAY)
                                                .aggregationEndTime(daysAgo(0, 1))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                                .medianComplexHourly(14D)
                                                .weightedAverageComplexHourly(23D)
                                                .aggregationBeginTime(daysAgo(1, 22))
                                                .aggregationEndTime(daysAgo(1, 23))
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                                .medianComplexHourly(14D)
                                                .weightedAverageComplexHourly(23D)
                                                .aggregationBeginTime(daysAgo(1, 23))
                                                .aggregationEndTime(TODAY)
                                                .build(),
                         PredefinedComplexHourly.builder()
                                                .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                                .medianComplexHourly(14D)
                                                .weightedAverageComplexHourly(23D)
                                                .aggregationBeginTime(TODAY)
                                                .aggregationEndTime(daysAgo(0, 1))
                                                .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
