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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.OnDemandFdnHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class OnDemandFdnHourlyDataset extends AbstractDataset {
    public static final OnDemandFdnHourlyDataset INSTANCE = new OnDemandFdnHourlyDataset();

    private OnDemandFdnHourlyDataset() {
        super("kpi_on_demand_fdn_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("moFdn",
                       "nodeFDN",
                       "on_demand_fdn_filter",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(8412L)
                                          .aggregationBeginTime(daysAgo(1, 22))
                                          .aggregationEndTime(daysAgo(1, 23))
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(8444L)
                                          .aggregationBeginTime(daysAgo(1, 23))
                                          .aggregationEndTime(TODAY)
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(8476L)
                                          .aggregationBeginTime(TODAY)
                                          .aggregationEndTime(daysAgo(0, 1))
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(2124L)
                                          .aggregationBeginTime(daysAgo(0, 1))
                                          .aggregationEndTime(daysAgo(0, 2))
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(8812L)
                                          .aggregationBeginTime(daysAgo(1, 22))
                                          .aggregationEndTime(daysAgo(1, 23))
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(8844L)
                                          .aggregationBeginTime(daysAgo(1, 23))
                                          .aggregationEndTime(TODAY)
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(8876L)
                                          .aggregationBeginTime(TODAY)
                                          .aggregationEndTime(daysAgo(0, 1))
                                          .build(),
                         OnDemandFdnHourly.builder()
                                          .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                          .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                          .onDemandFdnFilter(2224L)
                                          .aggregationBeginTime(daysAgo(0, 1))
                                          .aggregationEndTime(daysAgo(0, 2))
                                          .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
