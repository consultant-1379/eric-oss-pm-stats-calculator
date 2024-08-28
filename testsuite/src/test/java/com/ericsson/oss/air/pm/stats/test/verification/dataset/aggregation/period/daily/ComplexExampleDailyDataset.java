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

import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.A_DAY_AGO;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TODAY;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TWO_DAYS_AGO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.ComplexExampleDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

public final class ComplexExampleDailyDataset extends AbstractDataset {
    public static final ComplexExampleDailyDataset INSTANCE = new ComplexExampleDailyDataset();

    private ComplexExampleDailyDataset() {
        super("kpi_complex_example_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return List.of("nodeFDN",
                       "moFdn",
                       "aggregation_begin_time",
                       "aggregation_end_time",
                       "max_1440_complex");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(ComplexExampleDaily.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(3F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(3.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(2.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node1")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(3.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(2.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node2")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(3.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(3.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo1")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(4.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo2")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(2.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(TWO_DAYS_AGO)
                                            .aggregationEndTime(A_DAY_AGO)
                                            .max1440Complex(4.5F)
                                            .build(),
                         ComplexExampleDaily.builder()
                                            .nodeFDN("node3")
                                            .moFdn("mo3")
                                            .aggregationBeginTime(A_DAY_AGO)
                                            .aggregationEndTime(TODAY)
                                            .max1440Complex(3.5F)
                                            .build()
                        )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}