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
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.SimpleExampleHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class SimpleExampleHourlyDataset extends AbstractDataset {
    public static final SimpleExampleHourlyDataset INSTANCE = new SimpleExampleHourlyDataset();

    private SimpleExampleHourlyDataset() {
        super("kpi_simple_example_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("nodeFDN",
                       "moFdn",
                       "aggregation_begin_time",
                       "aggregation_end_time",
                       "sum_float_simple",
                       "sum_float_simple_hourly",
                       "max_float_simple_hourly",
                       "sum_array_simple");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(8F)
                                            .sumFloatSimpleDaily(8F)
                                            .maxFloatSimple(3F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(6.5F)
                                            .sumFloatSimpleDaily(6.5F)
                                            .maxFloatSimple(3F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(9F)
                                            .sumFloatSimpleDaily(9F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(3F)
                                            .sumFloatSimpleDaily(3F)
                                            .maxFloatSimple(3F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(9.5F)
                                            .sumFloatSimpleDaily(9.5F)
                                            .maxFloatSimple(3F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(10.5F)
                                            .sumFloatSimpleDaily(10.5F)
                                            .maxFloatSimple(4.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(8F)
                                            .sumFloatSimpleDaily(8F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(3.5F)
                                            .sumFloatSimpleDaily(3.5F)
                                            .maxFloatSimple(3.5F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(11.5F)
                                            .sumFloatSimpleDaily(11.5F)
                                            .maxFloatSimple(4.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(6.5F)
                                            .sumFloatSimpleDaily(6.5F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(15F)
                                            .sumFloatSimpleDaily(15F)
                                            .maxFloatSimple(5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(4F)
                                            .sumFloatSimpleDaily(4F)
                                            .maxFloatSimple(4F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(14.5F)
                                            .sumFloatSimpleDaily(14.5F)
                                            .maxFloatSimple(4.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(8F)
                                            .sumFloatSimpleDaily(8F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(11F)
                                            .sumFloatSimpleDaily(11F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(3F)
                                            .sumFloatSimpleDaily(3F)
                                            .maxFloatSimple(3F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(8F)
                                            .sumFloatSimpleDaily(8F)
                                            .maxFloatSimple(4F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(9F)
                                            .sumFloatSimpleDaily(9F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(7F)
                                            .sumFloatSimpleDaily(7F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(2.5F)
                                            .sumFloatSimpleDaily(2.5F)
                                            .maxFloatSimple(2.5F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(10.5F)
                                            .sumFloatSimpleDaily(10.5F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(10F)
                                            .sumFloatSimpleDaily(10F)
                                            .maxFloatSimple(4F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(9F)
                                            .sumFloatSimpleDaily(9F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(2.5F)
                                            .sumFloatSimpleDaily(2.5F)
                                            .maxFloatSimple(2.5F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(10.5F)
                                            .sumFloatSimpleDaily(10.5F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(7.5F)
                                            .sumFloatSimpleDaily(7.5F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(13F)
                                            .sumFloatSimpleDaily(13F)
                                            .maxFloatSimple(4.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(3F)
                                            .sumFloatSimpleDaily(3F)
                                            .maxFloatSimple(3F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(16F)
                                            .sumFloatSimpleDaily(16F)
                                            .maxFloatSimple(4.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(7.5F)
                                            .sumFloatSimpleDaily(7.5F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(8F)
                                            .sumFloatSimpleDaily(8F)
                                            .maxFloatSimple(2.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(1.5F)
                                            .sumFloatSimpleDaily(1.5F)
                                            .maxFloatSimple(1.5F)
                                            .sumArraySimple(15)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(2, 22))
                                            .aggregationEndTime(daysAgo(2, 23))
                                            .sumFloatSimple(12F)
                                            .sumFloatSimpleDaily(12F)
                                            .maxFloatSimple(4.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(2, 23))
                                            .aggregationEndTime(A_DAY_AGO)
                                            .sumFloatSimple(5.5F)
                                            .sumFloatSimpleDaily(5.5F)
                                            .maxFloatSimple(2F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(daysAgo(1, 1))
                                            .sumFloatSimple(9.5F)
                                            .sumFloatSimpleDaily(9.5F)
                                            .maxFloatSimple(3.5F)
                                            .build(),
                         SimpleExampleHourly.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(daysAgo(1, 1))
                                            .aggregationEndTime(daysAgo(1, 2))
                                            .sumFloatSimple(3.5F)
                                            .sumFloatSimpleDaily(3.5F)
                                            .maxFloatSimple(3.5F)
                                            .sumArraySimple(15)
                                            .build()
                        )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}