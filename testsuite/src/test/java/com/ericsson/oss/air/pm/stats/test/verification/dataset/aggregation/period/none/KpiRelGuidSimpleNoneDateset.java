/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.none;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.none.KpiRelGuidSimpleNone;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

/**
 * {@link AbstractDataset} implementation for the {@link KpiRelGuidSimpleNone} domain.
 */
public final class KpiRelGuidSimpleNoneDateset extends AbstractDataset {
    public static final KpiRelGuidSimpleNoneDateset INSTANCE = new KpiRelGuidSimpleNoneDateset();

    public KpiRelGuidSimpleNoneDateset() {
        super("kpi_rel_guid_s_guid_t_guid_simple_");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("nodeFDN",
                "moFdn",
                "sum_integer_simple");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node1")
                                .moFdn("mo1")
                                .sumIntegerSimple(33)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node1")
                                .moFdn("mo2")
                                .sumIntegerSimple(21)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node1")
                                .moFdn("mo3")
                                .sumIntegerSimple(27)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node2")
                                .moFdn("mo1")
                                .sumIntegerSimple(16)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node2")
                                .moFdn("mo2")
                                .sumIntegerSimple(26)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node2")
                                .moFdn("mo3")
                                .sumIntegerSimple(31)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node3")
                                .moFdn("mo1")
                                .sumIntegerSimple(20)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node3")
                                .moFdn("mo2")
                                .sumIntegerSimple(19)
                                .build(),
                        KpiRelGuidSimpleNone.builder()
                                .nodeFDN("node3")
                                .moFdn("mo3")
                                .sumIntegerSimple(24)
                                .build()
                )
                .map(DatabaseRow::convertToRow)
                .collect(Collectors.toList());
    }

}
