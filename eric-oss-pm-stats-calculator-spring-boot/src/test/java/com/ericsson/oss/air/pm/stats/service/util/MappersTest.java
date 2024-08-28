/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;

import org.junit.jupiter.api.Test;

class MappersTest {
    @Test
    void shouldMapToColumns() {
        final List<Column> actual = Mappers.toColumns(Arrays.asList("col1", "col2"));

        assertThat(actual).containsExactly(Column.of("col1"), Column.of("col2"));
    }

    @Test
    void shouldMapToColumnDefinition_fromDefinitions() {
        final KpiDefinitionEntity definition1 = entity("definition1", "REAL");
        final KpiDefinitionEntity definition2 = entity("definition2", "BOOLEAN");

        final List<ColumnDefinition> actual = Mappers.toColumnDefinition(Arrays.asList(definition1, definition2));

        assertThat(actual)
                .containsExactly(ColumnDefinition.of(Column.of("definition1"), KpiDataType.POSTGRES_REAL),
                        ColumnDefinition.of(Column.of("definition2"), KpiDataType.POSTGRES_BOOLEAN));
    }

    @Test
    void shouldMapToColumnDefinition_fromAggregationElements_andColumnNameDataType() {
        final Map<String, KpiDataType> columnNameToDataType = Map.of("fdn", KpiDataType.POSTGRES_UNLIMITED_STRING, "guid", KpiDataType.POSTGRES_LONG);

        final List<ColumnDefinition> actual = Mappers.toColumnDefinition(Arrays.asList("fdn", "guid"), columnNameToDataType);

        assertThat(actual)
                .containsExactly(ColumnDefinition.of(Column.of("fdn"), KpiDataType.POSTGRES_UNLIMITED_STRING),
                        ColumnDefinition.of(Column.of("guid"), KpiDataType.POSTGRES_LONG));
    }

    static KpiDefinitionEntity entity(final String name, final String objectType) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withObjectType(objectType);
        return builder.build();
    }

}