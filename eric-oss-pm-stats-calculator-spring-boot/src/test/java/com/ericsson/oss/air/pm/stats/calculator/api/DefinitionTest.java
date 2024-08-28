/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DefinitionTest {

    private static final String AGGREGATION_ELEMENTS = "aggregation_elements";
    private static final String CATEGORY = "category";
    private static final String EXECUTION_GROUP = "execution_group";
    private static final String EXPRESSION = "expression";
    private static final String FILTER = "filter";
    private static final String IDENTIFIER = "identifier";
    private static final String INP_DATA_IDENTIFIER = "inp_data_identifier";
    private static final String KEY = "key";
    private static final String NAME = "name";
    private static final String SCHEMA_DETAIL = "schema_detail";

    @CsvSource({
            "key,true",
            "unknown,false"
    })
    @ParameterizedTest(name = "[{index}] Attribute ''{0}'' exists: ''{1}''")
    void shouldVerifyAttributeExists(final String key, final boolean expected) {
        final Definition definition = new Definition(Collections.singletonMap(KEY, true));

        Assertions.assertThat(definition.doesAttributeExist(key)).isEqualTo(expected);
    }

    @Test
    void shouldReadAttribute() {
        final Definition definition = new Definition(Collections.singletonMap(KEY, true));
        final Boolean actual = definition.attribute(KEY, Boolean.class);
        Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldSetAttribute() {
        final Definition definition = new Definition(Collections.singletonMap(KEY, true));

        Assertions.assertThat(definition.getAttributeByName(KEY)).isEqualTo(true);

        definition.setAttribute(KEY, false);

        Assertions.assertThat(definition.getAttributeByName(KEY)).isEqualTo(false);
    }

    @Test
    void shouldReturnUnmodifiableMap_onGetAttributes() {
        final Definition definition = new Definition();
        Assertions.assertThatThrownBy(() -> {
            final Map<String, Object> actual = definition.getAttributes();
            actual.put(KEY, true);
        }).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReturnFilterList() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FILTER, Collections.singletonList(new Filter("filter")));

        Assertions.assertThat(new Definition(attributes).getFilters())
                .containsExactlyInAnyOrder(new Filter("filter"));
    }

    @MethodSource("provideGetKpiTypeData")
    @ParameterizedTest(name = "[{index}] KPI Definition with input identifier ''{0}'' and execution group ''{1}'' is complex ==> ''{2}''")
    void shouldGetKpiType(final String inputIdentifier, final String executionGroup, final KpiType expected) {
        final Definition definition = new Definition();
        definition.setAttribute(INP_DATA_IDENTIFIER, inputIdentifier);
        definition.setAttribute(EXECUTION_GROUP, executionGroup);

        Assertions.assertThat(definition.getKpiType()).isEqualTo(expected);
    }

    @MethodSource("provideIsComplexDefinitionData")
    @ParameterizedTest(name = "[{index}] KPI Definition with input identifier ''{0}'' and execution group ''{1}'' is complex ==> ''{2}''")
    void shouldDecideIfComplex(final String inputIdentifier, final String executionGroup, final boolean expected) {
        final Definition definition = new Definition();
        definition.setAttribute(INP_DATA_IDENTIFIER, inputIdentifier);
        definition.setAttribute(EXECUTION_GROUP, executionGroup);

        Assertions.assertThat(definition.isComplexDefinition()).isEqualTo(expected);
    }

    @Test
    void shouldDecideIfExecutionGroupExists() {
        final Definition definition = new Definition(Collections.singletonMap(EXECUTION_GROUP, "group_0"));

        Assertions.assertThat(definition.doesExecutionGroupExist()).isTrue();
    }

    @Test
    void shouldReturnName() {
        final Definition definition = new Definition(Collections.singletonMap(NAME, "kpi_A"));
        Assertions.assertThat(definition.getName()).isEqualTo("kpi_A");
    }

    @Test
    void shouldReturnExecutionGroup() {
        final Definition definition = new Definition(Collections.singletonMap(EXECUTION_GROUP, "group_0"));
        Assertions.assertThat(definition.getExecutionGroup()).isEqualTo("group_0");
    }

    @Test
    void shouldReturnExpression() {
        final Definition definition = new Definition(
                Collections.singletonMap(EXPRESSION, "SUM(kpi_simple_60.random_kpi) FROM kpi_db://kpi_simple_60"));
        Assertions.assertThat(definition.getExpression()).isEqualTo("SUM(kpi_simple_60.random_kpi) FROM kpi_db://kpi_simple_60");
    }

    @Test
    void shouldReturnSchemaDetail() {
        final SchemaDetail schemaDetail1 = SchemaDetail.builder().withId(1).withTopic("topic2").build();
        final Definition definition = new Definition(Collections.singletonMap(SCHEMA_DETAIL, schemaDetail1));
        Assertions.assertThat(definition.getSchemaDetail()).isEqualTo(schemaDetail1);
    }

    @Test
    void shouldReturnAggElements() {
        final Definition definition = new Definition(
                Collections.singletonMap(AGGREGATION_ELEMENTS, Arrays.asList("kpi_cell_sector_1440.kpi_A", "kpi_simple_60.kpi_B")));
        final List<String> expectedAggElements = Arrays.asList("kpi_cell_sector_1440.kpi_A", "kpi_simple_60.kpi_B");

        Assertions.assertThat(definition.getAggregationElements()).containsAll(expectedAggElements);
    }

    private static Stream<Arguments> provideIsComplexDefinitionData() {
        return Stream.of(
                Arguments.of(IDENTIFIER, null, false),
                Arguments.of(CATEGORY, EXECUTION_GROUP, false),
                Arguments.of(null, EXECUTION_GROUP, true),
                Arguments.of(null, null, false)
        );
    }

    private static Stream<Arguments> provideGetKpiTypeData() {
        return Stream.of(
                Arguments.of(IDENTIFIER, null, KpiType.SCHEDULED_SIMPLE),
                Arguments.of(IDENTIFIER, EXECUTION_GROUP, KpiType.SCHEDULED_SIMPLE),
                Arguments.of(null, EXECUTION_GROUP, KpiType.SCHEDULED_COMPLEX),
                Arguments.of(null, null, KpiType.ON_DEMAND)
        );
    }

    @ToString
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    private static final class Other {
    }
}