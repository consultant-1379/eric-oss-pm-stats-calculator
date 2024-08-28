/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

class DefinitionHelperTest {

    @Test
    void shouldGiveBackDistinctIdentifiers() {
        final Set<DataIdentifier> actual = DefinitionHelper.getDistinctDataIdentifiers(List.of(
                entity("first", "identifier", "category", "schema"),
                entity("second", "identifier", "category", "schema"),
                entity("third", "otherIdentifier", "category", "schema")
        ));

        assertThat(actual).hasSameElementsAs(identifierSet("identifier|category|schema", "otherIdentifier|category|schema"));
    }

    @Test
    void shouldGiveBackDefinitionsWithSameIdentifier() {
        final List<KpiDefinitionEntity> actual = DefinitionHelper.getDefinitionsWithSameIdentifier(DataIdentifier.of("identifier|category|schema"), List.of(
                entity("first", "identifier", "category", "schema"),
                entity("second", "identifier", "category", "schema"),
                entity("third", "otherIdentifier", "category", "schema")
        ));

        assertThat(actual).containsExactlyInAnyOrder(
                entity("first", "identifier", "category", "schema"),
                entity("second", "identifier", "category", "schema")
        );
    }

    static Set<DataIdentifier> identifierSet(final String @NonNull ... identifiers) {
        return Arrays.stream(identifiers).map(DataIdentifier::of).collect(Collectors.toSet());
    }

    static KpiDefinitionEntity entity(final String name, final String dataSpace, final String category, final String schemaName) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withSchemaDataSpace(dataSpace);
        builder.withSchemaCategory(category);
        builder.withSchemaName(schemaName);
        builder.withFilters(List.of());
        return builder.build();
    }
}