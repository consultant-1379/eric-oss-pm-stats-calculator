/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionFileRetriever;

import org.junit.jupiter.api.Test;

class KpiDefinitionRetrievalTestUtilsTest {
    static final List<KpiDefinition> KPI_DEFINITIONS = new ArrayList<>(KpiDefinitionRetrievalTestUtils.retrieveKpiDefinitions(new KpiDefinitionFileRetriever()));

    @Test
    void whenRetrievingKpiDefinitionsFromFile_thenDoubleSingleQuotesAreRemoved() {
        for (final KpiDefinition kpiDefinition : KPI_DEFINITIONS) {
            for (final String aggregationElement : kpiDefinition.getAggregationElements()) {
                assertThat(aggregationElement.contains("''")).isFalse();
            }
        }
    }
}
