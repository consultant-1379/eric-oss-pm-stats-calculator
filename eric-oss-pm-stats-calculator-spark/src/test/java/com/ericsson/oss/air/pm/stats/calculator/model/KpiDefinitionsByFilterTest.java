/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

class KpiDefinitionsByFilterTest {
    @Test
    void newInstanceWithKpiDefinitionFilterHelper() {
        final KpiDefinitionsByFilter objectUnderTest = KpiDefinitionsByFilter.newInstance();
        Assertions.assertThat(objectUnderTest).isNotNull();
    }

    KpiDefinition kpiDefinition(final Filter... filters) {
        return KpiDefinition.builder().withFilter(asList(filters)).build();
    }
    @Test
    void shouldAddKpiDefinitionsToDistinctFilters() {

        final Filter filter1 = Filter.of("kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'");
        final Filter filter2 = Filter.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2023-08-13'");
        final Filter filter3 = Filter.of(
            "kpi_db://kpi_simple_60.aggregation_begin_time BETWEEN (" +
                "date_trunc('hour', TO_TIMESTAMP('${param.start_date_time}')) - interval 1 day) " +
                "and " +
                "date_trunc('hour', TO_TIMESTAMP('${param.end_date_time}')" +
                ')'
        );

        final KpiDefinition kpiDefinition1 = kpiDefinition(filter1, filter2);
        final KpiDefinition kpiDefinition2 = kpiDefinition(filter1, filter2);
        final KpiDefinition kpiDefinition3 = kpiDefinition(filter2, filter1);
        final KpiDefinition kpiDefinition4 = kpiDefinition(filter2, filter1, filter3);
        final KpiDefinition kpiDefinition5 = kpiDefinition(filter3);

        List<KpiDefinition> kpiDefinitions = List.of(kpiDefinition1, kpiDefinition2,
            kpiDefinition3, kpiDefinition4, kpiDefinition5);

        List<Filter> filters = List.of(filter1, filter2, filter3);

        Set<KpiDefinition> kpiDefinitionSet = new HashSet<>(kpiDefinitions);
        Set<Filter> filterSet = new HashSet<>(filters);

        final KpiDefinitionsByFilter objectUnderTest = KpiDefinitionsByFilter.newInstance();

        objectUnderTest.addKpiDefinitions(filterSet, kpiDefinitionSet);

        assertThat(objectUnderTest.get(filterSet)).isEqualTo(kpiDefinitionSet);
    }
}
