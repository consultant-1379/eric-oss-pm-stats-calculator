/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.sqlProcessorDelegator;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

import org.apache.spark.sql.execution.SparkSqlParser;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AggregationSqlCreator}.
 */
class AggregationSqlCreatorTest {

    @Test
    void whenCreateAggregationSqlQuery_givenKpiListWithAggregationElements_thenOutputSqlHasCorrectFormat() {
        final KpiDefinition kpi = KpiDefinition.builder()
                .withName("test_kpi")
                .withAlias("cell_guid")
                .withExpression("(counters_cell.counter1 * counters_cell.counter2) / counters_cell.counter3 FROM pm_stats://counters_cell")
                .withAggregationType("SUM")
                .withAggregationPeriod("60")
                .withAggregationElements(Collections.singletonList("guid"))
                .build();

        final Set<KpiDefinition> kpiDefinitionList = new HashSet<>();
        kpiDefinitionList.add(kpi);

        final List<String> aggregationElements = new ArrayList<>(Arrays.asList("guid", "source_guid", "target_guid"));

        final AggregationSqlCreator aggCreator = new AggregationSqlCreator(sqlProcessorDelegator(new SparkSqlParser()), 1440);
        final String result = aggCreator.createAggregationSqlQuery(kpiDefinitionList, "cell_sector", aggregationElements);
        assertThat(result).isEqualTo("SELECT guid, source_guid, target_guid, "
                + "MAX(aggregation_begin_time) AS aggregation_begin_time, "
                + "SUM(test_kpi) AS test_kpi "
                + "FROM cell_sector_kpis "
                + "GROUP BY guid, source_guid, target_guid, aggregation_begin_time");
    }
}