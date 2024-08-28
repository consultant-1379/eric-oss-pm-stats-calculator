/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TabularParametersUtilsTest {

    static UUID CALCULATION_ID = UUID.fromString("b2531c89-8513-4a88-aa1a-b99484321628");

    @Test
    void shouldMakeUniqueTableNamesForListOfTabularParameters() {
        final List<String> tabularParameterNames = List.of("tabular_table_name", "tabular_table_name_2");

        final Collection<String> actual = TabularParameterUtils.makeUniqueTableNamesForListOfTabularParameters(tabularParameterNames, CALCULATION_ID);

        assertThat(actual).containsExactlyElementsOf(List.of("tabular_table_name_b2531c89", "tabular_table_name_2_b2531c89"));
    }

    @Test
    void shouldMakeUniqueTableNameForTabularParameters() {
        final String actual = TabularParameterUtils.makeUniqueTableNameForTabularParameters("tabular_table_name", CALCULATION_ID);

        assertThat(actual).isEqualTo("tabular_table_name_b2531c89");
    }

    @ParameterizedTest
    @MethodSource("providePredicateData")
    void shouldDecide(final AggregationElement aggregationElement, final boolean expected) {
        final TabularParameter tabularParameter = TabularParameter.builder().withName("TabularParameterTable").build();
        final Parameter parameter = Parameter.builder()
                .withName("parameter")
                .withTabularParameter(tabularParameter)
                .build();

        final Predicate<Parameter> predicate = TabularParameterUtils.aggregationElementPredicate(aggregationElement);
        final boolean actual = predicate.test(parameter);

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> providePredicateData() {
        return Stream.of(
                Arguments.of(aggElement("TabularParameterTable", "parameter"), true),
                Arguments.of(aggElement("IncorrectTable", "parameter"), false),
                Arguments.of(aggElement("TabularParameterTable", "IncorrectParameter"), false),
                Arguments.of(aggElement("IncorrectTable", "IncorrectParameter"), false)
        );
    }

    static AggregationElement aggElement(final String table, final String column) {
        return AggregationElement.builder()
                .withSourceColumn(column)
                .withSourceTable(table)
                .build();
    }
}