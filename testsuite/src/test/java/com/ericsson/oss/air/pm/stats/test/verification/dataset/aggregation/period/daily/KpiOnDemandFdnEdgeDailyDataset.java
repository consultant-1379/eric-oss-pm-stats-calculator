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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.KpiOnDemandFdnEdgeDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiOnDemandFdnEdgeDaily} domain.
 */
public class KpiOnDemandFdnEdgeDailyDataset extends AbstractDataset {
    public static final KpiOnDemandFdnEdgeDailyDataset INSTANCE = new KpiOnDemandFdnEdgeDailyDataset();

    private KpiOnDemandFdnEdgeDailyDataset() {
        super("kpi_ondemand_fdn_edge_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("moFdn",
                       "nodeFDN",
                       "fdn_concat_edge",
                       "aggregation_begin_time",
                       "aggregation_end_time");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                                .aggregationBeginTime(TimeUtils.TODAY)
                                                .aggregationEndTime(TimeUtils.daysAgo(-1))
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                                .aggregationBeginTime(TimeUtils.TODAY)
                                                .aggregationEndTime(TimeUtils.daysAgo(-1))
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1,SampleCellRelation=3")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=1")
                                                .aggregationBeginTime(TimeUtils.TODAY)
                                                .aggregationEndTime(TimeUtils.daysAgo(-1))
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=1")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                                .aggregationBeginTime(TimeUtils.TODAY)
                                                .aggregationEndTime(TimeUtils.daysAgo(-1))
                                                .build(),
                         KpiOnDemandFdnEdgeDaily.builder()
                                                .moFdn("ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2,SampleCellRelation=2")
                                                .nodeFDN("SubNetwork=SN1,MeContext=LTE01dg2ERBS00001,ManagedElement=LTE01")
                                                .fdnConcatEdge("SubNetwork=SN1,ManagedElement=LTE01dg2ERBS00001,SampleCellFDD=2")
                                                .aggregationBeginTime(TimeUtils.TODAY)
                                                .aggregationEndTime(TimeUtils.daysAgo(-1))
                                                .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
