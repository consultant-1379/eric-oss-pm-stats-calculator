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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.RelationSimpleHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class RelationSimpleHourlyDataset extends AbstractDataset {
    public static final RelationSimpleHourlyDataset INSTANCE = new RelationSimpleHourlyDataset();

    private RelationSimpleHourlyDataset() {
        super("kpi_relation_simple_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("moFdn",
                       "nodeFDN",
                       "sum_long_single_counter",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(4416L)
                                             .aggregationBeginTime(daysAgo(1, 22))
                                             .aggregationEndTime(daysAgo(1, 23))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(4448L)
                                             .aggregationBeginTime(daysAgo(1, 23))
                                             .aggregationEndTime(TODAY)
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(4480L)
                                             .aggregationBeginTime(TODAY)
                                             .aggregationEndTime(daysAgo(0, 1))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(1125L)
                                             .aggregationBeginTime(daysAgo(0, 1))
                                             .aggregationEndTime(daysAgo(0, 2))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(4816L)
                                             .aggregationBeginTime(daysAgo(1, 22))
                                             .aggregationEndTime(daysAgo(1, 23))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(4848L)
                                             .aggregationBeginTime(daysAgo(1, 23))
                                             .aggregationEndTime(TODAY)
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(4880L)
                                             .aggregationBeginTime(TODAY)
                                             .aggregationEndTime(daysAgo(0, 1))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(1225L)
                                             .aggregationBeginTime(daysAgo(0, 1))
                                             .aggregationEndTime(daysAgo(0, 2))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(5216L)
                                             .aggregationBeginTime(daysAgo(1, 22))
                                             .aggregationEndTime(daysAgo(1, 23))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(5248L)
                                             .aggregationBeginTime(daysAgo(1, 23))
                                             .aggregationEndTime(TODAY)
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(5280L)
                                             .aggregationBeginTime(TODAY)
                                             .aggregationEndTime(daysAgo(0, 1))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(1325L)
                                             .aggregationBeginTime(daysAgo(0, 1))
                                             .aggregationEndTime(daysAgo(0, 2))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(8412L)
                                             .aggregationBeginTime(daysAgo(1, 22))
                                             .aggregationEndTime(daysAgo(1, 23))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(8444L)
                                             .aggregationBeginTime(daysAgo(1, 23))
                                             .aggregationEndTime(TODAY)
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(8476L)
                                             .aggregationBeginTime(TODAY)
                                             .aggregationEndTime(daysAgo(0, 1))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(2124L)
                                             .aggregationBeginTime(daysAgo(0, 1))
                                             .aggregationEndTime(daysAgo(0, 2))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(8812L)
                                             .aggregationBeginTime(daysAgo(1, 22))
                                             .aggregationEndTime(daysAgo(1, 23))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(8844L)
                                             .aggregationBeginTime(daysAgo(1, 23))
                                             .aggregationEndTime(TODAY)
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(8876L)
                                             .aggregationBeginTime(TODAY)
                                             .aggregationEndTime(daysAgo(0, 1))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                             .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                             .sumLongSingleCounter(2224L)
                                             .aggregationBeginTime(daysAgo(0, 1))
                                             .aggregationEndTime(daysAgo(0, 2))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                             .sumLongSingleCounter(84416L)
                                             .aggregationBeginTime(daysAgo(1, 22))
                                             .aggregationEndTime(daysAgo(1, 23))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                             .sumLongSingleCounter(84448L)
                                             .aggregationBeginTime(daysAgo(1, 23))
                                             .aggregationEndTime(TODAY)
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                             .sumLongSingleCounter(84480L)
                                             .aggregationBeginTime(TODAY)
                                             .aggregationEndTime(daysAgo(0, 1))
                                             .build(),
                         RelationSimpleHourly.builder()
                                             .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                             .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                             .sumLongSingleCounter(21125L)
                                             .aggregationBeginTime(daysAgo(0, 1))
                                             .aggregationEndTime(daysAgo(0, 2))
                                             .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
