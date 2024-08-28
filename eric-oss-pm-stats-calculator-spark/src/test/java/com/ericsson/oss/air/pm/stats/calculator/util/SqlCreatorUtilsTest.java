/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlCreatorUtilsTest {

    @Test
    void shouldFilterByKpiName() {
        final String kpiNonRequired1 = "kpi_non_required_1";
        final String kpiNonRequired2 = "kpi_non_required_2";

        final KpiDefinition kpiDefinition1 = KpiDefinition.builder().withName(kpiNonRequired1).build();
        final KpiDefinition kpiDefinition2 = KpiDefinition.builder().withName("kpi_required").build();
        final KpiDefinition kpiDefinition3 = KpiDefinition.builder().withName(kpiNonRequired2).build();

        final List<KpiDefinition> kpiDefinitions = Arrays.asList(kpiDefinition1, kpiDefinition2, kpiDefinition3);

        final Set<KpiDefinition> actual = SqlCreatorUtils.filterKpiByName(kpiDefinitions, Arrays.asList(kpiNonRequired1, kpiNonRequired2));

        Assertions.assertThat(actual).containsExactly(kpiDefinition2);
    }
}