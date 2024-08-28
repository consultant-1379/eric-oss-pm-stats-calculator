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

import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.A_DAY_AGO;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.daysAgo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.ExampleTable;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class ExampleTableDataset extends AbstractDataset {
    public static final ExampleTableDataset INSTANCE = new ExampleTableDataset();

    private ExampleTableDataset() {
        super("kpi_example_table_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("nodeFDN",
                       "moFdn",
                       "aggregation_begin_time",
                       "aggregation_end_time",
                       "kpi_sum1",
                       "kpi_sum2",
                       "kpi_div");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node1")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node2")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo1")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo2")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(2, 22))
                                     .aggregationEndTime(daysAgo(2, 23))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(2, 23))
                                     .aggregationEndTime(A_DAY_AGO)
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(A_DAY_AGO)
                                     .aggregationEndTime(daysAgo(1, 1))
                                     .kpiSum1(4L)
                                     .kpiSum2(4L)
                                     .kpiDiv(1F)
                                     .build(),
                         ExampleTable.builder()
                                     .nodeFDN("node3")
                                     .moFdn("mo3")
                                     .aggregationBeginTime(daysAgo(1, 1))
                                     .aggregationEndTime(daysAgo(1, 2))
                                     .kpiSum1(1L)
                                     .kpiSum2(1L)
                                     .build()
                        )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}