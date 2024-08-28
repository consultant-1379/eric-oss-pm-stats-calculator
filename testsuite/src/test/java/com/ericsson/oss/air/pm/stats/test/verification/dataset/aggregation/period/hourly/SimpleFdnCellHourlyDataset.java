/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly;

import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TODAY;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.daysAgo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.SimpleFdnCellHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class SimpleFdnCellHourlyDataset extends AbstractDataset {
    public static final SimpleFdnCellHourlyDataset INSTANCE = new SimpleFdnCellHourlyDataset();

    private SimpleFdnCellHourlyDataset() {
        super("kpi_simple_fdn_cell_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("moFdn",
                       "nodeFDN",
                       "simple_fdn_expr",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 22))
                                            .aggregationEndTime(daysAgo(1, 23))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 23))
                                            .aggregationEndTime(TODAY)
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(TODAY)
                                            .aggregationEndTime(daysAgo(0, 1))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(0, 1))
                                            .aggregationEndTime(daysAgo(0, 2))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 22))
                                            .aggregationEndTime(daysAgo(1, 23))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 23))
                                            .aggregationEndTime(TODAY)
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(TODAY)
                                            .aggregationEndTime(daysAgo(0, 1))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(0, 1))
                                            .aggregationEndTime(daysAgo(0, 2))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 22))
                                            .aggregationEndTime(daysAgo(1, 23))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 23))
                                            .aggregationEndTime(TODAY)
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(TODAY)
                                            .aggregationEndTime(daysAgo(0, 1))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(0, 1))
                                            .aggregationEndTime(daysAgo(0, 2))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(daysAgo(1, 22))
                                            .aggregationEndTime(daysAgo(1, 23))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(daysAgo(1, 23))
                                            .aggregationEndTime(TODAY)
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(TODAY)
                                            .aggregationEndTime(daysAgo(0, 1))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(daysAgo(0, 1))
                                            .aggregationEndTime(daysAgo(0, 2))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(daysAgo(1, 22))
                                            .aggregationEndTime(daysAgo(1, 23))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(daysAgo(1, 23))
                                            .aggregationEndTime(TODAY)
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(TODAY)
                                            .aggregationEndTime(daysAgo(0, 1))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                            .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                            .simpleFdnExpr("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                            .aggregationBeginTime(daysAgo(0, 1))
                                            .aggregationEndTime(daysAgo(0, 2))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                            .simpleFdnExpr("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 22))
                                            .aggregationEndTime(daysAgo(1, 23))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                            .simpleFdnExpr("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(1, 23))
                                            .aggregationEndTime(TODAY)
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                            .simpleFdnExpr("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(TODAY)
                                            .aggregationEndTime(daysAgo(0, 1))
                                            .build(),
                         SimpleFdnCellHourly.builder()
                                            .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                            .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                            .simpleFdnExpr("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1")
                                            .aggregationBeginTime(daysAgo(0, 1))
                                            .aggregationEndTime(daysAgo(0, 2))
                                            .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
