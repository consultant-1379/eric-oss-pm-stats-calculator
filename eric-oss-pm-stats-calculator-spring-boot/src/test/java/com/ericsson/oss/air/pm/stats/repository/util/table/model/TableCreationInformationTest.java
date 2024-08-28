/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.model;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TableCreationInformationTest {
    @Test
    void shouldFailOnConstructing_withEmptyDefinitions() {
        Assertions.assertThatThrownBy(() -> TableCreationInformation.of("tableName", "-1", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("definitions should not be empty");
    }

    @Test
    void shouldReturnUnmodifiableDefinitions() {
        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "-1", List.of(entity()));
        final Collection<KpiDefinitionEntity> actual = tableCreationInformation.getDefinitions();

        Assertions.assertThat(actual).isUnmodifiable();
    }

    @Test
    void shouldCreateFromTableDefinitions() {
        final KpiDefinitionEntity definition = KpiDefinitionEntity.builder().withAggregationPeriod(60).withFilters(List.of()).build();
        final TableCreationInformation actual = TableCreationInformation.of(
                TableDefinitions.of(
                        Table.of("tableName"),
                        Sets.newLinkedHashSet(definition)
                )
        );

        Assertions.assertThat(actual.getAggregationPeriod()).isEqualTo("60");
        Assertions.assertThat(actual.getTableName()).isEqualTo("tableName");
        Assertions.assertThat(actual.getDefinitions()).containsExactlyInAnyOrder(definition);
    }

    @Test
    void shouldCollectAggregationElements() {
        final TableCreationInformation tableCreationInformation = TableCreationInformation.of(
                "tableName", "60", List.of(
                        entity(List.of("agg1", "agg2", "agg3")),
                        entity(List.of("agg1", "agg3", "agg4"))
                )
        );

        Assertions.assertThat(tableCreationInformation.collectAggregationElements()).containsExactlyInAnyOrder("agg1", "agg2", "agg3", "agg4");
    }

    @Test
    void shouldCollectSimpleDefinitions() {
        final KpiDefinitionEntity simpleDefinition = entity("simpleDefinition", "dataSpace", "category", "schemaName");
        final KpiDefinitionEntity complexDefinition = entity("complexDefinition");
        final TableCreationInformation tableCreationInformation =
                TableCreationInformation.of("tableName", "60", asList(simpleDefinition, complexDefinition));

        Assertions.assertThat(tableCreationInformation.collectSimpleDefinitions()).containsExactlyInAnyOrder(simpleDefinition);
    }

    @Test
    void shouldCollectComplexDefinitions() {
        final KpiDefinitionEntity simpleDefinition = entity("simpleDefinition", "dataSpace", "category", "schemaName");
        final KpiDefinitionEntity complexDefinition = entity("complexDefinition");
        final TableCreationInformation tableCreationInformation =
                TableCreationInformation.of("tableName", "60", asList(simpleDefinition, complexDefinition));

        Assertions.assertThat(tableCreationInformation.collectNotSimpleDefinitions()).containsExactlyInAnyOrder(complexDefinition);
    }

    @MethodSource("provideIsDefaultAggregationPeriodData")
    @ParameterizedTest(name = "[{index}] Aggregation period: ''{0}'' is default: ''{1}''")
    void shouldVerifyIsDefaultAggregationPeriod(final String aggregationPeriod, final boolean expected) {
        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", aggregationPeriod, List.of(entity()));

        Assertions.assertThat(tableCreationInformation.isDefaultAggregationPeriod()).isEqualTo(expected);
    }

    @MethodSource("provideIsNonDefaultAggregationPeriodData")
    @ParameterizedTest(name = "[{index}] Aggregation period: ''{0}'' is non-default: ''{1}''")
    void shouldVerifyIsNonDefaultAggregationPeriod(final String aggregationPeriod, final boolean expected) {
        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", aggregationPeriod, List.of(entity()));

        Assertions.assertThat(tableCreationInformation.isNonDefaultAggregationPeriod()).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsDefaultAggregationPeriodData() {
        return Stream.of(Arguments.of("-1", true), Arguments.of("60", false));
    }

    private static Stream<Arguments> provideIsNonDefaultAggregationPeriodData() {
        return Stream.of(Arguments.of("-1", false), Arguments.of("60", true));
    }

    static KpiDefinitionEntity entity() {
        return KpiDefinitionEntity.builder().build();
    }

    static KpiDefinitionEntity entity(final String name) {
        return KpiDefinitionEntity.builder().withName(name).build();
    }

    static KpiDefinitionEntity entity(final List<String> aggregationElements) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withAggregationElements(aggregationElements);
        return builder.build();
    }

    static KpiDefinitionEntity entity(final String name, final String dataSpace, final String category, final String schemaName) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withSchemaDataSpace(dataSpace);
        builder.withSchemaCategory(category);
        builder.withSchemaName(schemaName);
        return builder.build();
    }
}