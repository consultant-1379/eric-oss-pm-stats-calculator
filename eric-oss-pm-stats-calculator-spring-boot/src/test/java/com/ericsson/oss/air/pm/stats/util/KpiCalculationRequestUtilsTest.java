/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.util;

import static com.ericsson.oss.air.pm.stats.util.KpiCalculationRequestUtils.getNamesOfKpisWithUnresolvedParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.Parameter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KpiCalculationRequestUtilsTest {
    static final String AGGREGATION_PERIOD = "1440";
    static final String SOURCE = "TEST";
    static final String TEST_KPI_1_NAME = "TEST_KPI_1_NAME";
    static final String TEST_KPI_2_NAME = "TEST_KPI_2_NAME";
    static final String TEST_KPI_3_NAME = "TEST_KPI_3_NAME";
    static final String TEST_KPI_4_NAME = "TEST_KPI_4_NAME";

    @Nested
    class CheckKpiNamesEmptiness {

        @Test
        void whenValidKpiNamesProvidedInKpiCalculationRequest_thenIsIdentifiedDuringValidation() {
            assertFalse(KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(requestBuilder(Set.of(TEST_KPI_1_NAME, TEST_KPI_2_NAME))));
        }

        @Test
        void whenAnEmptyKpiNameIsProvidedInKpiCalculationRequest_thenIsIdentifiedDuringValidation() {
            assertTrue(KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(requestBuilder(Set.of(TEST_KPI_1_NAME, StringUtils.EMPTY))));
        }

        @Test
        void whenAnEmptyKpiNameIsProvidedInKpiCalculationRequest_thenIsIdentifiedDuringValidation2() {
            assertTrue(KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(requestBuilder(Set.of(TEST_KPI_1_NAME, ""))));
        }

        @Test
        void whenAnEmptySetIsProvidedInKpiCalculationRequest_thenIsIdentifiedDuringValidation2() {
            assertTrue(KpiCalculationRequestUtils.payloadContainsEmptyKpiNames(requestBuilder(Collections.emptySet())));
        }
    }

    @Nested
    class CheckMissingAttributes {

        @Test
        void whenKpiCalculationPayloadIsMissingSourceAttribute_thenIsIdentifiedDuringValidation() {
            final KpiCalculationRequestPayload payload = requestBuilderWithSource("", Set.of(TEST_KPI_1_NAME, TEST_KPI_2_NAME));

            assertTrue(KpiCalculationRequestUtils.payloadContainsMissingAttributes(payload));
        }

        @Test
        void whenKpiCalculationPayloadIsMissingNamesAttribute_thenIsIdentifiedDuringValidation() {
            final KpiCalculationRequestPayload payload = requestBuilderWithSource("test_source", Collections.emptySet());

            assertTrue(KpiCalculationRequestUtils.payloadContainsMissingAttributes(payload));
        }

        @Test
        void whenKpiCalculationPayloadIsValid_thenIsIdentifiedDuringValidation() {
            assertFalse(KpiCalculationRequestUtils.payloadContainsMissingAttributes(requestBuilder(Set.of(TEST_KPI_1_NAME, TEST_KPI_2_NAME))));
        }

    }

    @Nested
    class ApplyParameters {

        @Test
        void whenAllParameterTokensAreProvidedForKpiDefinition_thenAreAppliedCorrectly() {
            final Set<KpiDefinition> kpiDefinitions = getParameterizedKpiDefinitions();
            final Map<String, String> parameters = getParameters();

            final Set<Definition> parameterDecoratedDefinitions = KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(parameters,
                    kpiDefinitions);

            final Set<String> namesOfKpisWithUnresolvedParameters = getNamesOfKpisWithUnresolvedParameters(parameterDecoratedDefinitions);

            assertThat(namesOfKpisWithUnresolvedParameters).hasSize(0);
        }

        @Test
        void whenAParameterTokenIsNotProvided_thenTokenIsLeftInDefinitionAfterSubstitution() {
            final Set<KpiDefinition> kpiDefinitions = getParameterizedKpiDefinitions();
            final Map<String, String> parameters = getParameters();
            parameters.remove("params.filter");

            final Set<Definition> parameterDecoratedDefinitions = KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(parameters,
                    kpiDefinitions);

            final Set<String> namesOfKpisWithUnresolvedParameters = getNamesOfKpisWithUnresolvedParameters(parameterDecoratedDefinitions);

            assertThat(namesOfKpisWithUnresolvedParameters).hasSize(2);
            assertThat(namesOfKpisWithUnresolvedParameters).contains(TEST_KPI_1_NAME);
            assertThat(namesOfKpisWithUnresolvedParameters).contains(TEST_KPI_3_NAME);
        }

        @Test
        void whenKpiDefinitionsContainParameterTokens_andNullParameterProvided_thenTokensAreLeftInDefinitionAfterSubstitution() {
            final Set<KpiDefinition> kpiDefinitions = getParameterizedKpiDefinitions();

            final Set<Definition> parameterDecoratedDefinitions = KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(null, kpiDefinitions);

            final Set<String> namesOfKpisWithUnresolvedParameters = getNamesOfKpisWithUnresolvedParameters(parameterDecoratedDefinitions);

            assertThat(namesOfKpisWithUnresolvedParameters).hasSize(kpiDefinitions.size());
        }

        @Test
        void whenKpiDefinitionsContainParameterTokens_andEmptyParameterProvided_thenTokensAreLeftInDefinitionAfterSubstitution() {
            final Set<KpiDefinition> kpiDefinitions = getParameterizedKpiDefinitions();

            final Set<Definition> parameterDecoratedDefinitions = KpiCalculationRequestUtils.applyParameterValuesToKpiDefinitions(Collections.emptyMap(), kpiDefinitions);

            final Set<String> namesOfKpisWithUnresolvedParameters = getNamesOfKpisWithUnresolvedParameters(parameterDecoratedDefinitions);

            assertThat(namesOfKpisWithUnresolvedParameters).hasSize(kpiDefinitions.size());
        }
    }

    @Test
    void whenMatchingKpiCalculationRequestsToKpiDefinitions_thenCorrectNumberOfKpiDefinitionsAreReturned() {
        final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of(TEST_KPI_1_NAME, TEST_KPI_2_NAME));
        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitions();

        final List<KpiDefinition> matchedKpiDefinitions = KpiCalculationRequestUtils.getRequestedKpiDefinitionsFromExistingKpiDefinitions(
                kpiCalculationRequestPayload, kpiDefinitions);

        assertThat(matchedKpiDefinitions).hasSize(2);

        final List<KpiDefinition> reducedKpiDefinitions = getKpiDefinitions2();

        final List<KpiDefinition> matchedReducedKpiDefinitions = KpiCalculationRequestUtils.getRequestedKpiDefinitionsFromExistingKpiDefinitions(
                kpiCalculationRequestPayload, reducedKpiDefinitions);

        assertThat(matchedReducedKpiDefinitions).hasSize(1);
    }

    @Test
    void whenMatchingKpiCalculationRequestsToKpiDefinitions_andAllMatch_thenNoKpiCalculationRequestsReturned() {
        final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of(TEST_KPI_1_NAME, TEST_KPI_2_NAME));
        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitions();

        final Set<String> kpiCalculationRequestsNotMatching = KpiCalculationRequestUtils.checkThatKpisRequestedForCalculationExist(
                kpiCalculationRequestPayload.getKpiNames(), kpiDefinitions);

        assertThat(kpiCalculationRequestsNotMatching).isEmpty();
    }

    @Test
    void whenNoMatchBetweenKpiCalculationRequestsAndKpiDefinitions_thenNoKpiDefinitionsAreReturned() {
        final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of(TEST_KPI_1_NAME, TEST_KPI_2_NAME));

        final List<KpiDefinition> matchedKpiDefinitions = KpiCalculationRequestUtils.getRequestedKpiDefinitionsFromExistingKpiDefinitions(
                kpiCalculationRequestPayload, new HashSet<>());

        assertThat(matchedKpiDefinitions).isEmpty();
    }

    @Test
    void whenMatchingKpiCalculationRequestsToKpiDefinitions_andNotAllMatch_thenCorrectNumberOfKpiCalculationRequestsReturned() {
        final KpiCalculationRequestPayload kpiCalculationRequestPayload = requestBuilder(Set.of("NOT_FOUND", TEST_KPI_2_NAME));
        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitions();

        final Set<String> kpiCalculationRequestsNotMatching = KpiCalculationRequestUtils.checkThatKpisRequestedForCalculationExist(
                kpiCalculationRequestPayload.getKpiNames(), kpiDefinitions);

        assertThat(kpiCalculationRequestsNotMatching).hasSize(1);
    }

    @Test
    void whenGettingNamesOfKpisWithUnresolvedParameterTokens_thenCorrectNamesAreReturned() {
        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitions();
        final KpiDefinition kpiDefinitionWithUnresolvedParameterToken = KpiDefinition.builder()
                .withName("unresolved_parameter_kpi")
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withExpression("${token}")
                .withFilter(new ArrayList<>())
                .build();
        kpiDefinitions.add(kpiDefinitionWithUnresolvedParameterToken);

        final Set<Definition> rawDefinitions = KpiDefinitionUtils.convertKpiDefinitionsToRawDefinitionSet(kpiDefinitions);
        final Set<String> namesOfKpisWithUnresolvedParameters = getNamesOfKpisWithUnresolvedParameters(rawDefinitions);

        assertThat(namesOfKpisWithUnresolvedParameters).hasSize(1);
        assertThat(namesOfKpisWithUnresolvedParameters).contains("unresolved_parameter_kpi");
    }

    @Test
    void whenGettingNamesOfKpisWithUnresolvedParameterTokensAndNoParametersArePresent_thenEmptySetOfNamesReturned() {
        final Set<KpiDefinition> kpiDefinitions = getKpiDefinitions();

        final Set<Definition> rawDefinitions = KpiDefinitionUtils.convertKpiDefinitionsToRawDefinitionSet(kpiDefinitions);
        final Set<String> namesOfKpisWithUnresolvedParameters = getNamesOfKpisWithUnresolvedParameters(rawDefinitions);

        assertThat(namesOfKpisWithUnresolvedParameters).isEmpty();
    }

    KpiCalculationRequestPayload requestBuilder(final Set<String> kpiNames) {
        return requestBuilderWithSource(SOURCE, kpiNames);
    }

    KpiCalculationRequestPayload requestBuilderWithSource(final String source, final Set<String> kpiNames) {
        Parameter parameter = Parameter.builder().name("params.expression_param").value("WHERE column = value").build();
        Parameter parameter2 = Parameter.builder().name("params.filter_param").value("datasource://table.column > value").build();

        return KpiCalculationRequestPayload.builder().source(source).kpiNames(kpiNames).parameters(List.of(parameter, parameter2)).build();
    }

    Set<KpiDefinition> getKpiDefinitions() {
        final Set<KpiDefinition> kpiDefinitions = new HashSet<>();

        final KpiDefinition kpiDefinition1 = KpiDefinition.builder()
                .withName(TEST_KPI_1_NAME)
                .withExpression("SUM(table1.counterName1) from kpi_db://table1")
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withFilter(new ArrayList<>())
                .build();

        final KpiDefinition kpiDefinition2 = KpiDefinition.builder()
                .withName(TEST_KPI_2_NAME)
                .withExpression(
                        "SUM(table2.counterName2) FROM kpi_db://table2 INNER JOIN kpi_db://alias on table3.counterName3 = alias.counterName3")
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withFilter(new ArrayList<>())
                .withExecutionGroup("0 */5 * ? * *")
                .build();

        final KpiDefinition kpiDefinition3 = KpiDefinition.builder()
                .withName(TEST_KPI_3_NAME)
                .withExpression("SUM(table3.counterName3) FROM kpi_inmemory://table3")
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withFilter(new ArrayList<>())
                .withExecutionGroup("0 */5 * ? * *")
                .build();

        final KpiDefinition kpiDefinition4 = KpiDefinition.builder()
                .withName(TEST_KPI_4_NAME)
                .withExpression("SUM(table4.counterName3) FROM kpi_db://table4 LEFT JOIN kpi_db://table1 ON table1.counterName1 = someValue")
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withFilter(new ArrayList<>())
                .withExecutionGroup("0 */5 * ? * *")
                .build();

        kpiDefinitions.add(kpiDefinition1);
        kpiDefinitions.add(kpiDefinition2);
        kpiDefinitions.add(kpiDefinition3);
        kpiDefinitions.add(kpiDefinition4);

        return kpiDefinitions;
    }

    List<KpiDefinition> getKpiDefinitions2() {
        final List<KpiDefinition> kpiDefinitions = new ArrayList<>();
        final KpiDefinition kpiDefinition1 = KpiDefinition.builder()
                .withName(TEST_KPI_1_NAME)
                .withExpression("SUM(table1.counterName1) FROM kpi_db://table1")
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withFilter(new ArrayList<>())
                .build();
        kpiDefinitions.add(kpiDefinition1);
        return kpiDefinitions;
    }

    Set<KpiDefinition> getParameterizedKpiDefinitions() {
        final Set<KpiDefinition> kpiDefinitions = new HashSet<>();

        final List<Filter> parameterizedFilter1 = new ArrayList<>();
        parameterizedFilter1.add(new Filter("${params.filter}"));

        final KpiDefinition kpiDefinition1 = KpiDefinition.builder()
                .withName(TEST_KPI_1_NAME)
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withAggregationType("${params.aggregation_type}")
                .withExpression("${params.expression}")
                .withFilter(parameterizedFilter1)
                .build();

        final KpiDefinition kpiDefinition2 = KpiDefinition.builder()
                .withName(TEST_KPI_2_NAME)
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withAggregationType("${params.aggregation_type}")
                .withExpression("table.column FROM kpi_db://table.column ${params.where_expression}")
                .withFilter(new ArrayList<>())
                .build();

        final List<Filter> parameterizedFilter2 = new ArrayList<>();
        parameterizedFilter2.add(new Filter("${params.filter}"));
        parameterizedFilter2.add(new Filter("kpi_db://table.column = value"));

        final KpiDefinition kpiDefinition3 = KpiDefinition.builder()
                .withName(TEST_KPI_3_NAME)
                .withAggregationPeriod(AGGREGATION_PERIOD)
                .withAggregationType("${params.aggregation_type}")
                .withExpression("table.column FROM kpi_db://table.column ${params.where_expression}")
                .withFilter(parameterizedFilter2)
                .build();

        kpiDefinitions.add(kpiDefinition1);
        kpiDefinitions.add(kpiDefinition2);
        kpiDefinitions.add(kpiDefinition3);

        return kpiDefinitions;
    }

    Map<String, String> getParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("params.filter", "kpi_db://table.column = value");
        parameters.put("params.aggregation_type", "SUM");
        parameters.put("params.expression", "table.column FROM kpi_db://table.column");
        parameters.put("params.where_expression", "WHERE column = value");
        return parameters;
    }
}
