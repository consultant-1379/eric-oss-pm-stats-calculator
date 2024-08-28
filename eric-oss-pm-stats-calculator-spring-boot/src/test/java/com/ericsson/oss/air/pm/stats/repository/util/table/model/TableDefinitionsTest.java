/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.model;

import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

class TableDefinitionsTest {
    @Test
    void shouldFailOnConstructing_withEmptyDefinitions() {
        Assertions.assertThatThrownBy(() -> TableDefinitions.of(Table.of("tableName"), Collections.emptySet()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("definitions should not be empty");
    }

    @Test
    void shouldGetUnmodifiableDefinitions() {
        final TableDefinitions tableDefinitions = TableDefinitions.of(Table.of("tableName"), Sets.newLinkedHashSet(KpiDefinitionEntity.builder().build()));

        final Set<KpiDefinitionEntity> actual = tableDefinitions.getDefinitions();

        Assertions.assertThat(actual).isUnmodifiable();
    }

    @Test
    void shouldGetAggregationPeriod() {
        final KpiDefinitionEntity definition = KpiDefinitionEntity.builder().withAggregationPeriod(60).build();
        final TableDefinitions tableDefinitions = TableDefinitions.of(Table.of("tableName"), Sets.newLinkedHashSet(definition));

        final String actual = tableDefinitions.getAggregationPeriod();
        Assertions.assertThat(actual).isEqualTo("60");
    }
}