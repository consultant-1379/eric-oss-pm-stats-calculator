/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._spy;

import static kpi.model._helper.MotherObject.kpiDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import kpi.model.KpiDefinitionTable;
import kpi.model._helper.Serialization;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.ondemand.OnDemandTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionTableTest {

    @Spy
    KpiDefinitionTable<Table> objectUnderTest;

    @Test
    void shouldGiveBackDefinitions() {
        final OnDemandTable table = createTable();

        doReturn(List.of(table)).when(objectUnderTest).kpiOutputTables();

        final Set<KpiDefinition> actual = objectUnderTest.definitions();

        assertThat(actual).containsExactlyInAnyOrder(
                kpiDefinition(
                        "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), true, Collections.emptyList()
                ),
                kpiDefinition(
                        "definition_two", "FROM expression_2", "INTEGER", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), false, Collections.emptyList()
                ),
                kpiDefinition(
                        "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM,
                        List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2")
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideIsEmptyData")
    void shouldDecideIfEmpty(final List<Table> tableList, final boolean expected) {
        doReturn(tableList).when(objectUnderTest).kpiOutputTables();

        boolean actual = objectUnderTest.isEmpty();

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideIsEmptyData() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), true),
                Arguments.of(List.of(createTable()), false)
        );
    }

    static OnDemandTable createTable() {
        final String content = '{' +
                "   \"aggregation_period\": 60, " +
                "   \"alias\": \"alias_value\", " +
                "   \"aggregation_elements\": [\"table.column1\", \"table.column2\"]," +
                "   \"kpi_definitions\": [ " +
                "        { " +
                "            \"name\": \"definition_one\", " +
                "            \"expression\": \"FROM expression_1\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"exportable\": true" +
                "        }, " +
                "        { " +
                "            \"name\": \"definition_two\", " +
                "            \"expression\": \"FROM expression_2\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"exportable\": false " +
                "        }," +
                "        {" +
                "            \"name\": \"definition_three\"," +
                "            \"expression\": \"FROM expression_3\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"aggregation_elements\": [\"table.column3\", \"table.column4\"]," +
                "            \"filters\": [\"f_1\", \"f_2\"]" +
                "        }" +
                "   ] " +
                '}';

        return Serialization.deserialize(content, OnDemandTable.class);
    }
}