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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.PredefinedSimpleHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;

public final class PredefinedSimpleHourlyDataset extends AbstractDataset {
    public static final PredefinedSimpleHourlyDataset INSTANCE = new PredefinedSimpleHourlyDataset();

    private PredefinedSimpleHourlyDataset() {
        super("kpi_predefined_simple_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("moFdn",
                       "nodeFDN",
                       "array_sum_simple",
                       "relation_level_agg",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 1, 20, 2, 3, 60))
                                               .relationLevelAgg(4416L)
                                               .aggregationBeginTime(daysAgo(1, 22))
                                               .aggregationEndTime(daysAgo(1, 23))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 12, 20, 0, 0, 60))
                                               .relationLevelAgg(4448L)
                                               .aggregationBeginTime(daysAgo(1, 23))
                                               .aggregationEndTime(TODAY)
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 24, 24, 60))
                                               .relationLevelAgg(4480L)
                                               .aggregationBeginTime(TODAY)
                                               .aggregationEndTime(daysAgo(0, 1))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(2, 10, 15, 0, 5, 0, 0, 15))
                                               .relationLevelAgg(1125L)
                                               .aggregationBeginTime(daysAgo(0, 1))
                                               .aggregationEndTime(daysAgo(0, 2))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(12, 40, 60, 1, 20, 2, 3, 60))
                                               .relationLevelAgg(4816L)
                                               .aggregationBeginTime(daysAgo(1, 22))
                                               .aggregationEndTime(daysAgo(1, 23))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 16, 20, 0, 0, 60))
                                               .relationLevelAgg(4848L)
                                               .aggregationBeginTime(daysAgo(1, 23))
                                               .aggregationEndTime(TODAY)
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 28, 28, 60))
                                               .relationLevelAgg(4880L)
                                               .aggregationBeginTime(TODAY)
                                               .aggregationEndTime(daysAgo(0, 1))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(3, 10, 15, 0, 5, 0, 0, 15))
                                               .relationLevelAgg(1225L)
                                               .aggregationBeginTime(daysAgo(0, 1))
                                               .aggregationEndTime(daysAgo(0, 2))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(16, 40, 60, 2, 20, 3, 4, 60))
                                               .relationLevelAgg(5216L)
                                               .aggregationBeginTime(daysAgo(1, 22))
                                               .aggregationEndTime(daysAgo(1, 23))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 20, 20, 0, 0, 60))
                                               .relationLevelAgg(5248L)
                                               .aggregationBeginTime(daysAgo(1, 23))
                                               .aggregationEndTime(TODAY)
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 32, 32, 60))
                                               .relationLevelAgg(5280L)
                                               .aggregationBeginTime(TODAY)
                                               .aggregationEndTime(daysAgo(0, 1))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(4, 10, 15, 0, 5, 0, 0, 15))
                                               .relationLevelAgg(1325L)
                                               .aggregationBeginTime(daysAgo(0, 1))
                                               .aggregationEndTime(daysAgo(0, 2))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(20, 40, 60, 2, 20, 3, 4, 60))
                                               .relationLevelAgg(8412L)
                                               .aggregationBeginTime(daysAgo(1, 22))
                                               .aggregationEndTime(daysAgo(1, 23))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 24, 20, 0, 0, 60))
                                               .relationLevelAgg(8444L)
                                               .aggregationBeginTime(daysAgo(1, 23))
                                               .aggregationEndTime(TODAY)
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 36, 36, 60))
                                               .relationLevelAgg(8476L)
                                               .aggregationBeginTime(TODAY)
                                               .aggregationEndTime(daysAgo(0, 1))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(5, 10, 15, 0, 5, 0, 0, 15))
                                               .relationLevelAgg(2124L)
                                               .aggregationBeginTime(daysAgo(0, 1))
                                               .aggregationEndTime(daysAgo(0, 2))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(24, 40, 60, 3, 20, 4, 5, 60))
                                               .relationLevelAgg(8812L)
                                               .aggregationBeginTime(daysAgo(1, 22))
                                               .aggregationEndTime(daysAgo(1, 23))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 28, 20, 0, 0, 60))
                                               .relationLevelAgg(8844L)
                                               .aggregationBeginTime(daysAgo(1, 23))
                                               .aggregationEndTime(TODAY)
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 40, 40, 60))
                                               .relationLevelAgg(8876L)
                                               .aggregationBeginTime(TODAY)
                                               .aggregationEndTime(daysAgo(0, 1))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                               .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                               .arraySumSimple(JsonArrayUtils.create(6, 10, 15, 0, 5, 0, 0, 15))
                                               .relationLevelAgg(2224L)
                                               .aggregationBeginTime(daysAgo(0, 1))
                                               .aggregationEndTime(daysAgo(0, 2))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 0, 0, 60))
                                               .relationLevelAgg(84416L)
                                               .aggregationBeginTime(daysAgo(1, 22))
                                               .aggregationEndTime(daysAgo(1, 23))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 0, 0, 60))
                                               .relationLevelAgg(84448L)
                                               .aggregationBeginTime(daysAgo(1, 23))
                                               .aggregationEndTime(TODAY)
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                               .arraySumSimple(JsonArrayUtils.create(8, 40, 60, 0, 20, 0, 0, 60))
                                               .relationLevelAgg(84480L)
                                               .aggregationBeginTime(TODAY)
                                               .aggregationEndTime(daysAgo(0, 1))
                                               .build(),
                         PredefinedSimpleHourly.builder()
                                               .moFdn("ManagedElement=LTE02dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                               .nodeFDN("SubNetwork=SN2,MeContext=LTE02dg2ERBS00001,ManagedElement=LTE02")
                                               .arraySumSimple(JsonArrayUtils.create(2, 10, 15, 0, 5, 0, 0, 15))
                                               .relationLevelAgg(21125L)
                                               .aggregationBeginTime(daysAgo(0, 1))
                                               .aggregationEndTime(daysAgo(0, 2))
                                               .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
