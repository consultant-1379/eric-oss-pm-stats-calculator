/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Parameter;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ParameterRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnDemandParameterCastUtilsTest {

    @Mock ParameterRepository parameterRepositoryMock;

    @InjectMocks OnDemandParameterCastUtils objectUnderTest;

    @Test
    void applyParametersToKpiDefinitions_EmptyParameter() {
        final Collection<KpiDefinition> kpiDefinitions = testDefinitions();

        final Set<KpiDefinition> result = objectUnderTest.applyParametersToKpiDefinitions(Collections.emptyMap(), kpiDefinitions);

        assertThat(result).hasSize(2).containsExactlyElementsOf(kpiDefinitions);
    }

    @Test
    void shouldFailWithoutAllParametersDeclared() {
        final Collection<KpiDefinition> kpiDefinitions = testDefinitions();

        final Map<String, String> parameters = Map.of("param1", "23456");

        assertThatThrownBy(() -> objectUnderTest.applyParametersToKpiDefinitions(parameters, kpiDefinitions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All parameter keys must be included");
    }

    @Test
    void applyParametersToKpiDefinitions() {

        when(parameterRepositoryMock.findByNameIn(Set.of("param1", "param2", "param.thirdParam", "param4", "param5")))
                .thenReturn(List.of(
                        parameter("param1", "DOUBLE"),
                        parameter("param2", "STRING"),
                        parameter("param.thirdParam", "STRING"),
                        parameter("param4", "LONG"),
                        parameter("param5", "INTEGER")
                                   ));

        final Map<String, String> parameters = Map.of("param1", "23456",
                                                      "param2", "test_string",
                                                      "param.thirdParam", "testparam2",
                                                      "param4", "2000",
                                                      "param5", "1000"
                                                     );

        final Set<KpiDefinition> actual = objectUnderTest.applyParametersToKpiDefinitions(parameters, testDefinitions());

        final Set<KpiDefinition> expected = Set.of(
                KpiDefinition.builder()
                             .withName("kpi1")
                             .withExpression("SUM(kpi_fact_table_0.integerColumn0) + CAST('23456' AS DOUBLE) FROM kpi_db://kpi_fact_table_0")
                             .withAggregationPeriod("1440")
                             .withAggregationElements(List.of("kpi_fact_table_0.agg_column_0 AS 'test_string'", "kpi_fact_table_0.agg_column AS agg_column_1"))
                             .build(),
                KpiDefinition.builder()
                             .withName("kpi2")
                             .withExpression("SUM(kpi_simple_60.integer_simple + CAST('1000' AS INT)) FROM kpi_db://kpi_simple_60")
                             .withAggregationPeriod("60")
                             .withAggregationElements(List.of("kpi_simple_60.agg_column_0 AS 'testparam2'", "kpi_simple_60.nodeFDN AS nodeFDN"))
                             .withFilter(List.of(new Filter("kpi_db://kpi_simple_60.kpi > CAST('2000' AS LONG)")))
                             .build()
                                                  );

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Collection<KpiDefinition> testDefinitions() {
        final Set<KpiDefinition> kpiDefinitions = new HashSet<>();
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder()
                                                          .withName("kpi1")
                                                          .withExpression("SUM(kpi_fact_table_0.integerColumn0) + '${param1}' FROM kpi_db://kpi_fact_table_0")
                                                          .withAggregationPeriod("1440")
                                                          .withAggregationElements(List.of("kpi_fact_table_0.agg_column_0 AS '${param2}'", "kpi_fact_table_0.agg_column AS agg_column_1"))
                                                          .build();

        final KpiDefinition kpiDefinition2 = KpiDefinition.builder()
                                                          .withName("kpi2")
                                                          .withExpression("SUM(kpi_simple_60.integer_simple + '${param5}') FROM kpi_db://kpi_simple_60")
                                                          .withAggregationPeriod("60")
                                                          .withAggregationElements(List.of("kpi_simple_60.agg_column_0 AS '${param.thirdParam}'", "kpi_simple_60.nodeFDN AS nodeFDN"))
                                                          .withFilter(List.of(new Filter("kpi_db://kpi_simple_60.kpi > '${param4}'")))
                                                          .build();

        kpiDefinitions.add(kpiDefinition1);
        kpiDefinitions.add(kpiDefinition2);

        return kpiDefinitions;
    }

    static Parameter parameter(final String name, final String type) {
        return Parameter.builder()
                        .name(name)
                        .type(type)
                        .build();
    }

}